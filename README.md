# Ars Sky Shader Fix — shared resources

Shader-pack profiles (`resources/assets/arsskyfix/patches/*.json`) and textures used by **both** version
branches ([`1.21.1`](../../tree/1.21.1), [`1.20.1`](../../tree/1.20.1)).

This branch is not built on its own. Clone it into a folder named `shared` next to a version branch; the
version's `build.gradle` includes it via the relative path `../shared/resources`. See the README of a
version branch for the full build steps, and `PROFILES.md` there for the profile format.
