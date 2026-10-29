# 2.4.1 patch verification - solar panels without UI

Production JAR: `IndustrialTechnology-1.7.10-2.4.1.jar` (672,558 bytes). SHA256: `1851a14260605de1a0fa2980e1d7f12e024e8d15791220f65bcb1f91e0592721`.

144 recorded passing checks: 101 JUnit, 25 native initial, 1 genuine JVM restart, 17 in-game client checks. Completed reports and images are archived in docs/test-results/2.4.1.

The native run verifies all five panels ignore right-click and retired server GUI ID 3 returns no container. Existing daylight, weather, roof, RF, array, charging, cargo and train checks also pass. Four Ultimate panels still supply the measured 500-block / 100-tick Quarry test. The real client verifies a panel right-click leaves the screen closed, the retired client GUI route returns null, and freight, train, charging and passenger interfaces still work with NEI and clean GL state.

The patch audit allows only six changed classes: the panel block, client/server GUI routing, expansion container, expansion screen and mod version class. Solar generation/array code, all other machinery, assets, recipes and save formats compare byte-for-byte with 2.4.0. The prior 533-check expansion matrix remains archived in docs/test-results/2.4.0 and docs/TESTING-2.4.0.md; it is historical evidence, not rerun work claimed for this small patch.

Only disposable project worlds were used. Installed player worlds were untouched. Build and test with tools/build.ps1 (test/build/testModJar, productionTest), tools/verify-solar-rail-production.ps1 and railClient. Development clients replace the build output with a dev JAR; the delivered output was restored to the exact native-tested production bytes.
