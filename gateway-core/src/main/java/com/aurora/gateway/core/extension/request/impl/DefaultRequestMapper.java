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
package com.aurora.gateway.core.extension.request.impl;

import com.alibaba.fastjson.JSON;
import com.aurora.gateway.core.model.constants.GatewayConstants;
import com.aurora.gateway.core.model.exception.GatewayErrorEnum;
import com.aurora.gateway.core.model.ConvertResult;
import com.aurora.gateway.core.model.DubboParamDefinition;
import com.aurora.gateway.core.model.DubboServiceDefinition;
import com.aurora.gateway.core.model.PrimitiveTypeEnum;
import com.aurora.gateway.core.extension.request.RequestMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.*;

/**
 * 支持多种数据编码方式，解决http入参报文映射到dubbo方法入参的问题
 * 基于key - value 映射关系，支持get，post + form，post + json等3种形态
 */
@Slf4j
public class DefaultRequestMapper implements RequestMapper {

    @Override
    public Mono<ConvertResult> convert(ServerWebExchange serverWebExchange) {
        HttpMethod httpMethod = serverWebExchange.getRequest().getMethod();

        if (httpMethod.matches(HttpMethod.GET.name())) {
            return getConvert(serverWebExchange);
        } else if (httpMethod.matches(HttpMethod.POST.name())) {
            MediaType mediaType = serverWebExchange.getRequest().getHeaders().getContentType();
            if (mediaType == null) {
                // 错误的媒体类型
                return Mono.just(ConvertResult.failure(GatewayErrorEnum.ERROR_MEDIA_TYPE));
            } else if (mediaType.equalsTypeAndSubtype(MediaType.APPLICATION_JSON)) {
                return postJsonConvert(serverWebExchange);
            } else if (mediaType.equalsTypeAndSubtype(MediaType.APPLICATION_FORM_URLENCODED)) {
                return postFormConvert(serverWebExchange);
            } else {
                // 不支持的媒体类型
                return Mono.just(ConvertResult.failure(GatewayErrorEnum.UN_SUPPORT_MEDIA_TYPE));
            }
        } else {
            // 不支持的方法
            return Mono.just(ConvertResult.failure(GatewayErrorEnum.UN_SUPPORT_METHOD_TYPE));
        }
    }

    /**
     * 仅支持基于get的普通key => value 关系传参映射
     * 支持一参多值比如：field1=value1&field1=value2&field1=value3
     *
     * @param serverWebExchange
     * @return
     */
    private Mono<ConvertResult> getConvert(ServerWebExchange serverWebExchange) {
        MultiValueMap<String, String> paramMap = serverWebExchange.getRequest().getQueryParams();

        return processParamMap(serverWebExchange, paramMap);
    }

    private Mono<ConvertResult> postFormConvert(ServerWebExchange serverWebExchange) {
        return serverWebExchange.getFormData().flatMap(paramMap -> processParamMap(serverWebExchange, paramMap));
    }

    private Mono<ConvertResult> postJsonConvert(ServerWebExchange serverWebExchange) {
        DubboServiceDefinition dubboServiceDefinition = serverWebExchange.getAttribute(GatewayConstants.SERVICE_ITEM);

        List<DubboParamDefinition> dubboParamDefinitionList = dubboServiceDefinition.getParamDefinitionList();
        if (CollectionUtils.isEmpty(dubboParamDefinitionList)) {
            serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.PARAM_TYPES, Collections.EMPTY_LIST);
            serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.PARAM_VALUES, Collections.EMPTY_LIST);
            return Mono.just(ConvertResult.SUCCESS);
        } else {
            if (dubboParamDefinitionList.size() > 1) {
                // post + json的方式仅接受一个参数
                return Mono.just(ConvertResult.failure(GatewayErrorEnum.ONLY_SUPPORT_ONE_ARGS));
            }

            DubboParamDefinition dubboParamDefinition = dubboParamDefinitionList.get(0);

            return DataBufferUtils.join(serverWebExchange.getRequest().getBody()).map(dataBuffer -> {
                byte[] buffer = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(buffer);
                String bodyStr = new String(buffer, Charset.defaultCharset());
                DataBufferUtils.release(dataBuffer);
                return bodyStr;
            }).flatMap(body -> {
                try {
                    Object object;
                    if (body.startsWith("{")) {
                        object = JSON.parseObject(body);
                    } else if (body.startsWith("[")) {
                        object = JSON.parseArray(body);
                    } else {
                        // 非法数据，无法解析
                        return Mono.just(ConvertResult.failure(GatewayErrorEnum.UNABLE_PARSE_DATA));
                    }

                    List<String> paramTypeList = new ArrayList<>();
                    List<Object> paramValueList = new ArrayList<>();

                    paramTypeList.add(dubboParamDefinition.getParamType());
                    paramValueList.add(object);

                    serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.PARAM_TYPES, paramTypeList);
                    serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.PARAM_VALUES, paramValueList);
                    return Mono.just(ConvertResult.SUCCESS);
                } catch (Throwable throwable) {
                    log.error("parse body={} failure!", body, throwable);
                    // json数据解析错误
                    return Mono.just(ConvertResult.failure(GatewayErrorEnum.DATA_PARSE_FAILURE));
                }
            });
        }
    }

    /**
     * 处理请求参数
     * @param serverWebExchange
     * @param paramMap
     */
    private Mono<ConvertResult> processParamMap(ServerWebExchange serverWebExchange, MultiValueMap<String, String> paramMap) {
        DubboServiceDefinition dubboServiceDefinition = serverWebExchange.getAttribute(GatewayConstants.SERVICE_ITEM);

        List<DubboParamDefinition> dubboParamDefinitionList = dubboServiceDefinition.getParamDefinitionList();
        if (CollectionUtils.isEmpty(dubboParamDefinitionList)) {
            serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.PARAM_TYPES, Collections.EMPTY_LIST);
            serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.PARAM_VALUES, Collections.EMPTY_LIST);
            return Mono.just(ConvertResult.SUCCESS);
        } else {
            List<String> paramTypeList = new ArrayList<>();
            List<Object> paramValueList = new ArrayList<>();

            for (DubboParamDefinition dubboParamDefinition : dubboParamDefinitionList) {
                PrimitiveTypeEnum primitiveTypeEnum = PrimitiveTypeEnum.of(dubboParamDefinition.getParamType());
                if (primitiveTypeEnum == null) {
                    if (dubboParamDefinitionList.size() > 1) {
                        // 仅允许一个参数
                        return Mono.just(ConvertResult.failure(GatewayErrorEnum.ONLY_SUPPORT_ONE_ARGS));
                    } else {
                        // 自定义参数类型，仅支持往下找一级
                        List<DubboParamDefinition> subParamDefinitionList = dubboParamDefinition.getSubParamDefinitionList();
                        Map<String, Object> paramValueMap = new HashMap<>();
                        if (CollectionUtils.isNotEmpty(subParamDefinitionList)) {
                            for (DubboParamDefinition subParamDefinition : subParamDefinitionList) {
                                supportGetLevel1(subParamDefinition, primitiveTypeEnum, paramValueMap, paramMap);
                            }
                        }

                        paramTypeList.add(dubboParamDefinition.getParamType());
                        serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.PARAM_TYPES, paramTypeList);
                        serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.PARAM_VALUES, Arrays.asList(paramValueMap));
                        return Mono.just(ConvertResult.SUCCESS);
                    }
                } else {
                    supportGetLevel1(dubboParamDefinition, primitiveTypeEnum, paramTypeList, paramValueList, paramMap);
                }
            }

            serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.PARAM_TYPES, paramTypeList);
            serverWebExchange.getAttributes().putIfAbsent(GatewayConstants.PARAM_VALUES, paramValueList);
            return Mono.just(ConvertResult.SUCCESS);
        }
    }

    /**
     * 支持get方法组装一级参数
     * @param dubboParamDefinition
     * @param primitiveTypeEnum
     * @param paramValueMap
     * @param paramMap
     */
    private static void supportGetLevel1(DubboParamDefinition dubboParamDefinition, PrimitiveTypeEnum primitiveTypeEnum, Map<String, Object> paramValueMap, MultiValueMap<String, String> paramMap) {
        List<String> valueList = paramMap.get(dubboParamDefinition.getParamName());
        if (valueList == null) {
            valueList = new ArrayList<>();
        }
        if (primitiveTypeEnum == null) {
            if (CollectionUtils.isNotEmpty(valueList)) {
                paramValueMap.putIfAbsent(dubboParamDefinition.getParamName(), valueList.get(0));
            }
        } else if (primitiveTypeEnum.equals(PrimitiveTypeEnum.LIST)) {
            paramValueMap.putIfAbsent(dubboParamDefinition.getParamName(), valueList);
        } else if (primitiveTypeEnum.equals(PrimitiveTypeEnum.MAP)) {
            //不处理Map
            return;
        }
    }

    /**
     * 支持get方法组装一级参数
     * @param dubboParamDefinition
     * @param primitiveTypeEnum
     * @param paramTypeList
     * @param paramValueList
     * @param paramMap
     */
    private static void supportGetLevel1(DubboParamDefinition dubboParamDefinition, PrimitiveTypeEnum primitiveTypeEnum, List<String> paramTypeList, List<Object> paramValueList, MultiValueMap<String, String> paramMap) {
        List<String> valueList = paramMap.get(dubboParamDefinition.getParamName());
        if (valueList == null) {
            valueList = new ArrayList<>();
        }
        if (primitiveTypeEnum == null) {
            paramTypeList.add(dubboParamDefinition.getParamType());
            if (CollectionUtils.isNotEmpty(valueList)) {
                paramValueList.add(valueList.get(0));
            }
        } else if (primitiveTypeEnum.equals(PrimitiveTypeEnum.LIST)) {
            paramTypeList.add(dubboParamDefinition.getParamType());
            paramValueList.add(valueList);
        } else if (primitiveTypeEnum.equals(PrimitiveTypeEnum.MAP)) {
            //不处理Map
            return;
        }
    }
}
