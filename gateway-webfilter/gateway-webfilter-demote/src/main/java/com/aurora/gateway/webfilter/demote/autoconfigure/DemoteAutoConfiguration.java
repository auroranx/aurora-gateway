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
package com.aurora.gateway.webfilter.demote.autoconfigure;

import com.aurora.gateway.webfilter.demote.DemoteConfigStorage;
import com.aurora.gateway.webfilter.demote.DemoteWebFilter;
import com.aurora.gateway.webfilter.demote.config.DemoteConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.aurora.gateway.core.model.constants.GatewayConstants.GATEWAY_OPEN;
import static com.aurora.gateway.webfilter.demote.constants.CommonConstants.GATEWAY_DEMOTE_ENABLED;

@Configuration
@ConditionalOnProperty(name = {GATEWAY_OPEN, GATEWAY_DEMOTE_ENABLED}, havingValue = "true")
public class DemoteAutoConfiguration {

    @Bean
    public DemoteConfig demoteConfig() {
        return new DemoteConfig();
    }

    @Bean
    public DemoteConfigStorage demoteConfigStorage() {
        return new DemoteConfigStorage();
    }

    @Bean
    public DemoteWebFilter demoteWebFilter(DemoteConfigStorage demoteConfigStorage) {
        return new DemoteWebFilter(demoteConfigStorage);
    }
}
