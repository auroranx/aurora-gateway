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
package com.aurora.gateway.webfilter.cors;

import org.springframework.http.server.PathContainer;
import org.springframework.lang.Nullable;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GatewayCorsConfigurationSource implements CorsConfigurationSource {

    private final Map<PathPattern, CorsConfiguration> corsConfigurations;

    private final PathPatternParser patternParser;

    public GatewayCorsConfigurationSource() {
        this(new PathPatternParser());
    }

    public GatewayCorsConfigurationSource(PathPatternParser patternParser) {
        this.corsConfigurations = new ConcurrentHashMap<>();
        this.patternParser = patternParser;
    }

    /**
     * Set CORS configuration based on URL patterns.
     */
    public void setCorsConfigurations(@Nullable Map<String, CorsConfiguration> corsConfigurations) {
        this.corsConfigurations.clear();
        if (corsConfigurations != null) {
            corsConfigurations.forEach(this::registerCorsConfiguration);
        }
    }

    /**
     * 注册指定路径的跨域配置
     */
    public void registerCorsConfiguration(String path, CorsConfiguration config) {
        this.corsConfigurations.put(this.patternParser.parse(path), config);
    }

    /**
     * 注销指定路径的跨域配置
     * @param path
     */
    public void unregisterCorsConfiguration(String path) {
        this.corsConfigurations.remove(this.patternParser.parse(path));
    }

    @Override
    @Nullable
    public CorsConfiguration getCorsConfiguration(ServerWebExchange exchange) {
        PathContainer lookupPath = exchange.getRequest().getPath().pathWithinApplication();
        return this.corsConfigurations.entrySet().stream()
                .filter(entry -> entry.getKey().matches(lookupPath))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public CorsConfiguration getCorsConfiguration(String path) {
        return corsConfigurations.get(this.patternParser.parse(path));
    }
}
