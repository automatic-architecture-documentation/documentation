plugins {
    kotlin("jvm") version "1.9.22"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    implementation("net.sourceforge.plantuml:plantuml:1.2024.3")
    implementation("org.springframework:spring-core:6.1.5")
}
