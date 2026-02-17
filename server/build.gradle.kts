plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}

kotlin {
    jvmToolchain(23)
}
