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
package com.aurora.gateway.register.nacos.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.client.config.impl.AbstractConfigChangeParser;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * 支持nacos的JSON格式的配置
 */
public class JSONChangeParser extends AbstractConfigChangeParser {

    public JSONChangeParser() {
        super("json");
    }

    @Override
    public Map<String, ConfigChangeItem> doParse(String oldContent, String newContent, String type) throws IOException {
        JSONObject oldJsonObject;
        if (StringUtils.isBlank(oldContent)) {
            oldJsonObject = new JSONObject();
        } else {
            oldJsonObject = JSON.parseObject(oldContent);
        }

        JSONObject newJsonObject;
        if (StringUtils.isBlank(newContent)) {
            newJsonObject = new JSONObject();
        } else {
            newJsonObject = JSON.parseObject(newContent);
        }
        return filterChangeData(oldJsonObject, newJsonObject);
    }
}
