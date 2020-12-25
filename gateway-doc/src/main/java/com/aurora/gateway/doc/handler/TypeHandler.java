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
package com.aurora.gateway.doc.handler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author feixue
 */
public class TypeHandler {

    public static boolean isMap(Type type) {
        String typeName;
        if (type instanceof ParameterizedType) {
            typeName = ((ParameterizedType)type).getRawType().getTypeName();
        } else {
            typeName = type.getTypeName();
        }

        try {
            Class clazz = Class.forName(typeName);
            for (Class interfaceName : clazz.getInterfaces()) {
                if (doCheckIsMap(interfaceName.getName())) {
                    return true;
                }
            }
            if (clazz.isInterface()) {
                return doCheckIsMap(clazz.getName());
            }
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean doCheckIsMap(String checkName) {
        switch (checkName) {
            case "java.util.Map":
                return true;
            default:
                return false;
        }
    }

    public static boolean isPrimitiveBoxing(Type type) {
        switch (type.getTypeName()) {
            case "java.lang.Integer":
            case "java.lang.Boolean":
            case "java.lang.Character":
            case "java.lang.Byte":
            case "java.lang.Short":
            case "java.lang.Long":
            case "java.lang.Float":
            case "java.lang.Double":
            case "java.lang.Void":
            case "java.lang.String":
                return true;
            default:
                return false;
        }
    }
}