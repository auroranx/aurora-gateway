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
package com.aurora.gateway.doc.controller;

import com.aurora.gateway.doc.model.ApiDocCache;
import com.aurora.gateway.doc.model.ApiMetadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author feixue
 */
@RestController
@RequestMapping("/gateway")
public class GatewayDocController {

    @GetMapping("/api-docs")
    public List<ApiMetadata> getApiDoc() {
        return ApiDocCache.getInstance().getApiMetadataList();
    }
}
