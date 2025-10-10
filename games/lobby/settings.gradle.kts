rootProject.name = "lobby"

include(":default-block-handlers")
project(":default-block-handlers").projectDir = file("../../packages/default-block-handlers")
include(":server-bootstrap")
project(":server-bootstrap").projectDir = file("../../packages/server-bootstrap")
include(":world-management")
project(":world-management").projectDir = file("../../packages/world-management")