plugins {
    // 父工程只声明插件版本，具体模块按需 apply（子模块无需重复写版本）
    id("com.gradleup.shadow") version "8.3.6" apply false
    id("com.vanniktech.maven.publish") version "0.36.0" apply false
}

// 各模块目标 JDK：common / bukkit 兼容 Java 8，velocity 需要 Java 17
val javaReleaseByProject = mapOf("common" to 8, "bukkit" to 8, "velocity" to 17)

// 构建产物命名，同时作为 Maven artifactId
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

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.toVersion(javaRelease)
        targetCompatibility = JavaVersion.toVersion(javaRelease)
        withSourcesJar()
    }

    extensions.configure<BasePluginExtension> {
        archivesName.set(archiveNameByProject[project.name] ?: "EtherosLib-${project.name}")
    }

    dependencies {
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

    // Maven Central 发布约定：模块 apply 插件后自动套用坐标与 POM 信息
    plugins.withId("com.vanniktech.maven.publish") {
        apply(plugin = "signing")
        // 本地用 gpg 命令签名；CI 由 signingInMemoryKey 系列属性走内存签名
        if (providers.gradleProperty("signingInMemoryKey").orNull == null) {
            extensions.configure<org.gradle.plugins.signing.SigningExtension> {
                useGpgCmd()
            }
        }
        extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            publishToMavenCentral(true)
            signAllPublications()
            coordinates(
                "cn.etheros.etheroslib",
                archiveNameByProject[project.name] ?: "EtherosLib-${project.name}",
                project.version.toString()
            )
            pom {
                name.set(archiveNameByProject[project.name] ?: "EtherosLib-${project.name}")
                description.set(project.description ?: project.name)
                url.set("https://github.com/EtherosGroup/EtherosLib")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("Etheros")
                        name.set("Etheros Group")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/EtherosGroup/EtherosLib.git")
                    developerConnection.set("scm:git:https://github.com/EtherosGroup/EtherosLib.git")
                    url.set("https://github.com/EtherosGroup/EtherosLib")
                }
            }
        }
    }
}
