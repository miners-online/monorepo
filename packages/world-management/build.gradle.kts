plugins {
    id("java")
}

group = "uk.minersonline.games"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("net.sandrohc:schematic4j:1.1.0")
    compileOnly("net.minestom:minestom:2026.01.08-1.21.11")
    compileOnly(project(":server-bootstrap"))
}

tasks.test {
    useJUnitPlatform()
}