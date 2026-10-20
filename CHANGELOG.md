# Changelog

## 1.0.1

**New shaderpack support (tested):**
- **Solas** V3.7b — full sky (sun/moon/stars/clouds).
- **Astra Lex** V93.0 — full sky (sun/moon/stars/clouds).
- **Hysteria** v1.2.2 — full sky including volumetric clouds **and volumetric lighting** on the block.
- **Spooklementary** v2.0.4 — full sky (sun/moon/stars/clouds).
- **Bliss** v2.1.2 — full sky (sun/moon/stars/clouds).
- **Pastel** v1.200 and **Insanity** v1.650 — stars + clouds (their sun/moon are vanilla-texture based and don't show on block faces).
- **Mellow** v3.4.1a — stars + clouds (sun/moon vanilla-texture based).
- **Reverie** Beta v0.9 — sky, sun, moon, stars + solid buildable top (this pack composites clouds in a late depth-dependent pass, so they don't render on the block).

**Complementary:** verified on **r5.9**, and with **EuphoriaPatches up to 1.10.0** (Reimagined and Unbound).

**Fixes / polish:**
- Clearer in-game guidance: a few packs need the shader's **Temporal Filter (TAA)** turned off; this is now called out where required.

## 1.0.0

Initial release. Draws the Ars Nouveau Sky Block through the active shaderpack's own sky (runtime, in-memory injection — no pack files touched).

Supported: **Complementary Reimagined / Unbound** (±EuphoriaPatches), **Photon**, **BSL**, **SEUS Renewed**, **Sildur's Vibrant**. Config render modes: SHADER, SKYBASIC, HYBRID, SOLID.
