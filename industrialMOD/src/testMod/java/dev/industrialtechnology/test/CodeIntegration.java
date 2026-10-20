package dev.industrialtechnology.test;
import java.util.*;
import java.util.function.BiConsumer;
import java.io.*;
import net.minecraft.world.WorldServer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.*;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.*;
import com.mojang.authlib.GameProfile;
import dev.industrialtechnology.core.*;
import dev.industrialtechnology.wireless.*;
import dev.industrialtechnology.automation.*;
public final class CodeIntegration {
    private static WorldServer world;private static WirelessData data;private static WirelessEndpoint blue,items,pump,machine;private static WirelessTestFixtures.Tanks tank;private static TileEntityChest chest;
    private static long start,rise=-1,itemRise=-1;private static boolean held=true,itemHeld=true,done,itemDone;private static int x,z;
    private static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
    private static String code(String condition,String action){return "WHEN "+condition+"\nTHEN "+action+"\nEND";}
    private static WirelessEndpoint stick(FakePlayer p,net.minecraft.item.Item item,int x,int y,int z,int side){p.inventory.currentItem=1;p.inventory.setInventorySlotContents(1,new ItemStack(item));item.onItemUseFirst(p.getHeldItem(),p,world,x,y,z,side,.5f,.5f,.5f);net.minecraftforge.common.util.ForgeDirection d=net.minecraftforge.common.util.ForgeDirection.getOrientation(side);return data.devices.get(((TileRedstoneRelay)world.getTileEntity(x+d.offsetX,y+d.offsetY,z+d.offsetZ)).endpoint);}
    public static void run(WorldServer w,BiConsumer<String,Runnable> test){
        world=w;data=WirelessData.get(w);x=(w.getSpawnPoint().posX&~15)+96;z=(w.getSpawnPoint().posZ&~15)+96;w.getChunkFromChunkCoords(x>>4,z>>4);
        FakePlayer p=new FakePlayer(w,new GameProfile(UUID.fromString("ead46fea-5fce-41cf-a08d-47d942f365d9"),"CodeOwner")){@Override public void addChatMessage(net.minecraft.util.IChatComponent t){}};p.setPosition(x+2.5,91,z+2.5);p.capabilities.allowEdit=true;p.capabilities.isCreativeMode=true;p.setSneaking(false);p.inventory.setInventorySlotContents(0,new ItemStack(ModRegistry.terminal));p.inventory.currentItem=0;WirelessAccess.terminal(p,p.getHeldItem(),true);
        w.setBlock(x,90,z,WirelessTestFixtures.tanks,0,3);tank=(WirelessTestFixtures.Tanks)w.getTileEntity(x,90,z);tank.water.setFluid(new FluidStack(FluidRegistry.WATER,100));tank.lava.setFluid(null);
        w.setBlock(x,90,z+4,Blocks.chest,0,3);chest=(TileEntityChest)w.getTileEntity(x,90,z+4);w.setBlock(x+4,90,z,Blocks.redstone_lamp,0,3);w.setBlock(x+4,90,z+4,Blocks.redstone_lamp,0,3);
        blue=stick(p,ModRegistry.blueStick,x,90,z,5);items=stick(p,ModRegistry.blueStick,x,90,z+4,5);pump=stick(p,ModRegistry.yellowStick,x+4,90,z,2);machine=stick(p,ModRegistry.yellowStick,x+4,90,z+4,2);pump.name="Pump";machine.name="Machine";blue.codeMode=items.codeMode=true;blue.scriptEnabled=items.scriptEnabled=true;
        test.accept("Automation Guide has a normal shapeless book redstone paper recipe",()->{
            net.minecraft.inventory.InventoryCrafting grid=new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container(){public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer p){return true;}},3,3);grid.setInventorySlotContents(0,new ItemStack(Items.paper));grid.setInventorySlotContents(4,new ItemStack(Items.book));grid.setInventorySlotContents(8,new ItemStack(Items.redstone));ItemStack out=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(grid,w);check(out!=null&&out.getItem()==ModRegistry.automationGuide&&out.stackSize==1,"guide recipe missing");
        });
        test.accept("Exact water 200mb program compiles against real registered fluids and linked controls",()->{
            check(ScriptEngine.save(data,blue,code("FLUID \"water\" >= 200mb","PULSE \"Pump\" 7s")),blue.scriptError);ScriptEngine.poll(data,blue);check(!pump.powered&&!blue.scriptRules.get(0).latched,"100mb triggered 200mb rule");tank.water.fill(new FluidStack(FluidRegistry.WATER,100),true);ScriptEngine.poll(data,blue);
            check(pump.powered&&pump.pulseEnd-w.getTotalWorldTime()==140&&WirelessAccess.relay(pump).signal()&&w.getBlock(x+4,90,z)==Blocks.lit_redstone_lamp,"200mb did not emit real timed redstone");
        });
        test.accept("Stable true scripts preserve their edge and never extend a live pulse",()->{long deadline=pump.pulseEnd;ScriptEngine.poll(data,blue);check(pump.pulseEnd==deadline&&blue.scriptRules.get(0).latched,"stable truth retriggered");});
        test.accept("Invalid saves keep the working source compiled rules and latches",()->{
            String source=blue.script;ScriptRule rule=blue.scriptRules.get(0);check(!ScriptEngine.save(data,blue,code("FLUID \"unknown_liquid\" < 200mb","ON \"Pump\"")),"unknown fluid accepted");check(blue.script.equals(source)&&blue.scriptRules.get(0)==rule&&rule.latched&&blue.scriptErrorLine==1,"invalid save replaced working program");
            check(!ScriptEngine.save(data,blue,code("ITEM \"minecraft:unknown\" < 1","ON \"Pump\""))&&blue.scriptError.contains("Unknown item"),"unknown item accepted");
        });
        test.accept("Fluid API failures pause without treating missing data as zero or resetting the latch",()->{
            tank.unavailable=true;try{ScriptEngine.poll(data,blue);check(blue.scriptRules.get(0).latched&&blue.automationStatus.equals("FLUID DATA UNAVAILABLE"),"unavailable fluid became false/zero");}finally{tank.unavailable=false;}
        });
        test.accept("Low fluid conditions pause on missing API data and fire only from real quantities",()->{
            tank.water.setFluid(new FluidStack(FluidRegistry.WATER,100));tank.lava.setFluid(null);check(ScriptEngine.save(data,blue,code("FLUID \"water\" < 500mb","ON \"Pump\"")),blue.scriptError);WirelessAccess.off(data,pump);tank.unavailable=true;
            try{ScriptEngine.poll(data,blue);check(!pump.powered&&!blue.scriptRules.get(0).latched&&blue.automationStatus.equals("FLUID DATA UNAVAILABLE"),"missing fluid became zero and triggered low condition");}finally{tank.unavailable=false;}
            ScriptEngine.poll(data,blue);check(pump.powered&&blue.scriptRules.get(0).latched,"real low fluid did not trigger");tank.water.setFluid(new FluidStack(FluidRegistry.WATER,600));ScriptEngine.poll(data,blue);check(!blue.scriptRules.get(0).latched,"real high fluid did not reset low condition");
        });
        test.accept("Exposed same-fluid tank quantities are summed and shared by every script rule",()->{
            tank.water.setFluid(new FluidStack(FluidRegistry.WATER,100));tank.lava.setFluid(new FluidStack(FluidRegistry.WATER,100));String a=code("FLUID \"water\" >= 200mb","ON \"Pump\""),b=code("TANK FULL","OFF \"Machine\"");check(ScriptEngine.save(data,blue,a+"\n\n"+b),blue.scriptError);int reads=tank.reads;ScriptEngine.poll(data,blue);check(pump.powered&&pump.pulseEnd==0&&tank.reads==reads+1,"multiple fluid scans or no sum");
        });
        test.accept("All inventory tank conditions and ON OFF actions evaluate real machine data",()->{
            tank.water.setFluid(null);tank.lava.setFluid(null);check(ScriptEngine.save(data,blue,code("TANK EMPTY","OFF \"Pump\"")),blue.scriptError);ScriptEngine.poll(data,blue);check(!pump.powered,"OFF failed");tank.water.setFluid(new FluidStack(FluidRegistry.WATER,4000));tank.lava.setFluid(new FluidStack(FluidRegistry.LAVA,8000));check(ScriptEngine.save(data,blue,code("TANK FULL","ON \"Pump\"")),blue.scriptError);ScriptEngine.poll(data,blue);check(pump.powered&&pump.pulseEnd==0,"FULL/ON failed");
            chest.setInventorySlotContents(0,new ItemStack(Items.coal,16));check(ScriptEngine.save(data,items,code("ITEM \"minecraft:coal\" >= 16","ON \"Machine\"")),items.scriptError);ScriptEngine.poll(data,items);check(machine.powered,"item amount failed");chest.setInventorySlotContents(0,null);check(ScriptEngine.save(data,items,code("INVENTORY EMPTY","OFF \"Machine\"")),items.scriptError);ScriptEngine.poll(data,items);check(!machine.powered,"empty/off failed");
            for(int i=0;i<27;i++)chest.setInventorySlotContents(i,new ItemStack(Items.coal,64));check(ScriptEngine.save(data,items,code("INVENTORY FULL","ON \"Machine\"")),items.scriptError);ScriptEngine.poll(data,items);check(machine.powered,"inventory full failed");for(int i=0;i<27;i++)chest.setInventorySlotContents(i,null);
        });
        test.accept("The last simultaneous rising command wins without redstone oscillation",()->{
            tank.water.setFluid(null);tank.lava.setFluid(null);check(ScriptEngine.save(data,blue,code("TANK EMPTY","ON \"Pump\"")+"\n\n"+code("TANK EMPTY","OFF \"Pump\"")),blue.scriptError);ScriptEngine.poll(data,blue);check(!pump.powered&&blue.scriptRules.get(0).latched&&blue.scriptRules.get(1).latched,"conflict policy failed");ScriptEngine.poll(data,blue);check(!pump.powered,"stable contradictory rules oscillated");
        });
        test.accept("Pulse command overrides manual output uses configured default and keeps saved settings",()->{
            pump.pulseMode=false;pump.pulseSeconds=6;WirelessAccess.command(data,pump,"ON",0);check(WirelessAccess.command(data,pump,"PULSE",0)&&pump.pulseEnd-w.getTotalWorldTime()==120&&!pump.pulseMode&&pump.pulseSeconds==6,"default pulse or saved settings failed");long end=pump.pulseEnd;check(!WirelessAccess.command(data,pump,"PULSE",10)&&pump.pulseEnd==end,"pulse extended");WirelessAccess.command(data,pump,"ON",0);check(pump.pulseEnd==0&&pump.powered,"ON did not cancel pulse");WirelessAccess.command(data,pump,"OFF",0);check(!pump.powered&&pump.pulseEnd==0,"OFF did not cancel pulse");
        });
        test.accept("Duplicate names are rejected while explicit authorized control IDs remain unambiguous",()->{
            WirelessEndpoint duplicate=WirelessEndpoint.read(pump.write());duplicate.id=UUID.randomUUID();data.add(duplicate);try{check(!ScriptEngine.save(data,blue,code("TANK EMPTY","ON \"Pump\""))&&blue.scriptError.contains("More than one"),"duplicate chosen randomly");check(ScriptEngine.save(data,blue,code("TANK EMPTY","ON \"id:"+pump.id+"\"")),blue.scriptError);}finally{data.remove(duplicate);}
            WirelessEndpoint foreign=WirelessEndpoint.read(pump.write());foreign.id=UUID.randomUUID();foreign.owner=UUID.randomUUID();foreign.network=data.create(foreign.owner);foreign.name="Foreign";data.add(foreign);try{check(!ScriptEngine.save(data,blue,code("TANK EMPTY","ON \"id:"+foreign.id+"\"")),"foreign target accepted");}finally{data.remove(foreign);}
        });
        test.accept("Offline targets do not load chunks and an unavailable affected edge waits safely",()->{
            WirelessEndpoint offline=WirelessEndpoint.read(pump.write());offline.id=UUID.randomUUID();offline.name="Offline";offline.x=900000;offline.z=900000;data.add(offline);try{check(ScriptEngine.save(data,blue,code("TANK EMPTY","ON \"Offline\"")),blue.scriptError);ScriptEngine.poll(data,blue);check(!blue.scriptRules.get(0).latched&&blue.automationStatus.startsWith("WAITING")&&!w.getChunkProvider().chunkExists(offline.x>>4,offline.z>>4),"offline target consumed edge or loaded chunk");}finally{data.remove(offline);}
            check(ScriptEngine.save(data,blue,code("TANK EMPTY","ON \"Pump\"")),blue.scriptError);pump.enabled=false;ScriptEngine.poll(data,blue);check(!blue.scriptRules.get(0).latched,"unavailable target consumed edge");pump.enabled=true;ScriptEngine.poll(data,blue);check(blue.scriptRules.get(0).latched&&pump.powered,"resumed edge did not run");
        });
        test.accept("Switching SIMPLE CODE keeps both rule sets and only one mode executes",()->{
            items.rules[0].enabled=true;items.rules[0].item=new ItemStack(Items.coal);items.rules[0].minimum=1;items.rules[0].target=machine.id;items.rules[0].latched=false;chest.setInventorySlotContents(0,new ItemStack(Items.coal));
            check(ScriptEngine.save(data,items,code("ITEM \"minecraft:stone\" >= 1","OFF \"Machine\"")),items.scriptError);WirelessAccess.off(data,machine);items.codeMode=true;WirelessScheduler.poll(data,items);check(!machine.powered&&!items.rules[0].latched,"SIMPLE ran while CODE selected");items.codeMode=false;WirelessScheduler.poll(data,items);check(machine.powered&&items.rules[0].latched&&items.scriptRules.size()==1,"legacy SIMPLE lost or did not execute");items.codeMode=true;chest.setInventorySlotContents(0,null);WirelessAccess.off(data,machine);
        });
        test.accept("Editor snapshots omit source text and foreign uploads cannot overwrite a selected program",()->{
            p.inventory.currentItem=0;TerminalContainer c=new TerminalContainer(p);c.action(WirelessCommand.TAB,2,0,"");c.action(WirelessCommand.SELECT,0,0,blue.id.toString());NBTTagCompound view=c.snapshot();check(!view.hasKey("Script")&&view.hasKey("ScriptRevision"),"full script broadcast in periodic snapshot");String before=blue.script;c.saveScript(UUID.randomUUID(),"bad");check(blue.script.equals(before),"foreign upload replaced program");
        });
        test.accept("Missing saved registry IDs disable code instead of firing false low-resource rules",()->{
            WirelessAccess.off(data,pump);WirelessEndpoint loaded=WirelessEndpoint.read(blue.write());loaded.codeMode=true;loaded.scriptEnabled=true;ScriptRule r=loaded.scriptRules.get(0);r.kind=ScriptRule.Kind.FLUID;r.identifier="unknown_liquid";r.less=true;r.amount=500;
            ScriptEngine.poll(data,loaded);check(!loaded.scriptEnabled&&!pump.powered&&loaded.scriptError.contains("Unknown fluid"),"missing saved fluid became zero");
            loaded=WirelessEndpoint.read(blue.write());loaded.scriptEnabled=true;r=loaded.scriptRules.get(0);r.kind=ScriptRule.Kind.ITEM;r.identifier="minecraft:unknown";r.less=true;ScriptEngine.poll(data,loaded);check(!loaded.scriptEnabled&&!pump.powered&&loaded.scriptError.contains("Unknown item"),"missing saved item became zero");
            NBTTagCompound corrupt=blue.write();StringBuilder huge=new StringBuilder();for(int i=0;i<9000;i++)huge.append('x');corrupt.setString("Script",huge.toString());WirelessEndpoint safe=WirelessEndpoint.read(corrupt);check(!safe.scriptEnabled&&safe.script.isEmpty()&&safe.scriptRules.isEmpty(),"oversized saved code could break document packets");
        });
        test.accept("Script persistence retains mode enabled source compiled target and trigger memory",()->{
            blue.scriptEnabled=true;blue.codeMode=true;blue.scriptRules.get(0).latched=true;NBTTagCompound n=blue.write();WirelessEndpoint copy=WirelessEndpoint.read(n);check(copy.codeMode&&copy.scriptEnabled&&copy.script.equals(blue.script)&&copy.scriptRules.get(0).latched&&copy.scriptRules.get(0).target.equals(pump.id)&&copy.rules.length==8,"script NBT lost state");
        });
        if(cpw.mods.fml.common.Loader.isModLoaded("hbm"))hbm(test);
        tank.water.setFluid(new FluidStack(FluidRegistry.WATER,100));tank.lava.setFluid(null);pump.pulseSeconds=5;pump.pulseMode=false;machine.pulseMode=false;WirelessAccess.off(data,pump);WirelessAccess.off(data,machine);
        check(ScriptEngine.save(data,blue,code("FLUID \"water\" >= 200mb","PULSE \"Pump\" 7s")),blue.scriptError);check(ScriptEngine.save(data,items,code("ITEM \"minecraft:stone\" >= 1","PULSE \"Machine\" 5s")),items.scriptError);blue.scriptRules.get(0).latched=false;items.scriptRules.get(0).latched=false;blue.scriptEnabled=items.scriptEnabled=true;start=w.getTotalWorldTime();data.markDirty();
        try{NBTTagCompound n=new NBTTagCompound();n.setString("Blue",blue.id.toString());n.setString("Items",items.id.toString());n.setString("Pump",pump.id.toString());n.setString("Machine",machine.id.toString());n.setInteger("X",x);n.setInteger("Z",z);CompressedStreamTools.writeCompressed(n,new FileOutputStream("code-fixtures.nbt"));}catch(IOException e){throw new RuntimeException(e);}
    }
    private static void hbm(BiConsumer<String,Runnable> test){for(String name:new String[]{"com.hbm.tileentity.machine.TileEntityMachineChemicalPlant","com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank","com.hbm.tileentity.machine.storage.TileEntityMachineBigAssTank"})test.accept("Actual HBM fluid code threshold reads "+name.substring(name.lastIndexOf('.')+1),()->{
        try{int tx=x+10,tz=z+8;world.setBlock(tx,90,tz,OreCompatibilityFixtures.tileOre,0,3);TileEntity tile=(TileEntity)Class.forName(name).newInstance();world.setTileEntity(tx,90,tz,tile);Object all=tile.getClass().getMethod("getAllTanks").invoke(tile),tank=java.lang.reflect.Array.get(all,0),water=Class.forName("com.hbm.inventory.fluid.Fluids").getField("WATER").get(null);tank.getClass().getMethod("setTankType",water.getClass()).invoke(tank,water);tank.getClass().getMethod("setFill",int.class).invoke(tank,100);
            WirelessEndpoint source=WirelessEndpoint.read(blue.write());source.id=UUID.randomUUID();source.physical=false;source.x=tx;source.y=90;source.z=tz;source.side=5;source.tileToken=TargetAnchors.bind(tile);source.block=String.valueOf(net.minecraft.block.Block.blockRegistry.getNameForObject(world.getBlock(tx,90,tz)));WirelessAccess.off(data,pump);
            String id="hbm:"+water.getClass().getMethod("getName").invoke(water);check(ScriptEngine.save(data,source,code("FLUID \""+id+"\" >= 200mb","PULSE \"Pump\" 7s")),source.scriptError);ScriptEngine.poll(data,source);check(!pump.powered,"HBM100 triggered200");tank.getClass().getMethod("setFill",int.class).invoke(tank,200);ScriptEngine.poll(data,source);check(pump.powered&&pump.pulseEnd-world.getTotalWorldTime()==140,"HBM200 did not pulse");world.removeTileEntity(tx,90,tz);world.setBlockToAir(tx,90,tz);WirelessAccess.off(data,pump);
        }catch(ReflectiveOperationException e){throw new RuntimeException(e);}
    });}
    public static void tick(WorldServer w,BiConsumer<String,Runnable> test){if(tank==null)return;long now=w.getTotalWorldTime(),age=now-start;
        if(age==10){tank.water.fill(new FluidStack(FluidRegistry.WATER,100),true);chest.setInventorySlotContents(0,new ItemStack(Blocks.stone));}
        if(rise<0&&pump.powered)rise=now;if(itemRise<0&&machine.powered)itemRise=now;
        if(rise>=0&&now<rise+140&&!WirelessAccess.relay(pump).signal())held=false;
        if(itemRise>=0&&now<itemRise+100&&!WirelessAccess.relay(machine).signal())itemHeld=false;
        if(rise>=0&&now>=rise+140&&!done){done=true;test.accept("Real water rising through 200mb produces exactly 140 ticks of redstone then OFF",()->check(held&&!pump.powered&&blue.scriptRules.get(0).latched,"fluid timed signal incorrect"));}
        if(itemRise>=0&&now>=itemRise+100&&!itemDone){itemDone=true;test.accept("Real stone code insertion produces exactly 100 ticks and stable truth never retriggers",()->check(itemHeld&&!machine.powered&&items.scriptRules.get(0).latched,"item timed signal incorrect"));}
        if(age==275)test.accept("CODE automation runs without GUI players or duplicate SIMPLE actions",()->check(done&&itemDone&&!pump.powered&&!machine.powered,"code runtime scenario incomplete"));
    }
    public static void restore(WorldServer w,BiConsumer<String,Runnable> test){test.accept("Actual restart restores enabled code programs compiled bindings and latched truth without duplicate pulses",()->{
        try{NBTTagCompound n=CompressedStreamTools.readCompressed(new FileInputStream("code-fixtures.nbt"));int x=n.getInteger("X"),z=n.getInteger("Z");w.getChunkFromChunkCoords(x>>4,z>>4);WirelessData data=WirelessData.get(w);WirelessEndpoint source=data.devices.get(UUID.fromString(n.getString("Blue"))),target=data.devices.get(UUID.fromString(n.getString("Pump")));check(source.codeMode&&source.scriptEnabled&&source.script.contains("200mb")&&source.scriptRules.get(0).target.equals(target.id)&&source.scriptRules.get(0).latched,"code not restored");
            // The fixture tank keeps its starting water in world NBT below; polling a saved true edge must remain quiet.
            WirelessAccess.off(data,target);ScriptEngine.poll(data,source);check(!target.powered&&source.scriptRules.get(0).latched,"restart duplicated a saved true edge");
        }catch(IOException e){throw new RuntimeException(e);}
    });}
}
