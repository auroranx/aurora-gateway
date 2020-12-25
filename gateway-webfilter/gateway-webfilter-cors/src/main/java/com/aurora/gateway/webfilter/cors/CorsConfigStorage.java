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

import com.alibaba.fastjson.JSON;
import com.aurora.gateway.core.event.ConfigEvent;
import com.aurora.gateway.core.configuration.ConfigStorage;
import com.aurora.gateway.webfilter.cors.constants.CorsDefinition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collection;
import java.util.List;

public class CorsConfigStorage extends ConfigStorage<CorsDefinition> {

    public static final String NAME = "cors";

    private final GatewayCorsConfigurationSource gatewayCorsConfigurationSource;

    public CorsConfigStorage(GatewayCorsConfigurationSource gatewayCorsConfigurationSource) {
        this.gatewayCorsConfigurationSource = gatewayCorsConfigurationSource;
    }

    @Override
    public CorsDefinition getDefinition(String serviceAlias) {
        CorsConfiguration corsConfiguration = gatewayCorsConfigurationSource.getCorsConfiguration(serviceAlias);
        CorsDefinition corsDefinition = new CorsDefinition();
        corsDefinition.setPath(serviceAlias);
        corsDefinition.setCorsConfiguration(corsConfiguration);
        return corsDefinition;
    }

    @Override
    public void doNotify(ConfigEvent configEvent) {
        if (configEvent == null || StringUtils.isBlank(configEvent.getBody())) {
            return;
        }

        CorsDefinition corsDefinition = JSON.parseObject(configEvent.getBody(), CorsDefinition.class);

        if (configEvent.getSubscribe().equals(ConfigEvent.ADD_ITEM)) {
            gatewayCorsConfigurationSource.registerCorsConfiguration(corsDefinition.getPath(), corsDefinition.getCorsConfiguration());
        } else if (configEvent.getSubscribe().equals(ConfigEvent.DEL_ITEM)) {
            gatewayCorsConfigurationSource.unregisterCorsConfiguration(corsDefinition.getPath());
        } else if (configEvent.getSubscribe().equals(ConfigEvent.UPDATE_ITEM)) {
            //防止语义不一致，直接替换。会增加一些fullGC的成本，频繁更新场景注意
            gatewayCorsConfigurationSource.registerCorsConfiguration(corsDefinition.getPath(), corsDefinition.getCorsConfiguration());

//            CorsConfiguration oldCorsConfiguration = gatewayCorsConfigurationSource.getCorsConfiguration(corsContext.getPath());
//            if (oldCorsConfiguration == null) {
//                gatewayCorsConfigurationSource.registerCorsConfiguration(corsContext.getPath(), corsContext.getCorsConfiguration());
//            } else {
//                //出于内存高效使用的考虑，这里不直接替换
//                refreshCorsConfiguration(oldCorsConfiguration, corsContext.getCorsConfiguration());
//            }
        }
    }

    @Override
    public String subscribe() {
        return NAME;
    }

    /**
     * 执行刷新
     * @param oldCorsConfiguration
     * @param newCorsConfiguration
     */
    private void refreshCorsConfiguration(CorsConfiguration oldCorsConfiguration, CorsConfiguration newCorsConfiguration) {
        refresh(oldCorsConfiguration.getAllowedHeaders(), newCorsConfiguration.getAllowedHeaders());
        refresh(oldCorsConfiguration.getAllowedMethods(), newCorsConfiguration.getAllowedMethods());
        refresh(oldCorsConfiguration.getAllowedOrigins(), newCorsConfiguration.getAllowedOrigins());
        refresh(oldCorsConfiguration.getExposedHeaders(), newCorsConfiguration.getExposedHeaders());

        oldCorsConfiguration.setMaxAge(newCorsConfiguration.getMaxAge());
        oldCorsConfiguration.setAllowCredentials(newCorsConfiguration.getAllowCredentials());
    }

    private void refresh(List<String> oldList, List<String> newList) {
        Collection<String> needRemoveList = CollectionUtils.subtract(oldList, newList);
        Collection<String> needAddList = CollectionUtils.subtract(newList, oldList);

        oldList.removeAll(needRemoveList);
        oldList.addAll(needAddList);
    }
}
