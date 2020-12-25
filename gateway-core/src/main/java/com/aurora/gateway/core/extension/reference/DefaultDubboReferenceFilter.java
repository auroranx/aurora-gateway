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

import com.alibaba.dubbo.rpc.service.GenericService;
import com.aurora.gateway.core.extension.reference.model.ServiceDefinitionContext;
import com.aurora.gateway.core.model.constants.GatewayConstants;
import com.aurora.gateway.core.model.exception.GatewayErrorEnum;
import com.aurora.gateway.core.extension.FilterOrder;
import com.aurora.gateway.core.extension.Filter;
import com.aurora.gateway.core.extension.FilterChain;
import com.aurora.gateway.core.handler.ResponseHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class DefaultDubboReferenceFilter implements Filter {

    private final DubboServiceDefinitionConfigStorage dubboServiceDefinitionConfigStorage;

    public DefaultDubboReferenceFilter(DubboServiceDefinitionConfigStorage dubboServiceDefinitionConfigStorage) {
        this.dubboServiceDefinitionConfigStorage = dubboServiceDefinitionConfigStorage;
    }

    @Override
    public Mono<Void> doFilter(ServerWebExchange serverWebExchange, FilterChain filterChain) {
        String requestUrl = serverWebExchange.getAttribute(GatewayConstants.REQUEST_URL);
        ServiceDefinitionContext serviceDefinitionContext = dubboServiceDefinitionConfigStorage.getDefinition(requestUrl);
        if (serviceDefinitionContext == null) {
            // 找不到服务配置
            return ResponseHandler.writeResponse(serverWebExchange, GatewayErrorEnum.SERVICE_ITEM_CONFIG_NOT_FOUND);
        }
        serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.SERVICE_ITEM, serviceDefinitionContext.getDubboServiceDefinition());

        GenericService genericService = serviceDefinitionContext.getGenericService();
        if (genericService == null) {
            // 找不到服务提供者
            return ResponseHandler.writeResponse(serverWebExchange, GatewayErrorEnum.SERVICE_ITEM_PROVIDER_NOT_FOUND);
        }

        serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.SERVICE_ALIAS, requestUrl);
        serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.GENERIC_SERVICE, genericService);
        return filterChain.filter(serverWebExchange);
    }

    @Override
    public int getOrder() {
        return FilterOrder.GENERIC;
    }
}
