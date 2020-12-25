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
package com.aurora.gateway.core.utils;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.aurora.gateway.core.model.DubboServiceDefinition;
import org.apache.commons.lang3.StringUtils;

public class ReferenceConfigUtils {

    /**
     * 构建ReferenceConfig实例
     * @param dubboServiceDefinition
     * @param registryConfig
     * @return
     */
    public static ReferenceConfig buildReferenceConfig(DubboServiceDefinition dubboServiceDefinition, RegistryConfig registryConfig, ApplicationConfig applicationConfig) {
        ReferenceConfig referenceConfig = new ReferenceConfig();
        referenceConfig.setInterface(dubboServiceDefinition.getInterfaceName());
        String version = dubboServiceDefinition.getVersion();
        if (StringUtils.isNotBlank(version) && StringUtils.isNotBlank(version.trim())) {
            referenceConfig.setVersion(dubboServiceDefinition.getVersion().trim());
        }
        referenceConfig.setRegistry(registryConfig);
        referenceConfig.setApplication(applicationConfig);
        referenceConfig.setGeneric(Boolean.TRUE.toString());
        referenceConfig.setCheck(false);
        referenceConfig.setAsync(true);
        referenceConfig.setRetries(0);
        referenceConfig.setTimeout(4000);
        referenceConfig.setCluster("failfast");

        return referenceConfig;
    }
}
