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
package com.aurora.gateway.core.extension.response;

import com.alibaba.dubbo.rpc.RpcResult;
import com.aurora.gateway.core.model.constants.GatewayConstants;
import com.aurora.gateway.core.extension.FilterOrder;
import com.aurora.gateway.core.extension.Filter;
import com.aurora.gateway.core.extension.FilterChain;
import com.aurora.gateway.core.handler.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class DefaultResponseFilter implements Filter {

    @Override
    public Mono<Void> doFilter(ServerWebExchange serverWebExchange, FilterChain filterChain) {
        Object response = serverWebExchange.getAttribute(GatewayConstants.INVOKE_RESULT);
        if (response == null) {
            return ResponseHandler.writeResponse(serverWebExchange);
        } else if (response instanceof RpcResult) {
            return ResponseHandler.writeResponse(serverWebExchange, (RpcResult)response);
        } else {
            return ResponseHandler.writeResponse(serverWebExchange, response);
        }
    }

    @Override
    public int getOrder() {
        return FilterOrder.RESPONSE;
    }
}
