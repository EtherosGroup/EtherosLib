plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

description = "EtherosLib 跨平台核心库"

val etherosFrameworkVersion: String by project

// 所有平台模块共享，框架类型随 api 依赖传递给下游
dependencies {
    api("cn.etheros:EtherosFramework:$etherosFrameworkVersion")
}
