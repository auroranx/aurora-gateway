package com.aurora.gateway.demo.doc;

import com.aurora.gateway.doc.annotation.GwModelProperty;

import java.io.Serializable;

public class Param implements Serializable {

    @GwModelProperty(required = true, note = "域名")
    private String host;
}
