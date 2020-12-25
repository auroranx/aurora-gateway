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
package com.aurora.gateway.core.model.exception;

import lombok.Getter;

@Getter
public class GatewayException extends RuntimeException {

    private int code;

    private String desc;

    public GatewayException(int code, String desc) {
        super(desc);
        this.code = code;
        this.desc = desc;
    }

    public GatewayException(GatewayErrorEnum gatewayErrorEnum) {
        super(gatewayErrorEnum.getDesc());
        this.desc = gatewayErrorEnum.getDesc();
        this.code = gatewayErrorEnum.getCode();
    }

    public GatewayException(GatewayErrorEnum gatewayErrorEnum, Throwable cause) {
        super(gatewayErrorEnum.getDesc(), cause);
        this.desc = gatewayErrorEnum.getDesc();
        this.code = gatewayErrorEnum.getCode();
    }
}
