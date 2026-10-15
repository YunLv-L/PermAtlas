package com.yunlv.permatlas.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 命令路径标准化。
 *
 * <p>约定：小写、去前导斜杠、单空格分隔、去首尾空白。
 * 示例：{@code "Res TP 1"} → {@code "res tp 1"}
 */
public final class CommandPath {

    private CommandPath() {}

    public static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT);
        if (s.startsWith("/")) s = s.substring(1);
        return s.replaceAll("\\s+", " ").trim();
    }

    public static String of(String command, String... subCommands) {
        StringBuilder sb = new StringBuilder(normalize(command));
        if (subCommands != null) {
            for (String sub : subCommands) {
                if (sub == null || sub.isBlank()) continue;
                sb.append(' ').append(sub.trim().toLowerCase(Locale.ROOT));
            }
        }
        return normalize(sb.toString());
    }

    public static String root(String commandPath) {
        String n = normalize(commandPath);
        int i = n.indexOf(' ');
        return i < 0 ? n : n.substring(0, i);
    }

    public static String parent(String commandPath) {
        String n = normalize(commandPath);
        int i = n.lastIndexOf(' ');
        return i < 0 ? "" : n.substring(0, i);
    }

    /** 从最具体到最泛的祖先链：{"res tp 1", "res tp", "res"}。 */
    public static List<String> ancestry(String commandPath) {
        List<String> out = new ArrayList<>(4);
        String cur = normalize(commandPath);
        while (!cur.isEmpty()) {
            out.add(cur);
            cur = parent(cur);
        }
        return out;
    }
}