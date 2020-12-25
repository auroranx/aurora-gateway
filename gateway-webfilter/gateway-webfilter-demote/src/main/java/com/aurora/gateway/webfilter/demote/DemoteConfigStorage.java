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
package com.aurora.gateway.webfilter.demote;

import com.alibaba.fastjson.JSON;
import com.aurora.gateway.core.event.ConfigEvent;
import com.aurora.gateway.core.configuration.ConfigStorage;
import com.aurora.gateway.webfilter.demote.constants.DemoteDefinition;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DemoteConfigStorage extends ConfigStorage<String> {

    public static final String NAME = "demote";

    private Map<String, String> requestUrlDemoteMap = new ConcurrentHashMap<>(128);

    /**
     * 获取降级响应
     * @param serviceAlias
     * @return
     */
    @Override
    public String getDefinition(String serviceAlias) {
        return requestUrlDemoteMap.get(serviceAlias);
    }

    @Override
    public void doNotify(ConfigEvent configEvent) {
        if (configEvent == null || StringUtils.isBlank(configEvent.getBody())) {
            return;
        }

        DemoteDefinition demoteDefinition = JSON.parseObject(configEvent.getBody(), DemoteDefinition.class);

        if (configEvent.getSubscribe().equals(ConfigEvent.ADD_ITEM)) {
            requestUrlDemoteMap.put(demoteDefinition.getRequestUrl(), demoteDefinition.getDemoteResponse());
        } else if (configEvent.getSubscribe().equals(ConfigEvent.DEL_ITEM)) {
            requestUrlDemoteMap.remove(demoteDefinition.getRequestUrl());
        } else if (configEvent.getSubscribe().equals(ConfigEvent.UPDATE_ITEM)) {
            requestUrlDemoteMap.put(demoteDefinition.getRequestUrl(), demoteDefinition.getDemoteResponse());
        }
    }

    @Override
    public String subscribe() {
        return NAME;
    }
}
