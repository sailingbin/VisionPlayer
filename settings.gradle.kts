pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        // 本地Maven仓库，支持从本地引入player-core库
        // mavenLocal()
        // 阿里云镜像源，加速依赖下载
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenCentral()
        // 添加 GitHub Packages Maven 仓库
        maven {
            name = "GitHubPackages"
            url =
                uri("https://maven.pkg.github.com/sailingbin/visionplayer")
            credentials {
                // 从 Gradle 属性或环境变量中获取 GitHub 认证信息
                // **重要：为了从 GitHub Packages 下载，你需要提供认证信息。**
                // 方式一 (推荐): 在你的 ~/.gradle/gradle.properties (全局) 或 project_root/gradle.properties (项目级) 中设置:
                //     gpr.user=<你的GitHub用户名或PAT名称>
                //     gpr.token=<你的GitHub Personal Access Token>
                // 方式二: 设置环境变量 GITHUB_ACTOR (GitHub用户名) 和 GITHUB_TOKEN (PAT)。
                username =
                    providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password =
                    providers.gradleProperty("gpr.token").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "VisionPlay"
include(":app")
 