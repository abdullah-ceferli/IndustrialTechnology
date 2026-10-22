package dev.industrialtechnology.space;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;import cpw.mods.fml.common.gameevent.TickEvent;import net.minecraftforge.event.world.*;import net.minecraftforge.event.entity.living.*;import net.minecraft.entity.*;import net.minecraft.entity.player.*;import net.minecraft.entity.passive.EntityAnimal;import net.minecraft.item.ItemStack;import net.minecraft.util.*;import net.minecraft.nbt.NBTTagCompound;import dev.industrialtechnology.core.IndustrialTechnology;
public final class SpaceEvents {
    private static final DamageSource VACUUM=new DamageSource("space_vacuum").setDamageBypassesArmor();private static final DamageSource THERMAL=new DamageSource("space_thermal").setDamageBypassesArmor();
    @SubscribeEvent public void server(TickEvent.ServerTickEvent e){if(e.phase==TickEvent.Phase.START){Atmosphere.budget=SpaceConfig.globalRoomBudget;FacilityIndex.budget=512;}}
    @SubscribeEvent public void world(TickEvent.WorldTickEvent e){if(e.phase==TickEvent.Phase.END&&!e.world.isRemote){DimensionActivity.tick(e.world);FacilityIndex.tick(e.world);}}
    @SubscribeEvent public void chunkUnload(ChunkEvent.Unload e){Atmosphere.changed(e.world);}
    @SubscribeEvent public void unload(WorldEvent.Unload e){DimensionActivity.unload(e.world);Atmosphere.unload(e.world);Airlock.unload(e.world);}
    @SubscribeEvent public void place(BlockEvent.PlaceEvent e){Atmosphere.changed(e.world);}
    @SubscribeEvent public void breakBlock(BlockEvent.BreakEvent e){Atmosphere.changed(e.world);}
    @SubscribeEvent public void living(LivingEvent.LivingUpdateEvent e){EntityLivingBase b=e.entityLiving;int planet=SpaceDimensions.planet(b.dimension);if(planet<0)return;
        double g=SpaceDimensions.gravity(b.dimension);boolean supported=b instanceof EntityPlayer||b instanceof EntityAnimal;
        boolean flying=b instanceof EntityPlayer&&((EntityPlayer)b).capabilities.isFlying;
        if(supported&&!b.onGround&&!flying&&!b.isInWater()&&!b.handleLavaMovement()&&!b.isOnLadder()&&b.ridingEntity==null&&b.motionY<.5)b.motionY+=.08*(1-g);
        if(b.worldObj.isRemote||!(b instanceof EntityPlayerMP)||b.ticksExisted%20!=0)return;EntityPlayerMP p=(EntityPlayerMP)b;int ambientPressure=Atmosphere.pressure(p.worldObj,(int)Math.floor(p.posX),(int)Math.floor(p.posY+p.getEyeHeight()),(int)Math.floor(p.posZ));boolean cabinAir=false,safe=ambientPressure>=80;if(p.ridingEntity instanceof EntitySpaceRocket){TileSpaceMachine cabin=((EntitySpaceRocket)p.ridingEntity).control;if(cabin.tanks[2].getFluidAmount()>0&&cabin.tanks[2].getFluid().getFluid()==SpaceRegistry.air){cabin.tanks[2].drain(1,true);safe=true;cabinAir=true;}}ItemStack chest=p.inventory.armorItemInSlot(2);int air=ItemSpaceSuit.air(chest),tier=ItemSpaceSuit.fullTier(p);
        NBTTagCompound n=p.getEntityData();int exposed=n.getInteger("IndustrialSpaceExposure");String warning="";
        if(!safe&&!p.capabilities.isCreativeMode){if(tier>=0&&air>0){ItemSpaceSuit.setAir(chest,air-20);exposed=0;if(air<=1200)warning="AIR LOW: "+Math.max(0,air/20)+" seconds";if(p.ticksExisted%(1200*(tier+1))==0)for(ItemStack part:p.inventory.armorInventory)if(part!=null)part.damageItem(1,p);}else{exposed+=20;warning=tier<0?"VACUUM: full suit required":"AIR EMPTY: find pressure or refill";if(exposed>200)p.attackEntityFrom(VACUUM,1);if(tier<0&&exposed>300&&p.ticksExisted%40==0)p.attackEntityFrom(THERMAL,1);}}else exposed=0;
        if(safe&&!cabinAir&&ambientPressure<85)warning="ROOM PRESSURE LOW: "+ambientPressure+"%";n.setInteger("IndustrialSpaceExposure",exposed);if(!warning.isEmpty()&&p.ticksExisted%100==0)p.addChatMessage(new ChatComponentText(warning));
        NBTTagCompound hud=new NBTTagCompound();hud.setInteger("Planet",planet);hud.setInteger("Air",ItemSpaceSuit.air(chest)/20);hud.setInteger("AirMax",ItemSpaceSuit.capacity(chest)/20);hud.setInteger("Pressure",cabinAir?100:ambientPressure);hud.setString("Warning",warning);hud.setBoolean("Breathable",safe);IndustrialTechnology.network.sendTo(new SpaceState(-1,hud),p);p.inventory.markDirty();
    }
    @SubscribeEvent public void fall(LivingFallEvent e){int planet=SpaceDimensions.planet(e.entityLiving.dimension);if(planet>=0&&(e.entityLiving instanceof EntityPlayer||e.entityLiving instanceof EntityAnimal))e.distance*=SpaceDimensions.gravity(e.entityLiving.dimension);}
}
