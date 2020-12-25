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
package com.aurora.gateway.core.handler;

import com.aurora.gateway.core.model.constants.GatewayConstants;
import com.aurora.gateway.core.monitor.MetricsMonitor;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.handler.AbstractHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求接入层 level0
 */
public class DubboHandlerMapping extends AbstractHandlerMapping {

    private final DubboWebHandler dubboWebHandler;

    public DubboHandlerMapping(DubboWebHandler dubboWebHandler) {
        this.dubboWebHandler = dubboWebHandler;

        setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    protected Mono<?> getHandlerInternal(ServerWebExchange serverWebExchange) {
        Object value = serverWebExchange.getAttributes().get(GatewayConstants.MATCH_DUBBO);
        if (value == null || Boolean.FALSE.equals(value)) {
            return Mono.empty();
        } else {
            MetricsMonitor.GATEWAY_REQUEST_DUBBO.labels(serverWebExchange.getRequest().getPath().value()).inc();
            return Mono.just(dubboWebHandler);
        }
    }
}
