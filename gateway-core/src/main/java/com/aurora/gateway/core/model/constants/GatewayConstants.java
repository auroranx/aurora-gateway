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
package com.aurora.gateway.core.model.constants;

public interface GatewayConstants {

    String GATEWAY_PREFIX = "aurora.gateway";

    String GATEWAY_OPEN = GATEWAY_PREFIX + ".open";

    String REQUEST_URL = "request_url_no_root_config_path";

    String MATCH_DUBBO = "match_dubbo";

    /**
     * 服务别名
     */
    String SERVICE_ALIAS = "service_alias";

    /**
     * 服务条目
     */
    String SERVICE_ITEM = "service_item";

    /**
     * 泛化实例
     */
    String GENERIC_SERVICE = "generic_service";

    /**
     * 参数类型
     */
    String PARAM_TYPES = "param_types";

    /**
     * 参数值
     */
    String PARAM_VALUES = "param_values";

    /**
     * 执行结果
     */
    String INVOKE_RESULT = "invoke_result";
}
