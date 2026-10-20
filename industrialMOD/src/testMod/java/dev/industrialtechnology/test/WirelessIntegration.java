package dev.industrialtechnology.test;
import java.util.*;
import java.util.function.BiConsumer;
import java.io.*;
import net.minecraft.world.WorldServer;
import net.minecraft.block.Block;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.*;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.util.*;
import com.mojang.authlib.GameProfile;
import dev.industrialtechnology.wireless.*;
import dev.industrialtechnology.core.*;
import dev.industrialtechnology.quarry.*;
public final class WirelessIntegration {
    private static WirelessData data;private static WorldServer world;private static EntityPlayerMP owner;private static UUID network;
    private static WirelessEndpoint green,blue,yellow,yellow2;private static TileEntityChest chest;
    private static int x,z;private static long start,rise=-1,deadline,secondRise=-1;private static boolean held=true,offChecked,stableChecked,retriggerChecked;
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    public static void run(WorldServer w,BiConsumer<String,Runnable> test){
        CodeIntegration.run(w,test);StickPlacementIntegration.run(w,test);world=w;data=WirelessData.get(w);x=(w.getSpawnPoint().posX&~15)+2;z=(w.getSpawnPoint().posZ&~15)+2;w.getChunkFromChunkCoords(x>>4,z>>4);
        owner=new FakePlayer(w,new GameProfile(UUID.fromString("1784156a-3b73-4c97-b16d-6c983e830dc3"),"WirelessOwner")){@Override public void addChatMessage(net.minecraft.util.IChatComponent text){}};owner.setPosition(x+3.5,80,z+2.5);owner.capabilities.allowEdit=true;owner.capabilities.isCreativeMode=true;owner.setSneaking(false);
        owner.inventory.currentItem=0;owner.inventory.setInventorySlotContents(0,new ItemStack(ModRegistry.terminal));network=WirelessAccess.terminal(owner,owner.getHeldItem(),true);
        test.accept("Wireless terminal binds a persistent owner network; independent terminals remain separate",()->{
            check(network!=null&&data.owns(network,owner.getUniqueID()),"binding missing");UUID second=WirelessAccess.terminal(owner,new ItemStack(ModRegistry.terminal),true);check(second!=null&&!second.equals(network),"terminals shared accidentally");owner.getEntityData().setString("IndustrialTerminal",network.toString());
        });
        test.accept("All four wireless survival recipes produce the registered terminal and colored sticks",()->{
            net.minecraft.inventory.InventoryCrafting grid=new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container(){public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer p){return true;}},3,3);
            net.minecraft.item.Item[] input={Items.iron_ingot,Items.gold_ingot,Items.iron_ingot,Items.redstone,Items.diamond,Items.redstone,Items.iron_ingot,Items.ender_pearl,Items.iron_ingot};
            for(int i=0;i<9;i++)grid.setInventorySlotContents(i,new ItemStack(input[i]));ItemStack out=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(grid,w);check(out!=null&&out.getItem()==ModRegistry.terminal,"terminal recipe missing");
            for(int color=0;color<3;color++){for(int i=0;i<9;i++)grid.setInventorySlotContents(i,null);grid.setInventorySlotContents(1,color==2?new ItemStack(Items.dye,1,4):new ItemStack(color==0?Items.emerald:Items.gold_ingot));grid.setInventorySlotContents(4,new ItemStack(Items.redstone));grid.setInventorySlotContents(7,new ItemStack(Items.iron_ingot));out=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(grid,w);check(out!=null&&out.getItem()==new net.minecraft.item.Item[]{ModRegistry.greenStick,ModRegistry.yellowStick,ModRegistry.blueStick}[color],"colored stick recipe missing");}
        });
        test.accept("Tiered VOID filters retain legacy entries, have exactly 18/36/50 slots, and preserve the final slot",()->{
            for(TileQuarry q:new TileQuarry[]{new TileQuarry.MK1(),new TileQuarry.MK2(),new TileQuarry.MK3()}){
                q.yCoord=70;int capacity=new int[]{18,36,50}[q.tier()];check(q.filter.getSizeInventory()==capacity,"wrong capacity");
                NBTTagCompound legacy=new NBTTagCompound();NBTTagList list=new NBTTagList();for(int i=0;i<18;i++){NBTTagCompound item=new NBTTagCompound();new ItemStack(Blocks.stone).writeToNBT(item);item.setInteger("FilterSlot",i);list.appendTag(item);}legacy.setTag("VoidEntries",list);q.filter.read(legacy);check(q.filter.configured()==18,"legacy entries erased");
                for(int i=18;i<capacity;i++)q.filter.setInventorySlotContents(i,new ItemStack(Items.diamond));q.filter.enabled=true;
                NBTTagCompound saved=new NBTTagCompound();q.writeToNBT(saved);TileQuarry copy=q.tier()==0?new TileQuarry.MK1():q.tier()==1?new TileQuarry.MK2():new TileQuarry.MK3();copy.readFromNBT(saved);
                check(copy.filter.configured()==capacity&&copy.filter.getStackInSlot(capacity-1)!=null,"final entry lost");copy.filter.setInventorySlotContents(capacity,new ItemStack(Items.coal));check(copy.filter.configured()==capacity&&copy.filter.getStackInSlot(capacity)==null,"extra entry accepted");
                check(!copy.snapshot().hasKey("VoidEntries")&&copy.snapshot().getInteger("VoidConfigured")==capacity,"oversized filter snapshot");
            }
        });
        test.accept("VOID page validation prevents hidden edits and ghost icons never consume carried items",()->{
            w.setBlock(x+10,80,z,ModRegistry.quarries[2],2,3);TileQuarry q=(TileQuarry)w.getTileEntity(x+10,80,z);owner.setPosition(x+6,80,z);
            dev.industrialtechnology.machine.MachineContainer c=new dev.industrialtechnology.machine.MachineContainer(owner.inventory,q);c.setTab(2);owner.inventory.setItemStack(new ItemStack(Items.diamond,17));
            c.slotClick(139,0,0,owner);check(q.filter.getStackInSlot(49)==null,"hidden slot editable");c.setFilterPage(1);c.slotClick(139,0,0,owner);check(q.filter.getStackInSlot(49).stackSize==1&&owner.inventory.getItemStack().stackSize==17,"ghost consumed items");c.setFilterPage(2);check(c.filterPage==1,"third page accepted");owner.inventory.setItemStack(null);w.setBlockToAir(x+10,80,z);owner.setPosition(x+3.5,80,z+2.5);
        });
        w.setBlock(x,80,z,Blocks.chest,0,3);chest=(TileEntityChest)w.getTileEntity(x,80,z);chest.setInventorySlotContents(0,new ItemStack(Items.iron_ingot,64));chest.setInventorySlotContents(1,new ItemStack(Items.coal,16));
        w.setBlock(x+4,80,z,Blocks.stone,0,3);w.setBlock(x+6,80,z,Blocks.redstone_lamp,0,3);w.setBlock(x+4,80,z+4,Blocks.stone,0,3);
        test.accept("Green, Yellow and Blue sticks register real targets on their selected face",()->{
            green=stick(ModRegistry.greenStick,x,80,z,5);yellow=stick(ModRegistry.yellowStick,x+4,80,z,5);blue=stick(ModRegistry.blueStick,x,80,z,3);yellow2=stick(ModRegistry.yellowStick,x+4,80,z+4,5);
            check(green!=null&&yellow!=null&&blue!=null&&yellow2!=null&&WirelessAccess.relay(yellow)!=null,"registration failed");check(green.side==5&&w.getBlock(x,80,z)==Blocks.chest,"face or machine lost");
        });
        test.accept("Green monitoring reads real chest contents without transferring items",()->{
            MachineInspector.Reading r=MachineInspector.inspect(green,w.provider.dimensionId);check(r.status==WirelessAccess.Status.ONLINE&&r.inventory.slots.length==27&&r.inventory.amount(new ItemStack(Items.iron_ingot))==64,"wrong inventory data");check(chest.getStackInSlot(0).stackSize==64,"monitor modified chest");
        });
        test.accept("Forge monitoring reports both real tanks, quantities and capacities",()->{
            w.setBlock(x,80,z+4,WirelessTestFixtures.tanks,0,3);WirelessEndpoint e=endpoint(WirelessEndpoint.Kind.MONITOR,x,80,z+4,5);MachineInspector.Reading r=MachineInspector.inspect(e,w.provider.dimensionId);
            check(r.status==WirelessAccess.Status.ONLINE&&r.tanks.size()==2&&r.tanks.get(0).amount==1000&&r.tanks.get(0).capacity==4000&&r.tanks.get(1).amount==2000&&r.tanks.get(1).capacity==8000,"fabricated tank data");
        });
        test.accept("Unknown machine APIs show UNSUPPORTED instead of fabricated inventory values",()->{
            w.setBlock(x+2,80,z+4,OreCompatibilityFixtures.energyMachine,0,3);WirelessEndpoint e=endpoint(WirelessEndpoint.Kind.MONITOR,x+2,80,z+4,5);check(MachineInspector.inspect(e,w.provider.dimensionId).status==WirelessAccess.Status.UNSUPPORTED,"unknown API treated as empty inventory");
        });
        test.accept("Offline and cross-dimension links never load chunks or initialize dimensions",()->{
            WirelessEndpoint e=new WirelessEndpoint();e.network=network;e.owner=owner.getUniqueID();e.kind=WirelessEndpoint.Kind.MONITOR;e.dimension=w.provider.dimensionId;e.x=900000;e.y=80;e.z=900000;e.block="minecraft:chest";e.tileToken=UUID.randomUUID();check(!w.getChunkProvider().chunkExists(e.x>>4,e.z>>4),"fixture unexpectedly loaded");
            check(MachineInspector.inspect(e,w.provider.dimensionId).status==WirelessAccess.Status.OFFLINE&&!w.getChunkProvider().chunkExists(e.x>>4,e.z>>4),"monitor loaded chunk");e.dimension=73;check(MachineInspector.inspect(e,w.provider.dimensionId).status==WirelessAccess.Status.OTHER_DIMENSION&&net.minecraftforge.common.DimensionManager.getWorld(73)==null,"cross-dimension load");
        });
        test.accept("Replacing a target TileEntity invalidates its old monitoring identity",()->{
            w.setBlock(x+2,80,z+2,Blocks.chest,0,3);WirelessEndpoint e=endpoint(WirelessEndpoint.Kind.MONITOR,x+2,80,z+2,5);w.setTileEntity(x+2,80,z+2,new TileEntityChest());check(WirelessAccess.status(e,w.provider.dimensionId)==WirelessAccess.Status.DISCONNECTED,"replacement inherited old registration");
        });
        owner.inventory.currentItem=0;
        test.accept("Terminal ownership rejects stolen terminal copies and foreign endpoint selection",()->{
            EntityPlayerMP other=FakePlayerFactory.get(w,new GameProfile(UUID.fromString("1f731239-12e7-46de-bfd5-1b9da83490b3"),"OtherOwner"));other.inventory.currentItem=0;other.inventory.setInventorySlotContents(0,owner.getHeldItem().copy());check(WirelessAccess.terminal(other,other.getHeldItem(),false)==null&&!new TerminalContainer(other).canInteractWith(other),"stolen terminal authorized");
            WirelessEndpoint foreign=new WirelessEndpoint();foreign.owner=other.getUniqueID();foreign.network=data.create(foreign.owner);foreign.x=x;foreign.y=80;foreign.z=z;foreign.dimension=w.provider.dimensionId;data.add(foreign);
            TerminalContainer c=new TerminalContainer(owner);c.action(WirelessCommand.SELECT,0,0,green.id.toString());c.action(WirelessCommand.SELECT,0,0,foreign.id.toString());check(green.id.equals(c.selected),"foreign endpoint selected");
        });
        test.accept("Terminal rules use a validated ghost slot and linked Yellow targets only",()->{
            TerminalContainer c=new TerminalContainer(owner);c.action(WirelessCommand.TAB,2,0,"");c.action(WirelessCommand.SELECT,0,0,blue.id.toString());owner.inventory.setItemStack(new ItemStack(Blocks.stone,32));c.slotClick(36,0,0,owner);
            check(owner.inventory.getItemStack().stackSize==32&&c.ghost.getStackInSlot(0).stackSize==1&&c.inventorySlots.size()==37,"ghost or read-only container invalid");
            c.action(WirelessCommand.TARGET,0,0,yellow.id.toString());c.action(WirelessCommand.SAVE_RULE,0,1,"1");check(blue.rules[0].enabled&&blue.rules[0].item.getItem()==net.minecraft.item.Item.getItemFromBlock(Blocks.stone),"rule not saved");
            c.action(WirelessCommand.SAVE_RULE,20,-1,"1");check(blue.rules[0].minimum==1,"invalid rule accepted");owner.inventory.setItemStack(null);
        });
        test.accept("Yellow relays emit real weak power 15 and face-directed strong power, and light a lamp",()->{
            yellow.pulseMode=false;check(WirelessAccess.activate(data,yellow),"activation rejected");TileRedstoneRelay relay=WirelessAccess.relay(yellow);check(relay.signal()&&w.getBlockPowerInput(x+5,80,z)>=0,"signal absent");
            check(ModRegistry.redstoneRelay.isProvidingWeakPower(w,x+5,80,z,5)==15&&ModRegistry.redstoneRelay.isProvidingStrongPower(w,x+5,80,z,5)==15&&ModRegistry.redstoneRelay.isProvidingStrongPower(w,x+5,80,z,4)==0,"bad orientation");check(w.getBlock(x+6,80,z)==Blocks.lit_redstone_lamp,"real lamp did not respond");WirelessAccess.off(data,yellow);check(ModRegistry.redstoneRelay.isProvidingWeakPower(w,x+5,80,z,5)==0,"OFF still powered");
        });
        test.accept("Every Yellow pulse setting uses its own server deadline from 100 through 200 ticks",()->{
            yellow.pulseMode=true;for(int seconds=5;seconds<=10;seconds++){WirelessAccess.off(data,yellow);yellow.pulseSeconds=seconds;check(WirelessAccess.activate(data,yellow)&&yellow.pulseEnd-w.getTotalWorldTime()==seconds*20,"bad pulse deadline");long end=yellow.pulseEnd;check(!WirelessAccess.activate(data,yellow)&&yellow.pulseEnd==end,"active pulse retrigger extended deadline");}WirelessAccess.off(data,yellow);yellow.pulseSeconds=7;
        });
        test.accept("Automation conditions read item amounts, empty/full capacity, and missing items",()->{
            MachineInspector.InventoryView inv=MachineInspector.inspect(blue,w.provider.dimensionId).inventory;AutomationRule r=new AutomationRule();r.item=new ItemStack(Items.iron_ingot);r.minimum=64;check(r.evaluate(inv),"contains false");r.condition=AutomationRule.Condition.ITEM_AMOUNT_REACHED;check(r.evaluate(inv),"threshold false");r.condition=AutomationRule.Condition.ITEM_MISSING;r.item=new ItemStack(Items.diamond);check(r.evaluate(inv),"missing false");r.condition=AutomationRule.Condition.INVENTORY_EMPTY;check(!r.evaluate(inv),"nonempty marked empty");
            for(int i=0;i<27;i++)chest.setInventorySlotContents(i,new ItemStack(Items.iron_ingot,64));inv=MachineInspector.inspect(blue,w.provider.dimensionId).inventory;r.condition=AutomationRule.Condition.INVENTORY_FULL;r.item=null;check(r.evaluate(inv),"full chest not full");chest.setInventorySlotContents(26,new ItemStack(Items.iron_ingot,63));check(!r.evaluate(MachineInspector.inspect(blue,w.provider.dimensionId).inventory),"partial chest full");
            for(int i=0;i<27;i++)chest.setInventorySlotContents(i,null);r.condition=AutomationRule.Condition.INVENTORY_EMPTY;check(r.evaluate(MachineInspector.inspect(blue,w.provider.dimensionId).inventory),"empty chest not empty");
        });
        test.accept("One Blue summary evaluates multiple independent rules and multiple Yellow targets",()->{
            AutomationRule a=blue.rules[0];a.item=new ItemStack(Blocks.stone);a.minimum=1;a.target=yellow.id;a.enabled=true;a.latched=false;
            AutomationRule b=blue.rules[1];b.condition=AutomationRule.Condition.ITEM_MISSING;b.item=new ItemStack(Items.coal);b.minimum=16;b.target=yellow2.id;b.enabled=true;b.latched=false;
            chest.setInventorySlotContents(0,new ItemStack(Blocks.stone));WirelessScheduler.poll(data,blue);check(yellow.powered&&yellow2.powered&&a.latched&&b.latched,"multiple actions failed");long end=yellow.pulseEnd;WirelessScheduler.poll(data,blue);check(yellow.pulseEnd==end,"permanently true retriggered");
            chest.setInventorySlotContents(0,null);chest.setInventorySlotContents(1,new ItemStack(Items.coal,16));WirelessScheduler.poll(data,blue);check(!a.latched&&!b.latched,"conditions did not reset");b.enabled=false;WirelessAccess.off(data,yellow);WirelessAccess.off(data,yellow2);
        });
        test.accept("Wireless NBT round trip preserves identities, owners, names, pulse settings and all eight rules",()->{
            green.name="Main Arc Furnace";blue.rules[7].target=yellow2.id;blue.rules[7].item=new ItemStack(Items.diamond);blue.rules[7].minimum=64;blue.rules[7].enabled=true;
            NBTTagCompound n=new NBTTagCompound();data.writeToNBT(n);WirelessData copy=new WirelessData();copy.readFromNBT(n);check(copy.owns(network,owner.getUniqueID())&&copy.devices.get(green.id).name.equals(green.name)&&copy.devices.get(yellow.id).pulseSeconds==7&&copy.devices.get(blue.id).rules[7].minimum==64,"registry state changed");blue.rules[7].enabled=false;data.markDirty();
        });
        if(cpw.mods.fml.common.Loader.isModLoaded("hbm"))hbm(test);
        start=w.getTotalWorldTime();chest.setInventorySlotContents(0,null);yellow.pulseMode=true;yellow.pulseSeconds=7;WirelessAccess.off(data,yellow);blue.rules[0].latched=false;data.markDirty();
        try{NBTTagCompound n=new NBTTagCompound();n.setString("Network",network.toString());n.setString("Green",green.id.toString());n.setString("Blue",blue.id.toString());n.setString("Yellow",yellow.id.toString());n.setString("Yellow2",yellow2.id.toString());n.setInteger("X",x);n.setInteger("Z",z);CompressedStreamTools.writeCompressed(n,new FileOutputStream("wireless-fixtures.nbt"));}catch(IOException ex){throw new RuntimeException(ex);}
    }
    private static WirelessEndpoint stick(net.minecraft.item.Item item,int x,int y,int z,int side){owner.inventory.currentItem=1;owner.inventory.setInventorySlotContents(1,new ItemStack(item));item.onItemUseFirst(owner.getHeldItem(),owner,world,x,y,z,side,.5f,.5f,.5f);for(WirelessEndpoint e:data.linked(network))if(e.kind==((ItemWirelessStick)item).kind&&e.x==x&&e.y==y&&e.z==z)return e;return null;}
    private static WirelessEndpoint endpoint(WirelessEndpoint.Kind kind,int x,int y,int z,int side){WirelessEndpoint e=new WirelessEndpoint();e.owner=owner.getUniqueID();e.network=network;e.kind=kind;e.dimension=world.provider.dimensionId;e.x=x;e.y=y;e.z=z;e.side=side;e.block=String.valueOf(Block.blockRegistry.getNameForObject(world.getBlock(x,y,z)));TileEntity tile=world.getTileEntity(x,y,z);if(tile!=null)e.tileToken=TargetAnchors.bind(tile);check(data.add(e),"fixture endpoint limit");return e;}
    public static void tick(WorldServer w,BiConsumer<String,Runnable> test){
        CodeIntegration.tick(w,test);
        if(chest==null||blue==null||yellow==null)return;long now=w.getTotalWorldTime(),age=now-start;
        if(age==10)chest.setInventorySlotContents(0,new ItemStack(Blocks.stone));
        if(rise<0&&yellow.powered){rise=now;deadline=yellow.pulseEnd;}
        if(rise>=0&&now<deadline&&!WirelessAccess.relay(yellow).signal())held=false;
        if(rise>=0&&now>=deadline&&!offChecked){offChecked=true;test.accept("Real Blue chest insertion produces a seven-second pulse lasting exactly 140 server ticks",()->check(held&&deadline-rise==140&&!yellow.powered&&!WirelessAccess.relay(yellow).signal(),"pulse duration or expiration incorrect"));}
        if(offChecked&&now==deadline+20){stableChecked=true;test.accept("Keeping stone in the real chest does not continuously retrigger the relay",()->check(!yellow.powered&&blue.rules[0].latched,"constant condition retriggered"));chest.setInventorySlotContents(0,null);}
        if(stableChecked&&now==deadline+40)chest.setInventorySlotContents(0,new ItemStack(Blocks.stone));
        if(stableChecked&&now>deadline+40&&yellow.powered&&secondRise<0){secondRise=now;retriggerChecked=true;test.accept("Removing stone and adding it again produces a new false-to-true activation",()->check(blue.rules[0].latched&&yellow.pulseEnd-now==140,"new edge did not trigger"));}
        if(age==275)test.accept("Asynchronous automation completed during real server ticks without a GUI or online player",()->check(offChecked&&stableChecked&&retriggerChecked,"automation scenario did not complete"));
    }
    private static void hbm(BiConsumer<String,Runnable> test){
        for(String className:new String[]{"com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge","com.hbm.tileentity.machine.TileEntityMachineChemicalPlant","com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank","com.hbm.tileentity.machine.storage.TileEntityMachineBigAssTank"})test.accept("Actual HBM monitoring adapter: "+className.substring(className.lastIndexOf('.')+1),()->{
            try{TileEntity tile=(TileEntity)Class.forName(className).newInstance();int tx=x+10,tz=z+8;world.setBlock(tx,90,tz,OreCompatibilityFixtures.tileOre,0,3);world.setTileEntity(tx,90,tz,tile);WirelessEndpoint e=endpoint(WirelessEndpoint.Kind.MONITOR,tx,90,tz,5);
                // Use a supported insertion/extraction face; fill known inventory directly as test setup.
                if(tile instanceof net.minecraft.inventory.IInventory){net.minecraft.inventory.IInventory inv=(net.minecraft.inventory.IInventory)tile;if(inv.getSizeInventory()>0)inv.setInventorySlotContents(0,new ItemStack(Items.iron_ingot,23));}
                Object firstTank=null;long expectedFill=0,expectedCapacity=0;
                if(!className.contains("ArcFurnace")){Object tanks=tile.getClass().getMethod("getAllTanks").invoke(tile);firstTank=java.lang.reflect.Array.get(tanks,0);Object acid=Class.forName("com.hbm.inventory.fluid.Fluids").getField("SULFURIC_ACID").get(null);firstTank.getClass().getMethod("setTankType",acid.getClass()).invoke(firstTank,acid);expectedCapacity=((Number)firstTank.getClass().getMethod("getMaxFill").invoke(firstTank)).longValue();expectedFill=Math.min(expectedCapacity,12345);firstTank.getClass().getMethod("setFill",int.class).invoke(firstTank,(int)expectedFill);}
                MachineInspector.Reading r=MachineInspector.inspect(e,world.provider.dimensionId);check(r.status==WirelessAccess.Status.ONLINE,"HBM API unsupported: "+r.status+" "+r.message);
                if(firstTank!=null)check(r.tanks.get(0).amount==expectedFill&&r.tanks.get(0).capacity==expectedCapacity&&!r.tanks.get(0).localizationKey.isEmpty(),"HBM quantities or units were fabricated");
                if(className.contains("ArcFurnace"))check(r.inventory!=null,"HBM furnace inventory absent");else check(!r.tanks.isEmpty(),"HBM tanks absent");world.removeTileEntity(tx,90,tz);world.setBlockToAir(tx,90,tz);
            }catch(ReflectiveOperationException ex){throw new RuntimeException(ex);}
        });
        for(String controllerName:new String[]{"com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge","com.hbm.tileentity.machine.TileEntityMachineChemicalPlant"})test.accept("Actual HBM multiblock proxy resolves loaded "+controllerName.substring(controllerName.lastIndexOf('.')+1),()->{
            try{
                int tx=x+10,tz=z+8;TileEntity controller=(TileEntity)Class.forName(controllerName).newInstance();
                TileEntity proxy=(TileEntity)Class.forName("com.hbm.tileentity.TileEntityProxyCombo").newInstance();proxy.getClass().getMethod(controllerName.contains("ArcFurnace")?"inventory":"fluid").invoke(proxy);
                world.setBlock(tx,90,tz,OreCompatibilityFixtures.tileOre,0,3);world.setTileEntity(tx,90,tz,controller);
                world.setBlock(tx+1,90,tz,OreCompatibilityFixtures.tileOre,0,3);world.setTileEntity(tx+1,90,tz,proxy);
                Object position=Class.forName("com.hbm.util.fauxpointtwelve.BlockPos").getConstructor(int.class,int.class,int.class).newInstance(tx,90,tz);
                proxy.getClass().getField("cachedPosition").set(proxy,position);WirelessEndpoint e=endpoint(WirelessEndpoint.Kind.MONITOR,tx+1,90,tz,5);
                if(controllerName.contains("ArcFurnace")){
                    net.minecraft.inventory.ISidedInventory inv=(net.minecraft.inventory.ISidedInventory)controller;int[] accessible=new int[0];for(int side=0;side<6;side++){accessible=inv.getAccessibleSlotsFromSide(side);if(accessible.length>0){e.side=side;break;}}
                    check(accessible.length>0,"controller exposes no side");((ItemStack[])controller.getClass().getField("slots").get(controller))[accessible[0]]=new ItemStack(Items.iron_ingot,37);
                    MachineInspector.Reading r=MachineInspector.inspect(e,world.provider.dimensionId);check(r.status==WirelessAccess.Status.ONLINE&&r.inventory.amount(new ItemStack(Items.iron_ingot))==37,"proxy inventory did not resolve controller data");
                }else{
                    Object tanks=controller.getClass().getMethod("getAllTanks").invoke(controller),tank=java.lang.reflect.Array.get(tanks,0);tank.getClass().getMethod("setFill",int.class).invoke(tank,321);
                    MachineInspector.Reading r=MachineInspector.inspect(e,world.provider.dimensionId);check(r.status==WirelessAccess.Status.ONLINE&&r.tanks.get(0).amount==321,"proxy fluid data did not resolve controller");
                }
                TileEntity powerOnly=(TileEntity)Class.forName("com.hbm.tileentity.TileEntityProxyCombo").newInstance();powerOnly.getClass().getMethod("power").invoke(powerOnly);powerOnly.getClass().getField("cachedPosition").set(powerOnly,position);world.setTileEntity(tx+1,90,tz,powerOnly);
                WirelessEndpoint inaccessible=endpoint(WirelessEndpoint.Kind.MONITOR,tx+1,90,tz,e.side);check(MachineInspector.inspect(inaccessible,world.provider.dimensionId).status==WirelessAccess.Status.UNSUPPORTED,"power-only port exposed controller storage");
                world.removeTileEntity(tx+1,90,tz);world.removeTileEntity(tx,90,tz);world.setBlockToAir(tx+1,90,tz);world.setBlockToAir(tx,90,tz);
            }catch(ReflectiveOperationException ex){throw new RuntimeException(ex);}
        });
    }
    public static void restore(WorldServer w,BiConsumer<String,Runnable> test){
        CodeIntegration.restore(w,test);StickPlacementIntegration.restore(w,test);
        try{NBTTagCompound n=CompressedStreamTools.readCompressed(new FileInputStream("wireless-fixtures.nbt"));WirelessData d=WirelessData.get(w);UUID network=WirelessData.uuid(n.getString("Network"));WirelessEndpoint green=d.devices.get(WirelessData.uuid(n.getString("Green"))),blue=d.devices.get(WirelessData.uuid(n.getString("Blue"))),yellow=d.devices.get(WirelessData.uuid(n.getString("Yellow")));
            test.accept("Actual restart restores terminal networks, endpoint identities and renamed devices",()->check(d.networks.containsKey(network)&&green!=null&&green.name.equals("Main Arc Furnace")&&blue!=null&&yellow!=null,"wireless saved data lost"));
            test.accept("Actual restart restores target anchor identities without changing foreign machine NBT",()->check(WirelessAccess.status(green,w.provider.dimensionId)==WirelessAccess.Status.ONLINE&&MachineInspector.inspect(green,w.provider.dimensionId).inventory!=null,"target anchor not restored"));
            test.accept("Actual restart preserves eight automation rules, edge latch and seven-second pulse configuration",()->check(blue.rules.length==8&&blue.rules[0].enabled&&blue.rules[0].latched&&blue.rules[0].target.equals(yellow.id)&&yellow.pulseMode&&yellow.pulseSeconds==7,"rule or pulse state changed"));
        }catch(IOException ex){throw new RuntimeException(ex);}
    }
}
