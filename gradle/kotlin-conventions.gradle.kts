apply(plugin = "org.jetbrains.kotlin.jvm")

extensions.configure<JavaPluginExtension> {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25)) // если не соберётся на 25, поставьте 21
    }
}

repositories {
    mavenCentral()
}

dependencies {
    "testImplementation"("org.jetbrains.kotlin:kotlin-test")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}