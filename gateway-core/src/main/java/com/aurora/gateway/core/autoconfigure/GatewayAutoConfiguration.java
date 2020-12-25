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
package com.aurora.gateway.core.autoconfigure;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.aurora.gateway.core.configuration.GatewayConfig;
import com.aurora.gateway.core.extension.Filter;
import com.aurora.gateway.core.extension.invoke.DefaultDubboInvokeFilter;
import com.aurora.gateway.core.extension.reference.DefaultDubboReferenceFilter;
import com.aurora.gateway.core.extension.reference.DubboServiceDefinitionConfigStorage;
import com.aurora.gateway.core.extension.request.DefaultRequestFilter;
import com.aurora.gateway.core.extension.request.RequestMapper;
import com.aurora.gateway.core.extension.request.impl.DefaultRequestMapper;
import com.aurora.gateway.core.extension.response.DefaultResponseFilter;
import com.aurora.gateway.core.extension.response.ResponseMapper;
import com.aurora.gateway.core.extension.response.impl.DefaultResponseMapper;
import com.aurora.gateway.core.extension.rootpath.DefaultRootPathExtension;
import com.aurora.gateway.core.extension.rootpath.RootPathExtension;
import com.aurora.gateway.core.extension.rootpath.RootPathWebFilter;
import com.aurora.gateway.core.extension.rootpath.config.RootPathConfig;
import com.aurora.gateway.core.handler.DubboHandlerMapping;
import com.aurora.gateway.core.handler.DubboWebHandler;
import com.aurora.gateway.core.handler.ResponseHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import java.util.List;

import static com.aurora.gateway.core.model.constants.GatewayConstants.GATEWAY_OPEN;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ConditionalOnProperty(name = GATEWAY_OPEN, havingValue = "true")
public class GatewayAutoConfiguration {

    @Bean
    public GatewayConfig gatewayConfig() {
        return new GatewayConfig();
    }

    @Bean
    public RootPathConfig rootPathConfig() {
        return new RootPathConfig();
    }

    @Bean
    public DubboWebHandler dubboWebHandler(List<Filter> filterList) {
        return new DubboWebHandler(filterList);
    }

    @Bean
    public DubboHandlerMapping dubboHandlerMapping(DubboWebHandler dubboWebHandler) {
        return new DubboHandlerMapping(dubboWebHandler);
    }

    @Bean
    public DefaultDubboInvokeFilter defaultDubboInvokeFilterExtension() {
        return new DefaultDubboInvokeFilter();
    }

    /**
     * 服务定义配置存储管理
     * @param registryConfig
     * @param applicationConfig
     * @return
     */
    @Bean
    public DubboServiceDefinitionConfigStorage dubboServiceDefinitionConfigStorage(RegistryConfig registryConfig, ApplicationConfig applicationConfig) {
        return new DubboServiceDefinitionConfigStorage(registryConfig, applicationConfig);
    }

    @Bean
    public DefaultDubboReferenceFilter defaultDubboReferenceFilterExtension(DubboServiceDefinitionConfigStorage dubboServiceDefinitionConfigStorage) {
        return new DefaultDubboReferenceFilter(dubboServiceDefinitionConfigStorage);
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestMapper requestMapper() {
        return new DefaultRequestMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseMapper responseMapper() {
        return new DefaultResponseMapper();
    }

    @Bean
    public ResponseHandler responseHandler(ResponseMapper responseMapper) {
        return new ResponseHandler(responseMapper);
    }

    @Bean
    public DefaultRequestFilter defaultRequestFilterExtension(RequestMapper requestMapper) {
        return new DefaultRequestFilter(requestMapper);
    }

    @Bean
    public DefaultResponseFilter defaultResponseFilterExtension() {
        return new DefaultResponseFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public RootPathExtension rootPathExtension(RootPathConfig rootPathConfig) {
        return new DefaultRootPathExtension(rootPathConfig);
    }

    @Bean
    public RootPathWebFilter rootPathWebFilter(RootPathConfig rootPathConfig) {
        return new RootPathWebFilter(rootPathConfig);
    }
}
