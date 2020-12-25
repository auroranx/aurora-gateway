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
package com.aurora.gateway.core.extension.response;

import com.aurora.gateway.core.model.exception.GatewayErrorEnum;
import com.aurora.gateway.core.extension.Extension;

public interface ResponseMapper<F, T> extends Extension {

    /**
     * 成功情况下的处理
     * @param response
     * @return
     */
    T success(F response);

    /**
     * 网关异常情况下的处理
     * @param gatewayErrorEnum
     * @return
     */
    T error(GatewayErrorEnum gatewayErrorEnum);

    /**
     * 异常情况下的处理
     * @param throwable
     * @return
     */
    T failure(Throwable throwable);
}
