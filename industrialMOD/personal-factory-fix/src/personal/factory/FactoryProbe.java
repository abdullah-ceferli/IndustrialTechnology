package personal.factory;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.FMLInjectionData;
import org.lwjgl.input.Keyboard;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

@Mod(modid="factoryprobe", name="Personal Factory Fix", version="0.2.1", acceptableRemoteVersions="*")
public final class FactoryProbe {
    private static volatile boolean active;
    private static long deadline, started, requests, sampled, frames, previousFrame;
    private static boolean automaticDone;
    private static long firstRegion;
    private static long startPackets,startIdentical,startSuppressed,startRedraws;
    private static final Map<String,Long> sites = new HashMap<String,Long>();
    private static final Map<String,Long> sources = new HashMap<String,Long>();
    private static final Map<String,long[]> renderers = new HashMap<String,long[]>();
    private static final long[] frameBuckets = new long[5];
    private static final int MAX_KEYS = 2048;
    private static final StringBuilder hookErrors = new StringBuilder();
    private static final ThreadLocal<ArrayDeque<Long>> timers = new ThreadLocal<ArrayDeque<Long>>() {
        protected ArrayDeque<Long> initialValue() { return new ArrayDeque<Long>(); }
    };

    @Mod.EventHandler public void init(FMLInitializationEvent event) {
        if (FMLCommonHandler.instance().getSide().isClient()) FMLCommonHandler.instance().bus().register(this);
    }
    @SubscribeEvent public void tick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!automaticDone && firstRegion != 0 && System.nanoTime()-firstRegion >= 15000000000L) {
            automaticDone=true;
            if(!active)start();
        }
        if (!active) return;
        long now = System.nanoTime();
        if (previousFrame != 0) {
            long ms = (now - previousFrame) / 1000000;
            frameBuckets[ms < 17 ? 0 : ms < 34 ? 1 : ms < 50 ? 2 : ms < 100 ? 3 : 4]++;
        }
        previousFrame = now; frames++;
        if (now >= deadline) finish();
    }
    @SubscribeEvent public void key(InputEvent.KeyInputEvent event) {
        if(Keyboard.getEventKey()==Keyboard.KEY_F10 && Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent())DuctPacketFix.toggle();
        if(Keyboard.getEventKey()==Keyboard.KEY_F9 && Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent()) {
            automaticDone=true;
            if(active)finish();else start();
        }
    }
    private static synchronized void start() {
        sites.clear(); sources.clear(); renderers.clear(); Arrays.fill(frameBuckets,0);
        hookErrors.setLength(0); timers.get().clear(); requests=sampled=frames=previousFrame=0;
        started=System.nanoTime(); deadline=started+30000000000L; active=true;
        startPackets=DuctPacketFix.packets;startIdentical=DuctPacketFix.identical;
        startSuppressed=DuctPacketFix.suppressed;startRedraws=DuctPacketFix.redraws;
        System.out.println("[FactoryProbe] Recording 30 seconds. Face the affected factory. F9 stops early.");
    }
    public static void beginRender() { if(active)timers.get().push(System.nanoTime()); }
    public static void endRender(Object tile) {
        if (!active || tile == null || timers.get().isEmpty()) return;
        long timer=timers.get().pop();
        long elapsed=System.nanoTime()-timer;
        try {
            String key=tile.getClass().getName();
            long[] stats=renderers.get(key);
            if (stats==null) { stats=new long[3]; renderers.put(key,stats); }
            stats[0]++; stats[1]+=elapsed; stats[2]=Math.max(stats[2],elapsed);
        } catch (RuntimeException error) { error(error); }
    }
    public static void region(Object global, int x1,int y1,int z1,int x2,int y2,int z2) {
        if(firstRegion==0)firstRegion=System.nanoTime();
        if (!active) return;
        requests++;
        // Sample only one in 16 invalidations. Counts below are sampled request sites, not rebuild counts.
        if ((requests & 15) != 1) return;
        sampled++;
        try {
            int x=x1+(x2-x1)/2, y=y1+(y2-y1)/2, z=z1+(z2-z1)/2;
            Object world=null;
            for (Field field:global.getClass().getDeclaredFields()) {
                String type=field.getType().getName();
                if (type.equals("net.minecraft.client.multiplayer.WorldClient") || field.getName().equals("field_72769_h") || field.getName().equals("theWorld")) {
                    field.setAccessible(true); world=field.get(global); break;
                }
            }
            String block="unknown", tile="none";
            if (world!=null) {
                Object b=callAt(world,new String[]{"getBlock","func_147439_a"},x,y,z);
                Object t=callAt(world,new String[]{"getTileEntity","func_147438_o"},x,y,z);
                if(b!=null)block=b.getClass().getName(); if(t!=null)tile=t.getClass().getName();
            }
            increment(sites,x+","+y+","+z+" | block="+block+" | tile="+tile);
            StringBuilder trace=new StringBuilder();
            for (StackTraceElement frame:Thread.currentThread().getStackTrace()) {
                if (frame.getClassName().equals(Thread.class.getName()) || frame.getClassName().startsWith("personal.factory.")) continue;
                if(trace.length()>0)trace.append(" <- "); trace.append(frame.toString());
                if(trace.length()>1400)break;
            }
            increment(sources,trace.toString());
        } catch (Exception error) { error(error); }
    }
    private static Object callAt(Object world,String[] names,int x,int y,int z) throws Exception {
        for(String name:names) try { return world.getClass().getMethod(name,int.class,int.class,int.class).invoke(world,x,y,z); }
        catch(NoSuchMethodException ignored) {}
        throw new NoSuchMethodException(Arrays.toString(names));
    }
    private static void increment(Map<String,Long> map,String key) {
        if (!map.containsKey(key) && map.size()>=MAX_KEYS)key="[other sites: bounded capture]";
        Long old=map.get(key);map.put(key,old==null?1:old+1);
    }
    private static void error(Exception error) {
        if(hookErrors.length()<1500)hookErrors.append(error.toString()).append('\n');
    }
    private static synchronized void finish() {
        active=false;
        File base=(File)FMLInjectionData.data()[6];
        File output=new File(base,"logs/factoryprobe-"+System.currentTimeMillis()+".txt");
        try(PrintWriter out=new PrintWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF-8"))) {
            double seconds=(System.nanoTime()-started)/1e9;
            out.println("Personal Factory Fix 0.2.1; itemduct redundant redraw filter="+DuctPacketFix.enabled);
            out.printf(Locale.ROOT,"Seconds %.2f; frames %d; average %.1f FPS%n",seconds,frames,frames/seconds);
            out.println("Frame intervals <17/<34/<50/<100/>=100 ms: "+Arrays.toString(frameBuckets));
            out.println("Render invalidation calls: "+requests+"; sampled: "+sampled);
            out.println("Itemduct packets="+(DuctPacketFix.packets-startPackets)+"; identical="+(DuctPacketFix.identical-startIdentical)+"; skipped redraw calls="+(DuctPacketFix.suppressed-startSuppressed)+"; accepted redraws="+(DuctPacketFix.redraws-startRedraws));
            out.println("Sampled region centers (may be neighboring blocks, not the initiating machine):");
            dumpCounts(out,sites);
            out.println("Sampled invalidation call stacks:");dumpCounts(out,sources);
            out.println("Machine renderer elapsed time, CPU plus driver waits; includes probe overhead:");
            List<Map.Entry<String,long[]>> entries=new ArrayList<Map.Entry<String,long[]>>(renderers.entrySet());
            Collections.sort(entries,(a,b)->Long.compare(b.getValue()[1],a.getValue()[1]));
            for(Map.Entry<String,long[]> entry:entries) {
                long[] stats=entry.getValue();
                out.printf(Locale.ROOT,"%.2f ms total; %d calls; %.3f ms max | %s%n",stats[1]/1e6,stats[0],stats[2]/1e6,entry.getKey());
            }
            out.println("Diagnostic errors:\n"+hookErrors);
            System.out.println("[FactoryProbe] Saved "+output.getAbsolutePath());
        } catch(IOException error) { System.err.println("[FactoryProbe] Could not write report: "+error); }
    }
    private static void dumpCounts(PrintWriter out,Map<String,Long> map) {
        List<Map.Entry<String,Long>> entries=new ArrayList<Map.Entry<String,Long>>(map.entrySet());
        Collections.sort(entries,(a,b)->Long.compare(b.getValue(),a.getValue()));
        for(int i=0;i<Math.min(40,entries.size());i++)out.println(entries.get(i).getValue()+" | "+entries.get(i).getKey());
    }
}
