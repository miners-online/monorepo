# monorepo

This is our monorepo, it replaces the indepedant repos we used to have. All our games and related services can be found here.

- Games:
  - [Cave Run](./games/cave-run/)
  - [Golf](./games/golf/)
  - [Lobby](./games/lobby/)
  - [Mind Bending](./games/mind-bending/)
- Packages:
  - [Game Management](./packages/game-management/)
  - [Server Common](./packages/server-common/)
  - [World Mangement](./packages/world-mangement/)
- Services:
  - [Permissions Manager](./services/permissions-manager/)
  - [Player Profiles](./services/player-profiles/)
  - [Statistics](./services/statistics/)

## Why a monorepo?

We have lots of services that need to interact with eachother, when we used seperate repoistories we needed on-prem CI/CD server and a Maven repoistory to allow these services to share packages.

With a monorepo we can drop that on-prem infrastructure completely, as packages can be shared directly.