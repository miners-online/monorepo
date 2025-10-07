rootProject.name = "lobby"

include(":server-bootstrap")
project(":server-bootstrap").projectDir = file("../../packages/server-bootstrap")
include(":world-management")
project(":world-management").projectDir = file("../../packages/world-management")