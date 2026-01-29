rootProject.name = "lobby"

include(":game-materials")
project(":game-materials").projectDir = file("../../packages/game-materials")
include(":server-bootstrap")
project(":server-bootstrap").projectDir = file("../../packages/server-bootstrap")