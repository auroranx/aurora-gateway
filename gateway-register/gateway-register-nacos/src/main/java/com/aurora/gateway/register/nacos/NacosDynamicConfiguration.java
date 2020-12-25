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
package com.aurora.gateway.register.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.impl.ConfigChangeHandler;
import com.aurora.gateway.core.configuration.DynamicConfiguration;
import com.aurora.gateway.core.event.ConfigListener;
import com.aurora.gateway.register.nacos.config.NacosConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;

/**
 * 基于nacos的动态配置管理
 */
@Slf4j
public class NacosDynamicConfiguration implements DynamicConfiguration, InitializingBean {

    private ConfigService configService;

    private final NacosConfig nacosConfig;

    public NacosDynamicConfiguration(NacosConfig nacosConfig) {
        this.nacosConfig = nacosConfig;
    }

    @Override
    public void doSubscribe(ConfigListener configListener) {
        try {
            NacosListenerWrap listenerWrap = new NacosListenerWrap(configListener);
            String eventBody = configService.getConfig(configListener.subscribe(), nacosConfig.getGroup(), nacosConfig.getTimeoutMs());

            ConfigChangeEvent event = new ConfigChangeEvent(ConfigChangeHandler.getInstance().parseChangeData(null, eventBody, "json"));
            listenerWrap.receiveConfigChange(event);

            configService.addListener(configListener.subscribe(), nacosConfig.getGroup(), listenerWrap);
        } catch (NacosException | IOException e) {
            log.error("subscribe {} failure!", configListener.subscribe(), e);
            throw new RuntimeException("subscribe " + configListener.subscribe() + " from nacos failure!", e);
        }
    }

    @Override
    public String getName() {
        return "nacos";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        configService = NacosFactory.createConfigService(nacosConfig.getServerAddr());
    }
}
