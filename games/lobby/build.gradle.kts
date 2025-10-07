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
    implementation("net.minestom:minestom:2025.10.05-1.21.8")
    implementation(project(":server-bootstrap"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run the Lobby server"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("uk.minersonline.games.server_bootstrap.Main")
}