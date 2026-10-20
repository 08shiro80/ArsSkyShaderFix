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

Both branches share the **same shader-pack profiles** (branch [`shared`](../../tree/shared)) — the backport
swaps the loader glue, the config API and the 1.20.1 vertex-builder API. Oculus reuses Iris's
`net.irisshaders.iris.*` API namespace, so the shader-side logic carried over unchanged. 1.20.1 additionally
has an in-game config screen and re-applies the block's shader id at draw time (needed for the block-path
packs under Oculus + Embeddium).

## Supported shaderpacks (SHADER mode)

Complementary Reimagined/Unbound (±EuphoriaPatches), Photon, BSL, SEUS Renewed, Sildur's Vibrant, Solas,
Hysteria, Spooklementary, Mellow, Reverie, Bliss.
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

The shader-pack profiles and textures are shared by both versions and live in their own branch,
[`shared`](../../tree/shared). **You must clone that branch too**, into a folder named `shared` right next to
the version folder — the build picks it up via the relative path `../shared/resources`:

```
git clone -b 1.21.1 https://github.com/08shiro80/ArsSkyShaderFix.git ArsSkyShaderFix-1.21.1
git clone -b shared https://github.com/08shiro80/ArsSkyShaderFix.git shared
cd ArsSkyShaderFix-1.21.1
./gradlew build   # -> build/libs/arsskyfix-1.0.1.jar
```

(Use `-b 1.20.1` for the 1.20.1 version; both can sit next to the same `shared` folder.)

Without the `shared` folder the build still succeeds, but the jar contains no profiles and no textures —
SHADER mode does nothing and the SOLID/fallback faces render with a missing texture.

The addon compiles against Ars Nouveau + the shader mod (+ GeckoLib) as `compileOnly` dependencies, pulled
automatically from the Modrinth maven — no jars to download by hand.

## For modders — adding shaderpack support

Support is per-pack and driven by JSON profiles (no Java changes needed to add a pack).
See **[PROFILES.md](PROFILES.md)** for the profile format and how to add/edit one.

## License

[DLB Non-Commercial License 1.0](LICENSE).
