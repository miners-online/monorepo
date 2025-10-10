plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
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
    implementation(project(":default-block-handlers"))
    implementation(project(":server-bootstrap"))
    implementation(project(":world-management"))
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

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "uk.minersonline.games.server_bootstrap.Main" // Change this to your main class
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
    }
}