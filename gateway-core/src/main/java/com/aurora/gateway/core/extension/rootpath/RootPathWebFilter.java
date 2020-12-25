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
package com.aurora.gateway.core.extension.rootpath;

import com.aurora.gateway.core.extension.rootpath.config.RootPathConfig;
import com.aurora.gateway.core.model.constants.GatewayConstants;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class RootPathWebFilter implements WebFilter {

    private final RootPathConfig rootPathConfig;

    public RootPathWebFilter(RootPathConfig rootPathConfig) {
        this.rootPathConfig = rootPathConfig;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestUrl = exchange.getRequest().getPath().value();

        String finalRequestUrl = requestUrl;
        Optional<String> optional = rootPathConfig.getList().stream().filter(rootPath -> {
            if (finalRequestUrl.startsWith(rootPath)) {
                return true;
            } else {
                return false;
            }
        }).findFirst();

        if (optional.isPresent()) {
            requestUrl = requestUrl.substring(optional.get().length() + 1);
            exchange.getAttributes().put(GatewayConstants.MATCH_DUBBO, true);
        }

        exchange.getAttributes().put(GatewayConstants.REQUEST_URL, requestUrl);

        return chain.filter(exchange);
    }
}
