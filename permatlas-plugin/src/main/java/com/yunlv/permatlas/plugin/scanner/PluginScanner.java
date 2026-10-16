package com.yunlv.permatlas.plugin.scanner;

import com.yunlv.permatlas.api.CommandPath;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扫描所有已加载插件的 plugin.yml，提取"根命令 → 权限节点"的映射。
 *
 * <p>Bukkit 层面拿不到子命令信息，所以这里只覆盖根命令级。
 * 子命令级权限需要插件自己实现 Provider 来精确声明。
 */
public final class PluginScanner {

    private final Map<String, String> commandPermissions = new ConcurrentHashMap<>();

    /** 全量重扫。插件加载/卸载后调用。 */
    public void scanAll() {
        commandPermissions.clear();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            scanPlugin(plugin);
        }
    }

    /** 扫描单个插件。 */
    public void scanPlugin(Plugin plugin) {
        PluginDescriptionFile desc = plugin.getDescription();
        Map<String, Map<String, Object>> commands = desc.getCommands();
        if (commands == null || commands.isEmpty()) return;

        for (Map.Entry<String, Map<String, Object>> entry : commands.entrySet()) {
            String commandName = CommandPath.normalize(entry.getKey());
            Map<String, Object> data = entry.getValue();

            // 1) plugin.yml 里显式声明的 permission 字段
            String permission = asString(data.get("permission"));

            // 2) 兜底：运行时 PluginCommand 的 getPermission()
            if (permission == null) {
                PluginCommand cmd = plugin.getCommand(entry.getKey());
                if (cmd != null && cmd.getPermission() != null
                        && !cmd.getPermission().isEmpty()) {
                    permission = cmd.getPermission();
                }
            }

            if (permission == null || permission.isEmpty()) continue;

            register(commandName, permission);

            // 别名也登记一份
            Object aliasesObj = data.get("aliases");
            if (aliasesObj instanceof List<?> aliases) {
                for (Object alias : aliases) {
                    if (alias instanceof String s && !s.isBlank()) {
                        register(CommandPath.normalize(s), permission);
                    }
                }
            }
        }
    }

    /** 移除某插件贡献的所有条目。 */
    public void removePlugin(Plugin plugin) {
        PluginDescriptionFile desc = plugin.getDescription();
        Map<String, Map<String, Object>> commands = desc.getCommands();
        if (commands == null) return;
        for (String commandName : commands.keySet()) {
            commandPermissions.remove(CommandPath.normalize(commandName));
        }
    }

    public String getPermission(String commandPath) {
        return commandPermissions.get(CommandPath.normalize(commandPath));
    }

    public Map<String, String> all() {
        return Collections.unmodifiableMap(commandPermissions);
    }

    public int size() {
        return commandPermissions.size();
    }

    private void register(String command, String permission) {
        commandPermissions.put(command, permission);
    }

    private static String asString(Object o) {
        if (o instanceof String s && !s.isBlank()) return s;
        return null;
    }
}