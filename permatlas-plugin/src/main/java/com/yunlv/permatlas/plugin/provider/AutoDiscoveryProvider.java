package com.yunlv.permatlas.plugin.provider;

import com.yunlv.permatlas.api.CommandPath;
import com.yunlv.permatlas.api.CommandPermissionProvider;
import com.yunlv.permatlas.plugin.scanner.PluginScanner;

import java.util.Map;

/**
 * 内置自动发现 Provider。数据来自 {@link PluginScanner}。
 *
 * <p>注册时使用 {@code ServicePriority.Low}，保证排在插件自定义 Provider 之后，
 * 只在插件没有精确声明子命令权限时兜底。
 */
public final class AutoDiscoveryProvider implements CommandPermissionProvider {

    private final PluginScanner scanner;

    public AutoDiscoveryProvider(PluginScanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public String getProviderName() {
        return "AutoDiscovery";
    }

    @Override
    public String getPermissionForPath(String commandPath) {
        return scanner.getPermission(CommandPath.normalize(commandPath));
    }

    @Override
    public Map<String, String> getAllCommandPermissions() {
        return scanner.all();
    }
}