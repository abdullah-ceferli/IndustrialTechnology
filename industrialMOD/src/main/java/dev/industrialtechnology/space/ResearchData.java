package dev.industrialtechnology.space;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
/** Progress belongs to the physical HDD, never to a player or GUI. */
public final class ResearchData {
    private ResearchData(){}
    public static NBTTagCompound data(ItemStack s){if(s==null||s.getItem()!=SpaceRegistry.hdd)return null;if(!s.hasTagCompound())s.setTagCompound(new NBTTagCompound());return s.getTagCompound();}
    public static int required(int planet){return planet==1?SpaceConfig.marsResearch:SpaceConfig.moonResearch;}
    public static int progress(ItemStack s,int planet){NBTTagCompound n=s!=null&&s.getItem()==SpaceRegistry.hdd&&s.hasTagCompound()?s.getTagCompound():null;return n==null?0:n.getBoolean(planet==1?"MarsResearched":"MoonResearched")?required(planet):Math.max(0,Math.min(required(planet),n.getInteger(planet==1?"MarsTicks":"MoonTicks")));}
    public static boolean complete(ItemStack s,int planet){return progress(s,planet)>=required(planet);}
    public static boolean advance(ItemStack s,int planet){NBTTagCompound n=data(s);if(n==null||planet<0||planet>1||planet==1&&!complete(s,0))return false;int old=progress(s,planet);if(old>=required(planet))return false;n.setInteger(planet==1?"MarsTicks":"MoonTicks",old+1);if(old+1>=required(planet))n.setBoolean(planet==1?"MarsResearched":"MoonResearched",true);n.setInteger("SpaceDataVersion",1);return true;}
}
