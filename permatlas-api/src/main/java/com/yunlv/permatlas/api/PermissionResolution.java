package com.yunlv.permatlas.api;

import org.bukkit.plugin.ServicePriority;

/**
 * 一次权限解析的完整结果。
 *
 * <p>相较简单的 "路径 → 权限" 映射，本对象额外保留：
 * <ul>
 *     <li>{@code requestedPath}：用户原始输入标准化后的路径</li>
 *     <li>{@code matchedPath}：实际命中的祖先路径（可能是 requestedPath 的父级）</li>
 *     <li>{@code fallback}：是否来自兜底表而非真实 Provider</li>
 * </ul>
 *
 * <p>这样消费者（如 /whereis）可以明确告诉用户：
 * "你输入的 res tp 1 匹配到了 res tp，权限是 residence.command.tp"。
 */
public record PermissionResolution(
        String requestedPath,
        String matchedPath,
        String permission,
        String providerName,
        ServicePriority priority,
        boolean fallback
) {

    /** 是否是精确命中（requestedPath 与 matchedPath 相同）。 */
    public boolean isExactMatch() {
        return requestedPath.equals(matchedPath);
    }

    /** 命中的层级深度差：0 表示精确命中，1 表示回退一层，以此类推。 */
    public int fallbackDepth() {
        if (isExactMatch()) return 0;
        int requested = CommandPath.ancestry(requestedPath).size();
        int matched = CommandPath.ancestry(matchedPath).size();
        return Math.max(0, requested - matched);
    }
}