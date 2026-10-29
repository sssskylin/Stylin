val ktorVersion = libs.versions.ktor.get()

dependencies {
    implementation(project(":domain"))

    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("software.amazon.awssdk:s3:2.28.11")
    implementation("org.postgresql:postgresql:42.7.13")
}
