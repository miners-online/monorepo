# monorepo

Welcome to the monorepo containing all core code for our Minecraft server ecosystem. Here you'll find the source for our
games, shared packages, and essential backend services. This unified repository streamlines development, encourages code
reuse, and simplifies collaboration across all aspects of our Minecraft server infrastructure.

## Structure

- **Games**:
  - [Cave Run](./games/cave-run/) (placeholder)
  - [Golf](./games/golf/) (placeholder)
  - [Lobby](./games/lobby/) - The lobby server, where players first join
  - [Mind Bending](./games/mind-bending/) (placeholder)
- **Packages**:
  - [Game Management](./packages/game-management/) (placeholder)
  - [Game Materials](./packages/game-materials/) - Common block handlers and entities used across multiple games
  - [Server Bootstrap](./packages/server-bootstrap/) - The server bootstrapper, used to start all our games
  - [World Management](./packages/world-management/) - Tools for managing worlds and schematics

## Why a monorepo?

We have lots of services that need to interact with each-other, when we used separate repositories we needed on-prem
CI/CD server and a Maven repository to allow these services to share packages.

With a monorepo we can drop that on-prem infrastructure completely, as packages can be shared directly.

## Design Philosophy

We started our development by focusing on the lobby server, as it is quite simple, as we needed more features we separated
them into packages, as we knew our other games would need them too.

When making packages we focused on making them as generic as possible, so they can be reused in other projects. 
 
Our development is guided by following these principles:

1. Don't make too many single-class packages, group related classes together
2. Don't make packages too big, if a package has too many classes, consider splitting it
3. Only make packages that is not specific to a game, if it is specific to a game, it should be part of that game's codebase

