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
package com.aurora.gateway.doc.annotation;

import java.lang.annotation.*;

/**
 * @author feixue
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface GwModelProperty {

    /**
     * 参数注释说明
     * @return
     */
    String note() default "";

    /**
     * 示例值
     * @return
     */
    String demoValue() default "";

    /**
     * 是否必传
     * @return
     */
    boolean required() default false;
}

