package com.yunlv.permatlas.plugin;

import com.yunlv.permatlas.api.FallbackPermissions;
import com.yunlv.permatlas.api.PermAtlas;
import com.yunlv.permatlas.plugin.provider.AutoDiscoveryProvider;
import com.yunlv.permatlas.plugin.scanner.PluginLifecycleListener;
import com.yunlv.permatlas.plugin.scanner.PluginScanner;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class PermAtlasPlugin extends JavaPlugin {

    private PluginScanner scanner;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 1) 兜底表
        loadFallbackFromConfig();

        // 2) 自动发现 Provider（优先级 Low，排在插件自定义 Provider 之后）
        scanner = new PluginScanner();

        // 延迟 1 tick 全量扫描：STARTUP 阶段其他插件可能还没加载完
        Bukkit.getScheduler().runTask(this, () -> {
            scanner.scanAll();
            getLogger().info("Auto-discovery indexed " + scanner.size() + " commands");
        });

        PermAtlas.register(this, new AutoDiscoveryProvider(scanner), ServicePriority.Low);

        // 3) 动态监听插件加载/卸载
        getServer().getPluginManager().registerEvents(
                new PluginLifecycleListener(scanner), this);

        getLogger().info("PermAtlas API ready (v" + getDescription().getVersion() + ")");
    }

    @Override
    public void onDisable() {
        PermAtlas.unregisterAll(this);
    }

    private void loadFallbackFromConfig() {
        ConfigurationSection sec = getConfig().getConfigurationSection("fallback");
        if (sec == null) return;

        Map<String, String> loaded = new HashMap<>();
        for (String key : sec.getKeys(false)) {
            String value = sec.getString(key);
            if (value != null && !value.isBlank()) {
                loaded.put(key, value);
            }
        }
        if (!loaded.isEmpty()) {
            FallbackPermissions.replace(loaded);
            getLogger().info("Loaded " + loaded.size() + " fallback mappings");
        }
    }
}