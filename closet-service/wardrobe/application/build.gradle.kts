val ktorVersion = libs.versions.ktor.get()

dependencies {
    api(project(":domain"))
    api(project(":infrastructure"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    testImplementation(kotlin("test-junit5"))
}