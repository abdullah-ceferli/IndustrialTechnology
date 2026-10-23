package dev.industrialtechnology.core;

import java.io.File;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityEvent;

/** Read old player saves before the login packet can reference a removed dimension. */
public final class RetiredDimensionRecovery {
    private final int moon, mars;

    public RetiredDimensionRecovery(File mainConfig) {
        int oldMoon = 180, oldMars = 181;
        File legacy = new File(mainConfig.getParentFile(), "industrialtech-space.cfg");
        if (legacy.isFile()) {
            Configuration config = new Configuration(legacy);
            config.load();
            oldMoon = config.getInt("moonDimension", "dimensions", 180, 2, 65535, "");
            oldMars = config.getInt("marsDimension", "dimensions", 181, 2, 65535, "");
            // Deliberately do not save or create the retired configuration.
        }
        moon = oldMoon;
        mars = oldMars;
    }

    @SubscribeEvent public void constructing(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer && event.entity.worldObj != null && !event.entity.worldObj.isRemote) {
            event.entity.registerExtendedProperties("industrialtech_retired_dimension_recovery", new PlayerRecovery());
        }
    }

    @SubscribeEvent public void connected(cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent event) {
        // Forge reads raw saved NBT for ServerHello before normal player loading.
        // Clear only a stale retired override; the corrected vanilla join packet then wins.
        cpw.mods.fml.common.network.handshake.NetworkDispatcher dispatcher = cpw.mods.fml.common.network.handshake.NetworkDispatcher.get(event.manager);
        if (dispatcher == null) return;
        int dimension = dispatcher.getOverrideDimension(new net.minecraft.network.play.server.S01PacketJoinGame());
        if ((dimension == moon || dimension == mars) && !DimensionManager.isDimensionRegistered(dimension)) {
            dispatcher.setOverrideDimension(0);
        }
    }

    private final class PlayerRecovery implements IExtendedEntityProperties {
        private Entity player;
        public void init(Entity entity, World world) { player = entity; }
        public void saveNBTData(NBTTagCompound data) { }
        public void loadNBTData(NBTTagCompound data) {
            int dimension = player.dimension;
            if ((dimension != moon && dimension != mars) || DimensionManager.isDimensionRegistered(dimension)) return;
            WorldServer overworld = DimensionManager.getWorld(0);
            if (overworld == null) return;
            ChunkCoordinates spawn = overworld.provider.getRandomizedSpawnPoint();
            double x = spawn.posX + .5D, z = spawn.posZ + .5D;
            double y = Math.max(1, overworld.getTopSolidOrLiquidBlock(spawn.posX, spawn.posZ));
            player.dimension = 0;
            player.setWorld(overworld);
            player.setPosition(x, y, z);
            player.prevPosX = player.lastTickPosX = x;
            player.prevPosY = player.lastTickPosY = y;
            player.prevPosZ = player.lastTickPosZ = z;
            player.motionX = player.motionY = player.motionZ = 0;
            player.fallDistance = 0;
            data.setInteger("Dimension", 0);
            data.setTag("Pos", doubles(x, y, z));
            data.setTag("Motion", doubles(0, 0, 0));
            data.setFloat("FallDistance", 0);
            data.setBoolean("Sleeping", false);
            String mount = data.getCompoundTag("Riding").getString("id");
            if ("industrialtech.space_rocket".equals(mount) || "industrialtech:space_rocket".equals(mount)) data.removeTag("Riding");
            IndustrialTechnology.log.info("Returned player from retired dimension {} to the Overworld before login.", dimension);
        }
    }

    private static NBTTagList doubles(double x, double y, double z) {
        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagDouble(x));
        list.appendTag(new NBTTagDouble(y));
        list.appendTag(new NBTTagDouble(z));
        return list;
    }
}
