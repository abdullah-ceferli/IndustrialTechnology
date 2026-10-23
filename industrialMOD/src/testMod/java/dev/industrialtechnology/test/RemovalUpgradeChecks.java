package dev.industrialtechnology.test;
import java.util.*;import java.nio.file.*;import java.nio.charset.StandardCharsets;import cpw.mods.fml.common.*;import cpw.mods.fml.common.event.*;import cpw.mods.fml.common.eventhandler.SubscribeEvent;import cpw.mods.fml.common.gameevent.TickEvent;import net.minecraft.server.MinecraftServer;import net.minecraft.world.WorldServer;import net.minecraftforge.common.DimensionManager;import net.minecraft.init.*;import net.minecraft.util.EnumChatFormatting;import dev.industrialtechnology.core.*;
@Mod(modid="industrialtech_removal_upgrade_checks",name="Disposable removed-content upgrade checks",version="1",dependencies="required-after:industrialtech")
public final class RemovalUpgradeChecks {
    private int ticks,failed;private boolean done;private final List<String> report=new ArrayList<String>();
    @Mod.EventHandler public void init(FMLInitializationEvent e){if(Boolean.getBoolean("industrialtech.removalUpgrade"))FMLCommonHandler.instance().bus().register(this);}
    private void test(String text,Runnable work){try{work.run();report.add("PASS: "+text);}catch(Throwable t){failed++;report.add("FAIL: "+text+" :: "+t);IndustrialTechnology.log.error(text,t);}}
    private static void check(boolean b,String text){if(!b)throw new AssertionError(text);}
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e){if(done||e.phase!=TickEvent.Phase.END||++ticks<60)return;done=true;WorldServer w=MinecraftServer.getServer().worldServerForDimension(0);
        test("2.1.0 save opens without the removed-content startup prompt",()->check(w!=null,"old Overworld unavailable"));
        test("Previously placed green lamp retains its registry identity, position and powered light",()->{w.getChunkFromChunkCoords(134>>4,100>>4);check(w.getBlock(134,70,100)==ModRegistry.greenLamp,"old green lamp replaced");check(ModRegistry.greenLamp.getLightValue(w,134,70,100)==15,"old powered state lost");check(w.getBlock(135,70,100)==Blocks.redstone_block,"unrelated redstone source changed");});
        test("Only retired launch and machine blocks disappear when old chunks load",()->{for(int[] p:new int[][]{{160,70,160},{400,6,400},{341,5,326},{104,70,100}}){w.getChunkFromChunkCoords(p[0]>>4,p[2]>>4);check(w.isAirBlock(p[0],p[1],p[2]),"retired content remained at "+Arrays.toString(p));check(w.getTileEntity(p[0],p[1],p[2])==null,"retired tile remained");}});
        test("Old planet folders remain on disk without registering their dimensions",()->{check(!DimensionManager.isDimensionRegistered(180)&&!DimensionManager.isDimensionRegistered(181),"space dimensions active");check(new java.io.File("space-world/IndustrialTechMoon").isDirectory()&&new java.io.File("space-world/IndustrialTechMars").isDirectory(),"old dimension files deleted");});
        try{report.add("Failed: "+failed);Files.write(Paths.get("removal-upgrade.txt"),String.join("\n",report).getBytes(StandardCharsets.UTF_8));}catch(Exception ex){throw new RuntimeException(ex);}MinecraftServer.getServer().initiateShutdown();
    }
}
