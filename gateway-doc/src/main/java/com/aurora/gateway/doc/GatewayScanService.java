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
package com.aurora.gateway.doc;

import com.alibaba.dubbo.config.annotation.Service;
import com.aurora.gateway.doc.annotation.*;
import com.aurora.gateway.doc.config.DocConfig;
import com.aurora.gateway.doc.handler.TypeHandler;
import com.aurora.gateway.doc.model.ApiDocCache;
import com.aurora.gateway.doc.model.ApiMetadata;
import com.aurora.gateway.doc.model.GatewayDocException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author feixue
 */
@Slf4j
public class GatewayScanService implements EnvironmentAware, InitializingBean {

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private DocConfig docConfig;

    private Environment environment;

    private ClassPathScanningCandidateComponentProvider classPathScanningCandidateComponentProvider;

    public GatewayScanService(DocConfig docConfig) {
        this.docConfig = docConfig;
    }

    private void doScan() {
        /**
         * 检查是否有扫描路径
         */
        if (docConfig.getScanPackage().length <= 0) {
            //没有扫描路径，返回
            log.info("gateway scan service stopped, because gateway.doc.scanPackage is empty.");
            return;
        }

        Set<BeanDefinition> beanDefinitionSet = scanBeanDefinitionSet();

        long nowTime = System.currentTimeMillis();

        for (BeanDefinition beanDefinition : beanDefinitionSet) {
            if (beanDefinition instanceof ScannedGenericBeanDefinition) {
                ScannedGenericBeanDefinition scannedGenericBeanDefinition = (ScannedGenericBeanDefinition) beanDefinition;
                List<ApiMetadata> apiMetadataList = Arrays.stream(scannedGenericBeanDefinition.getMetadata().getInterfaceNames()).map(interfaceName -> {
                    try {
                        Class clazz = Class.forName(interfaceName);
                        GwApi gwApi = AnnotationUtils.findAnnotation(clazz, GwApi.class);
                        if (gwApi == null) {
                            //无@GwApi注解的，认为不需要收录到网关
                            return null;
                        }

                        ApiMetadata apiMetadata = new ApiMetadata();
                        apiMetadata.setNowTime(nowTime);

                        fillServiceMetadata(interfaceName, gwApi, apiMetadata);
                        List<ApiMetadata.MethodMetadata> methodMetadataList = new ArrayList<>();
                        Set<String> serviceAlisSet = new HashSet<>();
                        apiMetadata.setMethodMetadataList(methodMetadataList);
                        Arrays.stream(clazz.getMethods()).forEach(method -> {
                            ApiMetadata.MethodMetadata methodMetadata = generateMethodMetadata(method);
                            if (methodMetadata == null) {
                                return;
                            }
                            if (!serviceAlisSet.contains(methodMetadata.getServiceAlias())) {
                                serviceAlisSet.add(methodMetadata.getServiceAlias());

                                methodMetadataList.add(methodMetadata);
                            } else {
                                throw new GatewayDocException("interfaceName:" + interfaceName + ", methodName:" + method.getName() + ", serviceAlias for: " + methodMetadata.getServiceAlias() + " is duplicate!");
                            }
                        });

                        return apiMetadata;
                    } catch (ClassNotFoundException e) {
                        log.error("generate api metadata failure!", e);
                    }
                    return null;
                }).filter(apiMetadata -> apiMetadata != null).collect(Collectors.toList());

                ApiDocCache.getInstance().addApiMetadata(apiMetadataList);
            }
        }
    }

    /**
     * 生成方法响应参数元数据
     * @param method
     * @return
     */
    private List<ApiMetadata.ParamMetadata> generateMethodResponseParam(Method method) {
        ApiMetadata.ParamMetadata paramMetadata = new ApiMetadata.ParamMetadata();

        Set<String> alreadyAnalysisTypeSet = new HashSet<>();

        Type returnType = method.getGenericReturnType();
        paramMetadata.setParamType(returnType.getTypeName());

        try {
            if (returnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) returnType;

                Type baseType = parameterizedType.getRawType();
                Class baseTypeClazz = Class.forName(baseType.getTypeName());
                paramMetadata.setSubParamList(fillFieldMetadata(parameterizedType, baseTypeClazz, alreadyAnalysisTypeSet));
            } else if (TypeHandler.isMap(returnType)) {
                paramMetadata.setSubParamList(Collections.EMPTY_LIST);
            } else if (TypeHandler.isPrimitiveBoxing(returnType)) {
                paramMetadata.setSubParamList(Collections.EMPTY_LIST);
            } else if (((Class) returnType).isPrimitive()) {
                paramMetadata.setSubParamList(Collections.EMPTY_LIST);
            } else {
                Class subFieldClazz = Class.forName(returnType.getTypeName());
                paramMetadata.setSubParamList(fillFieldMetadata(null, subFieldClazz, alreadyAnalysisTypeSet));
            }
        } catch (Throwable throwable) {
            log.info("fill sub param list failure!", throwable);
            paramMetadata.setSubParamList(Collections.EMPTY_LIST);
        }
        return Arrays.asList(paramMetadata);
    }

    /**
     * 生成方法请求参数元数据
     * @param method
     * @return
     */
    private List<ApiMetadata.ParamMetadata> generateMethodRequestParam(Method method) {
        String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
        Parameter[] parameters = method.getParameters();

        List<ApiMetadata.ParamMetadata> paramMetadataList = new ArrayList<>();

        for (int index = 0; index < paramNames.length; index++) {
            String paramName = paramNames[index];
            Parameter parameter = parameters[index];

            Set<String> alreadyAnalysisTypeSet = new HashSet<>();

            ApiMetadata.ParamMetadata paramMetadata = new ApiMetadata.ParamMetadata();
            paramMetadataList.add(paramMetadata);
            fillParamMetadata(parameter, paramMetadata);
            paramMetadata.setParamName(paramName);
            paramMetadata.setParamType(parameter.getParameterizedType().getTypeName());
            paramMetadata.setPrimitiveType(parameter.getType().isPrimitive());

            if (parameter.getType().isPrimitive()) {
                paramMetadata.setSubParamList(Collections.EMPTY_LIST);
            } else if (TypeHandler.isMap(parameter.getType())) {
                paramMetadata.setSubParamList(Collections.EMPTY_LIST);
            } else if (TypeHandler.isPrimitiveBoxing(parameter.getType())) {
                paramMetadata.setSubParamList(Collections.EMPTY_LIST);
            } else {
                try {
                    if (parameter.getParameterizedType() instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();

                        Type baseType = parameterizedType.getRawType();
                        Class baseTypeClazz = Class.forName(baseType.getTypeName());
                        paramMetadata.setSubParamList(fillFieldMetadata(parameterizedType, baseTypeClazz, alreadyAnalysisTypeSet));
                    } else {
                        Class subFieldClazz = Class.forName(parameter.getType().getTypeName());
                        paramMetadata.setSubParamList(fillFieldMetadata(null, subFieldClazz, alreadyAnalysisTypeSet));
                    }
                } catch (Throwable throwable) {
                    log.info("fill sub param list failure!", throwable);
                    paramMetadata.setSubParamList(Collections.EMPTY_LIST);
                }
            }
        }

        return paramMetadataList;
    }

    /**
     * 填充范型数据结构
     * @param parameterizedType
     * @param paramMetadata
     * @param alreadyAnalysisTypeSet
     * @throws ClassNotFoundException
     */
    private void fillParameterizedType(ParameterizedType parameterizedType, ApiMetadata.ParamMetadata paramMetadata, Set<String> alreadyAnalysisTypeSet) throws ClassNotFoundException {
        if (TypeHandler.isMap(parameterizedType.getRawType())) {
            paramMetadata.setSubParamList(Collections.EMPTY_LIST);
        } else {
            Type[] actualTypes = parameterizedType.getActualTypeArguments();
            if (actualTypes.length <= 0) {
                paramMetadata.setSubParamList(Collections.EMPTY_LIST);
            } else {
                Type actualType = actualTypes[0];
                if (TypeHandler.isMap(actualType)) {
                    paramMetadata.setSubParamList(Collections.EMPTY_LIST);
                } else if (TypeHandler.isPrimitiveBoxing(actualType)) {
                    paramMetadata.setSubParamList(Collections.EMPTY_LIST);
                } else if (actualType instanceof ParameterizedType) {
                    ParameterizedType internalParameterizedType = (ParameterizedType)actualType;
                    String internalTypeName = internalParameterizedType.getRawType().getTypeName();
                    alreadyAnalysisTypeSet.add(internalTypeName);
                    Class subFieldClazz = Class.forName(internalTypeName);
                    paramMetadata.setSubParamList(fillFieldMetadata(internalParameterizedType, subFieldClazz, alreadyAnalysisTypeSet));
                } else {
                    String actualTypeName = actualType.getTypeName();
                    if (alreadyAnalysisTypeSet.contains(actualTypeName)) {
                        //类型已解析的，主动中断解析递归链路
                        paramMetadata.setSubParamList(Collections.EMPTY_LIST);
                    } else {
                        alreadyAnalysisTypeSet.add(actualTypeName);
                        Class subFieldClazz = Class.forName(actualTypeName);
                        paramMetadata.setSubParamList(fillFieldMetadata(null, subFieldClazz, alreadyAnalysisTypeSet));
                    }
                }
            }
        }
    }

    /**
     * 填充属性数据元数据
     * @param clazzParameterizedType
     * @param fieldClazz
     * @param alreadyAnalysisTypeSet
     * @return
     * @throws ClassNotFoundException
     */
    private List<ApiMetadata.ParamMetadata> fillFieldMetadata(ParameterizedType clazzParameterizedType, Class fieldClazz, Set<String> alreadyAnalysisTypeSet) throws ClassNotFoundException {
        Field[] fields = fieldClazz.getDeclaredFields();

        if (fields == null || fields.length <= 0) {
            if (clazzParameterizedType == null) {
                return Collections.EMPTY_LIST;
            }
            Type[] actualTypeArguments = clazzParameterizedType.getActualTypeArguments();
            if (actualTypeArguments == null || actualTypeArguments.length <= 0) {
                return Collections.EMPTY_LIST;
            } else {
                ApiMetadata.ParamMetadata paramMetadata = new ApiMetadata.ParamMetadata();
                fillParameterizedType(clazzParameterizedType, paramMetadata, alreadyAnalysisTypeSet);
                return Arrays.asList(paramMetadata);
            }
        }

        List<ApiMetadata.ParamMetadata> fieldMetadataList = new ArrayList<>();

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            GwPropertyIgnore gwPropertyIgnore = AnnotationUtils.findAnnotation(field, GwPropertyIgnore.class);
            if (gwPropertyIgnore != null) {
                //字段上有设置忽略注解，跳过该字段扫描
                continue;
            }

            field.setAccessible(true);

            ApiMetadata.ParamMetadata fieldMetadata = new ApiMetadata.ParamMetadata();
            fieldMetadataList.add(fieldMetadata);

            fillFieldMetadata(field, fieldMetadata);

            fieldMetadata.setPrimitiveType(field.getType().isPrimitive());
            fieldMetadata.setParamName(field.getName());
            fieldMetadata.setParamType(field.getType().getName());

            if (TypeHandler.isMap(field.getType())) {
                fieldMetadata.setSubParamList(Collections.EMPTY_LIST);
            } else if (field.getType().isPrimitive()) {
                fieldMetadata.setSubParamList(Collections.EMPTY_LIST);
            } else if (TypeHandler.isPrimitiveBoxing(field.getType())) {
                fieldMetadata.setSubParamList(Collections.EMPTY_LIST);
            } else {
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericType;
                    fieldMetadata.setParamType(parameterizedType.getTypeName());
                    fillParameterizedType(parameterizedType, fieldMetadata, alreadyAnalysisTypeSet);
                } else if (genericType instanceof TypeVariable) {
                    boolean isContinue = true;
                    if (clazzParameterizedType != null) {
                        Type[] actualTypeArguments = clazzParameterizedType.getActualTypeArguments();
                        if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                            fieldMetadata.setParamType(actualTypeArguments[0].getTypeName());
                            fillParameterizedType(clazzParameterizedType, fieldMetadata, alreadyAnalysisTypeSet);
                            isContinue = false;
                        }
                    }
                    if (isContinue) {
                        String typeName = field.getType().getTypeName();
                        if (alreadyAnalysisTypeSet.contains(typeName)) {
                            //类型已解析的，主动中断解析递归链路
                            fieldMetadata.setSubParamList(Collections.EMPTY_LIST);
                        } else {
                            Class subFieldClazz = Class.forName(typeName);
                            fieldMetadata.setSubParamList(fillFieldMetadata(null, subFieldClazz, alreadyAnalysisTypeSet));
                        }
                    }
                } else {
                    String typeName = field.getType().getTypeName();
                    if (alreadyAnalysisTypeSet.contains(typeName)) {
                        //类型已解析的，主动中断解析递归链路
                        fieldMetadata.setSubParamList(Collections.EMPTY_LIST);
                    } else {
                        alreadyAnalysisTypeSet.add(typeName);
                        Class subFieldClazz = Class.forName(typeName);
                        fieldMetadata.setSubParamList(fillFieldMetadata(null, subFieldClazz, alreadyAnalysisTypeSet));
                    }
                }
            }
        }

        return fieldMetadataList;
    }

    /**
     * 填充字段属性的元数据
     * @param field
     * @param fieldMetadata
     */
    private void fillFieldMetadata(Field field, ApiMetadata.ParamMetadata fieldMetadata) {
        GwModelProperty gwModelProperty = AnnotationUtils.findAnnotation(field, GwModelProperty.class);
        if (gwModelProperty != null) {
            fieldMetadata.setRequired(gwModelProperty.required());
            fieldMetadata.setNote(gwModelProperty.note());
            fieldMetadata.setDemoValue(gwModelProperty.demoValue());
        }
    }

    /**
     * 填充参数元数据
     * @param parameter
     * @param paramMetadata
     */
    private void fillParamMetadata(Parameter parameter, ApiMetadata.ParamMetadata paramMetadata) {
        GwParam gwParam = AnnotationUtils.findAnnotation(parameter, GwParam.class);
        if (gwParam != null) {
            paramMetadata.setNote(gwParam.note());
            paramMetadata.setRequired(gwParam.required());
            paramMetadata.setDemoValue(gwParam.demoValue());
        }
    }

    /**
     * 生成方法元数据
     * @param method
     * @return
     */
    private ApiMetadata.MethodMetadata generateMethodMetadata(Method method) {
        GwMethod gwMethod = AnnotationUtils.findAnnotation(method, GwMethod.class);
        if (gwMethod == null) {
            return null;
        }

        ApiMetadata.MethodMetadata methodMetadata = new ApiMetadata.MethodMetadata();
        methodMetadata.setMethodName(method.getName());
        if (gwMethod != null) {
            methodMetadata.setServiceAlias(gwMethod.serviceAlias());
            methodMetadata.setRemark(gwMethod.remark());
            methodMetadata.setTimeout(gwMethod.timeout());
        }

        methodMetadata.setRequestParamList(generateMethodRequestParam(method));
        methodMetadata.setResponseParamList(generateMethodResponseParam(method));

        return methodMetadata;
    }

    /**
     * 填充接口元数据
     * @param interfaceName
     * @param gwApi
     * @param apiMetadata
     * @return
     */
    private void fillServiceMetadata(String interfaceName, GwApi gwApi, ApiMetadata apiMetadata) {
        ApiMetadata.ServiceMetadata serviceMetadata = new ApiMetadata.ServiceMetadata();
        serviceMetadata.setAppName(docConfig.getAppName());
        serviceMetadata.setServiceName(interfaceName);
        serviceMetadata.setVersion(gwApi.version());
        apiMetadata.setServiceMetadata(serviceMetadata);
    }

    /**
     * 扫描bean集合
     * @return
     */
    private Set<BeanDefinition> scanBeanDefinitionSet() {
        classPathScanningCandidateComponentProvider = new ClassPathScanningCandidateComponentProvider(false, environment);
        //仅扫描带有 dubbo 的@Service注解的bean
        classPathScanningCandidateComponentProvider.addIncludeFilter(new AnnotationTypeFilter(Service.class));

        Set<BeanDefinition> beanDefinitionSet = new HashSet<>();
        Arrays.stream(docConfig.getScanPackage()).forEach(scanPackage -> {
            beanDefinitionSet.addAll(classPathScanningCandidateComponentProvider.findCandidateComponents(scanPackage));
        });

        return beanDefinitionSet;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        doScan();
    }
}
