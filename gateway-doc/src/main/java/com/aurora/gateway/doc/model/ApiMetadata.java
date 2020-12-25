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
package com.aurora.gateway.doc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 网关接口元数据
 * @author feixue
 */
@Data
public class ApiMetadata {

    /**
     * 当前时间戳，用于降低版本迭代频率，尽量避免无意义的同步行为，同时也可以识别过期的同步数据
     */
    private long nowTime;

    /**
     * 接口元数据
     */
    private ServiceMetadata serviceMetadata;

    /**
     * 方法元数据
     */
    private List<MethodMetadata> methodMetadataList;


    /**
     * 服务元数据定义
     */
    @Data
    @NoArgsConstructor
    public static class ServiceMetadata {

        /**
         * 应用名称
         */
        private String appName;

        /**
         * dubbo 服务接口
         */
        private String serviceName;

        /**
         * 服务版本
         */
        private String version;
    }

    @Data
    @NoArgsConstructor
    public static class MethodMetadata {

        /**
         * dubbo 服务方法
         */
        private String methodName;

        /**
         * 服务别名
         */
        private String serviceAlias;

        /**
         * 超时时间（秒）
         */
        private int timeout;

        /**
         * 备注
         */
        private String remark;

        /**
         * 请求参数集合
         */
        private List<ParamMetadata> requestParamList;

        /**
         * 响应参数集合
         */
        private List<ParamMetadata> responseParamList;
    }

    /**
     * 参数元数据定义
     */
    @Data
    @NoArgsConstructor
    public static class ParamMetadata {

        /**
         * 参数名称
         */
        private String paramName;

        /**
         * 参数类型
         */
        private String paramType;

        /**
         * 基础类型
         */
        private boolean primitiveType;

        /**
         * 子类型参数集合
         */
        private List<ParamMetadata> subParamList;

        /**
         * 参数注释说明
         */
        private String note;

        /**
         * 示例值
         */
        private String demoValue;

        /**
         * 是否必传
         */
        private boolean required;
    }
}