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
package com.aurora.gateway.webfilter.ratelimit;

import com.alibaba.fastjson.JSON;
import com.aurora.gateway.core.event.ConfigEvent;
import com.aurora.gateway.core.configuration.ConfigStorage;
import com.aurora.gateway.webfilter.ratelimit.constants.RateLimiterContext;
import com.aurora.gateway.webfilter.ratelimit.constants.RateLimitDefinition;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RateLimitConfigStorage extends ConfigStorage<RateLimiterContext> {

    public static final String NAME = "rate-limit";

    private final Map<String, RateLimiterContext> pathRateLimiterMap = new ConcurrentHashMap<>(128);

    @Override
    public RateLimiterContext getDefinition(String serviceAlias) {
        return pathRateLimiterMap.get(serviceAlias);
    }

    @Override
    public void doNotify(ConfigEvent configEvent) {
        if (configEvent == null) {
            return;
        }

        RateLimitDefinition rateLimitDefinition = JSON.parseObject(configEvent.getBody(), RateLimitDefinition.class);

        RateLimiterContext localRateLimiterContext = pathRateLimiterMap.get(rateLimitDefinition.getPath());

        if (configEvent.getSubscribe().equals(ConfigEvent.ADD_ITEM)) {
            if (localRateLimiterContext != null) {
                //已存在，仅接受新增消息
                log.warn("just accept add rate limit notify, ignore it! event={}", configEvent.getBody());
                return;
            }

            if (rateLimitDefinition.getPermitsPerSecond() <= 0) {
                //QPS不能 <= 0
                log.warn("event permitsPerSecond <= 0, ignore it! event={}", configEvent.getBody());
                return;
            }

            if (rateLimitDefinition.getRateLimitResponse() == null) {
                //响应不能为null
                log.warn("event response is null, ignore it! event={}", configEvent.getBody());
                return;
            }

            RateLimiter rateLimiter;
            if (rateLimitDefinition.getWarmupPeriod() == null) {
                rateLimiter = RateLimiter.create(rateLimitDefinition.getPermitsPerSecond());
            } else {
                rateLimiter = RateLimiter.create(rateLimitDefinition.getPermitsPerSecond(), rateLimitDefinition.getWarmupPeriod());
            }
            pathRateLimiterMap.putIfAbsent(rateLimitDefinition.getPath(), new RateLimiterContext(rateLimiter, rateLimitDefinition.getRateLimitResponse()));
        } else if (configEvent.getSubscribe().equals(ConfigEvent.DEL_ITEM)) {
            pathRateLimiterMap.remove(rateLimitDefinition.getPath());
        } else if (configEvent.getSubscribe().equals(ConfigEvent.UPDATE_ITEM)) {
            if (localRateLimiterContext == null) {
                //不存在，仅接受更新消息
                log.warn("just accept update rate limit notify, ignore it! event={}", configEvent.getBody());
                return;
            }

            if (rateLimitDefinition.getPermitsPerSecond() <= 0) {
                //QPS不能 <= 0
                log.warn("event permitsPerSecond <= 0, ignore it! event={}", configEvent.getBody());
                return;
            }

            if (rateLimitDefinition.getRateLimitResponse() == null) {
                //响应不能为null
                log.warn("event response is null, ignore it! event={}", configEvent.getBody());
                return;
            }
            localRateLimiterContext.getRateLimiter().setRate(rateLimitDefinition.getPermitsPerSecond());
            localRateLimiterContext.setRateLimitResponse(rateLimitDefinition.getRateLimitResponse());
        }
    }

    @Override
    public String subscribe() {
        return NAME;
    }
}
