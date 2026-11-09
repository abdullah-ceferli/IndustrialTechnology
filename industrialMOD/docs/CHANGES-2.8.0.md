# 2.8.0 — Green Stick energy and battery rows

- Green monitors open an Energy page on supported energy storage: actual stored/capacity values, charge bar, green incoming energy and red outgoing energy.
- The Batteries button toggles one battery or the longest touching straight row through it. Mixed tiers of original HBM storage blocks, even-sized rows, and all three axes work.
- Row totals retain 64-bit HE/RF, count batteries, save the selected mode, bound discovery to 32 blocks per side, and identify partial scans without loading chunks.
- Separate HBM rates observe actual energy changes, including simultaneous input/output and item charging; a 20-completed-tick window avoids confusing net charge changes with throughput.
- Existing Advanced Capacitor Bank multiblocks are read once via their network ledger. Other implementations lacking transfer observation explicitly report the limitation.
- Existing item/tank monitoring, Yellow redstone controls, Blue logic, recipes, textures, and saves remain unchanged.

Usage and measurement limits: [BATTERY_MONITOR_GUIDE.md](../BATTERY_MONITOR_GUIDE.md).

Implementation: a small Forge loading plugin adds observation calls after verified energy writes in HBM's original TileEntityMachineBattery and normal Battery Socket charge setter. Original writes, item calls, branch instructions, return values, NBT methods, and calculations are retained. Both development and SRG tick-method names are handled. The transformation is idempotent and adds a capability marker only when observation points were found. Unsupported implementations fall back to storage-only readings. HBM remains optional and its JAR is never edited.

Observation uses expiring weak references and bounded sample arrays; no block renderer, textures, server tick listener, chunk tickets, or permanent per-tick battery polling is added. Native server and client checks run in new disposable test directories.
