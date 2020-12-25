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
package com.aurora.gateway.doc.autoconfigure;

import com.aurora.gateway.doc.GatewayScanService;
import com.aurora.gateway.doc.config.DocConfig;
import com.aurora.gateway.doc.controller.GatewayDocController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.aurora.gateway.doc.constants.CommonConstants.GATEWAY_DOC_ENABLED;

@Configuration
@EnableConfigurationProperties(DocConfig.class)
@ConditionalOnProperty(name = {GATEWAY_DOC_ENABLED}, havingValue = "true")
public class DocAutoConfiguration {

    @Bean
    public GatewayDocController gatewayDocController() {
        return new GatewayDocController();
    }

    @Bean
    public DocConfig docConfig() {
        return new DocConfig();
    }

    @Bean
    public GatewayScanService gatewayScanService(DocConfig docConfig) {
        return new GatewayScanService(docConfig);
    }
}
