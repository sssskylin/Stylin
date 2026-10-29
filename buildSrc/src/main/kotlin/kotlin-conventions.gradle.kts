apply(plugin = "org.jetbrains.kotlin.jvm")

extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
    jvmToolchain(25)
}

repositories {
    mavenCentral()
}

dependencies {
    "testImplementation"(kotlin("test"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}