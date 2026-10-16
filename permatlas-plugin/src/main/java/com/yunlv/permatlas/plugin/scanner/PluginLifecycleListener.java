package com.yunlv.permatlas.plugin.scanner;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * 监听插件加载 / 卸载，增量更新索引。
 */
public final class PluginLifecycleListener implements Listener {

    private final PluginScanner scanner;

    public PluginLifecycleListener(PluginScanner scanner) {
        this.scanner = scanner;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnable(PluginEnableEvent event) {
        scanner.scanPlugin(event.getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisable(PluginDisableEvent event) {
        scanner.removePlugin(event.getPlugin());
    }
}