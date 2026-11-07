plugins {
    `java-library`
    id("com.gradleup.shadow")
    id("com.vanniktech.maven.publish")
}

description = "EtherosLib Bukkit / Paper 平台实现"

val paperApiVersion: String by project

dependencies {
    // 平台 API 仅编译期使用，运行期由服务端提供
    compileOnly("com.destroystokyo.paper:paper-api:$paperApiVersion")
    // 单测需要 Bukkit 类型
    testImplementation("com.destroystokyo.paper:paper-api:$paperApiVersion")
    // 跨平台核心：编译期传递给下游，运行期随 shadowJar 打包
    api(project(":common"))
}

// 普通 jar 用于 Maven 发布，shadowJar 作为插件产物
tasks.named<Jar>("shadowJar") {
    archiveClassifier.set("plugin")
}

// 让 build / assemble 默认产出插件包
tasks.named("assemble") {
    dependsOn(tasks.named("shadowJar"))
}

// 将工程版本写入 plugin.yml
tasks.named<Copy>("processResources") {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
