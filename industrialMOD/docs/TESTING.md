# 2.7.0 verification — colored storage bags

Production JAR: `IndustrialTechnology-1.7.10-2.7.0.jar` (778,744 bytes).
SHA256: `0af5bad1b21ede37837db37a82eaa3d23782f1876505f8eed319938228781b34`.

**167 passing recorded checks**: 125 existing JUnit tests, 17 native bag checks plus a genuine JVM restart both without and with actual ProjectE 1.7.10-PE1.10.1, and 6 graphical client checks. Exact identities/reports/screenshots are archived in docs/test-results/2.7.0.

Both native runs use the exact final production bytes. Real Minecraft crafting verifies all 16 colors and 16 dye recipes. Server tests cover the exact 104+36-slot layout, canonical shared-color inventory, separation by color/player, named item NBT, real vanilla click/right-click/drag/shift/number-key/throw transaction paths, nested-bag rejection, full and partial transfers, unstackable items, held-bag validation, replacement players after death, another real loaded dimension, recoloring without loss, slot 103 serialization, nonbag/invalid-color GUI guards, world data saving and genuine restart recovery. ProjectE coexistence is checked by loading the user's actual ProjectE JAR alongside the final release in a separate disposable world; no original ProjectE storage files are read or imported.

The NEI graphical client runs without ProjectE to establish independent operation and copied asset loading. Actual right-click item opening and vanilla window-click packets transfer a named stack, reject moving the active bag, open the same storage with a second Blue bag, and keep Red storage separate. All 104 storage slots synchronize and render on one screen alongside all 16 color icons and the ordinary player inventory. Screenshots were visually inspected; no OpenGL errors. Development client mapping is separate from native production loading tests.

tools/audit_bag_release.py verifies the production/tested/sealed JAR identity, matching sources/guides, byte-identical existing machine/wireless/renderer classes except required registry/GUI/version routes, unchanged old assets, and exact original hashes for all 16 ProjectE bag icons plus its GUI atlas. New runtime code is the bag package and client screen, with no bundled ProjectE classes or mandatory dependency. Asset credits and the unchanged upstream legacy README are packaged. Existing 2.6.1 screen fixes remain byte-identical.

Only fresh project run-test-* worlds were created. Installed mods, ProjectE inventories and all user worlds were not edited. Earlier machine/HBM/rail/console evidence remains archived in prior release folders.
