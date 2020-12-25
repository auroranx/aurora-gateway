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
 * 基于dubbo协议的请求参数定义
 */
@Data
public class DubboParamDefinition {

    /**
     * 参数类型
     */
    private String paramType;

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 子参数定义集合
     */
    private List<DubboParamDefinition> subParamDefinitionList;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendProperties = new HashMap<>();

    public void addDubboParamDefinition(DubboParamDefinition dubboParamDefinition) {
        if (subParamDefinitionList == null) {
            subParamDefinitionList = new ArrayList<>();
        }
        subParamDefinitionList.add(dubboParamDefinition);
    }
}
