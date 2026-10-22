package dev.industrialtechnology.space;

import java.util.*;
import api.hbm.fluidmk2.*;
import com.hbm.inventory.fluid.*;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.uninos.*;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.tileentity.TileEntity;

/** Optional real HBM network endpoint. This class is only linked when HBM loads. */
public final class HbmPipeAdapter {
    private static final FluidTank[] EMPTY=new FluidTank[0];
    private static final class State {final Set<NodeNet> nets=new HashSet<NodeNet>();long tick=Long.MIN_VALUE;int received;FluidTank[] views;}
    private static State state(TileSpaceMachine t){if(!(t.hbmPipeState instanceof State))t.hbmPipeState=new State();return (State)t.hbmPipeState;}
    public static boolean accepts(TileSpaceMachine t,FluidType f){return (t.kind==SpaceRegistry.LAUNCH||t.kind==SpaceRegistry.PORT)&&(f==Fluids.KEROSENE||f==Fluids.OXYGEN||f==Fluids.AIR);}
    private static int index(FluidType f){return f==Fluids.KEROSENE?0:f==Fluids.OXYGEN?1:f==Fluids.AIR?2:-1;}
    private static net.minecraftforge.fluids.Fluid own(int i){return i==0?SpaceRegistry.kerosene:i==1?SpaceRegistry.lox:SpaceRegistry.air;}
    private static boolean allowed(TileSpaceMachine t,FluidType f){if(!t.isLoaded()||t.getWorldObj().isRemote||!t.enabled||t.owner.isEmpty()||!accepts(t,f)||!SpaceProtection.interact(t,t.xCoord,t.yCoord,t.zCoord))return false;
        for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS){int x=t.xCoord+d.offsetX,y=t.yCoord+d.offsetY,z=t.zCoord+d.offsetZ;if(!t.loaded(x,y,z))continue;TileEntity p=t.getWorldObj().getTileEntity(x,y,z);if(p instanceof IFluidPipeMK2&&((IFluidPipeMK2)p).canConnect(f,d.getOpposite())&&SpaceProtection.interact(t,x,y,z)){GenNode n=UniNodespace.getNode(t.getWorldObj(),x,y,z,f.getNetworkProvider());if(n!=null&&n.hasValidNet()&&state(t).nets.contains(n.net))return true;}}return false;}
    public static void tick(TileSpaceMachine t){if(!accepts(t,Fluids.KEROSENE)||t.getWorldObj().getTotalWorldTime()%20!=0)return;State s=state(t);Set<NodeNet> next=new HashSet<NodeNet>();if(t.enabled&&!t.owner.isEmpty())for(FluidType f:new FluidType[]{Fluids.KEROSENE,Fluids.OXYGEN,Fluids.AIR})for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS){int x=t.xCoord+d.offsetX,y=t.yCoord+d.offsetY,z=t.zCoord+d.offsetZ;if(!t.loaded(x,y,z)||!SpaceProtection.interact(t,x,y,z))continue;TileEntity p=t.getWorldObj().getTileEntity(x,y,z);if(!(p instanceof IFluidPipeMK2)||!((IFluidPipeMK2)p).canConnect(f,d.getOpposite()))continue;GenNode n=UniNodespace.getNode(t.getWorldObj(),x,y,z,f.getNetworkProvider());if(n!=null&&n.hasValidNet()){n.net.addReceiver(t);next.add(n.net);}}
        for(NodeNet n:s.nets)if(!next.contains(n))n.removeReceiver(t);s.nets.clear();s.nets.addAll(next);
    }
    public static long demand(TileSpaceMachine t,FluidType f,int pressure){if(pressure!=0||!allowed(t,f))return 0;State s=state(t);long now=t.getWorldObj().getTotalWorldTime();if(s.tick!=now){s.tick=now;s.received=0;}int i=index(f);return Math.max(0,Math.min(1000-s.received,t.fill(ForgeDirection.UNKNOWN,new FluidStack(own(i),1000),false)));}
    /** HBM's API returns the unaccepted remainder, not the accepted amount. */
    public static long receive(TileSpaceMachine t,FluidType f,int pressure,long amount){if(amount<=0)return amount;int n=(int)Math.min(amount,demand(t,f,pressure));if(n<=0)return amount;int accepted=t.fill(ForgeDirection.UNKNOWN,new FluidStack(own(index(f)),n),true);state(t).received+=accepted;return amount-accepted;}
    public static FluidTank[] views(TileSpaceMachine t){if(!accepts(t,Fluids.KEROSENE))return EMPTY;State s=state(t);if(s.views==null)s.views=new FluidTank[]{new FluidTank(Fluids.KEROSENE,SpaceConfig.tankCapacity),new FluidTank(Fluids.OXYGEN,SpaceConfig.tankCapacity),new FluidTank(Fluids.AIR,SpaceConfig.tankCapacity)};for(int i=0;i<3;i++)s.views[i].setFill(t.tanks[i].getFluidAmount());return s.views;}
    public static void disconnect(TileSpaceMachine t){if(!(t.hbmPipeState instanceof State))return;State s=(State)t.hbmPipeState;for(NodeNet n:s.nets)n.removeReceiver(t);s.nets.clear();t.hbmPipeState=null;}
}
