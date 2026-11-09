# Offline Skins — Minecraft 1.7.10

Choose your own PNG skin for an offline Minecraft account. Version **1.2.0** adds
**automatic skin sharing in worlds opened to LAN**. Both players install the mod
and choose a PNG; the host and guests see each other's skins through the game
connection, including live changes. No separate skin service is needed for LAN.
Accepted-friends sharing on ordinary servers without the mod remains available.
Works with Forge 10.13.4.1614 and Java 8. No extra Minecraft mod dependencies.

## Install and choose a skin

1. Put `OfflineSkins-1.7.10-1.2.0.jar` from `build/libs/` in Minecraft's `mods` folder.
2. Start Minecraft once. The mod creates `config/offlineskins/skins/`.
3. Put your skin PNG in that folder. Naming it **`skin.png`** makes it the default.
4. Press **F8** to open the chooser, click **Refresh**, then click the PNG filename.
   You can also use **Mods → Offline Skins → Config** from the main menu.
5. Play any world. F5, the inventory player model, and your first-person hand use
   the selected skin. Selecting a skin does not require cheats or operator access.

The **Open skin folder** button opens the correct directory. You may keep several
PNGs there and switch between them. F8 is configurable in Options → Controls.
The selection is saved in `config/offlineskins/client.cfg`.

### Commands

| Command | Action |
| --- | --- |
| `/offlineskin` | Open the skin chooser |
| `/offlineskin folder` | Open the skin folder |
| `/offlineskin reload` | Reload the selected file after editing/replacing it |
| `/offlineskin use my skin.png` | Select a PNG, including names with spaces |
| `/offlineskin clear` | Restore the normal skin and tell other modded players |
| `/offlineskin friends` | Open friends, requests and the service connection |
| `/offlineskin connect address` | Connect to a friends service (the GUI supports longer addresses) |
| `/offlineskin request code` | Send a request to a public friend code |
| `/offlineskin accept code` | Accept an incoming request |
| `/offlineskin remove code` | Remove a friend and stop skin access |
| `/offlineskin disconnect` | Stop the service connection; remain in private mode |
| `/offlineskin server` | Explicitly return to normal server-mod sharing |

## Multiplayer

### Automatic Open to LAN sharing

1. Install **1.2.0 on the host and every player who wants to see the skins**.
   Replace the previous Offline Skins JAR; keep your PNGs and configuration.
2. Each player puts their PNG in `config/offlineskins/skins/` on their own device
   and chooses it with F8 or `/offlineskin use filename.png`.
3. The host opens the world to LAN. Guests join its usual IP and game port.

The host's skin becomes shareable as soon as Open to LAN is enabled. Guests
receive it when joining, and both sides receive later changes and skin-clear
operations automatically. The chooser shows **LAN skin sharing is active**.
Previously selected Friends mode does not require reconfiguration: LAN sharing
temporarily takes priority while the external friends service is paused.
On leaving LAN, the saved friends/server preference is restored.

Automatic LAN detection requires the host's LAN greeting and a local/private
connection address, or the host's own integrated connection. An Internet server
cannot enable automatic LAN sharing by sending that greeting alone. Normal
single-player keeps your skin local, with the friends service paused.

The same Forge JAR is intended for PC and Java Edition launchers on Android;
the Android client still needs a compatible Forge installation and matching
gameplay mods. This mod does not change Minecraft's account authentication or
your EasyLAN settings.

### Friends mode: no Minecraft server mod required

Use **F8 → Friends**. Run the included **friends-service/Start-for-Friends.cmd** on
one PC, connect both clients to that same reachable service address, share your
friend codes, and choose **Incoming → Accept**. Both players then receive skin
changes automatically, normally within about three seconds. Sending a request
alone gives no skin access. Removing a friend restores the normal skin and
prevents future downloads. Other, unaccepted players cannot download your PNG.

This works with a server hosted on Aternos without installing our mod on that
Minecraft server. It requires a separate reachable service; the package does not
provide public hosting. See **[friends-service/README.md](friends-service/README.md)**
for connection and internet/LAN hosting instructions. No Minecraft chat is used
for transmitting skin files.

Friends mode never broadcasts your PNG through an installed Minecraft server
skin mod. Service failure/disconnection does not silently enable server sharing.
Your own chosen skin and accepted friends persist between sessions.

**Single-player and Open to LAN:** friends service uploads, polling, downloads and
requests are paused. LAN players use automatic game-connection sharing instead.
The Friends screen on the main menu can connect/manage friends. Joining an
ordinary multiplayer server resumes accepted-friends syncing.

### Original server-mod mode

Install the **same release JAR** in the server's `mods/` directory and on all
clients that should see the custom skins. Restart the server after installation.
Each player chooses their own PNG on their own computer. The client sends it
once after joining and again when it changes; the server validates it and shares
it with the other modded clients. Late joiners receive existing players' skins.
Skins continue to work after death, changing dimensions, and reconnecting.
Choose **Use server sharing** in the Friends screen for this original mode.
The host's skin stays local in private single-player and is shared after Open to LAN.

Without either a service connection or the server mod, your selected skin works
locally. Players without the client mod see normal Minecraft skins. Server
authentication settings are not changed by this mod.

The server stores validated skins in `config/offlineskins/server-skins/<UUID>.png`.
Client uploads can only set the sending player's own skin. The mod does not read
or change blocks, items, player inventories, or world saves.

## Skin format

- PNG only, **64×32** or **64×64** pixels, at most **24 KiB**.
- Minecraft 1.7.10's classic **Steve** player model (4-pixel-wide arms).
- A 64×64 file is converted to the legacy top-half layout. The head and hat,
  torso, right arm, and right leg are used; the arm and leg are mirrored on the
  left as in vanilla 1.7.10.
- Modern separate left-limb textures, jacket/sleeve/trouser overlays, and the
  3-pixel Alex model are not supported. Export a classic Steve skin for an exact fit.
- Vanilla legacy skin transparency rules apply, including removal of an entirely
  opaque unused hat area. Capes are not changed.
- Pick another file or click Disable custom skin to restore the normal appearance.
  An invalid replacement file leaves an already loaded working skin in place.

PNGs stay outside the JAR so you can replace them without rebuilding the mod.
No skin artwork is included. Use a skin you own or have permission to use.

## Performance and network behavior

Local skin images are read only on startup, selection, or explicit reload. PNG data is
sent only on joining or changing a skin, not every frame/tick. The mod uses the
existing player renderer, does not add coremods/mixins, and never requests chunk
rebuilds. GL textures are updated on the client thread, and server state is handled
on the server tick. Pending uploads are coalesced and rate limited to one accepted
update per player per second. Incoming PNG dimensions and packet lengths are
validated before allocating image buffers.
Friends-service HTTP and PNG decoding run in one background worker. Small friend
lists refresh about every three seconds while active; unchanged PNGs are not
downloaded again. Only nearby players' textures are uploaded to the GPU. No
network operation runs on the render thread. Friends mode has no service traffic
in ordinary single-player or detected Open to LAN sessions. LAN PNGs travel only
on connection or a skin change, with no additional per-frame network traffic.

## Build from source

Use a **Java 8 JDK** (not just a JRE):

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk8'
.\gradlew.bat test build sourcesJar --console=plain
```

`build.ps1` uses the existing workspace's Java 8 toolchain and shared Forge cache
when available. The regular Gradle wrapper supports a separate checkout and
downloads its build dependencies on the first build.

Release, standalone friends-service, and source JARs are in `build/libs/`. The
friends service has its own `relayJar` build task and requires no third-party JARs.
Never install the service, `-sources.jar` or
`offline-skins-test-harness` JAR in your real game/server. The test harness uses
disposable worlds only. `tools/run-integration.py` is a local-workspace integration
runner; it launches two production Minecraft clients and a temporary localhost
server using the already available runtime.

MIT-licensed original mod code. Built with Minecraft Forge and the
[ForgeGradle 1.2 maintenance fork](https://github.com/anatawa12/ForgeGradle-1.2).
Minecraft belongs to Mojang/Microsoft. This is an independent mod.
