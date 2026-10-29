# 2.5.0 — bulk HBM steel and capacitor colors

- Added the Bulk Steel Factory: 16 iron ingots and 4 coal dust produce 16 actual HBM steel ingots per 100-tick batch, at 500 HE/t. Expensive two-stage recipe uses eight steel blocks, four tungsten blocks, a diamond block, two HBM motors and two circuits.
- HBM X5771 HE receiver joins adjacent real HBM power networks; no RF conversion is implied. Subscriptions and disconnects only inspect already loaded chunks. HBM classes remain optional and are not bundled.
- Two iron slots, two coal-dust slots, four steel slots, real player inventory, shift-click routing, full-width HE synchronization, saved inventory/progress/power, safe output backpressure, and atomic completion. Input validation accepts `ingotIron` and `dustCoal`, including HBM and Thermal coal dust. All faces support sided item automation; output extraction cannot take ingredients.
- Factory appearance uses the exact installed HBM Nuclear Blast Furnace (LEGACY) active/inactive front/top and side textures. Only a working-state metadata bit changes; no tile replacement or per-frame tile renderer.
- Bank input is green, output red, full charge gold in GUI/world gauge. Full-charge world color requires exact full capacity, rather than the previous highest rounded meter band. Existing Ender IO casing textures and bank RF/stacking rules stay intact.
- Factory GUI uses reserved new ID 6 and standard inventory/progress packets. Retired solar GUI ID 3 remains absent. No changes to solar generation, railways, wireless automation, quarry mining, or removed space content.

Usage and crafting: [Steel factory guide](../STEEL_FACTORY_GUIDE.md). Executed verification: [Testing](TESTING.md).
