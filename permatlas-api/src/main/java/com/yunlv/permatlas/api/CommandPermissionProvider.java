package com.yunlv.permatlas.api;

import java.util.Collections;
import java.util.Map;

/**
 * 子命令权限提供者契约。
 *
 * <p>实现方通过 ServicesManager 注册自身（见 {@link PermAtlas#register}），
 * 消费方通过 {@link PermAtlas} 的门面方法查询。
 *
 * <p><b>不要修改本接口的既有方法</b>。扩展请走 {@link CommandPermissionProviderV2}。
 */
public interface CommandPermissionProvider {

    /** 提供者名称，通常为插件名。 */
    String getProviderName();

    /**
     * 查询标准路径对应的权限节点。
     *
     * @param commandPath 已标准化路径（见 {@link CommandPath}）
     * @return 权限节点；若无需权限或未注册，返回 {@code null}
     */
    String getPermissionForPath(String commandPath);

    /**
     * 全量导出，用于批量扫描 / 审计 / 导出工具。
     * 默认空 Map；实现方若支持批量导出，覆写此方法。
     */
    default Map<String, String> getAllCommandPermissions() {
        return Collections.emptyMap();
    }

    /** 便捷方法：由命令 + 子命令查询。 */
    default String permissionFor(String command, String... subCommands) {
        return getPermissionForPath(CommandPath.of(command, subCommands));
    }
}