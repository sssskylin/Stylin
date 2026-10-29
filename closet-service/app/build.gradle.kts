plugins {
    id("io.ktor.plugin")
    kotlin("plugin.serialization")
    application
}

dependencies {
    implementation("com.closet.users:application:1.0.0")
    implementation("com.closet.users:infrastructure:1.0.0")
    implementation("com.closet.users:presentation:1.0.0")
    implementation("com.closet.wardrobe:application:1.0.0")
    implementation("com.closet.wardrobe:infrastructure:1.0.0")
    implementation("com.closet.wardrobe:presentation:1.0.0")

    implementation("io.ktor:ktor-server-netty:3.3.1")
    implementation("io.ktor:ktor-server-core:3.3.1")
    implementation("io.ktor:ktor-server-content-negotiation:3.3.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.1")
    implementation("io.ktor:ktor-server-auth:3.3.1")
    implementation("io.ktor:ktor-server-auth-jwt:3.3.1")
    implementation("io.ktor:ktor-server-status-pages:3.3.1")
    implementation("io.ktor:ktor-server-call-logging:3.3.1")
    implementation("io.ktor:ktor-server-call-id:3.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.zaxxer:HikariCP:6.2.1")
    runtimeOnly("org.postgresql:postgresql:42.7.4")
    implementation("org.liquibase:liquibase-core:4.30.0")
    implementation("ch.qos.logback:logback-classic:1.5.12")
}

application {
    mainClass.set("app.MainKt")
}