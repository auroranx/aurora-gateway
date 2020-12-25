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
package com.aurora.gateway.webfilter.ratelimit;

import com.aurora.gateway.core.model.GatewayWrapper;
import com.aurora.gateway.core.model.constants.GatewayConstants;
import com.aurora.gateway.core.handler.ResponseHandler;
import com.aurora.gateway.webfilter.ratelimit.constants.RateLimiterContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class RateLimitWebFilter implements WebFilter {

    private final RateLimitConfigStorage rateLimitConfigStorage;

    public RateLimitWebFilter(RateLimitConfigStorage rateLimitConfigStorage) {
        this.rateLimitConfigStorage = rateLimitConfigStorage;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        GatewayWrapper gatewayWrapper = check(exchange);
        if (gatewayWrapper.isSuccess()) {
            return chain.filter(exchange);
        } else {
            return ResponseHandler.writeResponse(exchange, gatewayWrapper.getBody().toString());
        }
    }

    /**
     * 检查是否被限流
     * @param exchange
     * @return
     */
    private GatewayWrapper check(ServerWebExchange exchange) {
        String requestPath = exchange.getAttribute(GatewayConstants.REQUEST_URL);

        RateLimiterContext rateLimiterContext = rateLimitConfigStorage.getDefinition(requestPath);
        if (rateLimiterContext == null) {
            return GatewayWrapper.SUCCESS;
        }

        if (rateLimiterContext.getRateLimiter().tryAcquire()) {
            return GatewayWrapper.SUCCESS;
        } else {
            return GatewayWrapper.failure(rateLimiterContext.getRateLimitResponse());
        }
    }
}
