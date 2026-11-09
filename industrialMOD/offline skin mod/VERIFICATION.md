# Verification - Offline Skins 1.2.0

Verified on 2026-11-02 for Minecraft 1.7.10 / Forge 10.13.4.1614.

- **22 automated tests passed**: PNG validation/normalization, safe skin filenames,
  bounded packet lengths, UUID identity, clear operations, the backward-compatible
  four-byte LAN greeting, local/private/IPv6 LAN addresses, rejection of Internet
  and unresolved LAN announcements, and private single-player/friends-mode policy.
- **Two real clients passed in an integrated Open to LAN world with the current
  full mod pack**, including EasyLAN, HBM, OpenComputers and OptiFine. Both clients
  had the saved friends-only preference enabled and no separate skin service.
  The host's skin stayed local in private single-player, then automatically shared
  on opening LAN. Host and guest saw each other's actual skin texture pixels.
  Both sides' live changes, clear/restore, guest reconnect, host death/respawn,
  and returning to a new private world passed. The external friends service stayed
  paused during LAN and the saved sharing preference was retained.
- **Accepted-friends regression passed with two real clients on a Forge server
  with no skin mod**, using the standalone HTTPS service. Before-acceptance
  privacy, live changes, removal/revocation, re-acceptance and reconnect passed.
- Runtime tests used the user's **Java 8u51**. Every world and client configuration
  was created under this project's ignored build directory. No main save was used.
- The LAN skin chooser was captured for visual review. Test clients closed and
  the integrated/dedicated test servers saved and stopped normally.
- Release JAR contains no test harness, Minecraft classes or bundled dependencies.
  The production class/resource bytes match all four tested clients.

Release SHA256: 12e7f70f1f6319380d1a9b501cdc74e3242bd01e456ffe5f82fa3cb87977de6b

Service SHA256: d0c49c892aa570a9277c3809d75c090245fcc04244632531c06d239036d67463

LAN evidence: build/lan-1.2.0-final-2/.
Friends evidence: build/friends-1.2.0-final/.
Unit results: build/test-results/test/.

The test skins are generated solid-color fixtures, not user artwork. Skin files,
accounts, secrets, world saves, test logs and screenshots are excluded from the
source ZIP. Classic Steve / 4-pixel arm limitations remain documented in README.

PNG uploads remain limited/coalesced to one accepted update per player per second.
LAN uses the existing game connection and sends PNGs only on join/change; the LAN
state transition causes one greeting, not a per-tick upload or chunk rebuild.

Android/Zalith was not exercised on a physical phone in this test environment.
Both devices need the same release JAR and compatible Forge/gameplay mods.
Minecraft authentication and EasyLAN settings are not changed by the skin mod.
