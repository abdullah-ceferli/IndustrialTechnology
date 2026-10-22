package dev.industrialtechnology.space;
import net.minecraft.item.ItemStack;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.world.World;import net.minecraft.tileentity.TileEntity;
/** Geometry shared by preflight, rendering and the mounted passenger. */
public final class ShuttleVehicle {
    public static boolean is(ItemStack s){return s!=null&&s.hasTagCompound()&&s.getTagCompound().getBoolean("Shuttle");}
    public static boolean is(TileSpaceMachine c){return is(c.items[0]);}
    public static int facing(TileSpaceMachine c){NBTTagCompound v=RocketTravel.vehicle(c);return v==null?3:BlockSpaceMachine.front(v.getInteger("Facing"));}
    public static float yaw(TileSpaceMachine c){int f=facing(c);return f==2?180:f==4?-90:f==5?90:0;}
    public static TileLaunchFacility root(TileSpaceMachine c){if(c instanceof TileLaunchFacility)return (TileLaunchFacility)c;NBTTagCompound v=RocketTravel.vehicle(c);if(v==null||!v.hasKey("LaunchRootY"))return null;int x=v.getInteger("LaunchRootX"),y=v.getInteger("LaunchRootY"),z=v.getInteger("LaunchRootZ");if(!c.loaded(x,y,z))return null;TileEntity te=c.getWorldObj().getTileEntity(x,y,z);return te instanceof TileLaunchFacility&&((TileLaunchFacility)te).facilityId.equals(v.getString("LaunchFacility"))?(TileLaunchFacility)te:null;}
    public static int x(TileSpaceMachine c){return c instanceof TileLaunchFacility?((TileLaunchFacility)c).shipX():c.xCoord;}
    public static int y(TileSpaceMachine c){return c instanceof TileLaunchFacility?((TileLaunchFacility)c).shipY():c.yCoord+1;}
    public static int z(TileSpaceMachine c){return c instanceof TileLaunchFacility?((TileLaunchFacility)c).shipZ():c.zCoord;}
    public static void attach(TileSpaceMachine c){if(!(c instanceof TileLaunchFacility))return;TileLaunchFacility t=(TileLaunchFacility)c;NBTTagCompound v=RocketTravel.vehicle(c);if(v==null)return;v.setInteger("LaunchRootX",t.xCoord);v.setInteger("LaunchRootY",t.yCoord);v.setInteger("LaunchRootZ",t.zCoord);v.setString("LaunchFacility",t.facilityId);v.setInteger("Facing",t.facing());}
    public static boolean clear(TileSpaceMachine c){int f=c instanceof TileLaunchFacility?((TileLaunchFacility)c).facing():facing(c),x=x(c),y=y(c),z=z(c);if(y<1||y+57>256)return false;World w=c.getWorldObj();for(int a=-13;a<=13;a++)for(int b=-6;b<=23;b++)for(int h=0;h<57;h++){int xx=x+LaunchFacilityPlan.dx(a,b,f),zz=z+LaunchFacilityPlan.dz(a,b,f);if(!c.loaded(xx,y+h,zz)||!w.isAirBlock(xx,y+h,zz))return false;}return true;}
}
