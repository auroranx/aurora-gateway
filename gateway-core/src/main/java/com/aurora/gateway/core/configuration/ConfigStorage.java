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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 配置定义
 * 完成存取相关操作，可以看成一个支持读写的内存数据库
 */
public abstract class ConfigStorage<T> implements InitializingBean, ApplicationContextAware, ConfigListener {

    protected DynamicConfiguration dynamicConfiguration;

    private ApplicationContext applicationContext;

    /**
     * 获取指定别名下的配置定义
     * @param serviceAlias
     * @return
     */
    public abstract T getDefinition(String serviceAlias);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        dynamicConfiguration = applicationContext.getBean(DynamicConfiguration.class);
        dynamicConfiguration.doSubscribe(this);
    }
}
