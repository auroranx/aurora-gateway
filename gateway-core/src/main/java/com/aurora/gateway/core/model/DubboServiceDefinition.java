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
package com.aurora.gateway.core.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于dubbo协议的服务定义
 */
@Data
public class DubboServiceDefinition {

    /**
     * 服务别名
     */
    private String serviceAlias;

    /**
     * 服务说明
     */
    private String serviceDesc;

    /**
     * dubbo 接口名称
     */
    private String interfaceName;

    /**
     * dubbo 方法名称
     */
    private String methodName;

    /**
     * dubbo 服务版本
     */
    private String version;

    /**
     * dubbo 方法定义入参集合
     */
    private List<DubboParamDefinition> paramDefinitionList;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendProperties = new HashMap<>();

    public void addDubboParamDefinition(DubboParamDefinition dubboParamDefinition) {
        if (paramDefinitionList == null) {
            paramDefinitionList = new ArrayList<>();
        }
        paramDefinitionList.add(dubboParamDefinition);
    }
}
