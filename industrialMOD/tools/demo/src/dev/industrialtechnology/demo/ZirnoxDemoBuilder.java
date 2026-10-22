package dev.industrialtechnology.demo;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import com.mojang.authlib.GameProfile;
import dev.industrialtechnology.core.ModRegistry;
import dev.industrialtechnology.wireless.*;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.*;
import net.minecraft.world.*;
import net.minecraftforge.common.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Offline world preparation only. No blocks/items registered; never installed in the user's mods. */
@Mod(modid="industrialtech_demo_builder",name="ZIRNOX demonstration world builder",version="1",dependencies="required-after:industrialtech;required-after:hbm")
public final class ZirnoxDemoBuilder {
    private WorldServer world;private FakePlayer player;private TileEntity reactor;private TileEntityChest chest;private WirelessEndpoint blue,green;
    private int ticks,phase,start,natural,thorium;private boolean ready,finished;private final List<String> report=new ArrayList<String>();
    private static final UUID OWNER=UUID.fromString("f7b08e6d-caa1-39fd-b1c4-ca8ea5f9ed43");
    public static final class DemoPlayer extends FakePlayer {
        DemoPlayer(WorldServer w){super(w,new GameProfile(OWNER,"Zahin"));capabilities.isCreativeMode=true;capabilities.allowEdit=true;capabilities.allowFlying=true;capabilities.disableDamage=true;}
        @Override public void addChatMessage(net.minecraft.util.IChatComponent message){}
    }
    @Mod.EventHandler public void init(FMLInitializationEvent event){FMLCommonHandler.instance().bus().register(this);}
    @Mod.EventHandler public void start(FMLServerStartedEvent event){ready=true;}
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event){
        if(!ready||finished||event.phase!=TickEvent.Phase.END||++ticks<40)return;
        try{
            if(phase==0&&Boolean.getBoolean("industrialtech.verifyDemo")){verifySaved();return;}
            if(phase==0){setup();phase=1;start=ticks;return;}
            check(!reactor.getClass().getField("isOn").getBoolean(reactor),"demonstration reactor unexpectedly powered");
            if(phase==1){if(countCore()!=24){check(ticks-start<1000,"feeder timeout: "+blue.reactorMessage);return;}
                Set<String> seen=new HashSet<String>();for(int i=0;i<24;i++){ItemStack rod=((IInventory)reactor).getStackInSlot(i);check(rod.getItemDamage()==meta(i),"wrong fuel position "+i);check(rod.hasTagCompound()&&seen.add(rod.getTagCompound().getString("DemoFuel")),"real rod data lost/duplicated");}check(seen.size()==24&&countChest()==0,"fuel conservation failed");
                pass("All 24 native reactor positions filled by the real scheduled Blue feeder, preserving 24 unique rod tags");stockChest();ItemStack removed=((IInventory)reactor).getStackInSlot(8);((IInventory)reactor).setInventorySlotContents(8,null);player.inventory.setInventorySlotContents(4,removed);phase=2;start=ticks;return;}
            if(phase==2){if(((IInventory)reactor).getStackInSlot(8)==null){check(ticks-start<200,"refill timeout");return;}
                check(countCore()==24&&countChest()==26,"automatic refill did not consume exactly one spare rod");check(((IInventory)reactor).getStackInSlot(8).getItemDamage()==thorium,"refill chose wrong fuel");pass("Removing one installed rod automatically refills its exact slot from the adjacent supply chest");stockChest();finish();}
        }catch(Throwable failure){report.add("FAIL: "+failure);failure.printStackTrace();stop(false);}
    }
    private void verifySaved()throws Exception{
        world=MinecraftServer.getServer().worldServerForDimension(0);for(int x=-2;x<=1;x++)for(int z=-2;z<=1;z++)world.getChunkFromChunkCoords(x,z);
        natural=variant("NATURAL_URANIUM_FUEL");thorium=variant("TH232");reactor=world.getTileEntity(0,64,0);chest=(TileEntityChest)world.getTileEntity(-2,67,-1);
        check(reactor instanceof IInventory&&chest!=null,"demonstration blocks missing after restart");check(countCore()==24&&countChest()==27,"saved fuel counts changed");
        for(WirelessEndpoint e:WirelessData.get(world).devices.values()){check(OWNER.equals(e.owner),"wrong demo owner");if(e.kind==WirelessEndpoint.Kind.AUTOMATION)blue=e;else if(e.kind==WirelessEndpoint.Kind.MONITOR)green=e;}
        check(blue!=null&&green!=null&&blue.reactor.running,"saved links/settings missing");check(HbmReactors.access(blue).online()&&HbmReactors.access(green).online(),"saved physical links cannot resolve native reactor");
        for(int i=0;i<24;i++)check(((IInventory)reactor).getStackInSlot(i).getItemDamage()==meta(i)&&blue.reactor.enabled[i]&&blue.reactor.metadata[i]==meta(i),"saved fuel position mismatch "+i);
        pass("Actual restart restores both physical links, ownership, 24 correct native rods, 27 chest spares and enabled feeder");
        player=new DemoPlayer(world);player.readFromNBT(CompressedStreamTools.readCompressed(new FileInputStream("demo-player.dat")));player.inventory.currentItem=0;
        TerminalContainer c=new TerminalContainer(player);c.action(WirelessCommand.TAB,2,0,"");c.action(WirelessCommand.SELECT,0,0,blue.id.toString());c.action(ReactorTerminal.VIEW,1,0,"");
        check(c.selected!=null&&c.reactors.view&&c.reactors.snapshot(blue).getTagList("Fuels",10).tagCount()==24,"user's saved terminal cannot open the native fuel grid");
        pass("The user's saved terminal opens its owned Blue reactor panel with all 24 native fuel positions");check(!reactor.getClass().getField("isOn").getBoolean(reactor),"saved reactor unexpectedly powered");pass("Demonstration reactor remains switched off after a real native restart");stop(true);
    }
    private static void check(boolean yes,String message){if(!yes)throw new IllegalStateException(message);}
    private void pass(String text){report.add("PASS: "+text);System.out.println("DEMO PASS: "+text);}
    private int meta(int slot){return slot<7?natural:thorium;}
    private ItemStack fuel(int type){try{return new ItemStack((Item)Class.forName("com.hbm.items.ModItems").getField("rod_zirnox").get(null),1,type);}catch(Exception x){throw new RuntimeException(x);}}
    private int variant(String name)throws Exception{Object[] types=Class.forName("com.hbm.items.machine.ItemZirnoxRod$EnumZirnoxType").getEnumConstants();for(int i=0;i<types.length;i++)if(((Enum)types[i]).name().equals(name))return i;throw new IllegalStateException("Fuel type missing: "+name);}
    private void setup()throws Exception{
        world=MinecraftServer.getServer().worldServerForDimension(0);for(int x=-2;x<=1;x++)for(int z=-2;z<=1;z++)world.getChunkFromChunkCoords(x,z);
        world.setWorldTime(6000);world.setSpawnLocation(-8,67,-6);world.getGameRules().setOrCreateGameRule("doMobSpawning","false");world.getGameRules().setOrCreateGameRule("doDaylightCycle","false");world.getGameRules().setOrCreateGameRule("keepInventory","true");
        for(int x=-12;x<=8;x++)for(int z=-9;z<=8;z++)world.setBlock(x,63,z,Blocks.stonebrick,0,2);
        // A viewing deck outside the native 5x5 reactor footprint, level with the upper access connection.
        for(int x=-10;x<=-4;x++)for(int z=-7;z<=3;z++)world.setBlock(x,66,z,Blocks.stained_hardened_clay,7,2);
        for(int x=-10;x<=-4;x++)for(int z=-7;z<=3;z++)if(x==-10||x==-4||z==-7||z==3)world.setBlock(x,66,z,Blocks.stained_hardened_clay,3,2);
        Block block=(Block)Class.forName("com.hbm.blocks.ModBlocks").getField("reactor_zirnox").get(null);world.setBlock(0,64,0,block,12,3);Class<?> type=block.getClass();Method fill=null;
        while(type!=null){try{fill=type.getDeclaredMethod("fillSpace",World.class,int.class,int.class,int.class,ForgeDirection.class,int.class);break;}catch(NoSuchMethodException next){type=type.getSuperclass();}}
        check(fill!=null,"native multiblock helper missing");fill.setAccessible(true);fill.invoke(block,world,0,64,0,ForgeDirection.NORTH,0);reactor=world.getTileEntity(0,64,0);check(reactor instanceof IInventory,"native reactor controller missing");
        reactor.getClass().getField("isOn").setBoolean(reactor,false);reactor.getClass().getField("heat").setInt(reactor,0);
        natural=variant("NATURAL_URANIUM_FUEL");thorium=variant("TH232");player=new DemoPlayer(world);player.inventory.setInventorySlotContents(0,new ItemStack(ModRegistry.terminal));player.inventory.currentItem=0;WirelessAccess.terminal(player,player.getHeldItem(),true);
        blue=link(ModRegistry.blueStick,-2,67,0,4);green=link(ModRegistry.greenStick,-2,65,0,4);blue.name="DEMO - ZIRNOX fuel feeder";green.name="DEMO - ZIRNOX monitor";
        check(world.isAirBlock(-2,67,-1),"supply space is occupied");world.setBlock(-2,66,-1,Blocks.stained_hardened_clay,5,3);world.setBlock(-2,67,-1,Blocks.chest,3,3);chest=(TileEntityChest)world.getTileEntity(-2,67,-1);check(chest!=null,"supply chest missing");
        HbmReactors.Access access=HbmReactors.access(blue);check(access.online()&&access.core==reactor&&access.layout.positions.size()==24,access.message);blue.reactor.supply=2;
        for(int i=0;i<24;i++){((IInventory)reactor).setInventorySlotContents(i,null);ItemStack rod=fuel(meta(i));blue.reactor.assign(i,rod);blue.reactor.enabled[i]=true;rod.setTagCompound(new NBTTagCompound());rod.getTagCompound().setString("DemoFuel","original-"+i);chest.setInventorySlotContents(i,rod);}
        blue.reactor.running=true;blue.reactorNextPoll=Long.MIN_VALUE;WirelessData.get(world).markDirty();
        // These signs are outside the reactor's native block footprint.
        sign(-7,67,-3,0,"ZIRNOX DEMO","CHEST + BLUE","Look this way","Reactor is OFF");
        sign(-5,67,-1,8,"SUPPLY CHEST","next to BLUE","same height","NO itemduct");
        sign(-8,67,1,8,"Open terminal","AUTOMATION","DEMO - ZIRNOX","then REACTOR");
        pass("Native ZIRNOX, real Blue/Green sticks and immediately adjacent chest built using the installed HBM version");
        pass("Blue access is (-2,67,0), chest is (-2,67,-1), torch is (-3,67,0): no pipe between chest and reactor");
        pass("Terminal and both physical links belong to the user's Zahin profile");
    }
    private WirelessEndpoint link(Item item,int x,int y,int z,int side){player.setPosition(x-1.5,y,z-1.5);player.inventory.currentItem=1;player.inventory.setInventorySlotContents(1,new ItemStack(item));item.onItemUseFirst(player.getHeldItem(),player,world,x,y,z,side,.5f,.5f,.5f);ForgeDirection d=ForgeDirection.getOrientation(side);TileEntity tile=world.getTileEntity(x+d.offsetX,y+d.offsetY,z+d.offsetZ);check(tile instanceof TileRedstoneRelay,"physical Blue/Green torch placement failed");WirelessEndpoint e=WirelessData.get(world).devices.get(((TileRedstoneRelay)tile).endpoint);check(e!=null,"link failed");return e;}
    private void sign(int x,int y,int z,int direction,String a,String b,String c,String d){world.setBlock(x,y,z,Blocks.standing_sign,direction,3);TileEntitySign t=(TileEntitySign)world.getTileEntity(x,y,z);t.signText=new String[]{a,b,c,d};t.markDirty();world.markBlockForUpdate(x,y,z);}
    private int countCore(){int n=0;for(int i=0;i<24;i++)if(((IInventory)reactor).getStackInSlot(i)!=null)n++;return n;}
    private int countChest(){int n=0;for(int i=0;i<27;i++){ItemStack s=chest.getStackInSlot(i);if(s!=null)n+=s.stackSize;}return n;}
    private void stockChest(){for(int i=0;i<27;i++){ItemStack rod=fuel(i<8?natural:thorium);rod.setTagCompound(new NBTTagCompound());rod.getTagCompound().setString("DemoFuel","spare-"+i);chest.setInventorySlotContents(i,rod);}chest.markDirty();}
    private ItemStack book(){ItemStack book=new ItemStack(Items.written_book);NBTTagCompound n=new NBTTagCompound();n.setString("title","ZIRNOX Chest Demo");n.setString("author","Codex");NBTTagList pages=new NBTTagList();
        for(String text:new String[]{"ZIRNOX CHEST DEMO\n\nThe reactor is OFF.\nThe Blue feeder is ON.\n\nYour chest is already at the correct position. No itemduct connects it to the reactor.","WHERE THE CHEST GOES\n\nBlue touches reactor access (-2,67,0).\n\nThe chest is at (-2,67,-1).\n\nThe chest directly touches that access block and is at the same height.","SEE THE PATTERN\n\nHold your terminal and right-click air.\n\nAutomation > DEMO - ZIRNOX fuel feeder > Reactor.\n\nAll 24 positions are configured. Top two rows: natural uranium. Other rows: Thorium-232.","WATCH IT WORK\n\nOpen the reactor and take out one fuel rod. Keep it in your inventory.\n\nBlue takes a matching spare from the chest and refills that exact position within about one second.","YOUR OWN REACTOR\n\nFeed any supply pipes INTO the adjacent chest. Do not run a pipe from that chest into ZIRNOX.\n\nSupply: NORTH is already selected here. Apply saves the pattern; Enable starts the feeder."})pages.appendTag(new NBTTagString(text));n.setTag("pages",pages);book.setTagCompound(n);return book;}
    private void finish()throws Exception{
        player.inventory.currentItem=0;player.inventory.setInventorySlotContents(1,book());player.inventory.setInventorySlotContents(2,new ItemStack(ModRegistry.blueStick,8));player.inventory.setInventorySlotContents(3,new ItemStack(Blocks.chest,8));player.inventory.setInventorySlotContents(5,fuel(natural));player.inventory.setInventorySlotContents(6,fuel(thorium));player.inventory.setInventorySlotContents(7,new ItemStack(ModRegistry.greenStick,8));player.inventory.setInventorySlotContents(8,new ItemStack(ModRegistry.automationGuide));
        player.setPositionAndRotation(-7.5,67,-5.5,-50,7);player.capabilities.isFlying=false;player.inventory.markDirty();NBTTagCompound p=new NBTTagCompound();player.writeToNBT(p);p.setInteger("Dimension",0);p.setFloat("Health",20);p.setShort("HealF",(short)20);p.setInteger("foodLevel",20);p.setBoolean("Invulnerable",true);
        CompressedStreamTools.writeCompressed(p,new FileOutputStream("demo-player.dat"));
        check(HbmReactors.access(blue).online()&&countCore()==24&&countChest()==27,"final arrangement invalid");WirelessData.get(world).markDirty();reactor.markDirty();chest.markDirty();world.saveAllChunks(true,null);
        pass("24 correct installed rods, 27 spare chest rods, explanatory book and linked player terminal saved");stop(true);
    }
    private void stop(boolean ok){finished=true;try{report.add("Failed: "+(ok?0:1));Files.write(Paths.get(Boolean.getBoolean("industrialtech.verifyDemo")?"demo-restart-report.txt":"demo-report.txt"),report,StandardCharsets.UTF_8);}catch(Exception e){e.printStackTrace();}MinecraftServer.getServer().initiateShutdown();}
}
