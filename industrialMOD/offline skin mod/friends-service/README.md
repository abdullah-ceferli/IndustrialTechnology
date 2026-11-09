# Friends-only skins without a Minecraft server mod

This companion service exchanges skins only between players who **both agree to
be friends**. It works alongside a Forge 1.7.10 client on a Minecraft server that
does not have Offline Skins installed. The Minecraft server does not relay the
skin data. Every participating player needs Offline Skins **1.2.0** on their PC.

## Start the service on one PC

1. Keep `OfflineSkins-Friends-Service-1.2.0.jar`, `Start-Friends-Service.ps1`, and
   `Start-for-Friends.cmd` together in this folder.
2. Double-click **Start-for-Friends.cmd**. Keep that window running while you play.
   Java 8 with `keytool.exe` is required; Minecraft's bundled Java 8 works on this PC.
3. The first start creates a private HTTPS certificate and service data in `data/`.
4. The window prints service addresses. Copy the **whole address, including the
   `#` and certificate fingerprint**. The fingerprint verifies this specific
   service; a self-signed certificate does not require disabling certificate checks.

For a local test on one computer, run `Start-Friends-Service.ps1` without arguments.
It listens on `127.0.0.1` by default. The launcher uses `-BindAddress 0.0.0.0` so
other PCs can connect when your network allows it. No firewall or router settings
are changed by the launcher. Stop the service with **Ctrl+C**, or type **stop**.

## Let friends reach it

- **Same home network:** use the `Local network: https://192.168...` address printed
  by the launcher, with its complete `#fingerprint`.
- **Different home networks:** the service PC needs a reachable address and TCP
  port **24464**, such as port forwarding or an existing private VPN connecting
  your friends. Use that reachable address instead of the local IP, retaining the
  same port and `#fingerprint`. Router restrictions/CGNAT may prevent direct public
  hosting. You can also run the service on a host you control.
- `127.0.0.1` always means the current computer. Friends on other PCs cannot use
  your `127.0.0.1` address to reach you.

This package does not provide a public hosted service. The Minecraft/Aternos
server address is **not** the skin-service address. Do not put the companion
service JAR in the Minecraft server's or client's `mods` directory.

## Connect and accept friends in Minecraft

1. Join your multiplayer server. Press **F8 → Friends**.
2. Paste the service address into the upper field and click **Connect**. Every
   participating friend must connect to the **same service**.
3. Click **Copy code** and share your public friend code with your friend.
4. One player pastes the other's code into the lower field and clicks **Send request**.
5. The other player opens **Incoming → Accept**. Only then can both retrieve skins.
6. Choose a PNG in the normal skin chooser. Accepted friends see the change
   automatically, normally within about **3 seconds**.

**Remove** stops future skin access and restores the normal skin on the other
client after its next refresh. **Decline** and **Cancel** remove pending requests.
Sending a request alone never grants access to a skin. PNGs are not sent through
Minecraft chat or the Aternos server.

Both your chosen PNG and your service identity/friends persist between sessions.
The service must be running and reachable for automatic updates. If it becomes
unreachable, the mod stops displaying relay skins until it can verify the friend
list again; your own local skin continues to work.

## Single-player, LAN, and privacy

- **Single-player and worlds opened to LAN:** your own skin works; all friends
  syncing is paused, including uploads, polls, downloads and friend requests.
- Joining an ordinary multiplayer server enables syncing. The Friends screen on
  the **main menu** can also connect/manage friends without joining a world.
- Friends mode never uploads your skin through the Minecraft server's skin mod,
  even if that server happens to have an older Offline Skins release installed.
- **Disconnect service** stops the connection and keeps private friends mode;
  it does not silently switch to public server sharing. **Use server sharing**
  explicitly returns to the original all-modded-players server mode.
- The service stores skins and accepted friendships; run it on a PC/host you trust.
  This is consent between service identities, not Mojang account authentication.
  Client rendering matches accepted friends' Minecraft names, which offline
  Minecraft servers do not authenticate. Duplicate accepted names are ignored.
- Removing a friend blocks new downloads/updates; it cannot erase an image they
  already saw or saved.

Keep `data/` backed up on the service PC. Keep each client's
`config/offlineskins/relay-accounts/` private; those files contain login tokens.
Public friend codes and service URLs may be shared. Never send someone your whole
client config folder or the service's private data to exchange a skin.

## Performance

HTTP, certificate verification and PNG decoding use one background worker per
client. A small friend-list check runs roughly every three seconds while active.
Skin PNGs upload only on connection/change and download only when their hash
changes. The render thread only selects/caches textures for nearby players; it
never waits for network requests. No chunk rebuilds are requested.

The service uses Java 8, with a 128 MiB maximum heap in the launcher. It is meant
for a small friend group, not a public account hosting platform.
