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
package com.aurora.gateway.core.extension.invoke.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;

/**
 * 执行结果
 */
public class  InvokeResultConfigEvent extends ApplicationEvent {

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public InvokeResultConfigEvent(EventBody source) {
        super(source);
    }

    public EventBody getEventBody() {
        return (EventBody) getSource();
    }

    public static InvokeResultConfigEvent SUCCESS(String requestUrl) {
        EventBody eventBody = new EventBody();
        eventBody.setSuccess(true);
        eventBody.setRequestUrl(requestUrl);
        InvokeResultConfigEvent invokeResultConfigEvent = new InvokeResultConfigEvent(eventBody);

        return invokeResultConfigEvent;
    }

    public static InvokeResultConfigEvent FAILURE(String requestUrl) {
        EventBody eventBody = new EventBody();
        eventBody.setSuccess(false);
        eventBody.setRequestUrl(requestUrl);
        InvokeResultConfigEvent invokeResultConfigEvent = new InvokeResultConfigEvent(eventBody);

        return invokeResultConfigEvent;
    }

    @Data
    @NoArgsConstructor
    public static class EventBody {

        private boolean success;

        private String requestUrl;
    }
}
