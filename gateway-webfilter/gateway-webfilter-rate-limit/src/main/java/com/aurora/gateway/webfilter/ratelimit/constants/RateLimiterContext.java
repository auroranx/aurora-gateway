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
package com.aurora.gateway.webfilter.ratelimit.constants;

import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;

@Data
public class RateLimiterContext {

    /**
     * 限流器
     */
    private RateLimiter rateLimiter;

    /**
     * 接口被限流后返回值
     */
    private String rateLimitResponse;

    public RateLimiterContext(RateLimiter rateLimiter, String rateLimitResponse) {
        this.rateLimiter = rateLimiter;
        this.rateLimitResponse = rateLimitResponse;
    }
}
