plugins {
    kotlin("jvm") version "2.1.0"
    application
}

group = "org.example.trivial"
version = "1.0.0"

repositories {
    mavenCentral()
}

application {
    mainClass.set("server.MainServerKt")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

kotlin {
    jvmToolchain(23)
}
