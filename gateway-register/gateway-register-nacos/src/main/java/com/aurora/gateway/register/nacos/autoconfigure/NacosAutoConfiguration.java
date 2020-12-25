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
package com.aurora.gateway.register.nacos.autoconfigure;

import com.aurora.gateway.core.configuration.DynamicConfiguration;
import com.aurora.gateway.register.nacos.NacosDynamicConfiguration;
import com.aurora.gateway.register.nacos.config.NacosConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.aurora.gateway.core.model.constants.GatewayConstants.GATEWAY_OPEN;

@Configuration
@ConditionalOnProperty(name = {GATEWAY_OPEN}, havingValue = "true")
public class NacosAutoConfiguration {

    @Bean
    public NacosConfig nacosConfig() {
        return new NacosConfig();
    }

    @Bean
    public DynamicConfiguration nacosDynamicConfiguration(NacosConfig nacosConfig) {
        return new NacosDynamicConfiguration(nacosConfig);
    }
}
