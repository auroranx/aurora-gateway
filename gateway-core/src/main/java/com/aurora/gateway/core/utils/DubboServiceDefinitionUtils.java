package com.aurora.gateway.core.utils;

import com.aurora.gateway.core.model.DubboServiceDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class DubboServiceDefinitionUtils {

    public static String getUniqueKey(DubboServiceDefinition dubboServiceDefinition) {
        StringBuilder builder = new StringBuilder();
        builder.append(dubboServiceDefinition.getInterfaceName());
        builder.append("-");
        if (StringUtils.isNotBlank(dubboServiceDefinition.getVersion())) {
            builder.append(dubboServiceDefinition.getVersion().trim());
        }

        return builder.toString().trim();
    }
}
