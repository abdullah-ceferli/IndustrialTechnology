package dev.industrialtechnology.test;
import java.util.*;import java.nio.file.*;import java.nio.charset.StandardCharsets;
import cpw.mods.fml.common.*;import cpw.mods.fml.common.event.FMLInitializationEvent;import cpw.mods.fml.common.eventhandler.SubscribeEvent;import cpw.mods.fml.common.gameevent.TickEvent;
import dev.industrialtechnology.core.*;import net.minecraft.server.MinecraftServer;import net.minecraft.world.WorldServer;
@Mod(modid="industrialtech_battery_checks",name="Disposable battery telemetry checks",version="1",dependencies="required-after:industrialtech")
public final class BatteryServerChecks {
    final List<String> report=new ArrayList<String>();int ticks,failed;boolean done;BatteryHbmChecks checks;
    interface Check{void run()throws Exception;}
    void test(String name,Check check){try{check.run();report.add("PASS: "+name);}catch(Throwable t){failed++;report.add("FAIL: "+name+" :: "+t);IndustrialTechnology.log.error(name,t);}}
    @Mod.EventHandler public void init(FMLInitializationEvent e){if(Boolean.getBoolean("industrialtech.batteryTest"))FMLCommonHandler.instance().bus().register(this);}
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e){if(done||e.phase!=TickEvent.Phase.END||!MinecraftServer.getServer().isDedicatedServer()||++ticks<10)return;
        if(!Loader.isModLoaded("hbm")){test("Optional battery meter starts with HBM absent",()->{new dev.industrialtechnology.wireless.BatteryMonitor();});finish();return;}
        try{WorldServer w=MinecraftServer.getServer().worldServerForDimension(0);if(ticks==10){checks=new BatteryHbmChecks(w,this);checks.setup();}else if(ticks<=35)checks.transfer();else{checks.finish();finish();}}
        catch(Throwable t){failed++;report.add("FAIL: native battery checks :: "+t);IndustrialTechnology.log.error("Battery checks",t);finish();}
    }
    private void finish(){done=true;try{report.add("Failed: "+failed);Files.write(Paths.get("battery-report.txt"),report,StandardCharsets.UTF_8);}catch(Exception e){throw new RuntimeException(e);}MinecraftServer.getServer().initiateShutdown();}
}
