# Ars Sky Shader Fix

Put down an Ars Nouveau **Sky Block**, load up your shaders, and… stare at a big black cube?
Ars Nouveau draws the Sky Block with its own custom shader, which shader mods ignore while a
shaderpack is running — so the block turns pitch black. This tiny addon draws the block **through
the shaderpack's own sky** instead, so it shows the real sky again (colour, clouds, sun, moon, stars),
with correct depth and a solid, buildable top. No shaderpack → the normal vanilla effect is untouched.

The pack is patched **at runtime, in memory** — the files in your `shaderpacks` folder are never modified.

## Versions (branches)

| Branch | Minecraft | Loader | Shader mod |
|--------|-----------|--------|------------|
| [`1.21.1`](../../tree/1.21.1) | 1.21.1 | NeoForge | Iris |
| [`1.20.1`](../../tree/1.20.1) | 1.20.1 | Forge 47.x | Oculus + Embeddium |

Both branches share the **same shader-pack profiles** and behave identically — the backport only swaps
the loader glue, the config API and the 1.20.1 vertex-builder API. Oculus reuses Iris's
`net.irisshaders.iris.*` API namespace, so the shader-side logic carried over unchanged.

## Supported shaderpacks (SHADER mode)

Complementary Reimagined/Unbound (±EuphoriaPatches), Photon, BSL, SEUS Renewed, Sildur's Vibrant.
Many other "normal-sky" packs also work out of the box. Pathtracers (SEUS PTGI, Kappa) and packs that
build their sky in an unusual way aren't supported — support is per-family because every pack builds its
sky differently.

## Config (`renderMode`)

- **SHADER** *(default)* — runtime-injected pack sky (sun/moon/stars/clouds), correct depth, buildable top,
  no self-shadow. Use this if your pack is supported.
- **SKYBASIC** — solid block painted by the pack's own sky shader: real sky colour + sun, correct
  occlusion, but no clouds. Cheap and stable.
- **SOLID** — flat, sky-tinted cube. Cheapest, always works.
- **HYBRID** — a preview of the full window for unsupported packs; very expensive, not for daily play.

The Sky Block's **top face** gets a solid colour (soft lavender by default) so placed blocks stay findable —
recolour or disable via `topMarker` / `topColor{R,G,B}`.

## Building

```
./gradlew build   # -> build/libs/arsskyfix-1.0.0.jar
```

The addon compiles against Ars Nouveau + the shader mod (+ GeckoLib) as `compileOnly` dependencies.
Drop the matching jars into a `libs/` folder in the project root before building:

- **1.21.1:** `ars_nouveau-1.21.1-5.13.0.jar`, `iris-neoforge-1.8.14-beta.1+mc1.21.1.jar`
  (GeckoLib is pulled from Modrinth maven automatically).
- **1.20.1:** `ars_nouveau-1.20.1-4.12.7-all.jar`, `oculus-mc1.20.1-1.8.0.jar`,
  `geckolib-forge-1.20.1-4.8.3.jar`.

## For modders — adding shaderpack support

Support is per-pack and driven by JSON profiles (no Java changes needed to add a pack).
See **[PROFILES.md](PROFILES.md)** for the profile format and how to add/edit one.

## License

[GNU GPL v3.0](LICENSE). Feel free to read, reference and reuse under its terms.
