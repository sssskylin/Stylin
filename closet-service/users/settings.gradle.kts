pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.2.20"
        kotlin("plugin.serialization") version "2.2.20"
        id("io.ktor.plugin") version "3.3.1"
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") { from(files("../../gradle/libs.versions.toml")) }
    }
}

rootProject.name = "closet-service.users"

include("domain")
include("application")
include("infrastructure")
include("presentation")