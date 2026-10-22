package dev.industrialtechnology.wireless;
import java.util.*;import net.minecraft.tileentity.TileEntity;import net.minecraft.world.World;import net.minecraft.block.Block;import net.minecraftforge.common.util.ForgeDirection;
/** Inspected HBM small-tank proxy arrows; finite walk, loaded cells only, exact native target class. */
public final class HbmFluidAccess {
    public static TileEntity storage(TileEntity port){if(port==null)return null;String type=port.getClass().getName();if(type.equals("com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank"))return port;
        if(!type.equals("com.hbm.tileentity.TileEntityProxyCombo"))return null;try{if(!HbmMonitoring.allows(port,"fluid"))return null;}catch(ReflectiveOperationException|LinkageError e){return null;}
        World w=port.getWorldObj();if(w==null||!WirelessAccess.loaded(w,port.xCoord,port.yCoord,port.zCoord))return null;Block block=w.getBlock(port.xCoord,port.yCoord,port.zCoord);if(!block.getClass().getName().equals("com.hbm.blocks.machine.MachineFluidTank"))return null;
        int x=port.xCoord,y=port.yCoord,z=port.zCoord;Set<Long> seen=new HashSet<Long>();for(int i=0;i<32;i++){
            if(Math.abs(x-port.xCoord)>8||Math.abs(y-port.yCoord)>8||Math.abs(z-port.zCoord)>8||!WirelessAccess.loaded(w,x,y,z)||w.getBlock(x,y,z)!=block||!seen.add(dev.industrialtechnology.space.RoomScanner.key(x,y,z)))return null;
            int meta=w.getBlockMetadata(x,y,z);if(meta>=12){TileEntity t=w.getTileEntity(x,y,z);return t!=null&&t.getClass().getName().equals("com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank")?t:null;}
            ForgeDirection d=ForgeDirection.getOrientation(meta>=6?meta-6:meta).getOpposite();if(d==ForgeDirection.UNKNOWN)return null;x+=d.offsetX;y+=d.offsetY;z+=d.offsetZ;
        }return null;
    }
    private HbmFluidAccess(){}
}
