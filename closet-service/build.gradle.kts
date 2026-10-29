plugins {
    kotlin("jvm") version "2.3.0" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false
    id("io.ktor.plugin") version "3.3.1" apply false
    id("org.liquibase.gradle") version "3.0.2" apply false
}

allprojects {
    group = "com.closet"
    version = "1.0.0"
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(from = rootProject.file("../gradle/kotlin-conventions.gradle.kts"))
}