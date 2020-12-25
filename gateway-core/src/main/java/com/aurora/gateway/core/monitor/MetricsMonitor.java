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
package com.aurora.gateway.core.monitor;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

/**
 * 指标监控
 */
public class MetricsMonitor {

    /**
     * dubbo请求次数统计
     */
    public static final Counter GATEWAY_REQUEST_DUBBO = Counter.build().name("request_dubbo_total").labelNames("url").help("request dubbo total for url").register();

    /**
     * dubbo请求执行耗时
     */
    private static final Histogram GATEWAY_DUBBO_INVOKE_TIME = Histogram.build().name("dubbo_invoke_time").labelNames("invoke_time").help("dubbo invoke time").register();

    public static Histogram.Timer getDubboInvokeTimer(String label) {
        return GATEWAY_DUBBO_INVOKE_TIME.labels(label).startTimer();
    }
}
