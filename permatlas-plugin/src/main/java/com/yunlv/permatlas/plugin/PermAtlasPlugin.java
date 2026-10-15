package com.yunlv.permatlas.plugin;

import com.yunlv.permatlas.api.FallbackPermissions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class PermAtlasPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadFallbackFromConfig();

        getLogger().info("PermAtlas API ready (v" + getDescription().getVersion() + ")");
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
            getLogger().info("Loaded " + loaded.size() + " fallback permission mappings");
        }
    }
}