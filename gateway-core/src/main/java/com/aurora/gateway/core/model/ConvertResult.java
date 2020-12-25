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

import com.aurora.gateway.core.model.exception.GatewayErrorEnum;
import lombok.Data;

@Data
public class ConvertResult {

    public static ConvertResult SUCCESS = new ConvertResult(true);

    private boolean success;

    private GatewayErrorEnum gatewayErrorEnum;

    private Object object;

    private ConvertResult() {
    }

    private ConvertResult(boolean success) {
        this.success = success;
    }

    public static ConvertResult success(Object object) {
        ConvertResult convertResult = new ConvertResult();
        convertResult.setSuccess(true);
        convertResult.setObject(object);

        return convertResult;
    }

    public static ConvertResult failure(Object object) {
        ConvertResult convertResult = new ConvertResult();
        convertResult.setSuccess(false);
        convertResult.setObject(object);

        return convertResult;
    }

    public static ConvertResult failure(GatewayErrorEnum gatewayErrorEnum) {
        ConvertResult convertResult = new ConvertResult();
        convertResult.setSuccess(false);
        convertResult.setGatewayErrorEnum(gatewayErrorEnum);

        return convertResult;
    }
}
