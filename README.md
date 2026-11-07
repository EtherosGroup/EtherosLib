# EtherosLib

Etheros 系列插件的跨平台核心库插件，支持 Bukkit/Paper 与 Velocity。

- 事件总线：跨插件共享的 `BusEvent` 投递，平台侧薄适配
- 命令框架：注解声明命令、子命令、参数解析与 Tab 补全，执行与补全共用一棵命令树
- DI：基于 [EtherosFramework](https://github.com/EtherosGroup/EtherosFramework) 的容器、全局 Bean 共享与生命周期
- 平台核心：记录运行平台与版本，提供跨模块服务注册表

## 模块

| 模块 | 目标平台 | 字节码 | Maven 坐标 | Release 产物 |
|---|---|---|---|---|
| `common` | 无平台依赖 | Java 8 | `cn.etheros.etheroslib:EtherosLib-Common:<版本>` | `EtherosLib-Common-<版本>.jar` |
| `bukkit` | Bukkit / Paper | Java 8 | `cn.etheros.etheroslib:EtherosLib-Bukkit:<版本>` | `EtherosLib-Bukkit-<版本>.jar`（shadow 插件包） |
| `velocity` | Velocity | Java 17 | `cn.etheros.etheroslib:EtherosLib-Velocity:<版本>` | `EtherosLib-Velocity-<版本>.jar`（shadow 插件包） |

Maven 发布的是普通 jar（`common`、`bukkit`、`velocity` 各一份），供下游 `compileOnly` 编译；Release 附件是 shadow 插件包（`-plugin.jar`，内含 `common` 与 EtherosFramework 2.4.1），供服务器安装。`EtherosLib-Bukkit` / `EtherosLib-Velocity` 的 POM 以 compile scope 依赖 `EtherosLib-Common`，后者以 compile scope 依赖 `EtherosFramework`。

## 环境与构建

- 构建 JDK：21（Gradle 8.14.5，wrapper 指向国内镜像）
- 仓库：阿里云 / 华为云镜像优先，PaperMC 与 Maven Central 兜底
- 命令：

```bash
./gradlew build
```

产物在 `bukkit/build/libs`、`velocity/build/libs`、`common/build/libs`。

## 下游插件接入

EtherosLib 以宿主插件方式运行：

1. 把 `EtherosLib-Bukkit` 或 `EtherosLib-Velocity` 放进对应端；
2. 下游插件编译期依赖 EtherosLib API（`compileOnly`，运行期由宿主插件提供）：

```kotlin
dependencies {
    // Bukkit 端
    compileOnly("cn.etheros.etheroslib:EtherosLib-Bukkit:1.2.1")
    // Velocity 端
    // compileOnly("cn.etheros.etheroslib:EtherosLib-Velocity:1.2.1")
}
```

依赖会传递 `EtherosLib-Common` 与 `EtherosFramework`，注解、DI 与命令框架 API 都在其中，不需要单独声明框架依赖；运行期不要打包这些类，由 EtherosLib 宿主插件提供。

3. Bukkit 侧 `plugin.yml` 声明依赖，Velocity 侧声明插件依赖：

```yaml
depend: [EtherosLib]
```

```java
@Plugin(id = "yourplugin", name = "YourPlugin", version = "1.0.0",
        dependencies = {@Dependency(id = "etheroslib")})
```

4. 入口启动 `ApplicationContext`，即可注入宿主注册的全局 Bean（`@Autowired` 会先查本插件容器，再回落 `SharedContext`）：

```java
public final class YourPlugin extends JavaPlugin {

    private ApplicationContext context;

    @Override
    public void onEnable() {
        context = ApplicationContext.run(YourPlugin.class);
    }

    @Override
    public void onDisable() {
        if (context != null) {
            context.shutdown();
        }
    }
}
```

## 平台核心

`cn.etheros.etheroslib.common.EtherosLibCore`（单例）：

| 方法 | 说明 |
|---|---|
| `initialize(Platform, String)` | 平台模块启动时绑定平台与版本 |
| `getPlatform()` / `getVersion()` | 当前平台与版本 |
| `registerService(Class, T)` / `getService(Class)` / `requireService(Class)` | 跨模块服务注册表 |
| `clearServices()` | 平台关闭时清理 |

## 事件总线

实现位于 `common.event.EtherosEventBus`，平台侧分别由 `BukkitEventBus`、`VelocityEventBus` 标注 `@GlobalService` 接入 DI，生命周期结束时调用 `shutdown()` 释放监听器。

```java
@Autowired
private EventBus eventBus;

// 订阅
EventBus.Subscription subscription = eventBus.subscribe("player.join", event -> {
    String playerName = (String) event.getData().get("playerName");
    // ...
}, Priority.HIGH);

// 投递
eventBus.post("player.join", new BusEvent()
        .put("playerName", player.getName())
        .put("playerId", player.getUniqueId()));

// 取消订阅
subscription.unsubscribe();
```

### 载荷约束

EtherosFramework 2.1.0 起，`BusEvent` 在设置载荷时逐项校验：

| 规则 | 处理 |
|---|---|
| 允许的值类型 | `String`、包装类型、`UUID`、JDK 枚举、JDK 集合/数组/`Optional` |
| 拒绝的值类型 | 插件自定义类、平台对象（`Player` 等）、其他非 JDK 类 |
| Map 键 | 必须是非空字符串，嵌套 Map 同样适用 |
| 环引用 | 拒绝 |
| 节点上限 | 单次校验 1024 个节点；不限深度，深度受节点预算约束 |

跨插件投递时不同插件的 classloader 下类型不一致，载荷里只放值语义数据；对象引用用 UUID、名称等标识表达，订阅方自行解析。

## 命令框架

### 声明

```java
@CommandHandler(name = "auth", aliases = {"a"}, permission = "etherosauth.use",
        description = "认证命令")
public final class AuthCommand {

    // 根执行器：/auth <玩家>
    @SubCommand("")
    public void root(CommandSender sender, @Arg("玩家") String player) {
        // ...
    }

    // /auth login <密码>，别名 /auth l
    @SubCommand(value = "login", aliases = {"l"}, permission = "etherosauth.login")
    public void login(Player player, @Arg("密码") String password) {
        // ...
    }

    // /auth admin reload，路径每段可写别名
    @SubCommand("admin|adm reload|rl")
    public void reload(CommandSender sender) {
        // ...
    }

    // 自定义补全
    @SubCommand("admin mode")
    public void mode(CommandSender sender,
                     @Arg(value = "模式", completer = ModeCompleter.class) String mode) {
        // ...
    }

    // 可选参数，非基本类型
    @SubCommand("admin limit")
    public void limit(CommandSender sender, @Arg(value = "上限", optional = true) Integer limit) {
        // ...
    }
}
```

```java
public final class ModeCompleter implements TabProvider {

    @Override
    public Collection<String> suggest(TabContext context) {
        return Arrays.asList("password", "totp");
    }
}
```

### 注册

```java
@Autowired
private BukkitCommandRegistry commandRegistry;

@Override
public void onEnable() {
    commandRegistry.register(this, new AuthCommand());
}

@Override
public void onDisable() {
    commandRegistry.unregister(this);
}
```

`register` 把命令注册到 Bukkit `CommandMap`，不需要在 `plugin.yml` 里重复声明命令；注销时从 `CommandMap` 移除并清理别名。

### 规则

| 事项 | 说明 |
|---|---|
| 命令别名 | `@CommandHandler.aliases` 作用于整个命令，声明后子命令不允许再声明别名（`@SubCommand.aliases` 与路径 `|` 都会报错） |
| 子命令别名 | 命令未声明别名时，路径每段可写 `规范名|别名`；`@SubCommand.aliases` 作为最后一段的补充别名 |
| 路径 | 空格分隔多级子命令，如 `admin reload` |
| 发送者 | 方法第一个参数不带 `@Arg` 时视为发送者：可声明 `CommandSender`、平台类型（如 `Player`）或 `CommandContext` |
| 参数 | 其余参数必须标 `@Arg`，`optional = true` 表示可省略，基本类型不可选 |
| 权限 | 命令级与子命令级权限分开校验，补全时无权限的子命令与候选隐藏 |
| 自动提示 | 权限不足、参数数量不符、解析失败、非玩家执行都有对应提示 |

### 参数类型

内置：`String`、`int`/`Integer`、`long`/`Long`、`float`/`Float`、`double`/`Double`、`boolean`/`Boolean`、Java 枚举。

Bukkit 后端额外注册：`Player`、`OfflinePlayer`、`World`、`Material`。

自定义类型通过 `ArgumentTypes.register(Class, ArgumentType)` 注册，需要时由平台后端在启动阶段完成。

## DI 约定

| 注解 | 查找顺序 | 说明 |
|---|---|---|
| `@GlobalService` | — | 创建后注册到 `SharedContext`，可被其他插件注入 |
| `@Autowired` | 本插件容器 → `SharedContext` | 默认用法，本地优先、宿主兜底 |
| `@GlobalAutowired` | `SharedContext` → 本插件容器 | 同名冲突时优先取宿主那份 |

`required = false` 时找不到返回 `null`；字段必须显式标注注入注解，构造器参数可省略注解。

## 目录结构

```
EtherosLib/
├── settings.gradle.kts          # 父子工程、仓库、模块声明
├── build.gradle.kts             # 公共约定：Java 版本、产物命名、测试、可重复产物
├── gradle.properties            # 工程坐标与依赖版本
├── common/                      # 跨平台核心
│   └── src/main/java/cn/etheros/etheroslib/common/
│       ├── EtherosLibCore.java
│       ├── Platform.java
│       ├── event/               # 事件总线实现
│       └── command/             # 命令框架核心（注解、命令树、参数、补全）
├── bukkit/                      # Bukkit/Paper 平台实现
│   └── src/main/java/cn/etheros/etheroslib/bukkit/
│       ├── EtherosLib.java
│       ├── event/BukkitEventBus.java
│       └── command/             # 命令注册后端、发送者包装、平台参数类型
├── velocity/                    # Velocity 平台实现
│   └── src/main/java/cn/etheros/etheroslib/velocity/
│       ├── EtherosLibVelocity.java
│       └── event/VelocityEventBus.java
└── .github/
    ├── workflows/ci.yml         # push/PR：构建、测试、产物校验
    ├── workflows/release.yml    # tag：构建并发布 GitHub Release
    └── scripts/verify-artifacts.sh
```

## 构建与发布

- `ci.yml`：推 `master` 与 PR 时构建、跑测试、校验产物（jar 完整性、插件元数据、common/framework 合并、字节码版本）；
- `release.yml`：推送 `v*` 标签时构建并创建 GitHub Release，附件为三个插件 jar 与 `SHA256SUMS.txt`；
- `publish.yml`：推送 `v*` 标签或手动触发时构建并发布到 Maven Central，需要仓库 Secrets：`MAVEN_CENTRAL_USERNAME`、`MAVEN_CENTRAL_PASSWORD`、`GPG_SIGNING_KEY`、`GPG_SIGNING_PASSWORD`；
- 发版流程：修改 `gradle.properties` 的 `projectVersion` → 提交 → 打同名标签（如 `v1.2.1`）→ 推送，`release.yml` 与 `publish.yml` 同时执行；标签与工程版本不一致时发布会失败。

## 兼容性

- Bukkit/Paper：以 `paper-api 1.12` 编译，Java 8 字节码；
- Velocity：以 `velocity-api 3.4.0` 编译，Java 17 字节码；
- 命令框架当前提供 Bukkit 后端，Velocity/BungeeCord 后端按平台需要接入；
- 事件总线已覆盖 Bukkit 与 Velocity。

## 许可

Apache License 2.0，见 [LICENSE](LICENSE)。

## 作者

西博卡兹（Etheros Group）
