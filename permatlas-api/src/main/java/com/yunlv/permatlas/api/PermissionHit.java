package com.yunlv.permatlas.api;

import org.bukkit.plugin.ServicePriority;

/** 一次查询命中的结果，附带来源与优先级，便于审计。 */
public record PermissionHit(
        String providerName,
        String permission,
        ServicePriority priority
) {}