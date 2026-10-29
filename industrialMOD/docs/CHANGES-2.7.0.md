# 2.7.0 — colored 104-slot storage bags

Added Storage Bags in all 16 wool colors, each opening a 13x8 inventory matching ProjectE's Alchemical Bag capacity. Original ProjectE 1.7.10-PE1.10.1 icons and storage-screen texture are used unchanged, with provenance and credits. No ProjectE dependency or generated artwork.

Same-color bags share personal storage by player UUID; different players/colors remain separate. World-owned data survives death, dimensions, lost bags and restarts. Ordinary vanilla clicks/drag/shift-click and normal item stack limits are supported. The opening hotbar slot is protected, including number-key/throw paths, and nested Storage Bags are rejected. Recoloring switches channels without deleting the original color's contents.

Craft from four matching wool, four leather and an Ender Chest; recolor with one vanilla dye. All recipes use ordinary crafting/NEI discovery. Added GUI ID 8 with held-bag validation before any tile lookup, without extra packets or ticking handlers. Item registration follows the existing consoles. Existing ProjectE items/storage and all older Industrial Technology machines remain unchanged. See BAG_GUIDE.md and docs/TESTING.md.
