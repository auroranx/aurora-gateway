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
package com.aurora.gateway.webfilter.demote;

import com.aurora.gateway.core.model.constants.GatewayConstants;
import com.aurora.gateway.core.handler.ResponseHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class DemoteWebFilter implements WebFilter {

    private final DemoteConfigStorage demoteConfigStorage;

    public DemoteWebFilter(DemoteConfigStorage demoteConfigStorage) {
        this.demoteConfigStorage = demoteConfigStorage;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestUrl = exchange.getAttribute(GatewayConstants.REQUEST_URL);

        String responseStr = demoteConfigStorage.getDefinition(requestUrl);
        if (StringUtils.isBlank(responseStr)) {
            return chain.filter(exchange);
        } else {
            return ResponseHandler.writeResponse(exchange, responseStr);
        }
    }
}
