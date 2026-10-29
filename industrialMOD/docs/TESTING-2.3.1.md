# Verification — Industrial Technology 2.3.1

This patch corrects the normal capacitor casing and makes its exterior gauge optional.
Java 8 / Minecraft 1.7.10 / Forge 10.13.4.1614.

| Archived report | Successful checks |
| --- | ---: |
| Existing unit reports (13 XML suites) | 78 |
| Native bank initial | 20 |
| Native bank restart | 1 |
| Real Ender IO + NEI comparison client | 15 |
| Total recorded successful executions | 114 |

The real client loads the installed Ender IO 2.3.0.430_beta and EnderCore 0.2.0.40_beta,
remapped only for the development environment, plus actual NEI. The screenshot compares our
two banks on the left with an actual Ender IO Vibrant 3x3 wall on the right, with normal/default
displays on both. All six faces start without a gauge. A framebuffer probe confirms no forced
cyan meter is painted over the default casing.

An actual server block-activation call with a sneaking, empty-handed player enables only the
clicked face's meter. The client receives the display choice and actual shared charge; the
framebuffer contains lit cyan pixels in one correctly cropped SINGLE atlas strip. A second
activation restores the normal casing. Normal right-click opens the original GUI. RF is unchanged
by these display actions. GUI packets, side modes, limits, stale window rejection, NEI tooltip
color/light stability and the actual NEI crafting screen are checked again; no OpenGL error is reported.

The exact reobfuscated distribution JAR runs in a fresh native standalone Forge server and a
second JVM loading that saved world. The native suite checks default/legacy missing-tag display
behavior, per-face toggles, NBT serialization, charged harvest-item replacement and display
choice across a real server restart. It also repeats all bank energy, shape, shared-budget,
partial-receiver, split/merge, true chunk unload/reload, 200-bank, crafting and Quarry tests.

The release audit requires every RF ledger/network, recipe, GUI and unrelated machine class
to be byte-identical to the sealed 2.3.0 distribution. Every PNG and license/credit file also
remains identical: the fix is in how and when the original art is rendered. Only the capacitor
renderer, activation, local display NBT and version metadata change. All 78 existing unit tests pass.

Previous full HBM, Thermal Dynamics, Ender IO cable and original subsystem regression evidence
is retained in [2.3.0 verification](TESTING-2.3.0.md), with 472 successful recorded checks. Those
full suites were not rerun for this appearance patch. The new client compares actual Ender IO
blocks but uses development builds; the native standalone server tests the exact packaged bytes.

The Ender IO project documents optional exterior side displays in its
[Capacitor Banks wiki](https://github.com/SleepyTrousers/EnderIO-1.5-1.12/wiki/Capacitor-Banks).
The installed renderer and FillGauge UV mapping were inspected directly. Its SINGLE meter uses
the first quarter of a four-part atlas, at 0.25 block width and 0.8 block height.

Reports and inspected screenshots: `docs/test-results/2.3.1/`. Audit: `tools/audit_release.py`.
No installed Minecraft mod or human user's saved world was changed.
