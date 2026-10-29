# Executed verification — Industrial Technology 2.4.0

Final ordinary production JAR: `IndustrialTechnology-1.7.10-2.4.0.jar` (673,388 bytes).
SHA256: `8b50576a3f61e0e34e3e0d8e36a71a4b6e5f26ff84df5cc8472bca8b5b86d650`.

**533 recorded passing checks**, zero failures in the archived completed runs: 101 JUnit checks, 415 native server checks (including real JVM restarts), 17 integrated client checks. Individual report assertions sometimes cover several subcases; counts are assertions reported, not a claim of that many distinct gameplay systems.

| Final production run | Initial checks | Genuine JVM restart checks |
|---|---:|---:|
| solar-standalone | 24 | 1 |
| hbm-regression | 176 | 13 |
| solar-pack | 26 | 1 |
| base-regression | 139 | 12 |
| bank-pack | 22 | 1 |

The pack uses the installed HBM X5771, CoFHCore, Thermal Foundation/Expansion/Dynamics, EnderCore and Ender IO versions; the original HBM regression uses X5771. The standalone runs have no required external technology/rail mod. The bank regression includes actual Thermal Dynamics and Ender IO cables. Every listed native run's copied production JAR matches the SHA256 above.

Solar coverage includes all five tiers and actual recipe outputs, noon/night/roof/rain, horizontal merge/split, read-only RF queries and simulations, exact 20,480 RF/t clear-noon output, real RF cables, charging and a real capacitor-to-Quarry chain mining 500 blocks in 100 measured ticks. Train coverage includes server movement/braking, three actual coupled freight wagons, logical cargo limits/metadata/NBT, normal extraction, 300 coal, steam warm-up and water-consuming movement, RF consumption, shared charge limits, ownership rejection and bounded high-entropy legacy packets. Cargo loader/unloader checks include an actual ordinary destination chest. Persistence is checked by stopping and starting a second native JVM.

Original subsystem coverage includes Fortune/Silk/ore drops, HBM ore and reactor adapters, protection, 20,000 RF/t balancing, output transactions, large void-filter selections, charged quarry replacement, all wireless torch colors, terminals, automation EVERY schedules/pulses, chunk tickets, plain green lamps and capacitor RF conservation/topology/settings/charged drops. The release audit separately compares existing classes and artwork against 2.3.1.

Client evidence is in `docs/test-results/2.4.0/images/`: all six rolling-stock models, five solar tiers, solar GUI, exactly three freight bins, driver cabin and movement, long-train third-person camera, control GUI and a separate passenger interior. Real NEI pixels are compared during normal/hover frames, with GL state assertions. The native render uses original OBJ meshes and official credited solar textures; no photographs are packaged as train surfaces. The final render/GUI sources match these checks; later native-only safeguards and the 1-in-16 grade are covered by the final native/unit runs.

Limits of verification: one real integrated client was exercised, plus dedicated native servers and distinct server-side player identities for permission tests. A two-human-client latency/soak test and a performance benchmark in the user's main world were not performed. No main-world FPS improvement is claimed. Optional catenary, cross-mod rail/coupler adapters, reverse throttle and derailment simulation are outside this release's implemented scope, as documented in SOLAR_AND_RAILWAY_GUIDE.md.

All fixtures are disposable project worlds. Installed player saves and game configuration were not modified. Earlier failed/incomplete development attempts are retained in their separate run-test directories and are not counted as passing verification.

Reproduce with tools/build.ps1 (test/build/testModJar plus productionTest), tools/verify-solar-rail-production.ps1, tools/verify-wireless-production.ps1, tools/verify-capacitor-production.ps1 and the railClient property. Use fresh run-test-* directories and Java 8. Run `python tools/audit_release.py` against the completed distribution. Running a development client rewrites the main build output with a dev JAR: rebuild/reobfuscate or restore the sealed production bytes before distribution.
