val ktorVersion = libs.versions.ktor.get()

dependencies {
    implementation(project(":domain"))
    implementation(project(":infrastructure"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    testImplementation(kotlin("test-junit5"))
}