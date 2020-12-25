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
package com.aurora.gateway.webfilter.circuitbreaker;

import com.alibaba.fastjson.JSON;
import com.aurora.gateway.core.event.ConfigEvent;
import com.aurora.gateway.core.configuration.ConfigStorage;
import com.aurora.gateway.webfilter.circuitbreaker.config.CircuitBreakerConfig;
import com.aurora.gateway.webfilter.circuitbreaker.constants.CircuitBreakerContext;
import com.aurora.gateway.webfilter.circuitbreaker.constants.CircuitBreakerDefinition;
import com.aurora.gateway.webfilter.circuitbreaker.failurestat.BucketGroup;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CircuitBreakerConfigStorage extends ConfigStorage<CircuitBreakerContext> {

    public static final String NAME = "circuit-breaker";

    private final CircuitBreakerConfig circuitBreakerConfig;

    private Map<String, CircuitBreakerContext> requestUrlCircuitBreakerMap = new ConcurrentHashMap<>(128);

    public CircuitBreakerConfigStorage(CircuitBreakerConfig circuitBreakerConfig) {
        this.circuitBreakerConfig = circuitBreakerConfig;
    }

    @Override
    public CircuitBreakerContext getDefinition(String serviceAlias) {
        return requestUrlCircuitBreakerMap.get(serviceAlias);
    }

    @Override
    public void doNotify(ConfigEvent configEvent) {
        if (StringUtils.isBlank(configEvent.getBody())) {
            return;
        }

        CircuitBreakerDefinition circuitBreakerDefinition = JSON.parseObject(configEvent.getBody(), CircuitBreakerDefinition.class);

        if (configEvent.getSubscribe().equals(ConfigEvent.ADD_ITEM)) {
            registerCircuitBreaker(circuitBreakerDefinition);
        } else if (configEvent.getSubscribe().equals(ConfigEvent.UPDATE_ITEM)) {
            updateCircuitBreaker(circuitBreakerDefinition);
        } else if (configEvent.getSubscribe().equals(ConfigEvent.DEL_ITEM)) {
            unregisterCircuitBreaker(circuitBreakerDefinition);
        }
    }

    @Override
    public String subscribe() {
        return NAME;
    }

    private void registerCircuitBreaker(CircuitBreakerDefinition circuitBreakerDefinition) {
        CircuitBreakerContext circuitBreakerContext = new CircuitBreakerContext();
        circuitBreakerContext.setCircuitBreakerDefinition(circuitBreakerDefinition);
        BucketGroup bucketGroup = new BucketGroup(circuitBreakerConfig.getBucketNums(), circuitBreakerConfig.getTimeInterval());
        circuitBreakerContext.setBucketGroup(bucketGroup);

        requestUrlCircuitBreakerMap.put(circuitBreakerDefinition.getRequestUrl(), circuitBreakerContext);
    }

    private void updateCircuitBreaker(CircuitBreakerDefinition circuitBreakerDefinition) {
        CircuitBreakerContext circuitBreakerContext = requestUrlCircuitBreakerMap.get(circuitBreakerDefinition.getRequestUrl());
        if (circuitBreakerContext != null) {
            circuitBreakerContext.setCircuitBreakerDefinition(circuitBreakerDefinition);
        }
    }

    private void unregisterCircuitBreaker(CircuitBreakerDefinition circuitBreakerDefinition) {
        requestUrlCircuitBreakerMap.remove(circuitBreakerDefinition.getRequestUrl());
    }
}
