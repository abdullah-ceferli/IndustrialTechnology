# Original HBM Energy Storage Block recipes — 2.7.2

Version 2.7.2 removes the LEGACY suffix from the eight restored block/cell display names. Registry IDs, recipes and behavior remain the same.

This optional restoration follows the last original recipes before HBM's battery rework: [source commit ef2b20d357ce159f7c3861cfecf8169796023a83](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT/tree/ef2b20d357ce159f7c3861cfecf8169796023a83), immediately before de2a11389c4e6209945ada236870f757bb6aba10. Exact ingredients, counts, crafting grids, output quantities, empty charge NBT, recipe durations and HE/t are preserved.

## Energy Storage Blocks: original HBM assembler

| Output (one block) | Original ingredients | Recipe time | HE/t |
| --- | --- | --- | --- |
| Energy Storage Block (LEGACY) | 1 welded steel plate, 12 sulfur dust, 12 lead dust | 100 ticks | 100 |
| Li-Ion Energy Storage Block (LEGACY) | 8 plastic ingots, 12 cobalt dust, 12 lithium dust | 100 ticks | 100 |
| Schrabidium Energy Storage Block (LEGACY) | 16 desh ingots, 12 neptunium-237 dust, 12 schrabidium dust | 200 ticks | 100 |
| Spark Energy Storage Block (LEGACY) | 24 dineutronium ingots, 12 Spark Blend, 1 Spark Arcane Mass-Energy Void (1,000-cell), 32 CMB steel ingots | 400 ticks | 100 |

Current HBM material aliases display the old welded steel plate as a Sextuple Steel Plate (`plateSextupleSteel`) and desh as Workers' Alloy (`ingotWorkersAlloy`). These are the installed version's equivalents of the original material definitions; counts are unchanged.

These appear on HBM's own Assembly Machine pages in NEI, not a custom Industrial Technology processing screen. HBM's existing processing code can round floating-point progress to an extra tick; the recipe itself retains its exact historical duration and HE/t.

The active file is `config/hbmRecipes/hbmAssemblyMachine.json`. It preserves all 617 entries from the installed template in order and appends only the four original storage recipes. The underscore template stays untouched. Other recipe files are unaffected. HBM loads this complete config rather than its default assembler list, so merge it with a newly generated template if you later update HBM.

## Spark battery/cells: original crafting grid

Enable `compatibility.restoreLegacyHbmSparkCellRecipes=true` in `config/industrialtech.cfg` and use Industrial Technology 2.7.1. The option defaults to false in other installations. Without HBM, it safely has no effect.

The five recipes use normal shaped ore crafting, so NEI shows its standard crafting-grid pages with HBM's original items/icons and layout. Existing modern Spark recipes remain alongside the restored one. All outputs start empty (`charge: 0`), as in the original recipes.

Legend: **P** dineutronium plate; **S** Spark Blend; **W** fine magnetized-tungsten wire; **B/C** the specified earlier empty battery/cell; **X** Spark Singularity.

Spark Battery (P + two Spark Blend):

```
P
S
S
```

Spark Power Cell (6-cell): B = Spark Battery, P below = Spark Blend:

```
BW
PW
BW
```

Spark Arcane Car Battery (25-cell): C = 6-cell:

```
W W
SCS
PSP
```

Spark Arcane Energy Storage Array (100-cell): B = 25-cell:

```
W W
BPB
SSS
```

Spark Arcane Mass-Energy Void (1,000-cell): C = 100-cell; P in this grid = Spark Blend:

```
PCP
CXC
PCP
```

The installed HBM build removed those four cell items, not just their recipes. They are restored under Industrial Technology IDs `legacy_battery_spark_cell_6`, `_25`, `_100`, `_1000`, using HBM's original names, unchanged original icons, legacy battery class, and 600M/2.5G/10G/100G HE capacities. Existing HBM cell items are reused if present. The cells are recipe prerequisites; the restoration does not convert their old battery API into modern battery-socket behavior.

Only these four Energy Storage Block tiers and their required Spark crafting chain are restored. FEnSU, the modern capacitors and unrelated legacy machines retain their existing recipes.

## Backup / restore

The deployment backup is outside the `mods` directory, under `game/mod-backups/industrialtech-legacy-storage-20261030`. It includes the previous Industrial Technology JAR, original industrialtech.cfg, original assembler template, and whether an active assembler file existed. Deployment checks hashes and uses only the tested config/JAR. Player saves are not edited or opened during installation.

To undo, close Minecraft, restore the previous JAR and industrialtech.cfg from that backup, and restore the old active assembler config if one existed; otherwise remove the newly added active `hbmAssemblyMachine.json`. A restore manifest records exact original and installed hashes. Avoid reverting the mod after saving a world containing newly restored cell items unless their removal is intended.

Sources: historical [assembler recipe definitions](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT/blob/ef2b20d357ce159f7c3861cfecf8169796023a83/src/main/java/com/hbm/inventory/recipes/AssemblyMachineRecipes.java) and [original crafting-grid definitions](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT/blob/ef2b20d357ce159f7c3861cfecf8169796023a83/src/main/java/com/hbm/main/CraftingManager.java). A source copy, four-recipe delta and exact active config are saved in config-updates/hbm-legacy-storage-20261030. Test evidence is recorded in docs/TESTING.md.
