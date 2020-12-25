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
package com.aurora.gateway.webfilter.circuitbreaker;

import com.aurora.gateway.core.model.constants.GatewayConstants;
import com.aurora.gateway.core.handler.ResponseHandler;
import com.aurora.gateway.webfilter.circuitbreaker.constants.CircuitBreakerContext;
import com.aurora.gateway.webfilter.circuitbreaker.constants.CircuitBreakerDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 检查服务别名是否需要熔断，需要，则返回熔断数据
 */
public class CircuitBreakerFilter implements WebFilter {

    private final CircuitBreakerStatHandler circuitBreakerStatHandler;

    private final CircuitBreakerConfigStorage circuitBreakerConfigStorage;

    public CircuitBreakerFilter(CircuitBreakerStatHandler circuitBreakerStatHandler, CircuitBreakerConfigStorage circuitBreakerConfigStorage) {
        this.circuitBreakerStatHandler = circuitBreakerStatHandler;
        this.circuitBreakerConfigStorage = circuitBreakerConfigStorage;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestUrl = exchange.getAttribute(GatewayConstants.REQUEST_URL);

        CircuitBreakerContext circuitBreakerContext = circuitBreakerConfigStorage.getDefinition(requestUrl);
        if (circuitBreakerContext == null) {
            //无熔断配置，继续执行
            return chain.filter(exchange);
        }
        CircuitBreakerDefinition circuitBreakerDefinition = circuitBreakerContext.getCircuitBreakerDefinition();

        if (!circuitBreakerDefinition.isEnabled()) {
            //存在熔断配置，但是处于关闭状态
            return chain.filter(exchange);
        }
        //检查当前统计失败率是否到达阈值
        if (circuitBreakerStatHandler.calculationFailure(circuitBreakerDefinition) && StringUtils.isNotBlank(circuitBreakerDefinition.getContext())) {
            //触发熔断，返回熔断设置值
            return ResponseHandler.writeResponse(exchange, circuitBreakerDefinition.getContext());
        } else {
            return chain.filter(exchange);
        }
    }
}
