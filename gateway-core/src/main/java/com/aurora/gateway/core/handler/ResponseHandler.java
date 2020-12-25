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

import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.rest.support.ContentType;
import com.alibaba.fastjson.JSON;
import com.aurora.gateway.core.extension.response.ResponseMapper;
import com.aurora.gateway.core.model.exception.GatewayErrorEnum;
import com.aurora.gateway.core.model.exception.GatewayException;
import com.aurora.gateway.core.model.ConvertResult;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

public class ResponseHandler {

    private static ResponseMapper responseMapper;

    public ResponseHandler(ResponseMapper responseMapper) {
        if (responseMapper == null) {
            throw new GatewayException(GatewayErrorEnum.RESPONSE_MAPPER_NOT_AVAILABLE);
        }
        ResponseHandler.responseMapper = responseMapper;
    }

    public static ConvertResult getResponse(RpcResult rpcResult) {
        if (rpcResult == null) {
            return null;
        }

        if (rpcResult.getException() != null) {
            return ConvertResult.failure(getResponse(rpcResult.getException()));
        }

        return ConvertResult.success(responseMapper.success(rpcResult.getValue()));
    }

    public static Object getResponse(Throwable throwable) {
        return responseMapper.failure(throwable);
    }

    public static Object getResponse(GatewayErrorEnum gatewayErrorEnum) {
        return responseMapper.error(gatewayErrorEnum);
    }

    public static Mono<Void> writeResponse(ServerWebExchange serverWebExchange, RpcResult rpcResult) {
        ServerHttpResponse serverHttpResponse = serverWebExchange.getResponse();
        serverHttpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON_UTF_8);
        ConvertResult convertResult = getResponse(rpcResult);
        if (convertResult != null) {
            if (convertResult.isSuccess()) {
                serverHttpResponse.setStatusCode(HttpStatus.OK);
            } else {
                serverHttpResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            DataBuffer dataBuffer = serverHttpResponse.bufferFactory().wrap(JSON.toJSONBytes(convertResult.getObject()));
            return serverWebExchange.getResponse().writeWith(Flux.just(dataBuffer));
        } else {
            return Mono.empty();
        }
    }

    public static Mono<Void> writeResponse(ServerWebExchange serverWebExchange, Throwable throwable) {
        serverWebExchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        Object responseObj = getResponse(throwable);
        return writeResponse(serverWebExchange, responseObj);
    }

    public static Mono<Void> writeResponse(ServerWebExchange serverWebExchange, GatewayErrorEnum gatewayErrorEnum) {
        serverWebExchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        Object responseObj = getResponse(gatewayErrorEnum);
        return writeResponse(serverWebExchange, responseObj);
    }

    public static Mono<Void> writeResponse(ServerWebExchange serverWebExchange, Object responseObj) {
        ServerHttpResponse serverHttpResponse = serverWebExchange.getResponse();
        serverHttpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON_UTF_8);
        if (responseObj != null) {
            DataBuffer dataBuffer = serverHttpResponse.bufferFactory().wrap(JSON.toJSONBytes(responseObj));
            return serverWebExchange.getResponse().writeWith(Flux.just(dataBuffer));
        } else {
            return Mono.empty();
        }
    }

    public static Mono<Void> writeResponse(ServerWebExchange serverWebExchange, String responseObj) {
        ServerHttpResponse serverHttpResponse = serverWebExchange.getResponse();
        serverHttpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON_UTF_8);
        if (responseObj != null) {
            DataBuffer dataBuffer = serverHttpResponse.bufferFactory().wrap(responseObj.getBytes(Charset.forName("utf-8")));
            return serverWebExchange.getResponse().writeWith(Flux.just(dataBuffer));
        } else {
            return Mono.empty();
        }
    }

    public static Mono<Void> writeResponse(ServerWebExchange serverWebExchange) {
        ServerHttpResponse serverHttpResponse = serverWebExchange.getResponse();
        serverHttpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON_UTF_8);
        return Mono.empty();
    }
}
