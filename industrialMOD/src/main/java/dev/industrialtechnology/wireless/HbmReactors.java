package dev.industrialtechnology.wireless;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

/** Optional X5808 adapter. All HBM symbols here were inspected in the installed production JAR. */
public final class HbmReactors {
    public static final int NONE=0,RBMK_FUEL=1,ZIRNOX=2,RBMK_CONTROL=3;
    private static final String P="com.hbm.tileentity.machine.rbmk.";
    private static API api;private static boolean checked;
    private static API api(){if(!checked){checked=true;try{api=new API();}catch(ReflectiveOperationException|LinkageError absent){api=null;}}return api;}
    private static final class API {
        final Class<?> rod=Class.forName(P+"TileEntityRBMKRod"),zirnox=Class.forName("com.hbm.tileentity.machine.TileEntityReactorZirnox"),console=Class.forName(P+"TileEntityRBMKConsole"),base=Class.forName(P+"TileEntityRBMKBase"),manual=Class.forName(P+"TileEntityRBMKControlManual"),automatic=Class.forName(P+"TileEntityRBMKControlAuto");
        final Class<?> fuel=Class.forName("com.hbm.items.machine.ItemRBMKRod"),zFuel=Class.forName("com.hbm.items.machine.ItemZirnoxRod");
        final Object[] variants=Class.forName("com.hbm.items.machine.ItemZirnoxRod$EnumZirnoxType").getEnumConstants();
        final Method enrichment=fuel.getMethod("getEnrichment",ItemStack.class),coreHeat=fuel.getMethod("getCoreHeat",ItemStack.class),hullHeat=fuel.getMethod("getHullHeat",ItemStack.class),poison=fuel.getMethod("getPoison",ItemStack.class),life=zFuel.getMethod("getLifeTime",ItemStack.class);
        final Field maxLife=variants[0].getClass().getField("maxLife"),breeding=variants[0].getClass().getField("breeding"),level=manual.getField("level"),target=manual.getField("targetLevel");
        final Method setTarget=manual.getMethod("setTarget",double.class),dx=console.getMethod("getXFromIndex",int.class),dz=console.getMethod("getZFromIndex",int.class);
        final Field tx=field(console,"targetX"),ty=field(console,"targetY"),tz=field(console,"targetZ");
        final Constructor<?> container=Class.forName("com.hbm.inventory.container.ContainerReactorZirnox").getConstructor(InventoryPlayer.class,zirnox);
        Layout layout;
        API()throws ReflectiveOperationException{}
    }
    private static Field field(Class<?> type,String name)throws ReflectiveOperationException{Field f=type.getDeclaredField(name);f.setAccessible(true);return f;}
    public static final class Position {
        public final int slot,x,y,row,column;
        Position(int slot,int x,int y,int row,int column){this.slot=slot;this.x=x;this.y=y;this.row=row;this.column=column;}
    }
    public static final class Layout {
        public final List<Position> positions;public final int rows;
        Layout(List<Position> positions,int rows){this.positions=Collections.unmodifiableList(positions);this.rows=rows;}
    }
    public static final class Access {
        public int type;public TileEntity clicked,core;public IInventory inventory;public Layout layout;
        public String status="UNSUPPORTED",message="HBM reactor API unavailable",binding="";
        public boolean online(){return "ONLINE".equals(status)&&core!=null;}
    }
    public static boolean validFuel(ItemStack stack){
        API a=api();return stack!=null&&stack.stackSize>0&&a!=null&&a.zFuel.isInstance(stack.getItem())&&stack.getItemDamage()>=0&&stack.getItemDamage()<a.variants.length;
    }
    public static Access access(WirelessEndpoint e){
        Access r=new Access();r.type=e.reactorType;WirelessAccess.Status status=WirelessAccess.status(e,e.dimension);
        if(status!=WirelessAccess.Status.ONLINE){r.status=status.name();r.message="Linked access point is "+status;return r;}
        API a=api();if(a==null)return r;
        try{
            r.clicked=WirelessAccess.tile(e);if(r.clicked==null)return r;Object resolved=HbmMonitoring.resolve(r.clicked);
            if(!(resolved instanceof TileEntity)){r.status="DATA_UNAVAILABLE";r.message="Controller is not loaded or cannot be resolved";return r;}
            r.core=(TileEntity)resolved;r.type=a.rod.isInstance(resolved)?RBMK_FUEL:a.zirnox.isInstance(resolved)?ZIRNOX:a.console.isInstance(resolved)?RBMK_CONTROL:NONE;
            if(r.type==NONE)return r;
            if(r.type!=RBMK_CONTROL&&!HbmMonitoring.allows(r.clicked,"inventory")){r.status="UNSUPPORTED";r.message="This HBM port does not expose reactor inventory access";return r;}
            if(e.reactorCore!=null&&(r.core.xCoord!=e.reactorX||r.core.yCoord!=e.reactorY||r.core.zCoord!=e.reactorZ||!TargetAnchors.matches(r.core,e.reactorCore))){r.status="DISCONNECTED";r.message="Reactor controller changed; relink the stick";return r;}
            if(e.reactorCore==null){e.reactorCore=TargetAnchors.bind(r.core);e.reactorX=r.core.xCoord;e.reactorY=r.core.yCoord;e.reactorZ=r.core.zCoord;WirelessData.get(r.core.getWorldObj()).markDirty();}
            if(e.reactorType!=r.type){e.reactorType=r.type;WirelessData.get(r.core.getWorldObj()).markDirty();}
            r.binding=e.reactorCore.toString();
            if(r.type==RBMK_CONTROL)r.binding+=":"+a.tx.getInt(r.core)+","+a.ty.getInt(r.core)+","+a.tz.getInt(r.core);
            if(r.core instanceof IInventory)r.inventory=(IInventory)r.core;
            if(r.type==ZIRNOX){if(a.layout==null)a.layout=layout(a,r.inventory);r.layout=a.layout;if(r.layout.positions.size()!=ReactorSettings.MAX_FUEL)throw new IllegalStateException("Unexpected core size");}
            r.status="ONLINE";r.message="HBM X5808 verified adapter";
        }catch(ReflectiveOperationException|RuntimeException|LinkageError badAPI){r.status="DATA_UNAVAILABLE";r.message="Reactor API/layout unavailable for this HBM version";}
        return r;
    }
    /** Derive slot IDs and staggered geometry from the installed common-side HBM container. */
    private static Layout layout(API a,IInventory inventory)throws ReflectiveOperationException{
        if(!(inventory instanceof ISidedInventory)||inventory.getSizeInventory()!=28)throw new IllegalStateException("Unsupported ZIRNOX inventory");
        int[] exposed=((ISidedInventory)inventory).getAccessibleSlotsFromSide(0);if(exposed==null||exposed.length!=24)throw new IllegalStateException("Unsupported fuel access");
        Set<Integer> ids=new HashSet<Integer>();for(int id:exposed)if(id>=0&&id<24)ids.add(id);if(ids.size()!=24)throw new IllegalStateException("Unsupported slot mapping");
        Container container=(Container)a.container.newInstance(new InventoryPlayer(null),inventory);List<Slot> slots=new ArrayList<Slot>();
        for(Object value:container.inventorySlots){Slot s=(Slot)value;if(s.inventory==inventory&&ids.contains(s.getSlotIndex()))slots.add(s);}
        if(slots.size()!=24)throw new IllegalStateException("Incomplete native layout");
        Collections.sort(slots,(l,r)->l.yDisplayPosition!=r.yDisplayPosition?Integer.compare(l.yDisplayPosition,r.yDisplayPosition):Integer.compare(l.xDisplayPosition,r.xDisplayPosition));
        List<Position> positions=new ArrayList<Position>();int row=-1,lastY=-1,col=0;
        for(Slot s:slots){if(s.yDisplayPosition!=lastY){row++;col=0;lastY=s.yDisplayPosition;}positions.add(new Position(s.getSlotIndex(),s.xDisplayPosition-8,s.yDisplayPosition-16,row,col++));}
        return new Layout(positions,row+1);
    }
    private static void number(NBTTagCompound n,String key,double value){if(Double.isFinite(value))n.setDouble(key,value);}
    private static boolean ratio(double value){return Double.isFinite(value)&&value>=0&&value<=1;}
    private static void icon(NBTTagCompound n,String key,ItemStack stack){if(stack==null)return;ItemStack safe=new ItemStack(stack.getItem(),1,stack.getItemDamage());NBTTagCompound tag=new NBTTagCompound();safe.writeToNBT(tag);n.setTag(key,tag);}
    public static NBTTagCompound fuel(ItemStack actual,int type){
        NBTTagCompound n=new NBTTagCompound();n.setString("State",actual==null?"EMPTY":"UNSUPPORTED FUEL");if(actual==null)return n;
        n.setString("Name",WirelessData.clean(actual.getDisplayName(),96));icon(n,"Icon",actual);n.setInteger("Meta",actual.getItemDamage());API a=api();if(a==null)return n;
        try{
            // HBM getters initialize missing NBT. Read a detached copy to keep Green strictly read-only.
            ItemStack copy=actual.copy();double remaining=Double.NaN;
            if(type==RBMK_FUEL&&a.fuel.isInstance(copy.getItem())){
                remaining=((Number)a.enrichment.invoke(null,copy)).doubleValue();
                number(n,"CoreHeat",((Number)a.coreHeat.invoke(null,copy)).doubleValue());number(n,"HullHeat",((Number)a.hullHeat.invoke(null,copy)).doubleValue());number(n,"Xenon",((Number)a.poison.invoke(null,copy)).doubleValue());
                n.setString("CNote","X5808 exposes yield depletion; no separate C Depletion field exists");
            }else if(type==ZIRNOX&&validFuel(copy)){
                int life=((Number)a.life.invoke(null,copy)).intValue(),max=a.maxLife.getInt(a.variants[copy.getItemDamage()]);
                if(max>0&&life>=0&&life<=max){remaining=1.0-(double)life/max;n.setLong("Life",life);n.setLong("MaxLife",max);}
                n.setBoolean("Breeding",a.breeding.getBoolean(a.variants[copy.getItemDamage()]));
            }
            if(ratio(remaining)){n.setDouble("Remaining",remaining*100);n.setDouble("Depletion",(1-remaining)*100);n.setString("State",remaining==0?"DEPLETED":n.getBoolean("Breeding")?"BREEDING":"FUEL INSTALLED");}
            else if(type==RBMK_FUEL&&a.fuel.isInstance(copy.getItem())||type==ZIRNOX&&validFuel(copy))n.setString("State","DATA UNAVAILABLE");
        }catch(ReflectiveOperationException|RuntimeException|LinkageError failure){n.setString("State","DATA UNAVAILABLE");}
        return n;
    }
    public static NBTTagCompound describe(WirelessEndpoint e,ReactorSettings draft,boolean details){
        Access r=access(e);NBTTagCompound n=new NBTTagCompound();n.setInteger("Type",r.type);n.setString("Status",r.status);n.setString("Message",r.message);n.setString("Binding",r.binding);
        if(!r.online())return n;n.setInteger("CoreX",r.core.xCoord);n.setInteger("CoreY",r.core.yCoord);n.setInteger("CoreZ",r.core.zCoord);if(!details)return n;
        if(r.type==RBMK_CONTROL){n.setInteger("LastRequest",e.reactorLastTarget);controls(r,n);return n;}
        if(r.type==ZIRNOX)try{
            // Same conversion used by X5808 GUIReactorZirnox and its getTemp OC callback.
            n.setDouble("TemperatureC",Math.round(r.core.getClass().getField("heat").getInt(r.core)*1.0E-5*780+20));
            n.setDouble("PressureBar",Math.round(r.core.getClass().getField("pressure").getInt(r.core)*1.0E-5*30));
        }catch(ReflectiveOperationException unavailable){}
        NBTTagList list=new NBTTagList();
        if(r.type==RBMK_FUEL){NBTTagCompound f=fuel(r.inventory.getStackInSlot(0),RBMK_FUEL);f.setInteger("Slot",0);list.appendTag(f);}
        else if(r.type==ZIRNOX){n.setInteger("Rows",r.layout.rows);n.setBoolean("Running",e.reactor.running);n.setInteger("Supply",draft.supply);
            if(e.kind==WirelessEndpoint.Kind.AUTOMATION){ReactorAutomation.describeSupply(e,r,n);n.setBoolean("AssignmentsSaved",draft.supply==e.reactor.supply&&draft.write().getTag("Assignments").equals(e.reactor.write().getTag("Assignments")));}
            for(Position p:r.layout.positions){NBTTagCompound f=fuel(r.inventory.getStackInSlot(p.slot),ZIRNOX);f.setInteger("Slot",p.slot);f.setInteger("GridX",p.x);f.setInteger("GridY",p.y);f.setInteger("Row",p.row);f.setInteger("Column",p.column);icon(f,"Desired",draft.icon(p.slot));f.setBoolean("Enabled",draft.enabled[p.slot]);f.setString("Automation",ReactorAutomation.positionStatus(e,r,p.slot,draft));list.appendTag(f);}
        }n.setTag("Fuels",list);return n;
    }
    private static final class Group {final List<TileEntity> manual=new ArrayList<TileEntity>();int automatic,unloaded;String error="";}
    /** Only the connected component rooted at the console's native anchor, clipped to its 15x15 view. */
    private static Group group(Access r)throws ReflectiveOperationException{
        API a=api();Group g=new Group();int cx=a.tx.getInt(r.core),cy=a.ty.getInt(r.core),cz=a.tz.getInt(r.core);WorldServer world=(WorldServer)r.core.getWorldObj();
        if(cy<0||cy>255||!WirelessAccess.loaded(world,cx,cy,cz)){g.error="Configured RBMK anchor is offline";return g;}
        TileEntity center=world.getTileEntity(cx,cy,cz);if(!a.base.isInstance(center)){g.error="Point the HBM console at an RBMK element first";return g;}
        Set<Long> allowed=new HashSet<Long>();for(int i=0;i<225;i++){int x=cx+((Number)a.dx.invoke(r.core,i)).intValue(),z=cz+((Number)a.dz.invoke(r.core,i)).intValue();allowed.add(key(x,z));}
        Set<Long> seen=new HashSet<Long>();ArrayDeque<int[]> todo=new ArrayDeque<int[]>();todo.add(new int[]{cx,cz});
        while(!todo.isEmpty()&&seen.size()<225){int[] p=todo.remove();long key=key(p[0],p[1]);if(!allowed.contains(key)||!seen.add(key))continue;
            if(!WirelessAccess.loaded(world,p[0],cy,p[1])){g.unloaded++;continue;}TileEntity tile=world.getTileEntity(p[0],cy,p[1]);if(!a.base.isInstance(tile))continue;
            if(a.manual.isInstance(tile))g.manual.add(tile);else if(a.automatic.isInstance(tile))g.automatic++;
            todo.add(new int[]{p[0]+1,p[1]});todo.add(new int[]{p[0]-1,p[1]});todo.add(new int[]{p[0],p[1]+1});todo.add(new int[]{p[0],p[1]-1});
        }return g;
    }
    private static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
    private static void controls(Access r,NBTTagCompound n){
        try{API a=api();Group g=group(r);n.setInteger("RodCount",g.manual.size());n.setInteger("Automatic",g.automatic);n.setInteger("Unloaded",g.unloaded);n.setString("GroupMessage",g.error);
            double sum=0,first=Double.NaN,target=Double.NaN;boolean mixed=false,mixedTarget=false;
            for(TileEntity rod:g.manual){double level=100*(1-a.level.getDouble(rod)),requested=100*(1-a.target.getDouble(rod));if(!Double.isFinite(level)||level<0||level>100||!Double.isFinite(requested)||requested<0||requested>100)throw new IllegalStateException();if(Double.isNaN(first)){first=level;target=requested;}else{mixed|=Math.abs(level-first)>.01;mixedTarget|=Math.abs(requested-target)>.01;}sum+=level;}
            if(!g.manual.isEmpty()){n.setDouble("AverageInsertion",sum/g.manual.size());if(!mixedTarget)n.setDouble("TargetInsertion",target);}n.setBoolean("Mixed",mixed);n.setBoolean("MixedTarget",mixedTarget);
        }catch(ReflectiveOperationException|RuntimeException|LinkageError failure){n.setString("GroupMessage","Control rod state unavailable");}
    }
    public static String setInsertion(WirelessEndpoint e,int percent,String binding){
        if(percent<0||percent>100||percent%10!=0)return "Invalid target: use 0-100 in steps of 10";
        Access r=access(e);if(!r.online()||r.type!=RBMK_CONTROL)return r.message;if(!r.binding.equals(binding))return "Reactor binding changed; reopen CONTROL RODS";
        try{
            WorldServer world=(WorldServer)r.core.getWorldObj();if(!ReactorPermissions.allow(e.owner,world,e.x,e.y,e.z,e.side)||!ReactorPermissions.allow(e.owner,world,r.core.xCoord,r.core.yCoord,r.core.zCoord,e.side))return "NO PERMISSION";
            Group g=group(r);if(!g.error.isEmpty())return g.error;API a=api();int accepted=0,blocked=0;
            for(TileEntity rod:g.manual){if(!ReactorPermissions.allow(e.owner,world,rod.xCoord,rod.yCoord,rod.zCoord,e.side)){blocked++;continue;}
                double target=1-percent/100.0; // HBM level is EXTRACTION: 0 inserted, 1 withdrawn.
                if(Math.abs(a.target.getDouble(rod)-target)>.0000001){a.setTarget.invoke(rod,target);rod.markDirty();}accepted++;
            }e.reactorLastTarget=percent;WirelessData.get(world).markDirty();
            return accepted+" / "+g.manual.size()+" manual rods confirmed; "+blocked+" blocked; "+g.automatic+" automatic excluded; "+g.unloaded+" unloaded boundaries";
        }catch(ReflectiveOperationException|RuntimeException|LinkageError failure){return "Control API unavailable; inspect current rod state";}
    }
    private HbmReactors(){}
}
