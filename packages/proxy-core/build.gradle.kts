plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.0"
}

group = "uk.minersonline.games"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)

    implementation(libs.lettuce)
    implementation(project(":message-exchange"))
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
    }
}
