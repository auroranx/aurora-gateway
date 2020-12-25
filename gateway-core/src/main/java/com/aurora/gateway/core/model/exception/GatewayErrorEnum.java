package com.aurora.gateway.core.model.exception;

public enum GatewayErrorEnum {
    INVOKE_FAILURE(100, "执行异常！"),
    REQUEST_MAPPER_NOT_AVAILABLE(110, "请求转换不可用！"),
    RESPONSE_MAPPER_NOT_AVAILABLE(120, "响应转换不可用！"),
    GATEWAY_CONFIG_CENTER_NOT_EXIST(130, "配置中心不可用！"),


    ERROR_MEDIA_TYPE(1000, "错误的媒体类型！"),
    UN_SUPPORT_MEDIA_TYPE(1001, "不支持的媒体类型！"),
    UN_SUPPORT_METHOD_TYPE(1002, "不支持的方法类型！"),
    ONLY_SUPPORT_ONE_ARGS(1003, "仅支持一个参数！"),
    UNABLE_PARSE_DATA(1004, "数据无法解析！"),
    DATA_PARSE_FAILURE(1005, "数据解析失败！"),

    SERVICE_ITEM_CONFIG_NOT_FOUND(2001, "服务配置未找到！"),
    SERVICE_ITEM_PROVIDER_NOT_FOUND(2002, "服务提供者未找到！"),
    SERVICE_ITEM_INVOKE_FAILURE(2003, "服务执行失败！"),

    SUBSCRIBE_FAILURE(3001, "订阅异常！"),
    ;

    private int code;
    private String desc;

    GatewayErrorEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
