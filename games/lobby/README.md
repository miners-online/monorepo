# lobby

This is the package for the lobby server. It's intended purpose is to be the first server players join,
from here they can select which game to play.

## Environment Variables

The lobby server can be configured through the following environment variables:

1. `SCHEMATIC_PATH`: The path to the schematic file to use for the lobby world. If not set, the default schematic will be used.
2. `USE_MOJANG_AUTH`: If set to a none-empty value, the server will use Mojang authentication. Default is empty.
3. `PROXY_AUTH_TYPE`: The type of proxy authentication to use. Can be `BUNGEE_GUARD`, `VELOCITY` or `NONE`. Default is `NONE`.
4. `VELOCITY_AUTH_SECRET`: The secret key to use for Velocity proxy authentication. Required if `PROXY_AUTH_TYPE` is set to `VELOCITY`.
5. `BUNGEE_GUARD_AUTH_TOKENS`: A comma-separated list of valid tokens for BungeeGuard proxy authentication. Required if `PROXY_AUTH_TYPE` is set to `BUNGEE_GUARD`.

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