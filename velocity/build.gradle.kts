plugins {
    java
    id("com.gradleup.shadow")
}

description = "EtherosLib Velocity 代理端实现"

val velocityApiVersion: String by project

dependencies {
    // 平台 API 仅编译期使用，运行期由代理端提供
    compileOnly("com.velocitypowered:velocity-api:$velocityApiVersion")
    // 注解处理器生成 velocity-plugin.json
    annotationProcessor("com.velocitypowered:velocity-api:$velocityApiVersion")
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
