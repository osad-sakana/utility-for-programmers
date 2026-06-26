# UtilitiesForProgrammers

Client-side [NeoForge](https://neoforged.net/) mod for Minecraft **1.21.10** that adds
utilities to support learning programming inside Minecraft.

This mod is **client-side only** and does not need to be installed on the server.

## Features

- **HUD** — absolute coordinates, facing direction, and a target-block inspector
  (block id + block-state properties).
- **Block-update highlighting** — colors blocks by placement order
  (newest = red, fading to blue) and fades out over time. Driven by a mixin on the
  client packet listener, so it works even against vanilla / unmodded servers.
- **Relative-coordinate ground grid** projected around the player.
- **Window controls** — keep rendering while unfocused, an always-on-top toggle,
  a free-mouse toggle to operate other windows, and a freeze toggle to hold the
  HUD / highlights still for copying.

## Requirements

- Minecraft 1.21.10
- NeoForge 21.10.x
- JDK 21 (for building from source)

## Building

```bash
./gradlew build
```

The built jar is produced under `build/libs/`.

## Running the dev client

```bash
./run-client.sh
```

The first run downloads game assets (a few minutes); later runs are quick.
It uses an offline dev account, so use it for singleplayer testing.

## License

[MIT](LICENSE) © Yu Osada
