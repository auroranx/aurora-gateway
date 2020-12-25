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
package com.aurora.gateway.core.extension.response.impl;

import com.aurora.gateway.core.model.exception.GatewayErrorEnum;
import com.aurora.gateway.core.model.exception.GatewayException;
import com.aurora.gateway.core.extension.response.ResponseMapper;

/**
 * 响应端的实现，仅提供最基础的能力
 */
public class DefaultResponseMapper implements ResponseMapper {

    @Override
    public Object success(Object response) {
        return response;
    }

    @Override
    public Object error(GatewayErrorEnum gatewayErrorEnum) {
        throw new GatewayException(gatewayErrorEnum);
    }

    @Override
    public Object failure(Throwable throwable) {
        throw new GatewayException(GatewayErrorEnum.INVOKE_FAILURE, throwable);
    }
}
