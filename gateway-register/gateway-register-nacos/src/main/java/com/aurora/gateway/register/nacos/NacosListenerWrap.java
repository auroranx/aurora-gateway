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
package com.aurora.gateway.register.nacos;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.PropertyChangeType;
import com.alibaba.nacos.client.config.listener.impl.AbstractConfigChangeListener;
import com.aurora.gateway.core.event.ConfigEvent;
import com.aurora.gateway.core.event.ConfigListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class NacosListenerWrap extends AbstractConfigChangeListener {

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final ConfigListener configListener;

    public NacosListenerWrap(ConfigListener configListener) {
        this.configListener = configListener;
    }

    @Override
    public void receiveConfigChange(ConfigChangeEvent event) {
        log.info("receive event={}", JSON.toJSONString(event));

        event.getChangeItems().stream().forEach(configChangeItem -> {
            ConfigEvent configEvent = new ConfigEvent();
            configEvent.setBody(configChangeItem.getNewValue());
            if (configChangeItem.getType().equals(PropertyChangeType.ADDED)) {
                configEvent.setSubscribe(ConfigEvent.ADD_ITEM);
            } else if (configChangeItem.getType().equals(PropertyChangeType.MODIFIED)) {
                configEvent.setSubscribe(ConfigEvent.UPDATE_ITEM);
            } else if (configChangeItem.getType().equals(PropertyChangeType.DELETED)) {
                configEvent.setSubscribe(ConfigEvent.DEL_ITEM);
            } else {
                //忽略消息
                return;
            }

            configListener.doNotify(configEvent);
        });
    }

    @Override
    public Executor getExecutor() {
        return EXECUTOR;
    }
}
