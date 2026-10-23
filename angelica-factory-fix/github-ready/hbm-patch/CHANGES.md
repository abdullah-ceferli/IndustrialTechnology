# HBM compatibility changes

Patch: `personal-hbm3`, prepared for sharing on 2026-10-23.

Upstream: Angelica 2.2.36, commit `e84c8c8d7accd6df0c93340c8409d997a43b9aaa`.

The three runtime source edits are also provided in [changes.patch](changes.patch), which applies to this pinned upstream base. The complete patched source is already present in this repository.

## Runtime changes

1. `TileEntityRenderBoundsRegistry`: classify `com.hbm.*` tile entities as dynamic before inspecting or caching bounds. Their current bounds are evaluated on the main render thread.
2. `HbmFactoryRenderFallback`: merge valid live HBM machines missing from the main-pass list when a special renderer exists and their current bounds intersect the viewport. Remove duplicate HBM identities and invalid HBM entries; preserve existing non-ticking models. Unavailable or uncertain bounds fail open.
3. `CeleritasWorldRenderer`: call the fallback once after main-pass section collection. Existing dispatcher render-distance and render-pass rules still apply.

The fallback scans the loaded tile-entity list every main render pass. This can cost CPU time and submit more models than section occlusion alone. It is intentionally limited to HBM classes, but it is not limited to bedrock-ore machines.

No world data, simulation, inventory, packets, or renderer settings are changed by the patch. Upstream's mod metadata stays at `2.2.36` for compatibility. The custom filename and in-JAR provenance note identify the overlay.

## Sharing/build changes

- Included the upstream source tree and original credits/license notices.
- Added portable overlay build/test scripts with explicit JDK/Forge paths and checksum-pinned dependencies.
- Added public documentation and a bug-report template; excluded local game data and generated artifacts.
- Disabled the original project's publishing destinations and omitted its GitHub workflows, CODEOWNERS, and JitPack configuration from this standalone upload.

This is an unofficial experimental workaround. The exact original bedrock-ore factory disappearance remains unconfirmed fixed.
