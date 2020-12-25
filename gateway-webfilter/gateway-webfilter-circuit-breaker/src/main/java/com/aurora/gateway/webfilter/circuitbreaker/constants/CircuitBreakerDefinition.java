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
package com.aurora.gateway.webfilter.circuitbreaker.constants;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class CircuitBreakerDefinition implements Serializable {

    /**
     * 针对url
     */
    private String requestUrl;

    /**
     * 熔断开启状态
     */
    private boolean enabled;

    /**
     * 熔断失败率阈值
     */
    private int failureThreshold;

    /**
     * 熔断内容
     */
    private String context;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendProperties = new HashMap<>();
}
