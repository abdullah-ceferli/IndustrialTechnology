package personal.offlineskins;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

/** Networking queues only; game state and persistence run on the server tick. */
public final class ServerSkins {
    private final File directory;
    private final Map<UUID, Session> sessions = new HashMap<UUID, Session>();
    private final ConcurrentHashMap<NetHandlerPlayServer, byte[]> pending = new ConcurrentHashMap<NetHandlerPlayServer, byte[]>();
    private long ticks;
    private boolean lan;

    private static final class Session {
        EntityPlayerMP player;
        byte[] png = new byte[0];
        boolean ready;
        long nextUpdate;
        Session(EntityPlayerMP player) { this.player = player; }
    }

    public ServerSkins(File directory) { this.directory = directory; }

    public void enqueue(NetHandlerPlayServer handler, byte[] png) {
        if (png != null && png.length <= SkinImage.MAX_BYTES) pending.put(handler, png);
    }

    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        Session session = new Session(player);
        File cached = new File(directory, player.getUniqueID().toString() + ".png");
        if (cached.isFile()) {
            try { session.png = SkinImage.read(cached).png; }
            catch (IOException ex) { OfflineSkins.log.warn("Ignoring invalid cached skin for {}", player.getUniqueID()); }
        }
        sessions.put(player.getUniqueID(), session);
        // Unmodded clients ignore this small unknown-channel message. No PNG is
        // shared with a connection until it answers this protocol greeting.
        OfflineSkins.network.sendTo(new Packets.Hello(OfflineSkins.proxy.isLanOpen()), player);
    }

    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        Session old = sessions.get(event.player.getUniqueID());
        if (old == null || old.player != event.player) return;
        sessions.remove(event.player.getUniqueID());
        pending.remove(old.player.playerNetServerHandler);
        broadcast(new Packets.Skin(event.player.getUniqueID(), new byte[0]));
    }

    @SubscribeEvent public void respawn(PlayerEvent.PlayerRespawnEvent event) { refreshPlayer(event.player); }
    @SubscribeEvent public void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { refreshPlayer(event.player); }

    private void refreshPlayer(net.minecraft.entity.player.EntityPlayer player) {
        Session session = sessions.get(player.getUniqueID());
        if (session != null && player instanceof EntityPlayerMP) session.player = (EntityPlayerMP) player;
    }

    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ticks++;
        boolean currentLan = OfflineSkins.proxy.isLanOpen();
        if (lan != currentLan) {
            lan = currentLan;
            // The host logged in before Open to LAN was clicked. Re-negotiate
            // once so its previously local-only skin is uploaded automatically.
            for (Session session : new ArrayList<Session>(sessions.values())) {
                if (session.player.playerNetServerHandler.netManager.isChannelOpen()) {
                    OfflineSkins.network.sendTo(new Packets.Hello(lan), session.player);
                }
            }
        }
        // At most one queued update per live connection, and one accepted change
        // per second. Fast repeated selections keep the latest pending value.
        for (Map.Entry<NetHandlerPlayServer, byte[]> entry : pending.entrySet()) {
            NetHandlerPlayServer handler = entry.getKey();
            Session session = sessions.get(handler.playerEntity.getUniqueID());
            if (session == null || session.player.playerNetServerHandler != handler || !handler.netManager.isChannelOpen()) {
                pending.remove(handler, entry.getValue());
                continue;
            }
            if (ticks < session.nextUpdate || !pending.remove(handler, entry.getValue())) continue;
            session.nextUpdate = ticks + 20;
            try {
                byte[] data = entry.getValue();
                byte[] clean = data.length == 0 ? data : SkinImage.decode(data).png;
                boolean first = !session.ready;
                boolean changed = !java.util.Arrays.equals(session.png, clean);
                session.png = clean;
                session.ready = true;
                if (changed) persist(session);
                if (first) {
                    for (Session other : sessions.values()) {
                        if (other.ready) OfflineSkins.network.sendTo(new Packets.Skin(other.player.getUniqueID(), other.png), session.player);
                    }
                }
                if (first || changed) broadcast(new Packets.Skin(session.player.getUniqueID(), clean));
            } catch (IOException ex) {
                OfflineSkins.log.warn("Rejected invalid skin for {}: {}", session.player.getUniqueID(), ex.getMessage());
            }
        }
    }

    private void persist(Session session) {
        try {
            Files.createDirectories(directory.toPath());
            File destination = new File(directory, session.player.getUniqueID().toString() + ".png");
            if (session.png.length == 0) { Files.deleteIfExists(destination.toPath()); return; }
            File temp = new File(directory, session.player.getUniqueID().toString() + ".png.tmp");
            Files.write(temp.toPath(), session.png);
            Files.move(temp.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            OfflineSkins.log.warn("Skin is shared, but could not be cached: {}", ex.getMessage());
        }
    }

    private void broadcast(Packets.Skin packet) {
        for (Session session : new ArrayList<Session>(sessions.values())) {
            if (session.ready && session.player.playerNetServerHandler.netManager.isChannelOpen()) {
                OfflineSkins.network.sendTo(packet, session.player);
            }
        }
    }

    public void reset() { sessions.clear(); pending.clear(); ticks = 0; lan = false; }
}
