# Colored Storage Bags — 2.7.0

Storage Bags match ProjectE's Alchemical Bag capacity and colors: **104 slots, 13 columns by 8 rows, 16 colors**. The item icons and gray storage-screen texture are the original files from the user's installed ProjectE 1.7.10-PE1.10.1, copied unchanged. ProjectE does not need to be installed.

## Use

Hold the bag in your hotbar and right-click in air to open it. All 104 storage slots appear on one screen above your ordinary player inventory. Click, right-click, drag and shift-click work normally, with each item's normal stack limit. Press Escape or the inventory key to close it.

All of **your bags of the same color share storage**. Different colors have separate inventories, and other players have their own storage. A second Blue bag opens the same Blue inventory; a Red bag opens a different inventory. The sixteen colors provide 1,664 storage slots per player in total.

The inventory is saved by the world for your player UUID and color. It survives relogging, changing dimensions, death and normal server/world restarts. If a bag is lost or destroyed, craft another of the same color to recover that color's stored items. Giving someone a bag lets them open their own storage, not yours.

The active bag's hotbar slot is locked while its screen is open. Storage Bags cannot go inside other Storage Bags. Close the screen to move or drop the bag normally.

## Crafting and colors

Use four matching colored wool blocks, four leather and one Ender Chest:

```
Wool    | Leather     | Wool
Leather | Ender Chest | Leather
Wool    | Leather     | Wool
```

The wool color determines the result. All sixteen shaped recipes appear in NEI. Recolor any Storage Bag by crafting it with one vanilla dye; these sixteen shapeless recipes appear in NEI as well.

Recoloring selects the destination color's storage. It does **not move or erase** the previous color's contents. Craft or recolor another bag back to the old color to access those items again. Recoloring creates a fresh item, so an anvil name is not carried over.

Available colors: White, Orange, Magenta, Light Blue, Yellow, Lime, Pink, Gray, Light Gray, Cyan, Purple, Blue, Brown, Green, Red and Black.

## Existing ProjectE bags

These items are named **Storage Bag**, under Industrial Technology. They coexist with ProjectE's **Alchemical Bag** items and use independent inventories. They do not import, replace or modify existing ProjectE bag contents. Transfer items manually if you want to use the new bags.

## Save and appearance details

Storage is saved in the overworld's `data/industrialtech_bags.dat`; bag items do not carry inventory NBT. Same-color copies always refer to the single server inventory. Normal Forge container packets synchronize items; no custom bag packet or continuously ticking service is added.

Original ProjectE asset provenance and artist credits are recorded in ASSET_CREDITS.md and development-assets/bags/projecte-assets.json. No generated images or recolored approximations are used. Testing creates only disposable project worlds and does not edit installed worlds or mods.
