val ktorVersion = libs.versions.ktor.get()

dependencies {
    implementation(project(":application"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:${ktorVersion}")
    implementation("io.ktor:ktor-server-auth-jwt:${ktorVersion}")
    implementation("io.ktor:ktor-server-auth:${ktorVersion}")

}
