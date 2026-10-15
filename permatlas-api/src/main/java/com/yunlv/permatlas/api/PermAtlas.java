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
 * PermAtlas 门面：注册 / 发现 / 查询的唯一入口。
 *
 * <p>Consumer 侧推荐调用 {@link #find(String)} 和 {@link #findAll(String)}，
 * 它们已经内置了"精确路径 + 逐级回退 + 兜底表"三层策略。
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
    // Consumer 侧
    // ------------------------------------------------------------

    /** 全部 Provider，按优先级从高到低。 */
    public static List<CommandPermissionProvider> providers() {
        List<RegisteredServiceProvider<CommandPermissionProvider>> list =
                new ArrayList<>(Bukkit.getServicesManager()
                        .getRegistrations(CommandPermissionProvider.class));
        list.sort(Comparator
                .comparingInt((RegisteredServiceProvider<CommandPermissionProvider> r)
                        -> r.getPriority().ordinal())
                .reversed());

        List<CommandPermissionProvider> out = new ArrayList<>(list.size());
        for (RegisteredServiceProvider<CommandPermissionProvider> r : list) {
            out.add(r.getProvider());
        }
        return out;
    }

    /** 精确取某个插件注册的 Provider。 */
    public static Optional<CommandPermissionProvider> providerOf(Plugin plugin) {
        for (RegisteredServiceProvider<CommandPermissionProvider> rsp :
                Bukkit.getServicesManager()
                        .getRegistrations(CommandPermissionProvider.class)) {
            if (plugin.equals(rsp.getPlugin())) {
                return Optional.of(rsp.getProvider());
            }
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------
    // 查询策略：精确 -> 逐级回退 -> 兜底表
    // ------------------------------------------------------------

    /**
     * 返回第一个命中的权限节点。
     * 查询顺序：对每个 Provider，按 {@link CommandPath#ancestry(String)}
     * 从最具体到最泛尝试；全部未命中则查兜底表。
     */
    public static Optional<String> find(String commandPath) {
        String path = CommandPath.normalize(commandPath);
        for (CommandPermissionProvider p : providers()) {
            String hit = queryProvider(p, path);
            if (hit != null && !hit.isEmpty()) return Optional.of(hit);
        }
        return Optional.ofNullable(FallbackPermissions.lookup(path));
    }

    /** 返回全部命中（多 Provider 同时声明同一路径时使用）。 */
    public static List<PermissionHit> findAll(String commandPath) {
        String path = CommandPath.normalize(commandPath);
        List<PermissionHit> hits = new ArrayList<>();

        List<RegisteredServiceProvider<CommandPermissionProvider>> list =
                new ArrayList<>(Bukkit.getServicesManager()
                        .getRegistrations(CommandPermissionProvider.class));
        list.sort(Comparator
                .comparingInt((RegisteredServiceProvider<CommandPermissionProvider> r)
                        -> r.getPriority().ordinal())
                .reversed());

        for (RegisteredServiceProvider<CommandPermissionProvider> r : list) {
            String hit = queryProvider(r.getProvider(), path);
            if (hit != null && !hit.isEmpty()) {
                hits.add(new PermissionHit(
                        r.getProvider().getProviderName(), hit, r.getPriority()));
            }
        }

        if (hits.isEmpty()) {
            String fb = FallbackPermissions.lookup(path);
            if (fb != null) {
                hits.add(new PermissionHit("PermAtlas/Vanilla", fb, ServicePriority.Lowest));
            }
        }
        return hits;
    }

    /** 对单个 Provider 走"精确 + 逐级回退"策略。 */
    private static String queryProvider(CommandPermissionProvider provider, String path) {
        for (String candidate : CommandPath.ancestry(path)) {
            String perm = provider.getPermissionForPath(candidate);
            if (perm != null && !perm.isEmpty()) return perm;
        }
        return null;
    }
}