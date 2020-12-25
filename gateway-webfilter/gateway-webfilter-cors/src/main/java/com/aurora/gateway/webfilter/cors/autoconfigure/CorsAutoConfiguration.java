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
package com.aurora.gateway.webfilter.cors.autoconfigure;

import com.aurora.gateway.webfilter.cors.CorsConfigStorage;
import com.aurora.gateway.webfilter.cors.GatewayCorsConfigurationSource;
import com.aurora.gateway.webfilter.cors.config.CorsConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.web.cors.reactive.CorsProcessor;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static com.aurora.gateway.core.model.constants.GatewayConstants.GATEWAY_OPEN;
import static com.aurora.gateway.webfilter.cors.constants.CommonConstants.GATEWAY_CORS_ENABLED;

@Configuration
@ConditionalOnProperty(name = {GATEWAY_OPEN, GATEWAY_CORS_ENABLED}, havingValue = "true")
public class CorsAutoConfiguration {

    @Bean
    public CorsConfig corsConfig() {
        return new CorsConfig();
    }

    @Bean
    public GatewayCorsConfigurationSource gatewayCorsConfigurationSource() {
        return new GatewayCorsConfigurationSource();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter(GatewayCorsConfigurationSource gatewayCorsConfigurationSource, @Nullable CorsProcessor corsProcessor) {
        if (corsProcessor == null) {
            return new CorsWebFilter(gatewayCorsConfigurationSource);
        } else {
            return new CorsWebFilter(gatewayCorsConfigurationSource, corsProcessor);
        }
    }

    @Bean
    public CorsConfigStorage corsConfigStorage(GatewayCorsConfigurationSource gatewayCorsConfigurationSource) {
        return new CorsConfigStorage(gatewayCorsConfigurationSource);
    }
}
