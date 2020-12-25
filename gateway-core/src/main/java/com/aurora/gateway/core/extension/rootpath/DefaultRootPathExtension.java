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
package com.aurora.gateway.core.extension.rootpath;


import com.aurora.gateway.core.extension.rootpath.config.RootPathConfig;

import java.util.Optional;

/**
 * 根路径的默认实现
 */
public class DefaultRootPathExtension implements RootPathExtension {

    private final RootPathConfig rootPathConfig;

    public DefaultRootPathExtension(RootPathConfig rootPathConfig) {
        this.rootPathConfig = rootPathConfig;
    }

    @Override
    public Optional<String> support(String rootPath) {
        return rootPathConfig.getList().stream().filter(item -> rootPath.startsWith(item)).findFirst();
    }
}
