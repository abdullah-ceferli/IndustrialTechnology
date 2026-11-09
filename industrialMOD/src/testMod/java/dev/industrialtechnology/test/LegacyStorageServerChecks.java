package dev.industrialtechnology.test;
import java.util.*;import java.io.*;import java.nio.file.*;import java.nio.charset.StandardCharsets;
import cpw.mods.fml.common.*;import cpw.mods.fml.common.event.FMLInitializationEvent;import cpw.mods.fml.common.eventhandler.SubscribeEvent;import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.server.MinecraftServer;import net.minecraft.world.WorldServer;import dev.industrialtechnology.core.IndustrialTechnology;import dev.industrialtechnology.integration.LegacyHbmRecipes;
@Mod(modid="industrialtech_legacy_checks",name="Disposable Legacy Storage Checks",version="1",dependencies="required-after:industrialtech")
public final class LegacyStorageServerChecks {
    private final List<String> report=new ArrayList<String>();private int ticks,failed;private boolean done;
    @Mod.EventHandler public void init(FMLInitializationEvent e){if(Boolean.getBoolean("industrialtech.legacyStorageTest"))FMLCommonHandler.instance().bus().register(this);}
    interface Check{void run()throws Exception;}
    void test(String name,Check code){try{code.run();report.add("PASS: "+name);}catch(Throwable t){failed++;report.add("FAIL: "+name+" :: "+t);IndustrialTechnology.log.error(name,t);}}
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e){if(done||e.phase!=TickEvent.Phase.END||!MinecraftServer.getServer().isDedicatedServer()||++ticks<10)return;boolean hbm=Loader.isModLoaded("hbm");String phase=new File("legacy-phase1.done").exists()?"restart":"initial";
        if(hbm)test("Legacy storage test fixture",()->LegacyStorageHbmChecks.run(MinecraftServer.getServer().worldServerForDimension(0),this));
        else test("Enabled optional Spark integration loads without HBM and adds no fake cells",()->{if(!LegacyHbmRecipes.sparkCells||LegacyHbmRecipes.cells[0]!=null)throw new AssertionError("optional branch");});
        done=true;try{report.add("Failed: "+failed);Files.write(Paths.get("legacy-"+phase+".txt"),report,StandardCharsets.UTF_8);Files.write(Paths.get("legacy-phase1.done"),Arrays.asList("tested"),StandardCharsets.UTF_8);}catch(Exception x){throw new RuntimeException(x);}MinecraftServer.getServer().initiateShutdown();
    }
}
