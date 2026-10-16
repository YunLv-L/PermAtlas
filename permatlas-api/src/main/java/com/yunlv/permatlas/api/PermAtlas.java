package com.yunlv.permatlas.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * PermAtlas 门面：注册 / 发现 / 解析的唯一入口。
 *
 * <p>典型用法（消费者侧）：
 * <pre>{@code
 * List<PermissionResolution> results = PermAtlas.resolve("res tp 1");
 * if (results.isEmpty()) {
 *     // 没找到任何 Provider 也没有兜底映射
 * } else {
 *     for (PermissionResolution r : results) {
 *         // r.providerName(), r.matchedPath(), r.permission()
 *     }
 * }
 * }</pre>
 */
public final class PermAtlas {

    private PermAtlas() {}

    // ------------------------------------------------------------
    // Provider 侧
    // ------------------------------------------------------------

    public static void register(Plugin plugin, CommandPermissionProvider provider) {
        register(plugin, provider, ServicePriority.Normal);
    }

    public static void register(Plugin plugin,
                                CommandPermissionProvider provider,
                                ServicePriority priority) {
        Bukkit.getServicesManager().register(
                CommandPermissionProvider.class, provider, plugin, priority);
    }

    /** onDisable() 时调用，避免遗留失效 Provider。 */
    public static void unregisterAll(Plugin plugin) {
        Bukkit.getServicesManager().unregisterAll(plugin);
    }

    // ------------------------------------------------------------
    // 发现
    // ------------------------------------------------------------

    /** 全部 Provider，按优先级从高到低。 */
    public static List<CommandPermissionProvider> providers() {
        List<CommandPermissionProvider> out = new ArrayList<>();
        for (RegisteredServiceProvider<CommandPermissionProvider> rsp : sortedRegistrations()) {
            out.add(rsp.getProvider());
        }
        return out;
    }

    /** 精确取某个插件注册的 Provider。 */
    public static Optional<CommandPermissionProvider> providerOf(Plugin plugin) {
        for (RegisteredServiceProvider<CommandPermissionProvider> rsp : sortedRegistrations()) {
            if (plugin.equals(rsp.getPlugin())) {
                return Optional.of(rsp.getProvider());
            }
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------
    // 解析
    // ------------------------------------------------------------

    /**
     * 解析命令路径，返回所有命中的结果（每个 Provider 至多一条，外加兜底）。
     *
     * <p>查询顺序：
     * <ol>
     *     <li>遍历所有 Provider（按优先级降序）</li>
     *     <li>对每个 Provider，按 {@link CommandPath#ancestry(String)} 从最具体到最泛尝试</li>
     *     <li>取该 Provider 的第一个命中，加入结果</li>
     *     <li>若所有 Provider 都未命中，再查兜底表（同样走 ancestry 回退）</li>
     * </ol>
     *
     * @param commandPath 用户输入的命令路径（可以是原始字符串，会被标准化）
     * @return 全部命中结果，按 Provider 优先级降序；无命中时返回空列表
     */
    public static List<PermissionResolution> resolve(String commandPath) {
        String requested = CommandPath.normalize(commandPath);
        List<PermissionResolution> results = new ArrayList<>();

        for (RegisteredServiceProvider<CommandPermissionProvider> rsp : sortedRegistrations()) {
            CommandPermissionProvider provider = rsp.getProvider();
            for (String candidate : CommandPath.ancestry(requested)) {
                String perm = provider.getPermissionForPath(candidate);
                if (perm != null && !perm.isEmpty()) {
                    results.add(new PermissionResolution(
                            requested,
                            candidate,
                            perm,
                            provider.getProviderName(),
                            rsp.getPriority(),
                            false
                    ));
                    break; // 该 Provider 只取最具体的命中
                }
            }
        }

        if (results.isEmpty()) {
            for (String candidate : CommandPath.ancestry(requested)) {
                String perm = FallbackPermissions.lookup(candidate);
                if (perm != null && !perm.isEmpty()) {
                    results.add(new PermissionResolution(
                            requested,
                            candidate,
                            perm,
                            "PermAtlas/Vanilla",
                            ServicePriority.Lowest,
                            true
                    ));
                    break;
                }
            }
        }

        return results;
    }

    /**
     * 返回最佳命中（即 {@link #resolve(String)} 的第一条）。
     * 适合"只要一个答案"的场景。
     */
    public static Optional<PermissionResolution> find(String commandPath) {
        List<PermissionResolution> all = resolve(commandPath);
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }

    // ------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------

    private static List<RegisteredServiceProvider<CommandPermissionProvider>> sortedRegistrations() {
        List<RegisteredServiceProvider<CommandPermissionProvider>> list =
                new ArrayList<>(Bukkit.getServicesManager()
                        .getRegistrations(CommandPermissionProvider.class));
        list.sort(Comparator
                .comparingInt((RegisteredServiceProvider<CommandPermissionProvider> r)
                        -> r.getPriority().ordinal())
                .reversed());
        return list;
    }
}