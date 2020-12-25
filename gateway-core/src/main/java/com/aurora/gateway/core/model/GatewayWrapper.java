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

import java.io.Serializable;

@Data
public class GatewayWrapper implements Serializable {

    public static final GatewayWrapper SUCCESS = new GatewayWrapper(true);

    private boolean success;

    private Object body;

    private GatewayWrapper() {
    }

    private GatewayWrapper(boolean success) {
        this.success = success;
    }

    public static GatewayWrapper failure(Object body) {
        GatewayWrapper gatewayWrapper = new GatewayWrapper();
        gatewayWrapper.setSuccess(false);
        gatewayWrapper.setBody(body);

        return gatewayWrapper;
    }
}
