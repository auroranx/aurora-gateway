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
package com.aurora.gateway.core.extension.impl;

import com.aurora.gateway.core.extension.Filter;
import com.aurora.gateway.core.extension.FilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

public class DefaultFilterChain implements FilterChain {

    private final List<Filter> filterList;

    public DefaultFilterChain(List<Filter> filterList) {
        this.filterList = filterList;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange) {
        return new VirtualFilterChain(filterList).filter(exchange);
    }

    private static class VirtualFilterChain implements FilterChain {

        private int index = 0;
        private final List<Filter> filterList;

        public VirtualFilterChain(List<Filter> filterList) {
            this.filterList = filterList;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            return Mono.defer(() -> {
                if (this.index < filterList.size()) {
                    Filter filter = filterList.get(this.index);
                    index++;
                    return filter.doFilter(exchange, this);
                } else {
                    return Mono.empty(); // complete
                }
            });
        }
    }
}
