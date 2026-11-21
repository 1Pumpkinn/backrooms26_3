# Backrooms26.3

A Fabric mod for Minecraft 26.3 that adds a Backrooms dimension, inspired by Escape the Backrooms.

## Progress

- Batch 1: blocks, datagen (models and language), creative tab, the `backrooms26_3:backrooms` dimension, `/backrooms enter` and `/backrooms leave`
- Batch 2: Level 0 is tiled with 17 x 17 maze-like cells, one per chunk, so there are no gaps or empty halls (templates are built by `tools/generate_templates.py`; re-run it after changing the layout rules)
- Fluorescent lights are thin 4 pixel panels (`FluorescentLightBlock`) whose json is hand written in `src/main/resources/assets`
- Batch 3 (planned): flickering lights and timed blackouts

## Development

Run `./gradlew runDatagen` to generate block models and language files, then `./gradlew runClient`.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
