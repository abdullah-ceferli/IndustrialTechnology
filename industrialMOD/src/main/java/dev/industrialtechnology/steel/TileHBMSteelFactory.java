package dev.industrialtechnology.steel;
import api.hbm.energymk2.*;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/** X5771 HE receiver. No HBM classes are packaged with Industrial Technology. */
public final class TileHBMSteelFactory extends TileSteelFactory implements IEnergyReceiverMK2 {
    @Override public boolean isLoaded(){return worldObj!=null&&!isInvalid()&&worldObj.blockExists(xCoord,yCoord,zCoord)&&worldObj.getTileEntity(xCoord,yCoord,zCoord)==this;}
    @Override public boolean canConnect(ForgeDirection side){return side!=ForgeDirection.UNKNOWN;}
    @Override public long transferPower(long input){if(input<=0||!isLoaded()||worldObj.isRemote)return input;long accepted=Math.min(input,MAX_HE-power);setPower(power+accepted);return input-accepted;}
    @Override public long getReceiverSpeed(){return 20000;}
    @Override public boolean allowDirectProvision(){return true;}
    @Override public ConnectionPriority getPriority(){return ConnectionPriority.NORMAL;}
    @Override public void trySubscribe(World w,DirPos p){trySubscribe(w,p.getX(),p.getY(),p.getZ(),p.getDir());}
    @Override public void trySubscribe(World w,int x,int y,int z,ForgeDirection d){
        if(w==null||w.isRemote||!w.blockExists(x,y,z))return;
        TileEntity t=w.getTileEntity(x,y,z);if(!(t instanceof IEnergyConductorMK2)||!((IEnergyConductorMK2)t).canConnect(d.getOpposite()))return;
        Nodespace.PowerNode node=Nodespace.getNode(w,x,y,z);if(node!=null&&node.net instanceof PowerNetMK2)((PowerNetMK2)node.net).addReceiver(this);
    }
    @Override public void tryUnsubscribe(World w,int x,int y,int z){if(w==null||!w.blockExists(x,y,z))return;Nodespace.PowerNode node=Nodespace.getNode(w,x,y,z);if(node!=null&&node.net instanceof PowerNetMK2)((PowerNetMK2)node.net).removeReceiver(this);}
    @Override protected void connect(){for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS)trySubscribe(worldObj,xCoord+d.offsetX,yCoord+d.offsetY,zCoord+d.offsetZ,d);}
    private void detach(){if(worldObj!=null&&!worldObj.isRemote)for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS)tryUnsubscribe(worldObj,xCoord+d.offsetX,yCoord+d.offsetY,zCoord+d.offsetZ);}
    @Override public void invalidate(){detach();super.invalidate();}
    @Override public void onChunkUnload(){detach();super.onChunkUnload();}
}
