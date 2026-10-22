package dev.industrialtechnology.test;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import dev.industrialtechnology.core.ModConfig;
import dev.industrialtechnology.core.ModRegistry;
import dev.industrialtechnology.core.IndustrialTechnology;
import dev.industrialtechnology.machine.MachineContainer;
import dev.industrialtechnology.machine.TileMachine;
import dev.industrialtechnology.machine.TilePoweredMachine;
import dev.industrialtechnology.machine.MachineStatus;
import dev.industrialtechnology.quarry.*;
import dev.industrialtechnology.chunkloading.TileChunkLoader;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Container;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.WorldServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/** Real Forge/Minecraft tests. Loaded only by -PtestServer, in a disposable server folder. */
@Mod(modid="industrialtech_tests", name="Industrial Technology Integration Tests", version="1", dependencies="required-after:industrialtech")
public final class ServerIntegrationMod {
    @SidedProxy(clientSide="dev.industrialtechnology.test.ClientTestProxy",serverSide="dev.industrialtechnology.test.TestProxy")
    public static TestProxy proxy;
    private WorldServer world;
    private boolean started, finished;
    private int ticks, blockedX = Integer.MIN_VALUE, observedFortune = -1;
    private boolean observedSilk, injectOverflow;
    private final List<String> results = new ArrayList<String>();
    private int failed, fixture;
    private int speedStage, speedTicks;
    private final int[] speedInputs = {200,2000,5000,10000,15000,20000,20000,20000};
    private final int[] speedTiers = {2,2,2,2,2,2,0,1};
    private final int[] expectedSpeeds = {1,10,25,50,75,100,100,100};
    private TileQuarry[] speedQuarries;
    private long[] baseline, received, previousMined, stoppedMined;
    private int[] stoppedCursor;
    private static final int BASE = 1600;
    private static final int RESTORE_QX = 1800, RESTORE_LX = 1820, RESTORE_Z = 1800;

    @Mod.EventHandler public void init(FMLInitializationEvent event) {
        OreCompatibilityFixtures.register();WirelessTestFixtures.register();
        FMLCommonHandler.instance().bus().register(this); MinecraftForge.EVENT_BUS.register(this);
        proxy.init();
    }
    @Mod.EventHandler public void serverStarted(FMLServerStartedEvent event) { started = true; }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event) {
        if (!started || finished || !MinecraftServer.getServer().isDedicatedServer()) return;
        if (speedStage > 0) {
            if (event.phase == TickEvent.Phase.START) {
                for (int i=0;i<speedQuarries.length;i++) received[i]+=speedQuarries[i].receiveEnergy(ForgeDirection.NORTH,speedInputs[i],false);
            } else runSpeedTick();
            return;
        }
        if (event.phase != TickEvent.Phase.END || ++ticks < 60) return;
        world = MinecraftServer.getServer().worldServerForDimension(0);
        try {
            if (new File("integration-phase1.done").exists()) { restoreTests(); finish(); }
            else { mainTests(); startSpeedTests(); }
        } catch (Throwable e) { fail("Harness setup", e); }
        if (speedStage == 0 && !finished) finish();
    }
    @SubscribeEvent public void harvest(BlockEvent.HarvestDropsEvent event) {
        if (event.world != world) return;
        observedFortune = event.fortuneLevel; observedSilk = event.isSilkTouching;
        if (injectOverflow) { event.drops.add(new ItemStack(Items.diamond, 120)); event.dropChance = 1; }
    }
    @SubscribeEvent public void protect(BlockEvent.BreakEvent event) {
        if (event.world == world && event.x == blockedX && event.y == 64) event.setCanceled(true);
    }
    private void mainTests() throws Exception {
        test("Legacy slow defaults migrate and loader RF configuration is removed", () -> {
            try {
                File cfg=new File("legacy-migration-test.cfg");
                String data="quarrymk1 {\n I:rfPerBlock=1600\n I:minimumTicksPerBlock=10\n}\n"+
                    "quarrymk2 {\n I:rfPerBlock=1200\n I:minimumTicksPerBlock=7\n}\n"+
                    "quarrymk3 {\n I:rfPerBlock=1000\n I:minimumTicksPerBlock=5\n}\n"+
                    "performance {\n I:removalsPerMachinePerTick=1\n I:scansPerMachinePerTick=64\n}\nenergy {\n I:chunkLoaderRFPerTick=123\n}\n";
                Files.write(cfg.toPath(),data.getBytes(StandardCharsets.UTF_8));ModConfig.load(cfg);
                assertTrue(ModConfig.energyCost[0]==4000&&ModConfig.energyCost[1]==4000&&ModConfig.energyCost[2]==4000&&ModConfig.removalsPerTick==5,"old delay/cost retained");
                String updated=new String(Files.readAllBytes(cfg.toPath()),StandardCharsets.UTF_8);
                assertTrue(!updated.contains("chunkLoaderRFPerTick")&&!updated.contains("minimumTicksPerBlock"),"obsolete settings retained");cfg.delete();
            } catch(Exception e) { throw new RuntimeException(e); }
        });
        for (int dx=-2;dx<=2;dx++) for (int dz=-2;dz<=2;dz++) world.getChunkFromChunkCoords((BASE>>4)+dx,(BASE>>4)+dz);
        for (int tier=0;tier<3;tier++) {
            final int index=tier;
            test("MK"+(tier+1)+" real Fortune harvest and energy cost", () -> {
                TileQuarry q=quarry(index); target(q,Blocks.diamond_ore,0); observedFortune=-1; observedSilk=false;
                int before=q.energy; assertTrue(MiningEngine.step(q)==MiningEngine.Result.MINED,"did not mine");
                assertTrue(world.isAirBlock(q.xCoord&~15,64,q.zCoord&~15),"ore remains");
                assertTrue(count(q,Items.diamond)>0,"missing diamond drops");
                assertTrue(observedFortune==index+1&&!observedSilk,"incorrect enchantment event");
                assertTrue(q.energy==before-q.cost(),"wrong RF cost"); destroy(q);
            });
        }
        test("Silk Touch stone and ore, mutually exclusive modes", () -> {
            TileQuarry q=quarry(2); q.setMode(true); target(q,Blocks.stone,0); MiningEngine.step(q);
            assertTrue(count(q,Item.getItemFromBlock(Blocks.stone))==1&&count(q,Item.getItemFromBlock(Blocks.cobblestone))==0,"wrong silk stone");
            assertTrue(observedSilk&&q.fortuneLevel()==0,"Fortune remains active");
            target(q,Blocks.diamond_ore,0); MiningEngine.step(q);
            assertTrue(count(q,Item.getItemFromBlock(Blocks.diamond_ore))==1,"wrong silk ore");
            q.setMode(false); assertTrue(!q.usesSilk()&&q.fortuneLevel()==3,"Silk remains active"); destroy(q);
        });
        test("Inventory TileEntities below the quarry remain intact with all their contents", () -> {
            TileQuarry q=quarry(0); q.setMode(true); target(q,Blocks.chest,0);
            TileEntityChest chest=(TileEntityChest)world.getTileEntity(q.xCoord&~15,64,q.zCoord&~15);
            chest.setInventorySlotContents(0,new ItemStack(Items.diamond,7)); MiningEngine.step(q);
            assertTrue(chest==world.getTileEntity(q.xCoord&~15,64,q.zCoord&~15)&&chest.getStackInSlot(0).stackSize==7,"protected chest changed");
            assertTrue(count(q,Items.diamond)==0&&count(q,Item.getItemFromBlock(Blocks.chest))==0,"protected chest generated drops"); destroy(q);
        });
        test("Metadata preserved through supported Silk harvest", () -> {
            TileQuarry q=quarry(1); q.setMode(true); target(q,Blocks.wool,11); MiningEngine.step(q);
            ItemStack found=null; for(ItemStack s:q.output.slots)if(s!=null&&s.getItem()==Item.getItemFromBlock(Blocks.wool))found=s;
            assertTrue(found!=null&&found.getItemDamage()==11,"wool metadata lost"); destroy(q);
        });
        test("Bedrock and unbreakable blocks are skipped without RF charge", () -> {
            TileQuarry q=quarry(0); target(q,Blocks.bedrock,0); int before=q.energy;
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.SKIPPED,"bedrock not skipped");
            assertTrue(world.getBlock(q.xCoord&~15,64,q.zCoord&~15)==Blocks.bedrock&&before==q.energy,"bedrock changed or RF charged"); destroy(q);
        });
        test("Forge BreakEvent protection prevents removal and drops", () -> {
            TileQuarry q=quarry(0); target(q,Blocks.diamond_ore,0); blockedX=q.xCoord&~15; int before=q.energy;
            try {
                assertTrue(MiningEngine.step(q)==MiningEngine.Result.SKIPPED,"protection bypassed");
                assertTrue(world.getBlock(blockedX,64,q.zCoord&~15)==Blocks.diamond_ore&&before==q.energy&&count(q,Items.diamond)==0,"protected ore changed");
            } finally { blockedX=Integer.MIN_VALUE; destroy(q); }
        });
        test("Zero RF and one 200-RF packet cannot mine", () -> {
            TileQuarry q=quarry(2); target(q,Blocks.stone,0); q.energy=0; q.work=0; q.enabled=true; q.inputAverage=0;
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.WAIT,"zero RF mined");
            q.receiveEnergy(ForgeDirection.NORTH,200,false); q.updateEntity();
            assertTrue(!q.ready()&&MiningEngine.step(q)==MiningEngine.Result.WAIT,"one packet mined at full speed"); destroy(q);
        });
        test("RF simulation is side-effect free and six sides share one tick cap", () -> {
            TileQuarry q=quarry(0); q.energy=0;
            assertTrue(q.receiveEnergy(ForgeDirection.NORTH,100,true)==100&&q.energy==0,"simulation mutated RF");
            int a=q.receiveEnergy(ForgeDirection.NORTH,15000,false),b=q.receiveEnergy(ForgeDirection.SOUTH,15000,false);
            assertTrue(a==15000&&b==5000&&q.energy==20000,"aggregate RF cap bypassed");
            assertTrue(q.extractEnergy(ForgeDirection.UP,20000,false)==0&&q.energy==20000,"quarry exported RF"); destroy(q);
        });
        test("200 RF/t work progress is one hundred times slower than 20000 RF/t", () -> {
            TileQuarry slow=quarry(2),fast=quarry(2); target(slow,Blocks.stone,0); target(fast,Blocks.stone,0);
            slow.enabled=fast.enabled=true; slow.work=fast.work=0; slow.energy=fast.energy=5000;
            slow.inputAverage=200; fast.inputAverage=20000;
            // Force same-tick sampling stable via one initial tile update, then set the measured meter inputs.
            slow.updateEntity();fast.updateEntity();slow.inputAverage=200;fast.inputAverage=20000;slow.work=fast.work=0;
            slow.updateEntity();fast.updateEntity();
            assertTrue(Math.abs(slow.work-.05)<.0001&&Math.abs(fast.work-5)<.0001,"RF speed scaling incorrect"); destroy(slow);destroy(fast);
        });
        test("Full output pauses before removing another block", () -> {
            TileQuarry q=quarry(0); target(q,Blocks.diamond_ore,0);
            for(int i=0;i<54;i++)q.setInventorySlotContents(i,new ItemStack(Items.iron_ingot,64));
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.WAIT,"full output still mined");
            q.enabled=true;q.updateEntity();assertTrue(q.status==MachineStatus.OUTPUT_FULL,"missing OUTPUT FULL status"); destroy(q);
        });
        test("Event-modified drops wait before world removal and commit once output space is available", () -> {
            TileQuarry q=quarry(0); target(q,Blocks.stone,0);
            for(int i=0;i<53;i++)q.setInventorySlotContents(i,new ItemStack(Items.iron_ingot,64));
            injectOverflow=true;
            int energy=q.energy;
            try{assertTrue(MiningEngine.step(q)==MiningEngine.Result.WAIT,"oversized harvest removed the block");}finally{injectOverflow=false;}
            assertTrue(world.getBlock(q.xCoord&~15,64,q.zCoord&~15)==Blocks.stone&&q.energy==energy&&count(q,Items.diamond)==0,"uncommitted harvest changed world or inventory");
            q.decrStackSize(0,64);q.decrStackSize(1,64);
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.MINED&&q.output.pending.isEmpty()&&count(q,Items.diamond)==120,"cached harvest was lost or rerolled");
            NBTTagCompound tag=new NBTTagCompound();q.writeToNBT(tag);TileQuarry restored=new TileQuarry.MK1();restored.readFromNBT(tag);
            assertTrue(count(restored,Items.diamond)==120,"committed harvest NBT changed");destroy(q);
        });
        test("ISidedInventory extraction rejects insertion and handles partial extraction", () -> {
            TileQuarry q=quarry(0);q.setInventorySlotContents(0,new ItemStack(Items.diamond,32));
            for(int side=0;side<6;side++)assertTrue(q.getAccessibleSlotsFromSide(side).length==54&&!q.canInsertItem(0,new ItemStack(Items.diamond),side)&&q.canExtractItem(0,q.getStackInSlot(0),side),"bad sided inventory");
            assertTrue(q.decrStackSize(0,7).stackSize==7&&q.getStackInSlot(0).stackSize==25,"partial extraction corrupt");
            assertTrue(q.decrStackSize(0,100).stackSize==25&&q.getStackInSlot(0)==null&&q.decrStackSize(0,1)==null,"over-extraction duplicated");destroy(q);
        });
        test("Sparse 15-chunk selection forces exactly those chunks", () -> {
            TileQuarry q=quarry(1);q.applySelection(ChunkArea.sanitize(ChunkArea.ALL,15));q.enabled=true;q.tickets.tick(q.wantsChunks());
            assertTrue(q.loadedCount==15&&forced(q).equals(selected(q)),"incorrect 15-chunk tickets");destroy(q);
        });
        test("Real mining visits exactly 15 selected chunks and preserves all 10 excluded chunks", () -> {
            TileQuarry q=quarry(1);q.applySelection(ChunkArea.sanitize(ChunkArea.ALL,15));
            for(int c=0;c<25;c++){
                int cx=(q.xCoord>>4)+ChunkArea.dx(c),cz=(q.zCoord>>4)+ChunkArea.dz(c);world.getChunkFromChunkCoords(cx,cz);
                world.setBlock(cx*16+15,0,cz*16+15,Blocks.diamond_ore,0,3);q.cursor.set(c,MiningCursor.CELLS_PER_CHUNK-1);
            }
            int operations=0;
            while(q.cursor.choose(q.selected)){
                q.work=ModConfig.maxBlocksPerTick[q.tier()];assertTrue(MiningEngine.step(q)==MiningEngine.Result.MINED,"selected chunk did not mine");operations++;
                assertTrue(operations<=15,"extra chunks mined");
            }
            assertTrue(operations==15,"wrong number of selected chunks mined");
            for(int c=0;c<25;c++){
                Block block=world.getBlock(((q.xCoord>>4)+ChunkArea.dx(c))*16+15,0,((q.zCoord>>4)+ChunkArea.dz(c))*16+15);
                assertTrue((q.selected&(1<<c))!=0?block==Blocks.air:block==Blocks.diamond_ore,"excluded terrain changed");
            }
            destroy(q);
        });
        test("Quarries do not harvest themselves or other technology machines", () -> {
            TileQuarry q=quarry(0);q.cursor.cell=12;q.cursor.set(12,(255-70)*256+34);int before=q.energy;
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.SKIPPED&&world.getTileEntity(q.xCoord,70,q.zCoord)==q&&before==q.energy,"quarry mined itself");destroy(q);
        });
        test("Full-buffer throttling retains measured supply and stops after the reserve drains", () -> {
            TileQuarry q=quarry(0);q.updateEntity();q.inputAverage=200;q.updateEntity();q.inputAverage=0;
            assertTrue(q.operatingInput()==200,"full buffer deadlocked");q.consume(q.cost());
            assertTrue(q.operatingInput()==200,"saturation caused feedback slowdown");q.consume(q.cost()*ModConfig.removalsPerTick);
            assertTrue(q.operatingInput()==0,"disconnected RF source kept full speed indefinitely");destroy(q);
            TileQuarry slow=quarry(0);slow.updateEntity();slow.inputAverage=20;slow.updateEntity();slow.inputAverage=0;
            assertTrue(slow.operatingInput()==20,"saturated low-power supply became full speed");destroy(slow);
        });
        test("Standalone loaders independently force 25 and 15 chunks; ticket failure is visible", () -> {
            TileChunkLoader a=loader(BASE+6,BASE+6),b=loader(BASE+7,BASE+7);
            a.applySelection(ChunkArea.ALL);b.applySelection(ChunkArea.sanitize(ChunkArea.ALL,15));a.enabled=b.enabled=true;a.tickets.tick(true);b.tickets.tick(true);
            assertTrue(forced(a).equals(selected(a))&&a.loadedCount==25&&b.loadedCount==15,"independent loaders incorrect");
            TileQuarry denied=quarry(2);denied.applySelection(ChunkArea.ALL);denied.enabled=true;denied.tickets.tick(true);
            assertTrue(!denied.ticketError.isEmpty()&&denied.loadedCount==0,"ticket failure concealed");
            assertTrue(a.loadedCount==25&&b.loadedCount==15,"ticket failure corrupted other owners");
            destroy(denied);destroy(a);assertTrue(forced(a).isEmpty()&&forced(b).size()==15,"destroying one loader released another");destroy(b);
        });
        test("Region changes release deselected chunks; disabling releases all", () -> {
            TileChunkLoader a=loader(BASE+6,BASE+6);a.applySelection(ChunkArea.ALL);a.enabled=true;a.tickets.tick(true);
            a.applySelection(ChunkArea.CENTER);a.tickets.tick(true);assertTrue(a.loadedCount==1&&forced(a).equals(selected(a)),"old region remained forced");
            a.enabled=false;a.tickets.tick(false);assertTrue(forced(a).isEmpty()&&a.loadedCount==0,"disabled loader leaked tickets");destroy(a);
        });
        test("NBT round trip preserves RF, inventory, mode, cursor, and sparse area", () -> {
            TileQuarry q=quarry(2);q.energy=12345;q.silkMode=true;q.enabled=true;q.work=2.5;q.mined=87;q.skipped=3;
            q.applySelection(ChunkArea.sanitize(ChunkArea.ALL,15));q.cursor.set(12,(256-70)*256+1234);q.cursor.set(1,(256-70)*256+777);q.setInventorySlotContents(5,new ItemStack(Items.diamond,11));
            NBTTagCompound tag=new NBTTagCompound();q.writeToNBT(tag);tag.setInteger("Tier",0);
            TileQuarry n=new TileQuarry.MK3();n.readFromNBT(tag);
            assertTrue(n.tier()==2&&n.energy==12345&&n.silkMode&&n.enabled&&n.work==2.5&&n.mined==87&&n.skipped==3,"state changed on reload");
            assertTrue(n.cursor.position(12)==(256-70)*256+1234&&n.cursor.position(1)==(256-70)*256+777&&n.selected==q.selected&&count(n,Items.diamond)==11,"progress or items reset");destroy(q);
        });
        test("Area completion stops mining without resetting progress", () -> {
            TileQuarry q=quarry(0);q.cursor.set(12,MiningCursor.CELLS_PER_CHUNK);q.enabled=true;q.updateEntity();
            assertTrue(q.status==MachineStatus.COMPLETED&&!q.enabled&&q.cursor.position(12)==MiningCursor.CELLS_PER_CHUNK,"completion failed");destroy(q);
        });
        test("All four survival crafting recipes resolve", () -> {
            Object[][] recipes={
                {Items.iron_ingot,ModRegistry.gear,Items.iron_ingot,ModRegistry.circuit,Items.diamond_pickaxe,ModRegistry.circuit,Items.iron_ingot,Blocks.redstone_block,Items.iron_ingot},
                {Items.diamond,ModRegistry.gear,Items.diamond,ModRegistry.circuit,ModRegistry.quarries[0],ModRegistry.circuit,Blocks.iron_block,Blocks.redstone_block,Blocks.iron_block},
                {Items.emerald,Blocks.gold_block,Items.emerald,Blocks.diamond_block,ModRegistry.quarries[1],Blocks.diamond_block,ModRegistry.circuit,Blocks.redstone_block,ModRegistry.circuit},
                {Items.iron_ingot,Items.gold_ingot,Items.iron_ingot,ModRegistry.circuit,Items.ender_pearl,ModRegistry.circuit,Items.iron_ingot,Items.redstone,Items.iron_ingot}};
            for(int r=0;r<4;r++) {
                InventoryCrafting matrix=new InventoryCrafting(new Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);
                for(int i=0;i<9;i++)matrix.setInventorySlotContents(i,recipes[r][i] instanceof Block?new ItemStack((Block)recipes[r][i]):new ItemStack((Item)recipes[r][i]));
                ItemStack result=CraftingManager.getInstance().findMatchingRecipe(matrix,world);
                assertTrue(result!=null&&result.getItem()==Item.getItemFromBlock(r==3?ModRegistry.loader:ModRegistry.quarries[r]),"recipe "+r+" failed");
            }
        });
        updateTests();
        compatibilityTests();
        QuarryItemIntegration.run(world,this::test);WirelessIntegration.run(world,this::test);ReactorIntegration.run(world,this::test);
        test("Several quarries obey the shared removal budget fairly", () -> {
            TileQuarry[] qs={quarry(0),quarry(1),quarry(2)};
            for(TileQuarry q:qs){target(q,Blocks.stone,0);q.enabled=true;MiningScheduler.submit(q);}
            int original=ModConfig.globalRemovals;ModConfig.globalRemovals=1;
            try{
                MiningScheduler scheduler=new MiningScheduler();scheduler.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
                long total=qs[0].mined+qs[1].mined+qs[2].mined;assertTrue(total==1,"global removal budget exceeded");
                for(TileQuarry q:qs)MiningScheduler.submit(q);
                scheduler.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
                assertTrue(qs[0].mined+qs[1].mined+qs[2].mined==2,"scheduler did not advance fairly");
            }finally{ModConfig.globalRemovals=original;for(TileQuarry q:qs)destroy(q);MiningScheduler.clear();}
        });
    }
    private void compatibilityTests() {
        test("An idle full buffer retains the real fill supply and can reach the configured maximum",()->{
            TileQuarry q=quarry(0);target(q,Blocks.stone,0);q.energy=q.capacity()-20000;q.work=0;
            assertTrue(q.receiveEnergy(ForgeDirection.NORTH,20000,false)==20000,"full-rate fill rejected");q.enabled=true;q.updateEntity();
            assertTrue(q.operatingInput()==20000&&Math.abs(q.work-5)<1e-9&&q.ready(),"meter startup throttled a full powered quarry");destroy(q);
        });
        test("Old 200-RF defaults migrate to 20000 RF/t, 4000 RF/block and five operations",()->{
            try {
                File cfg=new File("balance-migration-test.cfg");
                String data="energy {\n I:rfInputPerTick=200\n I:maximumSpeedRFPerTick=200\n}\nperformance {\n I:removalsPerMachinePerTick=4\n I:globalRemovalsPerTick=32\n}\n";
                for(int i=1;i<=3;i++)data+="quarrymk"+i+" {\n I:rfPerBlock=50\n I:maximumBlocksPerTick=4\n I:rfCapacity="+new int[]{40000,160000,640000}[i-1]+"\n}\n";
                Files.write(cfg.toPath(),data.getBytes(StandardCharsets.UTF_8));ModConfig.load(cfg);
                assertTrue(ModConfig.inputRate==20000&&ModConfig.speedTarget==20000&&ModConfig.removalsPerTick==5&&ModConfig.globalRemovals==40
                    &&ModConfig.energyCost[0]==4000&&ModConfig.capacity[0]==400000&&ModConfig.maxBlocksPerTick[2]==5,"old defaults retained");
                cfg.delete();ModConfig.load(new File("config/industrialtech.cfg"));
            }catch(Exception e){throw new RuntimeException(e);}
        });
        test("RF balance migration preserves custom settings and is not repeated",()->{
            try {
                File cfg=new File("custom-balance-test.cfg");
                String data="energy {\n I:rfInputPerTick=7000\n I:maximumSpeedRFPerTick=9000\n}\nquarrymk1 {\n I:rfPerBlock=321\n I:rfCapacity=70000\n I:maximumBlocksPerTick=8\n}\nperformance {\n I:removalsPerMachinePerTick=6\n}\n";
                Files.write(cfg.toPath(),data.getBytes(StandardCharsets.UTF_8));ModConfig.load(cfg);
                assertTrue(ModConfig.inputRate==7000&&ModConfig.speedTarget==9000&&ModConfig.energyCost[0]==321&&ModConfig.capacity[0]==70000&&ModConfig.maxBlocksPerTick[0]==8&&ModConfig.removalsPerTick==6,"custom balance overwritten");
                String saved=new String(Files.readAllBytes(cfg.toPath()),StandardCharsets.UTF_8).replace("I:rfInputPerTick=7000","I:rfInputPerTick=200");
                Files.write(cfg.toPath(),saved.getBytes(StandardCharsets.UTF_8));ModConfig.load(cfg);assertTrue(ModConfig.inputRate==200,"user edit remigrated");
                cfg.delete();ModConfig.load(new File("config/industrialtech.cfg"));
            }catch(Exception e){throw new RuntimeException(e);}
        });
        test("Generic modded ores without Forge tool declarations use the pickaxe fallback",()->{
            TileQuarry q=quarry(0);target(q,OreCompatibilityFixtures.unclassified,0);
            assertTrue(OreCompatibilityFixtures.unclassified.getHarvestTool(0)==null,"fixture declares a tool");
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.MINED&&count(q,Item.getItemFromBlock(OreCompatibilityFixtures.unclassified))==1,"unclassified mod ore skipped");destroy(q);
        });
        test("Quarry virtual tools support high harvest levels and custom Forge tool classes",()->{
            TileQuarry q=quarry(1);
            for(Block block:new Block[]{OreCompatibilityFixtures.highLevel,OreCompatibilityFixtures.customTool}){
                target(q,block,0);assertTrue(MiningEngine.step(q)==MiningEngine.Result.MINED&&count(q,Item.getItemFromBlock(block))==1,"declared mod tool/level unsupported");
            }
            assertTrue(ModRegistry.drill.getHarvestLevel(new ItemStack(ModRegistry.drill),"pickaxe")==4,"craftable hand drill was upgraded unnecessarily");destroy(q);
        });
        test("Registered ore TileEntities preserve their resource variant while machines remain protected",()->{
            TileQuarry q=quarry(0);target(q,OreCompatibilityFixtures.tileOre,0);
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.MINED,"natural ore TileEntity skipped");
            ItemStack drop=q.getStackInSlot(0);assertTrue(drop!=null&&drop.getItemDamage()==7,"ore TileEntity variant lost");
            target(q,OreCompatibilityFixtures.energyMachine,0);OreCompatibilityFixtures.EnergyTile battery=(OreCompatibilityFixtures.EnergyTile)world.getTileEntity(q.xCoord&~15,64,q.zCoord&~15);
            int before=q.energy;assertTrue(MiningEngine.step(q)==MiningEngine.Result.SKIPPED&&battery.energy==5000&&q.energy==before&&world.getTileEntity(q.xCoord&~15,64,q.zCoord&~15)==battery,"registered machine lost energy");destroy(q);
        });
        test("Changing a blocked ore TileEntity invalidates the cached drop plan",()->{
            TileQuarry q=quarry(0);q.filter.enabled=true;target(q,OreCompatibilityFixtures.tileOre,0);
            for(int i=0;i<54;i++)q.setInventorySlotContents(i,new ItemStack(Items.iron_ingot,64));
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.WAIT,"full storage removed ore TileEntity");
            ((OreCompatibilityFixtures.OreTile)world.getTileEntity(q.xCoord&~15,64,q.zCoord&~15)).resource=8;q.decrStackSize(0,64);
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.MINED&&q.getStackInSlot(0).getItemDamage()==8,"stale TileEntity harvest was committed");destroy(q);
        });
        test("Self-replacing ore callbacks preserve the original block until storage fits and apply their effect once",()->{
            TileQuarry q=quarry(0);q.filter.enabled=true;target(q,OreCompatibilityFixtures.replacingOre,0);
            for(int i=0;i<54;i++)q.setInventorySlotContents(i,new ItemStack(Items.iron_ingot,64));int energy=q.energy;
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.WAIT&&world.getBlock(q.xCoord&~15,64,q.zCoord&~15)==OreCompatibilityFixtures.replacingOre&&q.energy==energy,"prepared replacement committed before output fit");
            q.decrStackSize(0,64);assertTrue(MiningEngine.step(q)==MiningEngine.Result.MINED&&world.getBlock(q.xCoord&~15,64,q.zCoord&~15)==Blocks.water
                &&count(q,Item.getItemFromBlock(OreCompatibilityFixtures.replacingOre))==1&&q.energy==energy-q.cost(),"replacement effect or drops incorrect");destroy(q);
        });
        if(cpw.mods.fml.common.Loader.isModLoaded("hbm")) {
            String[] ores={"ore_uranium","ore_thorium","ore_copper","ore_lead","ore_titanium","ore_fluorite","ore_niter","ore_sulfur"};
            for(String name:ores) test("Actual HBM "+name+" mines with Fortune and supported Silk Touch",()->{
                Block block=hbmOre(name);assertTrue(block!=null&&Block.blockRegistry.getNameForObject(block)!=null,"HBM ore not registered");
                TileQuarry q=quarry(2);target(q,block,0);int energy=q.energy;
                MiningEngine.Result result=MiningEngine.step(q);
                assertTrue(result==MiningEngine.Result.MINED&&q.energy==energy-q.cost()&&observedFortune==3,"HBM Fortune mining failed: "+result+", "+q.issue+", observed Fortune "+observedFortune);
                assertTrue(q.output.hasSpace()&&anyOutput(q),"HBM harvest produced no output");
                q.setMode(true);target(q,block,0);assertTrue(MiningEngine.step(q)==MiningEngine.Result.MINED&&observedSilk&&count(q,Item.getItemFromBlock(block))>0,"HBM Silk mining failed");destroy(q);
            });
        }
    }
    private static Block hbmOre(String field) {
        try{return(Block)Class.forName("com.hbm.blocks.ModBlocks").getField(field).get(null);}
        catch(ReflectiveOperationException error){throw new RuntimeException(error);}
    }
    private static boolean anyOutput(TileQuarry q){for(ItemStack stack:q.output.slots)if(stack!=null&&stack.stackSize>0)return true;return false;}
    private void updateTests() {
        test("Y10 quarry protects its entire placement layer and starts at Y9 in all 25 chunks", () -> {
            TileQuarry old=quarry(0);int qx=old.xCoord,qz=old.zCoord;destroy(old);
            world.setBlock(qx,10,qz,ModRegistry.quarries[0],2,3);TileQuarry q=(TileQuarry)world.getTileEntity(qx,10,qz);
            q.applySelection(ChunkArea.ALL);q.energy=q.capacity();q.ensureMiningHeight();
            world.setBlock(qx+1,10,qz,Blocks.chest,0,3);TileEntityChest battery=(TileEntityChest)world.getTileEntity(qx+1,10,qz);
            battery.setInventorySlotContents(0,new ItemStack(Items.diamond,7));world.setBlock(qx-1,9,qz,Blocks.stone,0,3);world.setBlock(qx-1,10,qz,Blocks.redstone_wire,0,3);
            for(int cell=0;cell<25;cell++) {
                int x=((qx>>4)+ChunkArea.dx(cell))*16,z=((qz>>4)+ChunkArea.dz(cell))*16;world.getChunkFromChunkCoords(x>>4,z>>4);
                for(int y=9;y<=11;y++)world.setBlock(x,y,z,Blocks.stone,0,3);
                q.cursor.cell=cell;q.work=4;assertTrue(q.cursor.y()==9,"first layer is not quarryY - 1");
                assertTrue(MiningEngine.step(q)==MiningEngine.Result.MINED&&world.isAirBlock(x,9,z),"Y9 block not mined");
                assertTrue(world.getBlock(x,10,z)==Blocks.stone&&world.getBlock(x,11,z)==Blocks.stone,"protected placement/upper layer mined");
            }
            assertTrue(world.getTileEntity(qx+1,10,qz)==battery&&battery.getStackInSlot(0).stackSize==7,"adjacent chest changed");
            assertTrue(world.getBlock(qx-1,10,qz)==Blocks.redstone_wire,"adjacent supported wire changed");destroy(q);
        });
        test("Legacy cursor loading clamps every selected chunk below the original placement height", () -> {
            TileQuarry q=quarry(0);NBTTagCompound tag=new NBTTagCompound();q.writeToNBT(tag);
            tag.removeTag("MiningCeiling");tag.setIntArray("Cursor",new int[]{0,1,2});
            TileQuarry restored=new TileQuarry.MK1();restored.readFromNBT(tag);
            assertTrue(restored.miningCeiling==70&&restored.cursor.y()==69,"legacy height not migrated");
            for(int c=0;c<25;c++)assertTrue(restored.cursor.position(c)>=(256-70)*256,"legacy cursor above quarry");destroy(q);
        });
        test("Block exclusions use registry identity and protect non-inventory TileEntities", () -> {
            TileQuarry q=quarry(0);target(q,Blocks.furnace,0);int energy=q.energy;
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.SKIPPED&&world.getTileEntity(q.xCoord&~15,64,q.zCoord&~15)!=null&&q.energy==energy,"furnace was harvested");
            MiningProtection.exclude(Blocks.gold_block);target(q,Blocks.gold_block,0);
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.SKIPPED&&world.getBlock(q.xCoord&~15,64,q.zCoord&~15)==Blocks.gold_block,"registered exclusion ignored");destroy(q);
        });
        test("Void filter defaults OFF, deletes selected normal drops, and preserves Fortune diamonds", () -> {
            TileQuarry q=quarry(2);assertTrue(!q.filter.enabled&&q.filter.configured()==0,"filter default is destructive");
            q.filter.setInventorySlotContents(0,new ItemStack(Blocks.cobblestone));q.filter.enabled=true;
            target(q,Blocks.stone,0);assertTrue(MiningEngine.step(q)==MiningEngine.Result.MINED&&count(q,Item.getItemFromBlock(Blocks.cobblestone))==0,"cobblestone not voided");
            target(q,Blocks.diamond_ore,0);assertTrue(MiningEngine.step(q)==MiningEngine.Result.MINED&&count(q,Items.diamond)>0&&observedFortune==3,"nonmatching Fortune harvest lost");
            q.filter.enabled=false;target(q,Blocks.stone,0);MiningEngine.step(q);
            assertTrue(count(q,Item.getItemFromBlock(Blocks.cobblestone))==1,"disabled filter deleted drops");destroy(q);
        });
        test("Void filtering follows real Silk drops and allows all-filtered harvests with full storage", () -> {
            TileQuarry q=quarry(0);q.silkMode=true;q.filter.setInventorySlotContents(0,new ItemStack(Blocks.stone));q.filter.enabled=true;
            for(int i=0;i<54;i++)q.setInventorySlotContents(i,new ItemStack(Items.iron_ingot,64));
            target(q,Blocks.stone,0);int energy=q.energy;
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.MINED&&observedSilk&&q.energy==energy-q.cost()&&q.output.pending.isEmpty(),"full storage blocked all-filtered Silk harvest");
            target(q,Blocks.diamond_ore,0);energy=q.energy;
            assertTrue(MiningEngine.step(q)==MiningEngine.Result.WAIT&&q.energy==energy&&world.getBlock(q.xCoord&~15,64,q.zCoord&~15)==Blocks.diamond_ore,"nonfiltered full-output harvest was removed");destroy(q);
        });
        test("Filter exact and metadata-insensitive modes both preserve NBT identity", () -> {
            TileQuarry q=quarry(0);q.filter.enabled=true;ItemStack oak=new ItemStack(Blocks.log,1,0),birch=new ItemStack(Blocks.log,1,2);
            q.filter.setInventorySlotContents(0,oak);assertTrue(q.filter.matches(oak)&&!q.filter.matches(birch),"exact metadata ignored");
            q.filter.ignoreMetadata=true;assertTrue(q.filter.matches(birch)&&!q.filter.matches(new ItemStack(Blocks.log2,1,0)),"metadata mode confused unrelated items");
            NBTTagCompound n=new NBTTagCompound();n.setString("Identity","one");oak.setTagCompound(n);q.filter.setInventorySlotContents(0,oak);
            assertTrue(!q.filter.matches(birch),"NBT was ignored");birch.setTagCompound((NBTTagCompound)n.copy());assertTrue(q.filter.matches(birch),"equal NBT not matched");destroy(q);
        });
        test("Ghost filter clicks preserve real items, reject hidden/shift clicks, and reject oversized icons", () -> {
            TileQuarry q=quarry(0);net.minecraftforge.common.util.FakePlayer p=net.minecraftforge.common.util.FakePlayerFactory.get(world,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"FilterTester"));
            p.setPosition(q.xCoord+.5,q.yCoord+.5,q.zCoord+.5);ItemStack real=new ItemStack(Blocks.cobblestone,64);p.inventory.setItemStack(real);
            MachineContainer c=new MachineContainer(p.inventory,q);c.setTab(2);c.slotClick(90,0,0,p);
            assertTrue(p.inventory.getItemStack()==real&&real.stackSize==64&&q.filter.getStackInSlot(0).stackSize==1,"ghost click consumed or duplicated items");
            c.slotClick(90,0,1,p);assertTrue(real.stackSize==64&&q.filter.getStackInSlot(0).stackSize==1,"shift-click extracted ghost");
            c.setTab(0);c.slotClick(90,1,0,p);assertTrue(q.filter.getStackInSlot(0)!=null,"hidden ghost accepted mutation");
            c.setTab(2);c.slotClick(90,1,0,p);assertTrue(q.filter.getStackInSlot(0)==null&&real.stackSize==64,"right-click changed real items");
            NBTTagCompound huge=new NBTTagCompound();huge.setString("Oversized",new String(new char[8192]).replace('\0','x'));real.setTagCompound(huge);
            c.slotClick(90,0,0,p);assertTrue(q.filter.getStackInSlot(0)==null,"oversized filter icon accepted");p.inventory.setItemStack(null);destroy(q);
        });
        test("Filter enabled state, metadata, tags, and all 18 icons survive NBT without becoming output", () -> {
            TileQuarry q=quarry(1);q.filter.enabled=true;q.filter.ignoreMetadata=true;
            for(int i=0;i<18;i++){ItemStack icon=new ItemStack(Blocks.wool,64,i%16);NBTTagCompound n=new NBTTagCompound();n.setInteger("Index",i);icon.setTagCompound(n);q.filter.setInventorySlotContents(i,icon);}
            NBTTagCompound tag=new NBTTagCompound();q.writeToNBT(tag);TileQuarry n=new TileQuarry.MK2();n.readFromNBT(tag);
            assertTrue(n.filter.enabled&&n.filter.ignoreMetadata&&n.filter.configured()==18&&n.filter.getStackInSlot(17).stackSize==1&&n.filter.getStackInSlot(17).getTagCompound().getInteger("Index")==17&&count(n,Item.getItemFromBlock(Blocks.wool))==0,"filter persistence changed real items");
            n.filter.clear();assertTrue(n.filter.configured()==0,"Clear All failed");destroy(q);
        });
        test("Public description packets contain sparse selections, legal mining height, and only actual loaded chunks", () -> {
            TileQuarry q=quarry(0);q.enabled=true;q.applySelection(ChunkArea.CENTER|1|(1<<24));q.tickets.tick(q.wantsChunks());
            NBTTagCompound view=description(q);
            assertTrue(view.getBoolean("Active")&&view.getInteger("Selected")==q.selected&&view.getInteger("LoadedMask")==q.selected&&view.getInteger("MiningCeiling")==70,"public quarry state missing");
            q.toggleEnabled();view=description(q);
            assertTrue(!view.getBoolean("Active")&&view.getInteger("LoadedMask")==0,"STOP overlay state stale");
            TileChunkLoader loader=loader(BASE+10,BASE+10);loader.enabled=true;loader.applySelection(ChunkArea.sanitize(ChunkArea.ALL,15));loader.tickets.tick(true);
            NBTTagCompound remote=description(loader);
            assertTrue(remote.getBoolean("Active")&&remote.getInteger("LoadedMask")==loader.selected&&Integer.bitCount(remote.getInteger("LoadedMask"))==15,"loader public actual mask missing");
            loader.toggleEnabled();remote=description(loader);assertTrue(!remote.getBoolean("Active")&&remote.getInteger("LoadedMask")==0,"OFF border state stale");destroy(q);destroy(loader);
        });
    }
    private static NBTTagCompound description(TileMachine tile) {
        // The NBT getter on S35 is client-only in a real dedicated server. Read its payload by type here.
        Object packet=tile.getDescriptionPacket();
        try {
            for(java.lang.reflect.Field f:packet.getClass().getDeclaredFields()) if(f.getType()==NBTTagCompound.class){f.setAccessible(true);return(NBTTagCompound)f.get(packet);}
        }catch(ReflectiveOperationException error){throw new RuntimeException(error);}
        throw new AssertionError("Description packet has no NBT payload");
    }
    private void prepareRestartFixtures() throws Exception {
        // Fixtures remain enabled beyond spawn range. Forge saves them and their tickets on normal shutdown.
        test("Prepare real server-restart persistence fixtures", () -> {
            world.getChunkFromChunkCoords(RESTORE_QX>>4,RESTORE_Z>>4);world.getChunkFromChunkCoords(RESTORE_LX>>4,RESTORE_Z>>4);
            world.setBlock(RESTORE_QX,70,RESTORE_Z,ModRegistry.quarries[1],2,3);TileQuarry q=(TileQuarry)world.getTileEntity(RESTORE_QX,70,RESTORE_Z);
            q.enabled=true;q.everStarted=true;q.silkMode=true;q.energy=5000;q.cursor.set(12,(256-70)*256+4321);q.mined=17;q.work=2.0;
            q.filter.setInventorySlotContents(0,new ItemStack(Blocks.stone));q.filter.enabled=true;q.filter.ignoreMetadata=true;
            q.setInventorySlotContents(0,new ItemStack(Items.diamond,9));q.applySelection(ChunkArea.sanitize(ChunkArea.ALL,15));q.tickets.tick(true);q.markDirty();
            TileChunkLoader l=loader(RESTORE_LX,RESTORE_Z);l.enabled=true;l.applySelection(ChunkArea.sanitize(ChunkArea.ALL,15));l.tickets.tick(true);l.markDirty();
            TileChunkLoader disabled=loader(RESTORE_LX+1,RESTORE_Z);disabled.applySelection(ChunkArea.square3());disabled.markDirty();
            world.setBlock(RESTORE_QX+4,70,RESTORE_Z,ModRegistry.quarries[0],2,3);
            TileQuarry stopped=(TileQuarry)world.getTileEntity(RESTORE_QX+4,70,RESTORE_Z);stopped.everStarted=true;stopped.cursor.set(12,(256-70)*256+1234);stopped.silkMode=true;stopped.energy=321;stopped.markDirty();
            assertTrue(q.loadedCount==15&&l.loadedCount==15,"restart fixtures not forced");
        });
        Files.write(new File("integration-phase1.done").toPath(), "restart fixtures prepared".getBytes(StandardCharsets.UTF_8));
    }
    private void startSpeedTests() {
        speedQuarries=new TileQuarry[speedInputs.length];baseline=new long[speedInputs.length];received=new long[speedInputs.length];previousMined=new long[speedInputs.length];
        for(int i=0;i<speedQuarries.length;i++){
            TileQuarry q=quarry(speedTiers[i]);speedQuarries[i]=q;
            int x0=q.xCoord&~15,z0=q.zCoord&~15;
            // Dense test terrain is prepared once; fixture creation is excluded from measured ticks.
            for(int y=60;y<=64;y++)for(int z=0;z<16;z++)for(int x=0;x<16;x++)world.setBlock(x0+x,y,z0+z,Blocks.stone,0,2);
            q.cursor.cell=12;q.cursor.set(12,(255-64)*256);q.work=0;q.energy=0;q.enabled=true;q.everStarted=true;q.tickets.dirty();q.tickets.tick(true);
        }
        speedStage=1;speedTicks=0;
    }
    private void runSpeedTick() {
        WirelessIntegration.tick(world,this::test);ReactorIntegration.tick(world,this::test);
        speedTicks++;
        if(speedStage==1){
            for(int i=0;i<speedQuarries.length;i++){
                TileQuarry q=speedQuarries[i];long step=q.mined-previousMined[i];previousMined[i]=q.mined;
                if(step>5){fail("Per-machine five-block tick budget",new AssertionError("removed "+step));finish();return;}
                if(speedTicks==40)baseline[i]=q.mined;
            }
            if(speedTicks<240)return;
            for(int i=0;i<speedQuarries.length;i++){
                final int index=i;TileQuarry q=speedQuarries[i];
                test("Sustained MK"+(q.tier()+1)+" at "+speedInputs[i]+" RF/t reaches "+expectedSpeeds[i]+" blocks/s",()->{
                    long mined=q.mined-baseline[index];double speed=mined/10.0;
                    IndustrialTechnology.log.info("MEASURED MK{}: {} RF/t => {} blocks/s, energy {}",q.tier()+1,speedInputs[index],speed,q.energy);
                    assertTrue(Math.abs(speed-expectedSpeeds[index])<=.2,"unexpected measured speed "+speed);
                    assertTrue(q.energy==received[index]-q.mined*q.cost(),"RF ledger mismatch");
                    assertTrue(q.loadedCount==1&&q.metrics.blocksPerSecond()==expectedSpeeds[index],"offscreen loading or measured GUI rate wrong");
                });
            }
            stoppedMined=new long[speedQuarries.length];stoppedCursor=new int[speedQuarries.length];
            for(int i=0;i<speedQuarries.length;i++){
                TileQuarry q=speedQuarries[i];stoppedMined[i]=q.mined;stoppedCursor[i]=q.cursor.position(12);q.toggleEnabled();
            }
            speedStage=2;speedTicks=0;return;
        }
        if(speedStage==2&&speedTicks>=20){
            test("STOP immediately releases tickets and preserves cursor/inventory while RF is connected",()->{
                for(int i=0;i<speedQuarries.length;i++){
                    TileQuarry q=speedQuarries[i];assertTrue(!q.enabled&&q.loadedCount==0&&q.mined==stoppedMined[i]&&q.cursor.position(12)==stoppedCursor[i]&&q.status==MachineStatus.STOPPED,"stopped quarry changed");
                }
            });
            for(TileQuarry q:speedQuarries)q.toggleEnabled();speedStage=3;speedTicks=0;return;
        }
        if(speedStage==3&&speedTicks>=20){
            test("START resumes existing mining cursors for multiple powered quarries",()->{
                for(int i=0;i<speedQuarries.length;i++)assertTrue(speedQuarries[i].mined>stoppedMined[i]&&speedQuarries[i].cursor.position(12)>stoppedCursor[i],"resume reset or stalled");
            });
            for(TileQuarry q:speedQuarries)destroy(q);MiningScheduler.clear();speedStage=0;
            try{prepareRestartFixtures();}catch(Exception e){fail("Restart fixtures",e);}finish();
        }
    }
    private void finish(){finished=true;writeReport();MinecraftServer.getServer().initiateShutdown();}
    private void restoreTests() {
        WirelessIntegration.restore(world,this::test);ReactorIntegration.restore(world,this::test);QuarryItemIntegration.restore(world,this::test);
        test("Actual restart callback restores enabled Quarry and its exact 15 forced chunks", () -> {
            assertTrue(world.getChunkProvider().chunkExists(RESTORE_QX>>4,RESTORE_Z>>4),"owner chunk was not restored without a player");
            TileQuarry q=(TileQuarry)world.getTileEntity(RESTORE_QX,70,RESTORE_Z);
            assertTrue(q!=null&&q.enabled&&q.silkMode&&q.energy==5000&&q.mined==17&&q.cursor.position(12)==(256-70)*256+4321&&count(q,Items.diamond)==9,"quarry save state changed");
            assertTrue(q.miningCeiling==70&&q.filter.enabled&&q.filter.ignoreMetadata&&q.filter.getStackInSlot(0).getItem()==Item.getItemFromBlock(Blocks.stone),"height or filter changed on actual restart");
            assertTrue(q.loadedCount==15&&forced(q).equals(selected(q)),"quarry ticket callback failed");
        });
        test("Actual restart restores standalone loader without online players", () -> {
            assertTrue(world.getChunkProvider().chunkExists(RESTORE_LX>>4,RESTORE_Z>>4),"loader owner chunk missing");
            TileChunkLoader l=(TileChunkLoader)world.getTileEntity(RESTORE_LX,70,RESTORE_Z);
            assertTrue(l!=null&&l.enabled&&l.loadedCount==15&&forced(l).equals(selected(l)),"loader restoration failed");
        });
        test("Disabled loader and stopped quarry remain disabled after server restart", () -> {
            TileChunkLoader l=(TileChunkLoader)world.getTileEntity(RESTORE_LX+1,70,RESTORE_Z);
            TileQuarry q=(TileQuarry)world.getTileEntity(RESTORE_QX+4,70,RESTORE_Z);
            assertTrue(l!=null&&!l.enabled&&l.selected==ChunkArea.square3()&&l.loadedCount==0&&q!=null&&!q.enabled&&q.cursor.position(12)==(256-70)*256+1234&&q.energy==321&&q.silkMode,"disabled state changed");
            NBTTagCompound n=new NBTTagCompound();l.writeToNBT(n);
            assertTrue(!n.hasKey("Energy")&&!n.hasKey("Items")&&!cofh.api.energy.IEnergyReceiver.class.isInstance(l)&&!net.minecraft.inventory.IInventory.class.isInstance(l),"utility loader has power/storage interfaces");
            destroy(l);destroy(q);
        });
        test("Breaking restored machines releases every owned ticket", () -> {
            TileMachine q=(TileMachine)world.getTileEntity(RESTORE_QX,70,RESTORE_Z),l=(TileMachine)world.getTileEntity(RESTORE_LX,70,RESTORE_Z);
            destroy(q);destroy(l);assertTrue(forced(q).isEmpty()&&forced(l).isEmpty(),"restored tickets leaked after destruction");
        });
    }
    private TileQuarry quarry(int tier) {
        // Separate chunks prevent fixtures from sharing targeted terrain.
        int cx=(BASE>>4)+(fixture%5)-2,cz=(BASE>>4)+(fixture/5)%5-2;fixture++;
        int x=cx*16+2,z=cz*16+2;world.getChunkFromChunkCoords(cx,cz);
        world.setBlock(x,70,z,ModRegistry.quarries[tier],2,3);TileQuarry q=(TileQuarry)world.getTileEntity(x,70,z);
        q.energy=q.capacity();q.work=ModConfig.maxBlocksPerTick[tier];return q;
    }
    private TileChunkLoader loader(int x,int z) {
        world.getChunkFromChunkCoords(x>>4,z>>4);world.setBlock(x,70,z,ModRegistry.loader,2,3);return (TileChunkLoader)world.getTileEntity(x,70,z);
    }
    private void target(TileQuarry q,Block b,int metadata) {
        world.setBlock(q.xCoord&~15,64,q.zCoord&~15,b,metadata,3);
        q.cursor.cell=12;q.cursor.set(12,(255-64)*256);q.work=ModConfig.maxBlocksPerTick[q.tier()];q.energy=q.capacity();
    }
    private void destroy(TileMachine t) { if(t!=null)world.setBlockToAir(t.xCoord,t.yCoord,t.zCoord); }
    private static int count(TilePoweredMachine q,Item item) {
        int count=0;for(ItemStack s:q.output.slots)if(s!=null&&s.getItem()==item)count+=s.stackSize;
        for(ItemStack s:q.output.pending)if(s.getItem()==item)count+=s.stackSize;return count;
    }
    private Set<ChunkCoordIntPair> selected(TileMachine q) {
        Set<ChunkCoordIntPair> result=new HashSet<ChunkCoordIntPair>();
        for(int i=0;i<25;i++)if((q.selected&(1<<i))!=0)result.add(new ChunkCoordIntPair((q.xCoord>>4)+ChunkArea.dx(i),(q.zCoord>>4)+ChunkArea.dz(i)));return result;
    }
    private Set<ChunkCoordIntPair> forced(TileMachine q) {
        Set<ChunkCoordIntPair> result=new HashSet<ChunkCoordIntPair>();
        for(ChunkCoordIntPair c:world.getPersistentChunks().keySet())for(ForgeChunkManager.Ticket t:world.getPersistentChunks().get(c))
            if(t.getModData().getString("MachineId").equals(q.machineId.toString()))result.add(c);return result;
    }
    private void test(String name,Runnable run) {try{run.run();results.add("PASS: "+name);IndustrialTechnology.log.info("INTEGRATION PASS: "+name);}catch(Throwable e){fail(name,e);}}
    private void fail(String name,Throwable e) {failed++;results.add("FAIL: "+name+" :: "+e);IndustrialTechnology.log.error("INTEGRATION FAIL: "+name,e);}
    private static void assertTrue(boolean value,String reason) {if(!value)throw new AssertionError(reason);}
    private void writeReport() {
        try {
            String phase=new File("integration-phase1.done").exists()&&fixture==0?"restart":"initial";
            results.add("Failed: "+failed);Files.write(new File("integration-"+phase+".txt").toPath(),results,StandardCharsets.UTF_8);
            IndustrialTechnology.log.info("INTEGRATION FINISHED: "+results.size()+" results, "+failed+" failures");
        } catch(Exception e){IndustrialTechnology.log.error("Report write failed",e);}
    }
}
