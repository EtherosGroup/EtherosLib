import org.gradle.api.initialization.resolve.RepositoriesMode

// EtherosLib 多模块构建入口
// 所有仓库在父工程统一声明，子模块不再各自配置 repositories

pluginManagement {
    repositories {
        // 插件仓库：国内镜像优先，官方仓库兜底
        maven {
            name = "AliyunGradlePlugin"
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        }
        maven {
            name = "AliyunPublic"
            url = uri("https://maven.aliyun.com/repository/public")
        }
        maven {
            name = "AliyunGoogle"
            url = uri("https://maven.aliyun.com/repository/google")
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    // 禁止子模块自行声明仓库，避免同一定义出现多个来源
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 国内镜像：Maven Central / Google 的代理
        maven {
            name = "AliyunPublic"
            url = uri("https://maven.aliyun.com/repository/public")
        }
        maven {
            name = "AliyunCentral"
            url = uri("https://maven.aliyun.com/repository/central")
        }
        maven {
            name = "AliyunGoogle"
            url = uri("https://maven.aliyun.com/repository/google")
        }
        maven {
            name = "HuaweiCloud"
            url = uri("https://repo.huaweicloud.com/repository/maven/")
        }
        // Paper / Velocity API 与 Mojang 依赖（authlib 等）只在官方仓库发布，无可靠国内镜像
        maven {
            name = "PaperMC"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        mavenCentral()
    }
}

rootProject.name = "EtherosLib"

include("common", "bukkit", "velocity")
