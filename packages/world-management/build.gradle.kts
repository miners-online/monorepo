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

    compileOnly("net.minestom:minestom:2025.10.05-1.21.8")
    compileOnly(project(":server-bootstrap"))
}

tasks.test {
    useJUnitPlatform()
}