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
package com.aurora.gateway.webfilter.circuitbreaker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aurora.gateway.circuit-breaker")
public class CircuitBreakerConfig {

    /**
     * 是否启用熔断服务
     */
    private boolean enabled;

    /**
     * 桶数量
     */
    private int bucketNums = 16;

    /**
     * 重置统计桶的时间间隔（ms）
     */
    private int timeInterval = 10;
}
