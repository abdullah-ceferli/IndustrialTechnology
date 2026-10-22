# Industrial Technology 1.6.1

Broken quarry items now retain their configuration. This works for MK1, MK2
and MK3 through normal survival harvesting, pickup, inventory storage and placement.

Saved configuration:

- Every VOID selection, including the last slot and later filter pages.
- The chosen items' metadata and NBT, with ghost stack counts kept at one.
- VOID enabled/disabled and ignore-metadata matching.
- Silk Touch / Fortune choice and the selected relative chunk area.

Placement restores the configuration using the current server's allowed chunk
limit and Silk setting. Newly crafted items still use defaults. Moved quarries
start stopped with fresh mining progress below their new placement height.
The placing player receives ownership and a new machine identity. RF, outputs,
cursors, mining statistics and active chunk tickets are not copied onto the item.
Real mined items drop separately once; filter icons never become real item drops.
Breaking releases the original quarry's tickets. Creative breaking keeps its
normal no-block-drop behavior, and protected/canceled breaks leave the machine intact.

Normal configuration is stored directly in the item's IndustrialQuarrySettings
NBT. A saved-settings tooltip identifies configured quarry items. Forge 1.7.10
normally removes the TileEntity before computing drops; BlockQuarry now delays
removal until harvesting has read its settings, then performs the existing cleanup.

Minecraft 1.7.10 item packets allow less than 32 KiB of compressed NBT. Exceptional
configurations with many large item NBT tags use an immutable, deduplicated profile
in the world's industrialtech_quarry_items saved data once compressed settings
exceed 28,000 bytes. The item carries its profile ID and a small settings summary.
These profiles persist across restarts without loading a target chunk. Such items
must remain with their world's profile data; ordinary inline configurations are
self-contained. Missing profiles leave a stopped quarry with an explicit error.

## Changed files

- quarry/BlockQuarry.java: configured drops, native delayed harvesting and placement.
- quarry/TileQuarry.java: detached configuration export and validated restoration.
- quarry/ItemBlockQuarry.java: saved-settings item tooltip.
- quarry/QuarryItemSettings.java: packet-safe large-configuration profiles.
- core/ModRegistry.java: register the quarry ItemBlock under the existing item names.
- core/IndustrialTechnology.java and build.gradle: version 1.6.1.
- QuarryItemIntegration.java and ServerIntegrationMod.java: native harvest/pickup,
  item serialization/packets, replacement, enchantments, protection and restart checks.
- README, generated AUTOMATION_CODES.md, tools/create_automation_docs.py,
  tools/audit_release.py, versioned evidence and TESTING.md: patch documentation.

Existing 1.6.0 reactor, wireless, NEI and quarry features remain included. See
TESTING.md for this patch's actual checks and prior client coverage.
