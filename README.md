# PermAtlas

Subcommand permission metadata API for Paper 1.20.1 plugins. Declare your command permissions via a unified contract, and any query tool can discover them through Bukkit's `ServicesManager`.

## 架构

PermAtlas 提供一套标准接口 `CommandPermissionProvider`，任何插件都可以实现它并注册到 Bukkit `ServicesManager`。查询工具（如 `/whereis`）通过 `PermAtlas` 门面自动发现所有提供者，并按“精确路径 → 逐级回退 → 兜底表”三层策略查询权限节点。

## 模块

- `permatlas-api`：纯接口 + 门面，无插件本体，供其他插件 `compileOnly` 依赖
- `permatlas-plugin`：空壳插件，承载 API 类加载，服务器安装这个

## 快速接入

### 1. 添加依赖

```gradle
dependencies {
    compileOnly("com.yunlv.permatlas:permatlas-api:1.0.0")
}
```

### 2. 在 `plugin.yml` 里声明

```yaml
depend: [PermAtlas]
```

### 3. 实现 Provider

```java
public final class MyProvider implements CommandPermissionProvider {
    @Override
    public String getProviderName() {
        return "MyPlugin";
    }

    @Override
    public String getPermissionForPath(String commandPath) {
        return Map.of(
            "my cmd", "myplugin.command.cmd"
        ).get(commandPath);
    }
}
```

### 4. 在 `onEnable()` 注册

```java
PermAtlas.register(this, new MyProvider());
```

### 5. 在 `onDisable()` 注销

```java
PermAtlas.unregisterAll(this);
```