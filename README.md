# EtherosLib

Etheros 系列插件的跨平台核心库插件，支持 Bukkit/Paper 与 Velocity。

- 事件总线：跨插件共享的 `BusEvent` 投递，平台侧薄适配
- 命令框架：注解声明命令与子命令，注册期构建发送者、参数与执行器，统一错误处理与 Tab 补全
- DI：基于 [EtherosFramework](https://github.com/EtherosGroup/EtherosFramework) 的容器、全局 Bean 共享与生命周期
- 平台核心：记录运行平台与版本，提供跨模块服务注册表

## 模块

| 模块 | 目标平台 | 字节码 | Maven 坐标 | Release 产物 |
|---|---|---|---|---|
| `common` | 无平台依赖 | Java 8 | `cn.etheros.etheroslib:EtherosLib-Common:<版本>` | `EtherosLib-Common-<版本>.jar` |
| `bukkit` | Bukkit / Paper | Java 8 | `cn.etheros.etheroslib:EtherosLib-Bukkit:<版本>` | `EtherosLib-Bukkit-<版本>.jar`（shadow 插件包） |
| `velocity` | Velocity | Java 17 | `cn.etheros.etheroslib:EtherosLib-Velocity:<版本>` | `EtherosLib-Velocity-<版本>.jar`（shadow 插件包） |

Maven 发布的是普通 jar（`common`、`bukkit`、`velocity` 各一份），供下游 `compileOnly` 编译；Release 附件是 shadow 插件包（`-plugin.jar`，内含 `common` 与 EtherosFramework 2.5.0），供服务器安装。`EtherosLib-Bukkit` / `EtherosLib-Velocity` 的 POM 以 compile scope 依赖 `EtherosLib-Common`，后者以 compile scope 依赖 `EtherosFramework`。

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
    compileOnly("cn.etheros.etheroslib:EtherosLib-Bukkit:2.0.0")
    // Velocity 端
    // compileOnly("cn.etheros.etheroslib:EtherosLib-Velocity:2.0.0")
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

## 日志注入

`@InjectLogger` 把平台日志器注入到 `Logger` 字段，后端按平台品牌自动选择：

| 平台 | 后端 | 输出 |
|---|---|---|
| Bukkit / Paper | JDK JUL（`JdkLogBackend`） | 服务端控制台 |
| Velocity | SLF4J（`Slf4jLogBackend`） | 代理端日志 |
| 未安装后端（测试 / 独立使用） | JDK JUL | 控制台 |

### 类级用法（推荐）

给类打注解，类中所有已声明的 `Logger` 字段自动注入；**框架不生成字段**，字段仍需自行声明：

```java
@InjectLogger
@Service
public class AuthService {
    private Logger logger;     // 名字 = <logger.name>.AuthService
    private Logger dbLogger;   // 多个字段：<logger.name>.AuthService.dbLogger
}
```

### 字段级用法

只注入标注的字段，可用 `value` 指定后缀：

```java
@Service
public class AuthService {
    @InjectLogger("database")
    private Logger dbLogger;   // 名字 = <logger.name>.database
}
```

### 占位符

`{}` 为 SLF4J 风格占位符，Bukkit 与 Velocity 两端语义一致：

```java
logger.warn("玩家 {} 在 {} 登录失败", playerName, time);
logger.error("加载失败", exception);            // 末尾 Throwable 按异常输出（带堆栈）
logger.info("{} 和 {}", "only");                // 参数不足时保留原样：only 和 {}
logger.warn("a {} b", 1, 2);                    // 多余参数追加到末尾：a 1 b 2
```

`logger.debug(...)` 是否输出由平台日志级别决定（JUL 对应 `FINE`）。

日志名前缀取插件 jar 根目录 `application.properties` 的 `logger.name`，缺省为 `Etheros`：

```properties
logger.name=EtherosAuth
```

- 注入在 `@PostConstruct` 之前完成；
- `Logger` 字段类型：`cn.etheros.etheroslib.common.log.Logger`，`static` 字段不支持；
- 需要宿主 EtherosLib 已加载（平台入口负责安装后端、注册注入器）：Bukkit 侧 `depend: [EtherosLib]`，Velocity 侧 `@Dependency(id = "etheroslib")`。

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

`@CommandHandler` 声明命令；`@SubCommand` 标注的方法是**注册期工厂**：无参、返回 `SubCommandSpec`。
发送者、参数、执行器与错误文案都在工厂里声明，注册时调用一次并构建命令树。

```java
@CommandHandler(name = "auth", aliases = {"a"}, permission = "etherosauth.use",
        description = "认证命令")
public final class AuthCommand {

    // /auth info
    @SubCommand("info")
    public SubCommandSpec info() {
        return SubCommandSpec.builder()
                .sender(Player.class)                                    // 只允许玩家执行
                .parameter(CommandParameter.required("password", String.class))
                .onError(CommandError.PLAYER_ONLY, "请以玩家身份执行")
                .execute(context -> {
                    Player player = context.sender(Player.class);
                    String password = context.argument("password");
                    // ...
                })
                .build();
    }

    // /auth admin mode <mode>，路径每段可写别名
    @SubCommand("admin|adm mode|m")
    public SubCommandSpec mode() {
        return SubCommandSpec.builder()
                .parameter(CommandParameter.required("mode", String.class)
                        .completer(ModeCompleter.class))
                .execute(context -> {
                    String mode = context.argument("mode");
                    // ...
                })
                .build();
    }

    // /auth admin limit [limit]
    @SubCommand("admin limit")
    public SubCommandSpec limit() {
        return SubCommandSpec.builder()
                .parameter(CommandParameter.optional("limit", Integer.class))
                .execute(context -> {
                    Integer limit = context.argument("limit");   // 未提供时为 null
                    // ...
                })
                .build();
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

### 发送者与参数

| 事项 | 说明 |
|---|---|
| 发送者声明 | `.sender(Class)`，默认 `CommandSender`；声明平台类型（如 `Player`）时框架校验并注入 |
| 发送者读取 | `context.sender(Player.class)`；`context.sender()` 取 `CommandSender` 抽象，`context.rawSender()` 取平台原始对象 |
| 参数声明 | `CommandParameter.required/optional(名字, 类型)`，按声明顺序解析；自定义 `ArgumentType` 直接传入 |
| 参数读取 | `context.argument("name")` 按赋值目标推断类型；`context.argument("name", Class)` 带类型检查 |
| 可选参数 | 未提供时读取结果为 `null` |
| 参数名 | 同时是查找键与错误/i18n 素材，建议使用稳定英文名（如 `password`），显示文案由 i18n 层翻译 |

### 注册

```java
@Autowired
private BukkitCommandRegistry commandRegistry;

@Override
public void onEnable() {
    commandRegistry.setDefaultErrorHandler(this, i18n::message);   // 可选，插件级默认错误文案
    commandRegistry.register(this, new AuthCommand());
}

@Override
public void onDisable() {
    commandRegistry.unregister(this);
}
```

`register` 把命令注册到 Bukkit `CommandMap`，不需要在 `plugin.yml` 里重复声明命令；注销时从 `CommandMap` 移除并清理别名。
工厂方法在注册阶段调用，依赖注入须在注册前完成。

### 错误处理

框架把命令失败归为 7 种错误类型：

| `CommandError` | 触发 | 上下文可用字段 | 内置默认文案 |
|---|---|---|---|
| `NO_PERMISSION` | 命令级或子命令级权限不足 | `permission`、`path` | `无权限：<perm>` |
| `PLAYER_ONLY` | 声明游戏玩家类型，执行者不是玩家 | `requiredSenderType` | `该命令只能由玩家执行` |
| `SENDER_MISMATCH` | 其他发送者类型不匹配 | `requiredSenderType` | `命令执行者类型不匹配：<类型>` |
| `NO_EXECUTOR` | 节点没有执行器，只有子命令 | `usage`、`path` | 用法文案 |
| `INVALID_ARGUMENT_COUNT` | 必选参数缺失或参数过多 | `usage`、`minArguments`、`maxArguments` | 用法文案 |
| `INVALID_ARGUMENT` | 参数解析失败 | `argumentName`、`argumentInput`、`detail` | `错误：参数无效：<名>：<原因>` |
| `EXECUTION_ERROR` | 执行器抛异常 | `exception` | `命令执行出错：<消息>` |

错误文案按以下顺序查找，任一返回 `null` 继续回退，返回空串表示已处理、不再发送：

1. 子命令 `.onError(错误类型, 文案 / 处理器)`
2. 子命令 `.onError(处理器)` 兜底
3. 命令类实现 `CommandErrorHandler`
4. 注册表 `setDefaultErrorHandler(plugin, handler)`
5. 内置默认文案

每个错误都会构造 `CommandErrorContext`，可用 `command()`、`label()`、`path()`、`error()` 拼 i18n key，`defaultMessage()` 取内置文案。

执行器的异常语义：抛 `CommandArgumentException` 视为参数无效（`INVALID_ARGUMENT`）；其他异常按 `EXECUTION_ERROR` 处理——有自定义处理器则发送文案，否则按原样重新抛出。

### i18n 接入

参数名建议用稳定英文键；翻译层负责把参数名、错误文案映射到当前语言：

```java
@CommandHandler(name = "auth")
public final class AuthCommand implements CommandErrorHandler {

    @Autowired
    private Messages messages;   // 插件自己的 i18n 门面

    @SubCommand("info")
    public SubCommandSpec info() {
        return SubCommandSpec.builder()
                .parameter(CommandParameter.required("password", String.class))
                .execute(context -> {
                    String password = context.argument("password");
                })
                .build();
    }

    @Override
    public String message(CommandErrorContext context) {
        if (context.error() != CommandError.INVALID_ARGUMENT) {
            return null;   // 其他错误走注册表默认或内置默认
        }
        String name = messages.get("arg." + context.argumentName());   // arg.password → 密码 / password
        return messages.get("command.invalid-argument", name, context.detail());
    }
}
```

```yaml
# zh.yml
arg.password: "密码"
command.invalid-argument: "参数无效：{0}：{1}"

# en.yml
arg.password: "password"
command.invalid-argument: "Invalid argument: {0}: {1}"
```

参数名只做查找键与 i18n key 后缀，显示文案由翻译层负责。内置用法文案由参数名拼接；需要用法也完全本地化时，可在 `CommandErrorHandler` 里按 `command()`、`path()` 自行拼装，或用 `defaultMessage()` 拼接兜底文案。

### 规则

| 事项 | 说明 |
|---|---|
| 命令别名 | `@CommandHandler.aliases` 作用于整个命令，声明后子命令不允许再声明别名（`@SubCommand.aliases` 与路径 `|` 都会报错） |
| 子命令别名 | 命令未声明别名时，路径每段可写 `规范名|别名`；`@SubCommand.aliases` 作为最后一段的补充别名 |
| 路径 | 空格分隔多级子命令，如 `admin reload` |
| 发送者 | `.sender(Class)` 声明；不声明时任意发送者均可执行 |
| 参数 | 按 `SubCommandSpec` 中 `CommandParameter` 的声明顺序解析，数量不符报 `INVALID_ARGUMENT_COUNT` |
| 权限 | 命令级与子命令级权限分开校验，补全时无权限的子命令与候选隐藏 |
| 错误 | 统一走 `CommandError` 与 `CommandErrorHandler`，见「错误处理」 |
| 补全 | 子命令名 + 参数类型的 `suggest`，可用 `.completer(...)` 覆盖 |

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
| `@InjectLogger` | — | 注入平台日志器（类级 / 字段级），见「日志注入」 |

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
│       ├── log/                 # @InjectLogger、Logger 门面、{} 占位符、后端 SPI、JUL 后端、注入器
│       └── command/             # 命令框架核心（声明、命令树、错误上下文、补全）
├── bukkit/                      # Bukkit/Paper 平台实现
│   └── src/main/java/cn/etheros/etheroslib/bukkit/
│       ├── EtherosLib.java
│       ├── event/BukkitEventBus.java
│       └── command/             # 命令注册后端、发送者包装、平台参数类型
├── velocity/                    # Velocity 平台实现
│   └── src/main/java/cn/etheros/etheroslib/velocity/
│       ├── EtherosLibVelocity.java
│       ├── event/VelocityEventBus.java
│       └── log/Slf4jLogBackend.java
└── .github/
    ├── workflows/ci.yml         # push/PR：构建、测试、产物校验
    ├── workflows/release.yml    # tag：构建并发布 GitHub Release
    └── scripts/verify-artifacts.sh
```

## 构建与发布

- `ci.yml`：推 `master` 与 PR 时构建、跑测试、校验产物（jar 完整性、插件元数据、common/framework 合并、字节码版本）；
- `release.yml`：推送 `v*` 标签时构建并创建 GitHub Release，附件为三个插件 jar 与 `SHA256SUMS.txt`；
- `publish.yml`：推送 `v*` 标签或手动触发时构建并发布到 Maven Central，需要仓库 Secrets：`MAVEN_CENTRAL_USERNAME`、`MAVEN_CENTRAL_PASSWORD`、`GPG_SIGNING_KEY`、`GPG_SIGNING_PASSWORD`；
- 发版流程：修改 `gradle.properties` 的 `projectVersion` → 提交 → 打同名标签（如 `v2.0.0`）→ 推送，`release.yml` 与 `publish.yml` 同时执行；标签与工程版本不一致时发布会失败。

## 兼容性

- Bukkit/Paper：以 `paper-api 1.12` 编译，Java 8 字节码；
- Velocity：以 `velocity-api 3.4.0` 编译，Java 17 字节码；
- 命令框架当前提供 Bukkit 后端，Velocity/BungeeCord 后端按平台需要接入；
- 命令框架 2.0 起改为注册期 `SubCommandSpec` 声明，旧式反射声明（方法形参 + `@Arg`）已移除；
- 事件总线已覆盖 Bukkit 与 Velocity。

## 许可

Apache License 2.0，见 [LICENSE](LICENSE)。

## 作者

西博卡兹（Etheros Group）
