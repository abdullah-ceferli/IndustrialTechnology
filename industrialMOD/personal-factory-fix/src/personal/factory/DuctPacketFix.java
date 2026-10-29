package personal.factory;

import java.io.DataInputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Redundant client redraws for exact plain/Ender Thermal Dynamics 1.2.1 itemduct types. */
public final class DuctPacketFix {
    public static boolean enabled=true;
    public static long packets, identical, suppressed, redraws;
    public static long enderPackets, enderIdentical, enderSuppressed, enderRedraws;
    private static final long REFRESH_NS=1000000000L;
    private static final Map<Object,State> previous=Collections.synchronizedMap(new WeakHashMap<Object,State>());
    private static final ThreadLocal<Context> current=new ThreadLocal<Context>();
    private static final Map<Class<?>,Method> markMethods=new ConcurrentHashMap<Class<?>,Method>();
    private static boolean warned;
    private static final class State {
        final byte[] bytes; final long refreshed;
        final WeakReference<Object> world;
        final int x,y,z;
        State(Context context) {
            bytes=context.bytes;refreshed=context.now;
            world=new WeakReference<Object>(context.world);
            x=context.x;y=context.y;z=context.z;
        }
    }
    private static final class Context {
        Object tile,world; byte[] bytes; int x,y,z; boolean skip,marked,ender,invalidated; long now;
    }
    public static void toggle() {
        enabled=!enabled; previous.clear(); current.remove();
        System.out.println("[FactoryFix] Redundant itemduct redraw filter "+(enabled?"ON":"OFF"));
    }
    public static void beginPacket(Object tile,Object packet,boolean server) {
        current.remove();
        if(!enabled || server || !supported(tile))return;
        try {
            DataInputStream input=(DataInputStream)packet.getClass().getField("datain").get(packet);
            int size=input.available();
            if(!input.markSupported() || size<=0 || size>65536)return;
            byte[] bytes=new byte[size];
            input.mark(size+1);
            try { input.readFully(bytes); } finally { input.reset(); }
            Context c=new Context();c.tile=tile;c.bytes=bytes;c.now=System.nanoTime();
            c.ender=tile.getClass().getName().equals("cofh.thermaldynamics.duct.item.TileItemDuctEnder");
            c.x=((Number)field(tile,"xCoord","field_145851_c")).intValue();
            c.y=((Number)field(tile,"yCoord","field_145848_d")).intValue();
            c.z=((Number)field(tile,"zCoord","field_145849_e")).intValue();
            c.world=field(tile,"worldObj","field_145850_b");
            if(c.world==null)return;
            State last=previous.get(tile);
            boolean same=last!=null && last.world.get()==c.world && last.x==c.x && last.y==c.y && last.z==c.z && Arrays.equals(last.bytes,bytes);
            c.skip=same && c.now-last.refreshed<REFRESH_NS;
            packets++;if(same)identical++;
            if(c.ender) { enderPackets++;if(same)enderIdentical++; }
            current.set(c);
        } catch(Exception error) {
            // Unsupported packet streams/layouts keep their original redraw behavior.
            if(!warned) { warned=true;System.err.println("[FactoryFix] Unsupported packet; retaining original redraw: "+error); }
        }
    }
    public static void endPacket() {
        Context c=current.get();current.remove();
        if(c!=null && !c.skip && !c.invalidated)previous.put(c.tile,new State(c));
    }
    /** Power, side/attachment and other info packets can change state between full descriptions. */
    public static void invalidate(Object tile) {
        if(!supported(tile))return;
        previous.remove(tile);
        Context c=current.get();
        if(c!=null && c.tile==tile) { c.skip=false;c.invalidated=true; }
    }
    private static boolean supported(Object tile) {
        if(tile==null)return false;
        String name=tile.getClass().getName();
        return name.equals("cofh.thermaldynamics.duct.item.TileItemDuct") || name.equals("cofh.thermaldynamics.duct.item.TileItemDuctEnder");
    }
    public static void innerMark(Object world,int x,int y,int z) { mark(world,x,y,z,false); }
    public static void outerMark(Object world,int x,int y,int z) { mark(world,x,y,z,true); }
    private static void mark(Object world,int x,int y,int z,boolean outer) {
        Context c=current.get();
        boolean match=c!=null && c.world==world && c.x==x && c.y==y && c.z==z;
        if(match && (c.skip || (outer && c.marked))) { suppressed++;if(c.ender)enderSuppressed++;return; }
        originalMark(world,x,y,z);
        if(match) { c.marked=true;redraws++;if(c.ender)enderRedraws++; }
    }
    private static Object field(Object tile,String... names) throws Exception {
        for(Class<?> type=tile.getClass();type!=null;type=type.getSuperclass()) {
            for(String name:names)try {
                Field member=type.getDeclaredField(name);member.setAccessible(true);return member.get(tile);
            }catch(NoSuchFieldException ignored){}
        }
        throw new NoSuchFieldException(Arrays.toString(names));
    }
    private static void originalMark(Object world,int x,int y,int z) {
        try {
            Method method=markMethods.get(world.getClass());
            if(method==null) {
                for(String name:new String[]{"markBlockForUpdate","func_147471_g"})try {
                    method=world.getClass().getMethod(name,int.class,int.class,int.class);break;
                }catch(NoSuchMethodException ignored){}
                if(method==null)throw new NoSuchMethodException("World.markBlockForUpdate");
                markMethods.put(world.getClass(),method);
            }
            method.invoke(world,x,y,z);
        } catch(InvocationTargetException error) {
            Throwable cause=error.getCause();
            if(cause instanceof RuntimeException)throw (RuntimeException)cause;
            if(cause instanceof Error)throw (Error)cause;
            throw new IllegalStateException(cause);
        } catch(ReflectiveOperationException error) { throw new IllegalStateException("Cannot invoke original world redraw",error); }
    }
}
