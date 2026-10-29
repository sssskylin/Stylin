val ktorVersion = libs.versions.ktor.get()

dependencies {

    implementation(project(":domain"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-config-yaml:$ktorVersion")
    implementation("at.favre.lib:bcrypt:0.10.2")

    implementation("org.jetbrains.exposed:exposed-core:1.0.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0")
    implementation("org.jetbrains.exposed:exposed-java-time:1.0.0")

    implementation("org.postgresql:postgresql:42.7.5")
    implementation("com.zaxxer:HikariCP:6.2.1")

    implementation("org.liquibase:liquibase-core:4.30.0")

    implementation("ch.qos.logback:logback-classic:1.5.16")
}
