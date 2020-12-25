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

import com.aurora.gateway.core.extension.invoke.event.InvokeResultConfigEvent;
import com.aurora.gateway.webfilter.circuitbreaker.constants.CircuitBreakerContext;
import com.aurora.gateway.webfilter.circuitbreaker.constants.CircuitBreakerDefinition;
import com.aurora.gateway.webfilter.circuitbreaker.failurestat.BucketGroup;
import org.springframework.context.ApplicationListener;

public class CircuitBreakerStatHandler implements ApplicationListener<InvokeResultConfigEvent> {

    private final CircuitBreakerConfigStorage circuitBreakerConfigStorage;

    public CircuitBreakerStatHandler(CircuitBreakerConfigStorage circuitBreakerConfigStorage) {
        this.circuitBreakerConfigStorage = circuitBreakerConfigStorage;
    }

    /**
     * 计算失败率
     * @param circuitBreakerDefinition
     * @return
     */
    public boolean calculationFailure(CircuitBreakerDefinition circuitBreakerDefinition) {
        CircuitBreakerContext circuitBreakerContext = circuitBreakerConfigStorage.getDefinition(circuitBreakerDefinition.getRequestUrl());
        if (circuitBreakerContext == null) {
            return false;
        } else {
            BucketGroup bucketGroup = circuitBreakerContext.getBucketGroup();
            if (bucketGroup == null) {
                //刚初始化的桶，不会触发
                return false;
            } else {
                return doCalculationFailure(circuitBreakerDefinition, bucketGroup);
            }
        }
    }

    private static boolean doCalculationFailure(CircuitBreakerDefinition circuitBreakerDefinition, BucketGroup bucketGroup) {
        if (circuitBreakerDefinition.getFailureThreshold() >= bucketGroup.calculationFailure()) {
            //满足熔断阈值，触发熔断
            return true;
        } else {
            //尚未满足阈值
            return false;
        }
    }

    @Override
    public void onApplicationEvent(InvokeResultConfigEvent event) {
        InvokeResultConfigEvent.EventBody eventBody = event.getEventBody();
        CircuitBreakerContext circuitBreakerContext = circuitBreakerConfigStorage.getDefinition(eventBody.getRequestUrl());
        if (circuitBreakerContext == null) {
            return;
        }
        BucketGroup bucketGroup = circuitBreakerContext.getBucketGroup();
        if (eventBody.isSuccess()) {
            bucketGroup.recordRequest(true);
        } else {
            bucketGroup.recordRequest(false);
        }
    }
}
