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
package com.aurora.gateway.webfilter.ratelimit.autoconfigure;

import com.aurora.gateway.webfilter.ratelimit.RateLimitConfigStorage;
import com.aurora.gateway.webfilter.ratelimit.RateLimitWebFilter;
import com.aurora.gateway.webfilter.ratelimit.config.RateLimitConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import static com.aurora.gateway.core.model.constants.GatewayConstants.GATEWAY_OPEN;
import static com.aurora.gateway.webfilter.ratelimit.constants.CommonConstants.GATEWAY_RATE_LIMIT_ENABLED;

@Configuration
@ConditionalOnProperty(name = {GATEWAY_OPEN, GATEWAY_RATE_LIMIT_ENABLED}, havingValue = "true")
public class RateLimitAutoConfiguration {

    @Bean
    public RateLimitConfig rateLimitConfig() {
        return new RateLimitConfig();
    }

    @Bean
    public RateLimitConfigStorage rateLimitConfigStorage() {
        return new RateLimitConfigStorage();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public RateLimitWebFilter rateLimitWebFilter(RateLimitConfigStorage rateLimitConfigStorage) {
        return new RateLimitWebFilter(rateLimitConfigStorage);
    }
}
