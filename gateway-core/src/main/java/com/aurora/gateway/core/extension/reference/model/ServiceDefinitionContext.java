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
package com.aurora.gateway.core.extension.reference.model;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.aurora.gateway.core.model.DubboServiceDefinition;
import lombok.Data;

@Data
public final class ServiceDefinitionContext {

    private DubboServiceDefinition dubboServiceDefinition;

    private ReferenceConfig referenceConfig;

    public <T> T getGenericService() {
        return (T) referenceConfig.get();
    }
}
