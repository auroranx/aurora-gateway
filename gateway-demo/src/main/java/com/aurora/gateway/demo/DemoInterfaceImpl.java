package com.aurora.gateway.demo;

import com.alibaba.dubbo.config.annotation.Service;

@Service
public class DemoInterfaceImpl implements DemoInterface {
    @Override
    public Result invoke(Param param) {
        Result result = new Result();
        result.setCode(1111);
        result.setDesc("大发发都发");
        return result;
    }
}
