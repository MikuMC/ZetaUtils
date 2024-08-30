plugins {
    kotlin("jvm") version "1.9.21"
    id("java-library")
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
}

group = "i.mrhua269"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://maven.moliatopia.icu/repository/maven-public/")
    }

    publishing {
        repositories {
            maven {
                name = "moliaMavenRepo"
                url = uri("https://maven.moliatopia.icu/repository/maven-snapshots/")

                credentials.username = System.getenv("MAVEN_REPO_USER")
                credentials.password = System.getenv("MAVEN_REPO_PASSWORD")
            }
        }
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
        processResources {
            filesMatching("**/plugin.yml") {
                expand(rootProject.project.properties)
            }

            // Always re-run this task
            outputs.upToDateWhen { false }
        }
    }
}
