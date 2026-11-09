# 2.10.1 — wearable right-click crash fix

- Fix the Amulet of Nightvision and Ring of Flight crashing a Survival server when right-click equipped. Minecraft 1.7.10 requires a non-null, zero-count return stack so vanilla can clear and synchronize the hand.
- Preserve held items when the corresponding Baubles slots are full and retain Creative items as before.
- Scope the Java sided proxy to industrialtech so the bundled OpenComputers Scala loader does not attempt to inject it.
- Add real vanilla ItemInWorldManager regression checks and actual client right-click packets in disposable Survival LAN worlds, including ProjectE present with Baublelicious absent.

Baubles is still required. Recipes, original artwork/audio, coal-only tool fuel, machines and the complete original OpenComputers payload are preserved. Installed saved worlds are never opened or edited by this update.
