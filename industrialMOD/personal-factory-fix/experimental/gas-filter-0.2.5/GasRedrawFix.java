package personal.factory;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.concurrent.ConcurrentHashMap;

/** Client packet render flags only; block data, light processing and gas simulation remain original. */
public final class GasRedrawFix {
    public static boolean enabled = true;
    public static boolean failedOpen;
    public static long candidates, suppressed, nearGeometry, unloaded, failures;
    private static final ConcurrentHashMap<Class<?>,WorldAccess> worlds = new ConcurrentHashMap<Class<?>,WorldAccess>();
    private static final ConcurrentHashMap<Class<?>,BlockAccess> blocks = new ConcurrentHashMap<Class<?>,BlockAccess>();
    private static boolean warned;
    public static void toggle() { enabled = !enabled; System.out.println("[FactoryFix] Clear-space gas redraw filter " + (enabled ? "ON" : "OFF")); }

    public static int renderFlags(int flags, Object world, int x, int y, int z, Object next, int metadata) {
        if (failedOpen || !enabled || (flags & 2) == 0 || kind(next) == 0 || y < 2 || y > 253 || x < -29999998 || z < -29999998 || x > 29999997 || z > 29999997) return flags;
        try {
            WorldAccess access = worlds.get(world.getClass());
            if (access == null) { access = new WorldAccess(world.getClass()); worlds.put(world.getClass(), access); }
            if (!(boolean) access.remote.invokeExact(world)) return flags;
            // No read may create a missing neighboring chunk.
            if (!access.exists(world, x-2,y,z-2) || !access.exists(world,x+2,y,z-2) || !access.exists(world,x-2,y,z+2) || !access.exists(world,x+2,y,z+2)) { unloaded++; return flags; }
            Object previous = access.block(world,x,y,z);
            int oldKind = kind(previous), newKind = kind(next);
            if (oldKind == 0 || (oldKind == 1 && newKind == 1)) return flags;
            if (previous == next && (int) access.metadata.invokeExact(world,x,y,z) == metadata) return flags;
            candidates++;
            if (!invisible(previous) || !invisible(next)) return flags;
            if ((Object) access.tile.invokeExact(world,x,y,z) != null) { nearGeometry++;return flags; }
            Object extra = null;
            // Protect faces, AO, fluids and adjacent machines: require a full 5x5x5
            // halo containing only the three audited invisible, unlit block types.
            for (int dx=-2; dx<=2; dx++) for (int dy=-2; dy<=2; dy++) for (int dz=-2; dz<=2; dz++) {
                Object neighbor = access.block(world,x+dx,y+dy,z+dz);
                if (neighbor == previous || neighbor == next || neighbor == extra) continue;
                if (!invisible(neighbor)) { nearGeometry++; return flags; }
                extra = neighbor;
            }
            suppressed++;
            return flags & ~2;
        } catch (Throwable error) {
            if (error instanceof VirtualMachineError) throw (VirtualMachineError) error;
            if (error instanceof ThreadDeath) throw (ThreadDeath) error;
            failures++;
            failedOpen=true;
            if (!warned) { warned=true; System.err.println("[FactoryFix] Gas redraw check unavailable; keeping original flags: " + error); }
            return flags;
        }
    }

    private static int kind(Object block) {
        if (block == null) return 0;
        String type = block.getClass().getName();
        if (type.equals("net.minecraft.block.BlockAir")) return 1;
        if (type.equals("com.hbm.blocks.gas.BlockGasRadon")) return 2;
        if (type.equals("com.hbm.blocks.gas.BlockGasAsbestos")) return 3;
        return 0;
    }
    private static boolean invisible(Object block) throws Throwable {
        if (kind(block) == 0) return false;
        BlockAccess access = blocks.get(block.getClass());
        if (access == null) { access = new BlockAccess(block.getClass()); blocks.put(block.getClass(),access); }
        return (int) access.render.invokeExact(block) == -1 && !(boolean) access.opaque.invokeExact(block)
            && (int) access.opacity.invokeExact(block) == 0 && (int) access.light.invokeExact(block) == 0
            && !(boolean) access.tile.invokeExact(block,0);
    }

    private static final class WorldAccess {
        final MethodHandle block, metadata, loaded, remote, tile;
        WorldAccess(Class<?> type) throws ReflectiveOperationException {
            block = method(type,new String[]{"func_147439_a","getBlock"},Object.class,int.class,int.class,int.class);
            metadata = method(type,new String[]{"func_72805_g","getBlockMetadata"},int.class,int.class,int.class,int.class);
            loaded = method(type,new String[]{"func_72899_e","blockExists"},boolean.class,int.class,int.class,int.class);
            tile = method(type,new String[]{"func_147438_o","getTileEntity"},Object.class,int.class,int.class,int.class);
            Field field;
            try { field = type.getField("field_72995_K"); } catch (NoSuchFieldException ignored) { field = type.getField("isRemote"); }
            field.setAccessible(true);
            remote = MethodHandles.lookup().unreflectGetter(field).asType(MethodType.methodType(boolean.class,Object.class));
        }
        Object block(Object world,int x,int y,int z) throws Throwable { return (Object) block.invokeExact(world,x,y,z); }
        boolean exists(Object world,int x,int y,int z) throws Throwable { return (boolean) loaded.invokeExact(world,x,y,z); }
    }
    private static final class BlockAccess {
        final MethodHandle render, opaque, opacity, light, tile;
        BlockAccess(Class<?> type) throws ReflectiveOperationException {
            render=method(type,new String[]{"func_149645_b","getRenderType"},int.class);
            opaque=method(type,new String[]{"func_149662_c","isOpaqueCube"},boolean.class);
            opacity=method(type,new String[]{"func_149717_k","getLightOpacity"},int.class);
            light=method(type,new String[]{"func_149750_m","getLightValue"},int.class);
            tile=method(type,new String[]{"hasTileEntity"},boolean.class,int.class);
        }
    }
    private static MethodHandle method(Class<?> type,String[] names,Class<?> result,Class<?>... parameters) throws ReflectiveOperationException {
        for (String name : names) try {
            Method method=type.getMethod(name,parameters);method.setAccessible(true);
            Class<?>[] signature=new Class<?>[parameters.length+1]; signature[0]=Object.class;
            System.arraycopy(parameters,0,signature,1,parameters.length);
            return MethodHandles.lookup().unreflect(method).asType(MethodType.methodType(result,signature));
        } catch (NoSuchMethodException ignored) {}
        throw new NoSuchMethodException(type.getName()+" "+java.util.Arrays.toString(names));
    }
}
