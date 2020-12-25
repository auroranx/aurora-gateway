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
package com.aurora.gateway.core.extension.request;

import com.aurora.gateway.core.model.ConvertResult;
import com.aurora.gateway.core.extension.FilterOrder;
import com.aurora.gateway.core.extension.Filter;
import com.aurora.gateway.core.extension.FilterChain;
import com.aurora.gateway.core.handler.ResponseHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class DefaultRequestFilter implements Filter {

    private final RequestMapper requestMapper;

    public DefaultRequestFilter(RequestMapper requestMapper) {
        this.requestMapper = requestMapper;
    }

    @Override
    public Mono<Void> doFilter(ServerWebExchange serverWebExchange, FilterChain filterChain) {
        Mono<ConvertResult> result = requestMapper.convert(serverWebExchange);
        return result.flatMap((convertResult -> {
            if (convertResult.isSuccess()) {
                return filterChain.filter(serverWebExchange);
            } else {
                return ResponseHandler.writeResponse(serverWebExchange, convertResult.getGatewayErrorEnum());
            }
        }));
    }

    @Override
    public int getOrder() {
        return FilterOrder.REQUEST;
    }
}
