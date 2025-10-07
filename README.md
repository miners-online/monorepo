# monorepo

This is our monorepo, it replaces the independent repos we used to have. All our games and related services can be
found here.

- Games:
  - [Cave Run](./games/cave-run/)
  - [Golf](./games/golf/)
  - [Lobby](./games/lobby/)
  - [Mind Bending](./games/mind-bending/)
- Packages:
  - [Game Management](./packages/game-management/)
  - [Server Bootstrap](./packages/server-bootstrap/)
  - [World Management](./packages/world-management/)
- Services:
  - [Permissions Manager](./services/permissions-manager/)
  - [Player Profiles](./services/player-profiles/)
  - [Statistics](./services/statistics/)

## Why a monorepo?

We have lots of services that need to interact with each-other, when we used separate repositories we needed on-prem
CI/CD server and a Maven repository to allow these services to share packages.

With a monorepo we can drop that on-prem infrastructure completely, as packages can be shared directly.