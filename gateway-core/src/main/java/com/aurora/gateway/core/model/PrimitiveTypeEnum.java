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

public enum PrimitiveTypeEnum {

    INTEGER("java.lang.Integer"),
    LONG("java.lang.Long"),
    SHORT("java.lang.Short"),
    FLOAT("java.lang.Float"),
    DOUBLE("java.lang.Double"),
    BYTE("java.lang.Byte"),
    STRING("java.lang.String"),
    CHARACTER("java.lang.Character"),
    BOOLEAN("java.lang.Boolean"),
    MAP("java.util.Map"),
    LIST("java.util.List"),;

    private String type;

    PrimitiveTypeEnum(String type) {
        this.type = type;
    }

    public static PrimitiveTypeEnum of(String type) {
        for (PrimitiveTypeEnum typeEnum : PrimitiveTypeEnum.values()) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }
}
