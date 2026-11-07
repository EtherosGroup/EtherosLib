plugins {
    java
    id("com.gradleup.shadow")
}

description = "EtherosLib Bukkit / Paper 平台实现"

val paperApiVersion: String by project

dependencies {
    // 平台 API 仅编译期使用，运行期由服务端提供
    compileOnly("com.destroystokyo.paper:paper-api:$paperApiVersion")
    // 单测需要 Bukkit 类型
    testImplementation("com.destroystokyo.paper:paper-api:$paperApiVersion")
    // 跨平台核心代码随插件一起打包
    implementation(project(":common"))
}

// 插件产物统一由 shadowJar 输出（内含 common），普通 jar 不再单独产出
tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<Jar>("shadowJar") {
    archiveClassifier.set("")
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
