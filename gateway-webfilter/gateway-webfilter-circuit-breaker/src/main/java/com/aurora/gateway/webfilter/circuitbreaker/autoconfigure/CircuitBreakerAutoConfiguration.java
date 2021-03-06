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
package com.aurora.gateway.webfilter.circuitbreaker.autoconfigure;

import com.aurora.gateway.webfilter.circuitbreaker.CircuitBreakerConfigStorage;
import com.aurora.gateway.webfilter.circuitbreaker.CircuitBreakerFilter;
import com.aurora.gateway.webfilter.circuitbreaker.CircuitBreakerStatHandler;
import com.aurora.gateway.webfilter.circuitbreaker.config.CircuitBreakerConfig;
import com.aurora.gateway.webfilter.circuitbreaker.constants.CommonConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.aurora.gateway.core.model.constants.GatewayConstants.GATEWAY_OPEN;

@Configuration
@ConditionalOnProperty(name = {GATEWAY_OPEN, CommonConstants.GATEWAY_CIRCUIT_BREAKER_ENABLED}, havingValue = "true")
public class CircuitBreakerAutoConfiguration {

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return new CircuitBreakerConfig();
    }

    @Bean
    public CircuitBreakerFilter circuitBreakerFilter(CircuitBreakerStatHandler circuitBreakerStatHandler, CircuitBreakerConfigStorage circuitBreakerConfigStorage) {
        return new CircuitBreakerFilter(circuitBreakerStatHandler, circuitBreakerConfigStorage);
    }

    @Bean
    public CircuitBreakerStatHandler circuitBreakerStatHandler(CircuitBreakerConfigStorage circuitBreakerConfigStorage) {
        return new CircuitBreakerStatHandler(circuitBreakerConfigStorage);
    }

    @Bean
    public CircuitBreakerConfigStorage circuitBreakerConfigStorage(CircuitBreakerConfig circuitBreakerConfig) {
        return new CircuitBreakerConfigStorage(circuitBreakerConfig);
    }
}
