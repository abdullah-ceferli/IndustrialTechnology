
package copybench;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.io.*;import java.lang.reflect.*;import java.util.*;
@Mod(modid="fpscopybench",name="Disposable copy FPS comparison",version="1",acceptableRemoteVersions="*")
public final class CopyBench {
 private Object mc,settings;private boolean launched,finished,measuring;private int stage;private long ready,measureStart,last;private long frames;private final ArrayList<Long> intervals=new ArrayList<Long>();private final ArrayList<String> results=new ArrayList<String>();
 private final int[] distances=new int[]{16,16};
 @Mod.EventHandler public void init(FMLInitializationEvent e){if(Boolean.getBoolean("fpsbench.copy")&&FMLCommonHandler.instance().getSide().isClient())FMLCommonHandler.instance().bus().register(this);}
 private static Field field(Object o,String...names)throws Exception {for(Class<?> c=o.getClass();c!=null;c=c.getSuperclass())for(String n:names)try{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}catch(NoSuchFieldException ignored){}throw new NoSuchFieldException(Arrays.toString(names));}
 private static Object get(Object o,String...n)throws Exception{return field(o,n).get(o);}
 private static Method method(Class<?> c,String[] names,Class<?>...args)throws Exception{for(String n:names)try{return c.getMethod(n,args);}catch(NoSuchMethodException ignored){}throw new NoSuchMethodException(Arrays.toString(names));}
 private void log(String s)throws Exception{System.out.println("[COPY FPS] "+s);results.add(s);write();}
 private void write()throws Exception{File base=(File)cpw.mods.fml.relauncher.FMLInjectionData.data()[6];try(PrintWriter w=new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(base,"fps-copy-report.txt")),"UTF-8"))){for(String s:results)w.println(s);}}
 @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END||finished)return;try{
  if(mc==null){Class<?> type=Class.forName("net.minecraft.client.Minecraft");mc=method(type,new String[]{"getMinecraft","func_71410_x"}).invoke(null);settings=get(mc,"gameSettings","field_71474_y");field(settings,"pauseOnLostFocus","field_82881_y").setBoolean(settings,false);}
  Object world=get(mc,"theWorld","field_71441_e"),player=get(mc,"thePlayer","field_71439_g");
  if(!launched){Object screen=get(mc,"currentScreen","field_71462_r");if(screen==null||!screen.getClass().getName().endsWith("GuiMainMenu"))return;launched=true;log("Launching disposable world copy; original is never opened");method(mc.getClass(),new String[]{"launchIntegratedServer","func_71371_a"},String.class,String.class,Class.forName("net.minecraft.world.WorldSettings")).invoke(mc,"fps-copy","FPS comparison COPY",null);return;}
  if(world==null||player==null)return;
  if(ready==0){method(mc.getClass(),new String[]{"displayGuiScreen","func_147108_a"},Class.forName("net.minecraft.client.gui.GuiScreen")).invoke(mc,new Object[]{null});log("Loaded copy; position="+get(player,"posX","field_70165_t")+","+get(player,"posY","field_70163_u")+","+get(player,"posZ","field_70161_v")+" yaw="+get(player,"rotationYaw","field_70177_z")+" pitch="+get(player,"rotationPitch","field_70125_A")+" size="+get(mc,"displayWidth","field_71443_c")+"x"+get(mc,"displayHeight","field_71440_d"));setDistance();return;}
  long now=System.nanoTime();
  if(!measuring&&now>=ready){measuring=true;measureStart=now;last=0;frames=0;intervals.clear();log("Measuring distance="+distances[stage]+" for 20 seconds, fixed saved view");}
  else if(measuring&&now-measureStart>=20000000000L){double sec=(now-measureStart)/1e9;Collections.sort(intervals);long sum=0;for(long n:intervals)sum+=n;double p95=intervals.isEmpty()?0:intervals.get((int)((intervals.size()-1)*.95))/1e6;log(String.format(Locale.ROOT,"RESULT distance=%d seconds=%.3f frames=%d fps=%.2f p95_ms=%.3f pos=%s,%s,%s",distances[stage],sec,frames,frames/sec,p95,get(player,"posX","field_70165_t"),get(player,"posY","field_70163_u"),get(player,"posZ","field_70161_v")));capture();measuring=false;if(++stage>=distances.length){log("Complete");finished=true;method(mc.getClass(),new String[]{"shutdown","func_71400_g"}).invoke(mc);}else setDistance();}
 }catch(Throwable t){try{log("ERROR: "+t);}catch(Exception ignored){}t.printStackTrace();finished=true;System.exit(2);}}
 private void setDistance()throws Exception{field(settings,"ofFastRender").setBoolean(settings,stage==1);log("Fast Render="+(stage==1));field(settings,"renderDistanceChunks","field_151451_c").setInt(settings,distances[stage]);Object renderer=get(mc,"renderGlobal","field_71438_f");method(renderer.getClass(),new String[]{"loadRenderers","func_72712_a"}).invoke(renderer);ready=System.nanoTime()+(stage==0?30000000000L:20000000000L);log("Settling distance="+distances[stage]);}

 private void capture()throws Exception{Object fb=method(mc.getClass(),new String[]{"getFramebuffer","func_147110_a"}).invoke(mc);Class<?> screenshot=Class.forName("net.minecraft.util.ScreenShotHelper");method(screenshot,new String[]{"saveScreenshot","func_148259_a"},File.class,String.class,int.class,int.class,Class.forName("net.minecraft.client.shader.Framebuffer")).invoke(null,(File)cpw.mods.fml.relauncher.FMLInjectionData.data()[6],"fast-"+(stage==1?"on":"off")+".png",get(mc,"displayWidth","field_71443_c"),get(mc,"displayHeight","field_71440_d"),fb);Object error=Class.forName("org.lwjgl.opengl.GL11").getMethod("glGetError").invoke(null);log("Screenshot fast="+(stage==1)+" GL error="+error);}
 @SubscribeEvent public void frame(TickEvent.RenderTickEvent e){if(e.phase!=TickEvent.Phase.END||!measuring||finished)return;long n=System.nanoTime();if(last!=0)intervals.add(n-last);last=n;frames++;}
}
