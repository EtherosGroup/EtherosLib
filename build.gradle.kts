plugins {
    // 父工程只声明插件版本，具体模块按需 apply（子模块无需重复写版本）
    id("com.gradleup.shadow") version "8.3.6" apply false
}

// 各模块目标 JDK：common / bukkit 兼容 Java 8，velocity 需要 Java 17
val javaReleaseByProject = mapOf("common" to 8, "bukkit" to 8, "velocity" to 17)

// 构建产物命名
val archiveNameByProject = mapOf(
    "common" to "EtherosLib-Common",
    "bukkit" to "EtherosLib-Bukkit",
    "velocity" to "EtherosLib-Velocity"
)

// 父工程统一坐标，子模块自动继承
allprojects {
    group = property("projectGroup")!!
    version = property("projectVersion")!!
}

subprojects {
    apply(plugin = "java")

    val javaRelease = javaReleaseByProject[project.name] ?: 8
    val etherosFrameworkVersion = property("etherosFrameworkVersion").toString()

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.toVersion(javaRelease)
        targetCompatibility = JavaVersion.toVersion(javaRelease)
        withSourcesJar()
    }

    extensions.configure<BasePluginExtension> {
        archivesName.set(archiveNameByProject[project.name] ?: "EtherosLib-${project.name}")
    }

    dependencies {
        add("implementation", "cn.etheros:EtherosFramework:$etherosFrameworkVersion")

        add("testImplementation", platform("org.junit:junit-bom:5.11.4"))
        add("testImplementation", "org.junit.jupiter:junit-jupiter")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        // 使用 --release 锁定 API 面，避免误用高版本 JDK 的类库
        options.release.set(javaRelease)
    }

    tasks.withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    // 产物可重复，便于校验与分发
    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}
