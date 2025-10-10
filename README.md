# monorepo

This is our monorepo, it replaces the independent repos we used to have. All our games and related services can be
found here.

- Games:
  - [Cave Run](./games/cave-run/) (placeholder)
  - [Golf](./games/golf/) (placeholder)
  - [Lobby](./games/lobby/) - The lobby server, where players first join
  - [Mind Bending](./games/mind-bending/) (placeholder)
- Packages:
  - [Default Block Handlers](./packages/default-block-handlers/) - Default block handlers for Minestom
  - [Game Management](./packages/game-management/) (placeholder)
  - [Server Bootstrap](./packages/server-bootstrap/) - The server bootstrapper, used to start all our games
  - [World Management](./packages/world-management/) - Tools for managing worlds and schematics
- Services:
  - [Permissions Manager](./services/permissions-manager/) (placeholder)
  - [Player Profiles](./services/player-profiles/) (placeholder)
  - [Statistics](./services/statistics/) (placeholder)

## Why a monorepo?

We have lots of services that need to interact with each-other, when we used separate repositories we needed on-prem
CI/CD server and a Maven repository to allow these services to share packages.

With a monorepo we can drop that on-prem infrastructure completely, as packages can be shared directly.