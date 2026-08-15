# Adding / editing shaderpack profiles

Support is **per shaderpack family**, driven by JSON "profiles" — no Java changes needed to add a pack.
Each profile tells the runtime patcher how to inject the Sky Block sky into that pack's shader sources
(in memory; the files on disk are never touched).

## Where profiles live

```
src/main/resources/assets/arsskyfix/patches/<family>.json
```

Registered families are listed in `ArsPatcher.FAMILIES`
(`complementary`, `photon`, `bsl`, `seus`, `sildurs`). **To add a new pack, add its `<family>.json`
here and add the file's base name to that array.**

## Profile format

```jsonc
{
  "name":   "Display name",
  "detect": ["substring1", "substring2"],   // ALL must appear in the lower-cased pack name (AND)
  "render": "skybasic",                       // "skybasic" (default) or "block" — see below
  "patches": [
    {
      "file":    "program/gbuffers_skybasic.glsl",  // shader path, or "block.properties"
      "op":      "INSERT_AFTER",                     // see ops below
      "anchor":  "…exact text from the real pack file…",
      "payload": "…text to insert / to replace the anchor with…",
      "absent":  "…optional: skip this patch if this string is already present…"
    }
  ]
}
```

### `render`
- **`skybasic`** *(default)* — the block's side faces are drawn through the pack's `gbuffers_skybasic`
  (real depth = occlusion). Works for packs that write their sky into `colortex0` in that pass
  (Complementary, BSL, SEUS Renewed, …).
- **`block`** — the faces go through `gbuffers_block` at real depth with a `blockEntityId` marker; the
  pack's own deferred sky pass then paints over them. Needed for packs that build their sky in deferred
  (Photon, Sildur's). A profile using `block` typically also adds a `block.properties` entry so the pack
  assigns the marker id.

### `op` values
- **`INSERT_AFTER`** — insert `payload` right after `anchor`.
- **`REPLACE`** — replace `anchor` with `payload`.
- **`block.properties` only:** `NEW_FILE`, `APPEND`, `REMOVE_FIRST`, `REMOVE_ALL`.

All ops are **idempotent**: `INSERT_AFTER` / `REPLACE` become no-ops if `payload` is already present, and
`absent` lets you skip a patch when a marker string already exists. If an `anchor` isn't found the patch is
skipped with a warning in the log (`[<family>] … anchor not found`).

## The one rule that matters

**Anchors are matched byte-for-byte.** The smallest whitespace/formatting drift between pack versions
breaks them silently. Before building, **simulate every anchor against the real pack file** (a tiny Python
`str.find` / `in` check is enough) so you know each `anchor` exists exactly once and each `payload` lands
where you expect.

## Sketch for a new pack

1. Read the pack's `gbuffers_skybasic` (or its deferred sky pass).
2. Pick a free colortex channel to carry a marker for the block's green vertex colour
   (`{0,1,0}`). If the fragment stage has no vertex colour, add a small `arsGlColor` varying.
3. In `skybasic`, write that marker; in `deferred`, turn the marker into "this fragment is sky"
   (e.g. force the sky branch / `z = 1.0`) so the pack paints its sky (and clouds) onto the block.
4. Encode those edits as `anchor`/`payload` pairs, verify against the real files, add the `<family>.json`,
   register the family, and rebuild.

The profiles are **Minecraft-version independent** (Iris and Oculus share the same shader source format),
so a profile added on one branch applies verbatim to the other.
