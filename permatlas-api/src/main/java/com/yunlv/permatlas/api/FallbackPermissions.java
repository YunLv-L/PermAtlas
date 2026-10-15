package com.yunlv.permatlas.api;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 内置兜底映射：覆盖原版命令和未提供 Provider 的场景。
 *
 * <p>真实项目里这张表建议从 config 或独立 json 加载，
 * 这里给最小可用版本，后续由 PermAtlas 插件在 onEnable 时注入。
 */
public final class FallbackPermissions {

    private static volatile Map<String, String> table = defaultTable();

    private FallbackPermissions() {}

    private static Map<String, String> defaultTable() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("gamemode survival",  "minecraft.command.gamemode");
        m.put("gamemode creative",  "minecraft.command.gamemode");
        m.put("gamemode spectator", "minecraft.command.gamemode");
        m.put("tp",                 "minecraft.command.tp");
        m.put("give",               "minecraft.command.give");
        m.put("execute",            "minecraft.command.execute");
        m.put("summon",             "minecraft.command.summon");
        m.put("effect",             "minecraft.command.effect");
        return Map.copyOf(m);
    }

    /** 由 PermAtlas 插件在 onEnable 时用 config 内容覆盖。 */
    public static void replace(Map<String, String> newTable) {
        table = Map.copyOf(newTable);
    }

    public static String lookup(String commandPath) {
        return table.get(CommandPath.normalize(commandPath));
    }

    public static Map<String, String> all() {
        return table;
    }
}