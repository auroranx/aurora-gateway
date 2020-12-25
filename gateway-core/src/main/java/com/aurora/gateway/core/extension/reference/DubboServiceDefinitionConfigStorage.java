/*
 * Copyright 2020 aurora
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aurora.gateway.core.extension.reference;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.fastjson.JSON;
import com.aurora.gateway.core.configuration.ConfigStorage;
import com.aurora.gateway.core.event.ConfigEvent;
import com.aurora.gateway.core.extension.reference.model.ServiceDefinitionContext;
import com.aurora.gateway.core.utils.DubboServiceDefinitionUtils;
import com.aurora.gateway.core.model.DubboServiceDefinition;
import com.aurora.gateway.core.utils.ReferenceConfigUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DubboServiceDefinitionConfigStorage extends ConfigStorage<ServiceDefinitionContext> {

    public static final String NAME = "service-item";

    private Map<String, ServiceDefinitionContext> dubboServiceDefinitionMap = new ConcurrentHashMap<>(256);
    private Map<String, DefinitionContext> definitionContextMap = new ConcurrentHashMap<>(256);

    private final RegistryConfig registryConfig;
    private final ApplicationConfig applicationConfig;

    public DubboServiceDefinitionConfigStorage(RegistryConfig registryConfig, ApplicationConfig applicationConfig) {
        this.registryConfig = registryConfig;
        this.applicationConfig = applicationConfig;
    }

    @Override
    public ServiceDefinitionContext getDefinition(String serviceAlias) {
        return dubboServiceDefinitionMap.get(serviceAlias);
    }

    @Override
    public void doNotify(ConfigEvent configEvent) {
        if (configEvent == null || StringUtils.isBlank(configEvent.getBody())) {
            return;
        }

        DubboServiceDefinition dubboServiceDefinition = JSON.parseObject(configEvent.getBody(), DubboServiceDefinition.class);
        String definitionKey = DubboServiceDefinitionUtils.getUniqueKey(dubboServiceDefinition);

        DefinitionContext definitionContext = definitionContextMap.get(definitionKey);

        if (configEvent.getSubscribe().equals(ConfigEvent.ADD_ITEM)) {
            if (definitionContext == null) {
                definitionContext = initDefinitionContext(dubboServiceDefinition);
            }
            ServiceDefinitionContext serviceDefinitionContext = new ServiceDefinitionContext();
            serviceDefinitionContext.setDubboServiceDefinition(dubboServiceDefinition);
            serviceDefinitionContext.setReferenceConfig(definitionContext.getReferenceConfig());
            ServiceDefinitionContext oldServiceDefinitionContext = dubboServiceDefinitionMap.putIfAbsent(dubboServiceDefinition.getServiceAlias(), serviceDefinitionContext);
            if (oldServiceDefinitionContext == null) {
                //并发下仅有一个线程能+1成功
                definitionContext.getCount().incrementAndGet();
            }
        } else if (configEvent.getSubscribe().equals(ConfigEvent.UPDATE_ITEM)) {
            ServiceDefinitionContext serviceDefinitionContext = dubboServiceDefinitionMap.get(dubboServiceDefinition.getServiceAlias());
            if (serviceDefinitionContext != null) {
                serviceDefinitionContext.setDubboServiceDefinition(dubboServiceDefinition);
            } else {
                //仅接受更新消息，新增的忽略。并行更新的话，需要自行控制分组串行
            }
        } else if (configEvent.getSubscribe().equals(ConfigEvent.DEL_ITEM)) {
            dubboServiceDefinitionMap.remove(dubboServiceDefinition.getServiceAlias());
            decrement(definitionContext);
        }
    }

    private void decrement(DefinitionContext definitionContext) {
        if (definitionContext == null) {
            //忽略
            return;
        }
        long value = definitionContext.getCount().accumulateAndGet(1, (left, right) -> {
            if (left <= 0) {
                //防止溢出，导致无法补救回来，同时支持幂等重试删除
                return 0;
            } else {
                return left - right;
            }
        });
        if (value <= 0) {
            //延期销毁，给一个补救的机会，避免误操作销毁昂贵资源
            Mono.delay(Duration.ofMinutes(10)).subscribe(aLong -> {
                //再次检查
                if (definitionContext.getCount().get() <= 0) {
                    //执行销毁
                    definitionContext.getReferenceConfig().destroy();
                }
            });
        }
    }

    @Override
    public String subscribe() {
        return NAME;
    }

    @Data
    @NoArgsConstructor
    private static class DefinitionContext {

        private AtomicLong count;

        private ReferenceConfig referenceConfig;
    }

    private DefinitionContext initDefinitionContext(DubboServiceDefinition dubboServiceDefinition) {
        String uniqueKey = DubboServiceDefinitionUtils.getUniqueKey(dubboServiceDefinition);

        DefinitionContext definitionContext = definitionContextMap.get(uniqueKey);

        if (definitionContext == null) {
            definitionContext = new DefinitionContext();
            ReferenceConfig referenceConfig = ReferenceConfigUtils.buildReferenceConfig(dubboServiceDefinition, registryConfig, applicationConfig);
            definitionContext.setReferenceConfig(referenceConfig);
            definitionContext.setCount(new AtomicLong(0));

            DefinitionContext oldDefinitionContext = definitionContextMap.putIfAbsent(uniqueKey, definitionContext);

            if (oldDefinitionContext != null) {
                //并发保护
                return oldDefinitionContext;
            }
        }
        return definitionContext;
    }
}
