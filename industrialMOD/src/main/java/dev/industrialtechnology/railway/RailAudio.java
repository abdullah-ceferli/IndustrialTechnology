package dev.industrialtechnology.railway;
import net.minecraft.world.World;
public final class RailAudio {private static World world;private static long second=Long.MIN_VALUE;private static int used;public static void unload(World w){if(world==w){world=null;used=0;}}public static void play(EntityRollingStock e,String name,float volume,float pitch){long s=e.worldObj.getTotalWorldTime()/20;if(world!=e.worldObj||second!=s){world=e.worldObj;second=s;used=0;}if(used++>=24)return;e.worldObj.playSound(e.posX,e.posY,e.posZ,"industrialtech:"+name,volume,pitch,false);}}
