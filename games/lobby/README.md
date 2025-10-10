# lobby

This is the package for the lobby server. It's intended purpose is to be the first server players join,
from here they can select which game to play.

## Environment Variables

The lobby server can be configured through the following environment variables:

| Variable               | Description                                      | Default |
|------------------------|--------------------------------------------------|---------|
| `SCHEMATIC_PATH`       | Path to the schematic file to load               | None    |

## Running

To run the lobby server we recommend using our Docker images, here is an example for Docker Compose:

```yaml
services:
  lobby:
    image: ghcr.io/miners-online/game-lobby:latest
    container_name: lobby
    restart: unless-stopped
    ports:
      - "25565:25565"
    environment:
      - SCHEMATIC_PATH=/data/full_lobby.schem
    volumes:
      - ./data:/data
```

or remove the environment variable and volumes to use the default schematic.