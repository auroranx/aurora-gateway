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

import com.aurora.gateway.core.extension.Filter;
import com.aurora.gateway.core.extension.impl.DefaultFilterChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * dubbo处理链路，驱动执行链路进行执行
 */
@Slf4j
public class DubboWebHandler implements WebHandler {

    private final List<Filter> filterList;

    public DubboWebHandler(List<Filter> filterList) {
        this.filterList = filterList.stream().sorted(AnnotationAwareOrderComparator.INSTANCE).collect(Collectors.toList());

        log.info("gateway filter extension invoke order：" + getInvokeOrder());
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {
        return new DefaultFilterChain(filterList).filter(exchange);
    }

    /**
     * 获取链路执行顺序
     * @return
     */
    private String getInvokeOrder() {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (Filter filter : filterList) {
            if (!isFirst) {
                builder.append(" ---> ");
            } else {
                isFirst = false;
            }
            builder.append(filter.getName());
        }
        return builder.toString();
    }
}
