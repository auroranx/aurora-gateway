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
package com.aurora.gateway.core.extension.invoke;

import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.aurora.gateway.core.extension.Filter;
import com.aurora.gateway.core.extension.FilterChain;
import com.aurora.gateway.core.extension.FilterOrder;
import com.aurora.gateway.core.extension.invoke.event.InvokeResultConfigEvent;
import com.aurora.gateway.core.model.DubboServiceDefinition;
import com.aurora.gateway.core.model.constants.GatewayConstants;
import com.aurora.gateway.core.model.exception.GatewayErrorEnum;
import com.aurora.gateway.core.monitor.MetricsMonitor;
import com.aurora.gateway.core.handler.ResponseHandler;
import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.Future;

@Slf4j
public class DefaultDubboInvokeFilter implements Filter, ApplicationEventPublisherAware {

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Mono<Void> doFilter(ServerWebExchange serverWebExchange, FilterChain filterChain) {
        String serviceAlias = serverWebExchange.getAttribute(GatewayConstants.SERVICE_ALIAS);

        DubboServiceDefinition dubboServiceDefinition = serverWebExchange.getAttribute(GatewayConstants.SERVICE_ITEM);
        List<String> paramTypeList = serverWebExchange.getAttribute(GatewayConstants.PARAM_TYPES);
        List<Object> paramValueList = serverWebExchange.getAttribute(GatewayConstants.PARAM_VALUES);
        GenericService genericService = serverWebExchange.getAttribute(GatewayConstants.GENERIC_SERVICE);

        Histogram.Timer timer = MetricsMonitor.getDubboInvokeTimer(serviceAlias);
        try {
            genericService.$invoke(dubboServiceDefinition.getMethodName(), paramTypeList.toArray(new String[]{}), paramValueList.toArray(new Object[]{}));
        } catch (Throwable throwable) {
            timer.observeDuration();
            timer.close();

            return ResponseHandler.writeResponse(serverWebExchange, throwable);
        }

        Future future = RpcContext.getContext().getFuture();
        if (future == null) {
            return ResponseHandler.writeResponse(serverWebExchange, GatewayErrorEnum.SERVICE_ITEM_INVOKE_FAILURE);
        }
        ResponseFuture responseFuture = ((FutureAdapter)future).getFuture();
        return Mono.create(monoSink -> responseFuture.setCallback(new ResponseCallback() {
            @Override
            public void done(Object response) {
                /**
                 * dubbo 2.6.9 版本
                 * dubbo的DefaultFuture在处理正常回调时，会吃掉done回调中产生的Exception。
                 * 此时mono上层无法侦测到抛出来的错误，会导致mono调用链被挂起，请求链接被挂起。如果这里有复杂的逻辑，需要主动捕获并主动告知mono
                 */
                applicationEventPublisher.publishEvent(InvokeResultConfigEvent.SUCCESS(serviceAlias));
                monoSink.success(response);
            }

            @Override
            public void caught(Throwable exception) {
                RpcContext.getContext().setFuture(null);
                applicationEventPublisher.publishEvent(InvokeResultConfigEvent.FAILURE(serviceAlias));

                try {
                    /**
                     * dubbo 2.6.9 版本
                     * dubbo的DefaultFuture在处理异常时，会吃掉caught回调中产生的Exception。
                     * 此时mono上层无法侦测到抛出来的错误，会导致mono调用链被挂起，请求链接被挂起。需要主动补偿exception到调用链
                     */
                    Object error = ResponseHandler.getResponse(exception);
                    if (error == null) {
                        monoSink.error(exception);
                    } else {
                        //异常有被处理，则转换为success
                        monoSink.success(error);
                    }
                } catch (Throwable throwable) {
                    monoSink.error(throwable);
                }
            }
        })).doOnNext(result -> {
            timer.observeDuration();
            timer.close();

            serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.INVOKE_RESULT, result);
        }).then(filterChain.filter(serverWebExchange));
    }

    @Override
    public int getOrder() {
        return FilterOrder.INVOKE;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
