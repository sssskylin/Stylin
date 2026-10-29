plugins {
    kotlin("jvm") version "2.3.0" apply false
}

subprojects {
    group = "com.closet.wardrobe"
    version = "1.0.0"
    apply(from = rootProject.file("../../gradle/kotlin-conventions.gradle.kts"))
}