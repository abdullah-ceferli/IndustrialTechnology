package personal.offlineskins.test;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;

@Mod(modid="offlineskinstest", name="Offline Skins Test Harness", version="1", acceptableRemoteVersions="*", dependencies="after:offlineskins")
public class SkinIntegration {
    private int settle;
    @SidedProxy(clientSide="personal.offlineskins.test.ClientProbe", serverSide="personal.offlineskins.test.Probe")
    public static Probe proxy;
    @Mod.EventHandler public void init(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(this);
        proxy.init();
    }
    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event) { place(event.player); }
    @SubscribeEvent public void respawn(PlayerEvent.PlayerRespawnEvent event) { place(event.player); }
    private void place(net.minecraft.entity.player.EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) return;
        EntityPlayerMP mp = (EntityPlayerMP) player;
        mp.setSpawnChunk(new ChunkCoordinates(1, 4, 1), true);
        mp.setPositionAndUpdate(mp.getCommandSenderName().endsWith("A") ? 1.5 : 4.5, 5, 1.5);
        settle = 40;
    }
    @SubscribeEvent public void settleFixture(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || settle <= 0 || --settle != 0) return;
        // The fixture teleports logins away from their generated random spawn.
        // Refresh their spawn packets after the new destination chunks arrive.
        java.util.List players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for (Object target : players) for (Object subject : players) {
            if (target != subject) ((EntityPlayerMP)target).playerNetServerHandler.sendPacket(new S0CPacketSpawnPlayer((EntityPlayerMP)subject));
        }
    }
}
