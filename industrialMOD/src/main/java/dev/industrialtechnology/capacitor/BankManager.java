package dev.industrialtechnology.capacitor;

import java.util.*;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import dev.industrialtechnology.core.IndustrialTechnology;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

/** Loaded components only. No chunk tickets, no per-tick flood fills or per-tile ticking. */
public final class BankManager {
    private static final Map<World,BankManager> WORLDS=new IdentityHashMap<World,BankManager>();
    public static synchronized BankManager of(World w){BankManager m=WORLDS.get(w);if(m==null){m=new BankManager(w);WORLDS.put(w,m);}return m;}
    public static synchronized void clear(){WORLDS.clear();}
    private final World world;
    private final LoadedBankAccess powerView;
    private final Map<Long,Set<TileCapacitorBank>> byChunk=new HashMap<Long,Set<TileCapacitorBank>>();
    private final Set<Network> networks=new LinkedHashSet<Network>();
    private final Set<TileCapacitorBank> pending=new LinkedHashSet<TileCapacitorBank>();
    private final Set<Long> unloading=new HashSet<Long>();
    private boolean rebuilding;
    public long rebuilds;
    private BankManager(World w){world=w;powerView=new LoadedBankAccess(w,this);}
    public static BankManager events(){return new BankManager(null);}
    private static long chunkKey(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
    boolean loaded(int x,int z){return !unloading.contains(chunkKey(x>>4,z>>4))&&world.getChunkProvider().chunkExists(x>>4,z>>4);}
    public TileCapacitorBank at(int x,int y,int z){
        if(y<0||y>255||!loaded(x,z))return null;
        TileEntity te=world.getTileEntity(x,y,z);return te instanceof TileCapacitorBank&&!te.isInvalid()&&!((TileCapacitorBank)te).claimed?(TileCapacitorBank)te:null;
    }
    private boolean available(TileCapacitorBank t){return t.attached&&!t.isInvalid()&&loaded(t.xCoord,t.zCoord)&&at(t.xCoord,t.yCoord,t.zCoord)==t;}
    private void remember(TileCapacitorBank t){t.attached=true;long key=chunkKey(t.xCoord>>4,t.zCoord>>4);Set<TileCapacitorBank> set=byChunk.get(key);if(set==null){set=new HashSet<TileCapacitorBank>();byChunk.put(key,set);}set.add(t);}
    public void attach(TileCapacitorBank t){if(world.isRemote)return;if(!t.attached){remember(t);pending.add(t);for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS){TileCapacitorBank n=at(t.xCoord+d.offsetX,t.yCoord+d.offsetY,t.zCoord+d.offsetZ);if(n!=null)invalidate(n.network);}}}
    public void detach(TileCapacitorBank t){Set<TileCapacitorBank> set=byChunk.get(chunkKey(t.xCoord>>4,t.zCoord>>4));if(set!=null){set.remove(t);if(set.isEmpty())byChunk.remove(chunkKey(t.xCoord>>4,t.zCoord>>4));}t.attached=false;pending.remove(t);invalidate(t.network);t.network=null;}
    public void invalidate(Network n){if(n==null||!n.valid)return;n.valid=false;networks.remove(n);pending.addAll(n.members);}
    public void changed(TileCapacitorBank t){
        if(world.isRemote)return;attach(t);boolean topology=false;
        for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS){TileCapacitorBank n=at(t.xCoord+d.offsetX,t.yCoord+d.offsetY,t.zCoord+d.offsetZ);if(t.adjacent[d.ordinal()]!=n){t.adjacent[d.ordinal()]=n;topology=true;}}
        if(topology){invalidate(t.network);pending.add(t);}
        boolean portsChanged=false;
        for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS){int x=t.xCoord+d.offsetX,y=t.yCoord+d.offsetY,z=t.zCoord+d.offsetZ;TileEntity te=y>=0&&y<=255&&loaded(x,z)?world.getTileEntity(x,y,z):null;if(t.neighbors[d.ordinal()]!=te){t.neighbors[d.ordinal()]=te;portsChanged=true;}}
        boolean power=powerView.powered(t.xCoord,t.yCoord,t.zCoord);
        if(t.network!=null&&t.network.valid){if(portsChanged)t.network.portsDirty=true;if(power!=t.powered)t.network.powered+=power?1:-1;}t.powered=power;
    }
    public Network ensure(TileCapacitorBank t){if(!loaded(t.xCoord,t.zCoord)||at(t.xCoord,t.yCoord,t.zCoord)!=t)return null;attach(t);if(!available(t))return null;if(t.network==null||!t.network.valid)rebuild();return t.network!=null&&t.network.valid?t.network:null;}
    private void rebuild(){
        if(rebuilding)return;
        // A receiver callback may change blocks. Finish the RF transaction before reconstructing.
        for(TileCapacitorBank p:pending)if(p.network!=null&&p.network.busy)return;
        rebuilding=true;
        try{while(!pending.isEmpty()){
            TileCapacitorBank seed=pending.iterator().next();pending.remove(seed);if(!available(seed))continue;
            if(seed.network!=null&&seed.network.valid)continue;
            List<TileCapacitorBank> members=new ArrayList<TileCapacitorBank>();Set<TileCapacitorBank> seen=Collections.newSetFromMap(new IdentityHashMap<TileCapacitorBank,Boolean>());
            ArrayDeque<TileCapacitorBank> queue=new ArrayDeque<TileCapacitorBank>();queue.add(seed);seen.add(seed);
            while(!queue.isEmpty()){
                TileCapacitorBank t=queue.remove();members.add(t);pending.remove(t);
                if(t.network!=null&&t.network.valid)invalidate(t.network);
                for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS){TileCapacitorBank n=at(t.xCoord+d.offsetX,t.yCoord+d.offsetY,t.zCoord+d.offsetZ);t.adjacent[d.ordinal()]=n;if(n!=null){remember(n);if(seen.add(n))queue.add(n);}}
            }
            pending.removeAll(members);
            Collections.sort(members,(a,b)->a.xCoord!=b.xCoord?Integer.compare(a.xCoord,b.xCoord):a.yCoord!=b.yCoord?Integer.compare(a.yCoord,b.yCoord):Integer.compare(a.zCoord,b.zCoord));
            Network n=new Network(this,members);for(TileCapacitorBank t:members)t.network=n;networks.add(n);rebuilds++;
        }}finally{rebuilding=false;}
    }
    public void tick(){
        // Event handlers run before the provider removes a chunk. Retain that guard only until removal.
        if(!unloading.isEmpty()){Iterator<Long> it=unloading.iterator();while(it.hasNext()){long key=it.next();if(!world.getChunkProvider().chunkExists((int)(key>>32),(int)key))it.remove();}}
        rebuild();long now=world.getTotalWorldTime();for(Network n:new ArrayList<Network>(networks))if(n.valid)n.tick(now);}
    private void chunk(Chunk c,boolean load){
        long key=chunkKey(c.xPosition,c.zPosition);if(load)unloading.remove(key);else unloading.add(key);
        for(Object o:new ArrayList<Object>(c.chunkTileEntityMap.values()))if(o instanceof TileCapacitorBank){TileCapacitorBank t=(TileCapacitorBank)o;if(load){attach(t);changed(t);}else detach(t);}
        // Only banks in this chunk and its neighbors can gain or lose an RF/redstone neighbor.
        for(int cx=c.xPosition-1;cx<=c.xPosition+1;cx++)for(int cz=c.zPosition-1;cz<=c.zPosition+1;cz++){
            Set<TileCapacitorBank> local=byChunk.get(chunkKey(cx,cz));if(local==null)continue;
            for(TileCapacitorBank t:new ArrayList<TileCapacitorBank>(local))if(available(t)){
                boolean power=powerView.powered(t.xCoord,t.yCoord,t.zCoord);
                if(t.network!=null&&t.network.valid){t.network.portsDirty=true;if(power!=t.powered)t.network.powered+=power?1:-1;}t.powered=power;
            }
        }
    }
    @SubscribeEvent public void worldTick(TickEvent.WorldTickEvent e){if(e.phase==TickEvent.Phase.END&&!e.world.isRemote)of(e.world).tick();}
    @SubscribeEvent public void load(ChunkEvent.Load e){if(!e.world.isRemote)of(e.world).chunk(e.getChunk(),true);}
    @SubscribeEvent public void unload(ChunkEvent.Unload e){if(!e.world.isRemote)of(e.world).chunk(e.getChunk(),false);}
    @SubscribeEvent public void unloadWorld(WorldEvent.Unload e){synchronized(BankManager.class){WORLDS.remove(e.world);}}

    public static final class Network {
        public final List<TileCapacitorBank> members;
        public final BankLedger ledger;
        private final BankManager manager;
        public boolean valid=true,busy,portsDirty=true;
        private final List<Port> ports=new ArrayList<Port>();
        private int portCursor,powered,lastDisplay=-1;
        private long visualTick=Long.MIN_VALUE,pushTick=Long.MIN_VALUE;
        public long inputLimit,outputLimit,revision;public int redstone;
        private Network(BankManager manager,List<TileCapacitorBank> members){
            this.manager=manager;this.members=Collections.unmodifiableList(members);List<BankLedger.Cell> cells=new ArrayList<BankLedger.Cell>();TileCapacitorBank settings=members.get(0);
            for(TileCapacitorBank t:members){cells.add(t.cell);if(t.revision>settings.revision)settings=t;}
            ledger=new BankLedger(cells,BankConfig.input,BankConfig.output);inputLimit=settings.inputLimit;outputLimit=settings.outputLimit;redstone=settings.redstone;revision=settings.revision;
            copySettings();
            for(TileCapacitorBank t:members){t.powered=manager.powerView.powered(t.xCoord,t.yCoord,t.zCoord);if(t.powered)powered++;}
        }
        public long maxInput(){return inputLimit<0?ledger.maxInput():Math.min(inputLimit,ledger.maxInput());}
        public long maxOutput(){return outputLimit<0?ledger.maxOutput():Math.min(outputLimit,ledger.maxOutput());}
        private void copySettings(){for(TileCapacitorBank t:members)if(t.inputLimit!=inputLimit||t.outputLimit!=outputLimit||t.redstone!=redstone||t.revision!=revision){t.inputLimit=inputLimit;t.outputLimit=outputLimit;t.redstone=redstone;t.revision=revision;t.markDirty();}}
        public void configure(int action,long value){
            if(action==0)inputLimit=value<0?-1:Math.min(value,ledger.maxInput());else if(action==1)outputLimit=value<0?-1:Math.min(value,ledger.maxOutput());else if(action==2)redstone=(redstone+1)%3;else return;
            revision=Math.max(revision+1,manager.world.getTotalWorldTime());copySettings();
        }
        public boolean outputEnabled(){return redstone==0||redstone==1&&powered>0||redstone==2&&powered==0;}
        private void refreshPorts(){
            ports.clear();for(TileCapacitorBank t:members)for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS){if(!t.mode(d).output)continue;int x=t.xCoord+d.offsetX,y=t.yCoord+d.offsetY,z=t.zCoord+d.offsetZ;if(!manager.loaded(x,z)||y<0||y>255)continue;TileEntity te=manager.world.getTileEntity(x,y,z);if(te instanceof IEnergyReceiver&&!(te instanceof TileCapacitorBank))ports.add(new Port(t,d,te));}portsDirty=false;
        }
        public void tick(long now){
            ledger.roll(now);
            if(portsDirty)refreshPorts();
            if(pushTick!=now&&ledger.stored()>0&&outputEnabled()&&!busy){pushTick=now;int size=ports.size();for(int i=0;i<size&&valid&&ledger.outputUsed(now)<maxOutput()&&ledger.stored()>0;i++){
                Port p=ports.get((portCursor+i)%size);ForgeDirection side=p.face.getOpposite();
                if(!manager.loaded(p.receiver.xCoord,p.receiver.zCoord)||p.receiver.isInvalid()){portsDirty=true;continue;}
                IEnergyReceiver receiver=(IEnergyReceiver)p.receiver;
                if(!receiver.canConnectEnergy(side))continue;
                BankLedger.Reservation r=ledger.reserveOutput(Integer.MAX_VALUE,now,outputLimit);if(r.amount==0)break;
                busy=true;int accepted=r.amount;
                try{accepted=receiver.receiveEnergy(side,r.amount,false);}catch(Throwable failure){
                    // A broken receiver may have accepted before throwing: never refund unknown RF.
                    IndustrialTechnology.log.warn("RF receiver failed at {},{},{}; reserved RF consumed to prevent duplication",p.receiver.xCoord,p.receiver.yCoord,p.receiver.zCoord,failure);portsDirty=true;
                }finally{r.settle(accepted);busy=false;}
            }if(size>0)portCursor=(portCursor+1)%size;}
            if(visualTick==Long.MIN_VALUE||now-visualTick>=BankConfig.visualInterval){visualTick=now;int level=ledger.stored()==0?0:ledger.stored()==ledger.capacity()?16:Math.min(15,(int)Math.ceil(16.0*ledger.stored()/ledger.capacity()));
                // O(1) on unchanged charge. Walk the members only for a new visible level.
                if(lastDisplay!=level){lastDisplay=level;for(TileCapacitorBank t:members)t.setDisplay(level);}
            }
        }
        private static final class Port {final TileCapacitorBank source;final ForgeDirection face;final TileEntity receiver;Port(TileCapacitorBank t,ForgeDirection d,TileEntity r){source=t;face=d;receiver=r;}}
    }
}
