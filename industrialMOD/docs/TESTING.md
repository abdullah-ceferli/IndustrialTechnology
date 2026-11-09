# 2.10.1 verification — wearable right-click crash fix

Installed runtime: `IndustrialTechnology-1.7.10-2.10.1.jar`, 17,566,605 bytes.
SHA256: `96d716a42f54f6fcc8386faac6c06669525479d2bb19d116a49fb5d38d6c2262`.
Sealed runtime/sources: `.toolchain/accessories-release-2.10.1`.
Evidence, real GUI screenshots and deployment record: `docs/test-results/2.10.1`.

203 passing checks: 130 JUnit cases, 57 production Forge checks including genuine JVM restarts, and 16 graphical checks. The old sealed 2.10.0 JAR reproduces both amulet and flight-ring NullPointerExceptions at ItemInWorldManager.func_73085_a line 336 with ProjectE/HBM present and Baublelicious absent. Those two expected failures and exact crash excerpts are retained separately. Earlier GUI-slot checks did not cover the right-click equip path.

The fixed native tests call the actual vanilla ItemInWorldManager.tryUseItem: Survival held-stack consumption, preserved named-item NBT, both ring slots, full-slot rejection and Creative retention. Nightvision begins on the ordinary first Baubles worn tick, rather than immediately in the item-use callback. Existing effect tests and the real graphical session verify that timing and synchronization. Native runs cover no ProjectE/Baublelicious and ProjectE/HBM without Baublelicious; restart checks reload actual Baubles files and charged tools. Neither final native log contains the prior mixed Java/Scala sided-proxy warning.

The real integrated Survival world opens to LAN and sends actual C08 item-use packets for the amulet and both ring slots. It verifies synchronized nightvision/flight, empty Survival hands, named amulet preservation, full ring-slot/amulet-slot rejection, original Baubles UI equip/unequip, charge packets and five NEI recipes. The test returns to the main menu, saves/unloads the disposable server and exits cleanly. A preliminary harness shutdown was corrected to use that normal menu transition; no runtime code change was required.

The final payload audit permits exactly ItemWearable.class, IndustrialTechnology.class and two version metadata entries to differ from sealed 2.10.0. Every other entry is byte-identical, including all original OpenComputers content, sprites/audio, recipes and earlier machines. Sources match the final current Java files and packaged guides.

Installed 2.10.1 matches the sealed hash. Previous 2.10.0 is backed up in `game/mod-backups/industrialtech-equip-fix-20261101`. All 349 other installed mod/config files retain their hashes. ProjectE and Baubles remain installed; Baublelicious remains absent as selected by the user. No installed saved world was opened, edited or restored. All test worlds are disposable under `.toolchain/accessory-tests`; protected run-test folders are untouched.

Run `tools/audit_release.py` for the release audit. Rebuild production after any future runClient usage.

---

# 2.10.0 verification — standalone accessories and catalysts

Installed runtime: `IndustrialTechnology-1.7.10-2.10.0.jar`, 17,566,597 bytes.
SHA256: `58c14ff4b4fc51186a4f0fb55055afdfecebf34ed175fc6acbfb3760708dd0fe`.
Sealed runtime/sources: `.toolchain/accessories-release-2.10.0`.
Evidence, original-art screenshots and deployment checksums: `docs/test-results/2.10.0`.

199 passing checks: 130 existing JUnit cases, 49 native checks and 20 graphical checks. Separate production servers test absence of both ProjectE/Baublelicious and coexistence with those mods plus HBM. Coverage includes all five exact intended recipes, real Baubles slot types, survival/creative/second-ring flight behavior, original nightvision strength/duration/refresh/removal, coal/charcoal/coal-block credit, all three actual HBM coke metadata variants including petroleum, rejection of wood and other fuels, full-inventory atomic payment/change, original charge/depth/radius/cost tables, six-direction 3×3 tunnel geometry, actual mining/hardness limits/drops, Forge break cancellation, partial loot pickup/NBT, original entity-safe Nova blast, projectile fuel, world saving and actual JVM restart. The restart check also saves/loads the real Baubles player file and verifies both equipped items and restored effects.

Two native graphical runs, with and without the original content mods, open the real gold-ring inventory button and Baubles container, equip/remove both items with vanilla inventory packets, verify client flight/nightvision state, send the actual held-tool charge packet, reject a stale-slot packet, and render all five NEI recipe pages. Eight screenshots per run preserve the real original artwork and equipment screen; no mock GUI or AI art is used.

The release audit compares the final native tested payload with the delivered JAR, original sprite/audio SHA256 provenance, all unchanged earlier classes/art, complete original OpenComputers runtime content and matching current sources. Only the existing main entry, registry and client proxy change for registration; the new code stays in accessories/ and client/AccessoryInput. Original ProjectE/Baublelicious mod entry points are not imported or bundled. Baubles is an external required equipment API/UI dependency.

The installed JAR matches the sealed production hash. The previous 2.9.0 JAR is backed up outside mods; 350 other installed mod/config files retain their pre-install hashes. ProjectE, Baublelicious, Baubles and all saved worlds are untouched. All disposable testing lives under `.toolchain/accessory-tests`; the protected first/last-three run-test folders are untouched.

Run `tools/audit_release.py` for the current final audit. Test launchers are `tools/verify-accessories-production.ps1` and `tools/verify-accessories-client.ps1`; production must be rebuilt after future runClient usage.

---

# 2.9.0 verification вЂ” complete OpenComputers

Final runtime: `IndustrialTechnology-1.7.10-2.9.0.jar`, 17,477,651 bytes.
SHA256: `da90be83cdea3ba2572338ee75af861110df8b3728aacc91a069100f7adf59c8`.
Sealed runtime/sources: `.toolchain/opencomputers-release-2.9.0`.
Evidence, catalog snapshots, three graphical screenshots and deployment checksums: `docs/test-results/2.9.0`.

178 passing recorded checks: 130 existing JUnit cases, 44 native checks including unmodified OpenComputers reference runs, and four graphical checks. Native tests use actual Forge production mappings, original NativeLua53Architecture, actual component registration/crafting, mounted original OpenOS resources, real EEPROM execution writing a filesystem marker, physical original Adapter calls reading our entire three-bank RF network without transferring energy, HBM battery-meter transformer coexistence, real world save and JVM restart. The installed HBM, Thermal, AE2 and ProjectE pack also passes. The standalone catalog (169 descriptors/192 crafting recipes) and pack catalog (168 descriptors/196 recipes) exactly match their unmodified OpenComputers reference installations.

The graphical integrated-world test boots original OpenOS 1.8.10 from the original program disk and Lua BIOS, renders the original case inventory and live terminal, sends original clipboard/keyboard packets and executes an echo command. Native screen output and framebuffer screenshots are retained; no custom mock interface is used.

`tools/audit_opencomputers_release.py` compares every upstream executable/resource file to the pinned official universal archive, checks mounted ZIP directories, original sources/resources/notices, exact final native tested payloads, and preserved old gameplay/artwork. The only changed pre-existing gameplay class is the version/driver-registration main entry point; all previous machine, packet, battery-meter and art payloads are byte-identical to 2.8.0. Shared manifest, mcmod.info and pack description are intentionally combined.

Test launchers: `tools/verify-opencomputers-production.ps1`, `tools/verify-opencomputers-client.ps1`. All new test worlds live under `.toolchain/oc-tests`; no installed saved worlds are accessed. The old first/last-three run-test folders remain protected. Rebuild production after any future runClient, which writes development mappings. This installation backs up 2.8.0 outside mods and preserves existing HBM JAR, assembler config and Industrial Technology config hashes. The native pack shutdown prints an AE2 CSV export thread message in both stock-reference and combined runs; the server checks and exit codes pass in both.

The complete original content is packaged; these tests do not individually exercise every program, robot action, or optional integration with mods absent from this pack. Original execution budgets and configuration defaults are retained; no FPS guarantee is made.

---

# 2.7.2 verification Р Р†Р вЂљРІР‚Сњ restored-item name cleanup

## 2.8.0 РІР‚вЂќ Green battery energy and rows

Evidence: docs/test-results/2.8.0; sealed runtime/sources: .toolchain/battery-monitor-release-2.8.0.

The regular suite includes five new FlowWindow checks (130 JUnit cases total). A disposable production Forge server with installed HBM X5771, Thermal and AE2 checks exact HE/long totals, a real physical Green torch, mixed-tier five-battery rows, NBT defaults/round-trip, owner/type/input rejection, all axes/even counts/gaps, bounded chunk access, the 65-member cap, RF multiblock de-duplication, read-only behavior, item charging through HBM's production tick method, simultaneous +1500/-500 HE/t, Single mode, and disconnect invalidation. A separate server checks optional loading without HBM. The native client tests the actual row button packet, retained choice, correct 3000/1000 and 15000/5000 HE/t readings, Items/Tanks, GUI scales 3 and 2, readable tooltips and GL state; six screenshots are retained.

Run tools/audit_release.py to verify that final production classes/resources match the executed HBM server payload, original unrelated classes/textures are unchanged, no HBM/test classes are bundled, and sources match the workspace. GUI tests use runClient, which creates development artifacts; rebuild production before recording or deploying.


Production JAR: IndustrialTechnology-1.7.10-2.7.2.jar, 800,961 bytes. SHA256 `001104c6ef0138bc08fcc5ccb63a3a2c51ac62c0305e5c7ce16aee80737c8680`.

The existing HBM/NEI graphical inspection was rerun and records nine native recipe pages with their real item display names. All four Energy Storage Blocks and four restored Spark cells show no LEGACY suffix; the base Spark Battery remains correctly named. Existing layout/ingredient/empty-charge checks pass. The initial attempt to override language assets lost to HBM's load order and was removed. The final solution applies only eight cleaned translations using the public StringTranslate inject API after ordinary language resource loading. It is a client resource-reload listener, without ticking work, recipe changes, item replacement or HBM JAR edits.

Normal build validation retains 125 passing existing JUnit cases. No additional unit/gameplay tests were introduced for this name-only change. Package auditing proves all gameplay/integration/recipe classes and all old assets byte-identical to 2.7.1; only client-proxy registration, a new client name listener and the version-bearing main class change. Existing original recipe/native-module/restart verification remains archived in 2.7.1. Source package and production mappings are verified by tools/audit_legacy_names_release.py.

The installed update backs up 2.7.1 and replaces only the Industrial Technology JAR. It preserves the active assembler JSON, compatibility flag, HBM JAR and all saved worlds. Deployment hashes are recorded after installation.
