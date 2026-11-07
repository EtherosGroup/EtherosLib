plugins {
    java
    id("com.gradleup.shadow")
}

description = "EtherosLib Velocity 代理端实现"

val velocityApiVersion: String by project

// 生成版本常量：@Plugin(version = ...) 必须是编译期常量，无法直接读 project.version
val generatedBuildInfoDir = layout.buildDirectory.dir("generated/buildinfo")
val generateBuildInfo = tasks.register("generateBuildInfo") {
    val version = project.version.toString()
    val outputDir = generatedBuildInfoDir
    inputs.property("version", version)
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile.resolve("cn/etheros/etheroslib/velocity")
        dir.mkdirs()
        dir.resolve("BuildInfo.java").writeText(
            """
            package cn.etheros.etheroslib.velocity;

            /** 由 Gradle 在构建时生成，请勿手动修改。 */
            public final class BuildInfo {

                public static final String VERSION = "$version";

                private BuildInfo() {
                }
            }
            """.trimIndent() + "\n"
        )
    }
}

sourceSets.named("main") {
    java.srcDir(generatedBuildInfoDir)
}

tasks.named("compileJava") {
    dependsOn(generateBuildInfo)
}

// sourcesJar 也会打包生成的源码，需显式声明依赖
tasks.named("sourcesJar") {
    dependsOn(generateBuildInfo)
}

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
