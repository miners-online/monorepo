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
    implementation("net.minestom:minestom:2026.01.08-1.21.11")
    implementation("dev.hollowcube:schem:2.0.1")
    implementation(project(":game-materials"))
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