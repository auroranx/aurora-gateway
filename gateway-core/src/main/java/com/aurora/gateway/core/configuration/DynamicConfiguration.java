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
package com.aurora.gateway.core.configuration;

import com.aurora.gateway.core.event.ConfigListener;
import com.aurora.gateway.core.extension.Named;

/**
 * 实现层要解决如下几个问题：
 * 1.适配具体存储器API；
 * 2.根据状态驱动数据通知；
 * 3.管理订阅映射关系，并根据关系推送配置；
 */
public interface DynamicConfiguration extends Named {

    /**
     * 注册监听器
     * @param configListener
     */
    void doSubscribe(ConfigListener configListener);
}
