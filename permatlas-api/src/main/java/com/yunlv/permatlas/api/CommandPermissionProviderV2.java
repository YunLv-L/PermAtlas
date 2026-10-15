package com.yunlv.permatlas.api;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * v2 扩展契约，向后兼容 v1。
 *
 * <p>消费者用 {@code instanceof CommandPermissionProviderV2} 判断能力，
 * ServicesManager 中始终只注册 {@link CommandPermissionProvider} 这一个 key，
 * v1 和 v2 提供者可共存。
 *
 * <p>实现 v2 的提供者 <b>必须保证线程安全</b>，因为异步方法可能被并发调用。
 */
public interface CommandPermissionProviderV2 extends CommandPermissionProvider {

    /**
     * 反向查询：给定权限节点，返回所有覆盖它的命令路径。
     * 一个节点通常对应多条路径（如 residence.command.tp 覆盖 res tp / res tpa / res tphere）。
     */
    Set<String> getCommandPathsForPermission(String permissionNode);

    /**
     * 异步查询。实现方应在给定 executor 上执行实际工作，
     * 不要在主线程内求值。
     */
    CompletableFuture<String> getPermissionForPathAsync(String commandPath, Executor executor);
}