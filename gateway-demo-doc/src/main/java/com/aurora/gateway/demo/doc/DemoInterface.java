package com.aurora.gateway.demo.doc;

import com.aurora.gateway.doc.annotation.GwApi;
import com.aurora.gateway.doc.annotation.GwMethod;
import com.aurora.gateway.doc.annotation.GwParam;

@GwApi
public interface DemoInterface {

    @GwMethod(serviceAlias = "gw.dns", remark = "demo示例")
    Result invoke(@GwParam(note = "请求参数", required = true) Param param);
}
