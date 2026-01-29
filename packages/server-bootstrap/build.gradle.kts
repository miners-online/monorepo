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
    implementation("net.minestom:minestom:2026.01.08-1.21.11")
    implementation("org.tinylog:slf4j-tinylog:2.8.0-M1")
    implementation("org.tinylog:tinylog-api:2.8.0-M1")
    implementation("org.tinylog:tinylog-impl:2.8.0-M1")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("io.github.classgraph:classgraph:4.8.181")
}

tasks.test {
    useJUnitPlatform()
}