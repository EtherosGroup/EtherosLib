plugins {
    `java-library`
    id("com.gradleup.shadow")
    id("com.vanniktech.maven.publish")
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
