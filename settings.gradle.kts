rootProject.name = "monorepo"

include(":game-materials")
project(":game-materials").projectDir = file("./packages/game-materials")
include(":proxy-core")
project(":proxy-core").projectDir = file("./packages/proxy-core")
include(":server-bootstrap")
project(":server-bootstrap").projectDir = file("./packages/server-bootstrap")
include(":game-lobby")
project(":game-lobby").projectDir = file("./games/lobby")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }
}
