plugins {
    id("java")
}

group = "uk.minersonline.games"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)

    implementation(libs.amqp.client)
}

tasks.test {
    useJUnitPlatform()
}