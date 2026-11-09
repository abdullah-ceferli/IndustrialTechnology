# 2.9.0 — complete original OpenComputers

- Embeds official OpenComputers 1.8.10 (`667626d`) for Forge 1.7.10 into the same runtime JAR. Preserves every upstream executable/resource file byte for byte, with only the shared manifest, mod-list metadata and pack description combined.
- Keeps the original `OpenComputers` mod ID and namespaces so original content, programs and optional-mod detection remain compatible. All original computer/robot/drone/tablet/server components, recipes, manual, art and Lua runtimes are retained.
- One combined core loader registers the original OC transformer/container and the unchanged HBM battery-meter transformer. Original OC access transformers are retained.
- Preserves explicit ZIP directory entries required to mount OpenOS and other program disks.
- Adds a passive, read-only `industrialtech` Adapter component for our capacitor networks, valid Green monitors and factory consoles; no new background polling or chunk loading.
- Vendors the verified original universal/development/source archives, upstream metadata and notices; source distribution includes upstream Scala/Java sources and original resources.

See `OPENCOMPUTERS_GUIDE.md` for first-computer steps, adapter methods and installation. Do not also install a standalone OpenComputers JAR. Existing item/block IDs, world data and previous automation are retained. User worlds are not used for development tests.
