package personal.factory;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicLongArray;

/** F9-only CPU/driver elapsed times, including vanilla rebuilds, with a location timeline. */
public final class PhaseCapture {
    static final String[] NAMES = {"client tick", "camera/world/GUI render", "vanilla section rebuild",
        "terrain draw", "entities and tile render", "local server tick", "display update", "FPS limiter wait", "GL flush wait"};
    private static final int SIZE = NAMES.length;
    private static final AtomicLongArray total = new AtomicLongArray(SIZE);
    private static final AtomicLongArray counts = new AtomicLongArray(SIZE);
    private static final AtomicLongArray maximum = new AtomicLongArray(SIZE);
    private static final long[] previous = new long[SIZE];
    private static final long[] delta = new long[SIZE];
    private static final List<Bin> bins = new ArrayList<Bin>();
    private static final List<SlowFrame> slowest = new ArrayList<SlowFrame>();
    private static volatile boolean active;
    private static volatile long started;
    private static volatile Thread client;
    private static long hooks;
    private static Object minecraft;
    private static String contextError;
    private static final ThreadLocal<ScopeState> state = new ThreadLocal<ScopeState>() {
        protected ScopeState initialValue() { return new ScopeState(); }
    };

    private static final class ScopeState {
        long epoch;
        final long[] starts = new long[SIZE];
        final int[] depth = new int[SIZE];
    }
    private static final class Bin {
        int second, frames;
        long intervals, worst;
        String position;
        final long[] scopes = new long[SIZE];
    }
    private static final class SlowFrame {
        long interval;
        double seconds;
        long[] scopes;
        String position;
    }

    static synchronized void registerHook(int index) { hooks |= 1L << index; }
    static void beginFrame() {
        if (!active || Thread.currentThread() != client) return;
        // A renderer exception caught by another mod must not poison later frames.
        ScopeState local = state.get();
        Arrays.fill(local.depth, 0);
    }
    static void stop() { active = false; }
    static void start() {
        active = false;
        bins.clear(); slowest.clear();
        for (int i = 0; i < SIZE; i++) { total.set(i, 0); counts.set(i, 0); maximum.set(i, 0); }
        Arrays.fill(previous, 0);
        client = Thread.currentThread(); minecraft = null; contextError = null;
        try {
            Class<?> type = Class.forName("net.minecraft.client.Minecraft");
            for (String name : new String[]{"func_71410_x", "getMinecraft"}) {
                try { Method method = type.getMethod(name); minecraft = method.invoke(null); break; }
                catch (NoSuchMethodException ignored) {}
            }
        } catch (ReflectiveOperationException | LinkageError failure) { contextError = failure.toString(); }
        started = System.nanoTime();
        active = true;
    }

    public static void begin(int index) {
        if (!active || index < 0 || index >= SIZE || (index != 5 && Thread.currentThread() != client)) return;
        ScopeState local = state.get();
        if (local.epoch != started) {
            local.epoch = started;
            Arrays.fill(local.depth, 0);
        }
        if (local.depth[index]++ == 0) local.starts[index] = System.nanoTime();
    }
    public static void end(int index) {
        if (!active || index < 0 || index >= SIZE || (index != 5 && Thread.currentThread() != client)) return;
        ScopeState local = state.get();
        if (local.epoch != started || local.depth[index] <= 0 || --local.depth[index] != 0) return;
        long elapsed = System.nanoTime() - local.starts[index];
        total.addAndGet(index, elapsed);
        counts.incrementAndGet(index);
        long old;
        do { old = maximum.get(index); if (old >= elapsed) break; }
        while (!maximum.compareAndSet(index, old, elapsed));
    }

    static void frame(long interval) {
        if (!active) return;
        double elapsed = (System.nanoTime() - started) / 1e9;
        for (int i = 0; i < SIZE; i++) { long value = total.get(i); delta[i] = value - previous[i]; previous[i] = value; }
        if (interval <= 0) return;
        int second = (int) Math.min(60, elapsed);
        Bin bin = bins.isEmpty() ? null : bins.get(bins.size() - 1);
        if (bin == null || bin.second != second) {
            bin = new Bin(); bin.second = second; bin.position = position(); bins.add(bin);
        }
        bin.frames++; bin.intervals += interval; bin.worst = Math.max(bin.worst, interval);
        for (int i = 0; i < SIZE; i++) bin.scopes[i] += delta[i];
        int smallest = -1;
        if (slowest.size() >= 10) {
            smallest = 0;
            for (int i = 1; i < slowest.size(); i++) if (slowest.get(i).interval < slowest.get(smallest).interval) smallest = i;
            if (slowest.get(smallest).interval >= interval) return;
        }
        SlowFrame slow = new SlowFrame(); slow.interval = interval; slow.seconds = elapsed;
        slow.scopes = delta.clone(); slow.position = position();
        if (smallest < 0) slowest.add(slow); else slowest.set(smallest, slow);
    }

    static void finish(PrintWriter out, long frames) {
        active = false;
        out.println("F9 elapsed-time scopes (nested; do not add these times together):");
        for (int i = 0; i < SIZE; i++) {
            long count = counts.get(i), nanos = total.get(i);
            if ((hooks & (1L << i)) == 0) out.println(NAMES[i] + ": unavailable (hook not loaded)");
            else if (count == 0) out.println(NAMES[i] + ": no completed calls during this capture (remote server work cannot be measured here)");
            else out.printf(Locale.ROOT, "%s: %.3f ms total; %d calls; %.3f ms/call; %.3f ms/frame; %.3f ms max%n",
                NAMES[i], nanos / 1e6, count, nanos / 1e6 / count, nanos / 1e6 / Math.max(1, frames), maximum.get(i) / 1e6);
        }
        out.println("Timeline: second | FPS from frame intervals | worst ms | tick/render/rebuild/terrain/entities/server/display/limiter/flush ms | position at bin start");
        for (Bin bin : bins) {
            out.printf(Locale.ROOT, "%d | %.1f | %.3f | %s | %s%n", bin.second,
                bin.frames * 1e9 / Math.max(1, bin.intervals), bin.worst / 1e6, times(bin.scopes), bin.position);
        }
        out.println("Slow frames: second | frame ms | tick/render/rebuild/terrain/entities/server/display/limiter/flush ms | position");
        Collections.sort(slowest, (a, b) -> Long.compare(b.interval, a.interval));
        for (SlowFrame slow : slowest) out.printf(Locale.ROOT, "%.3f | %.3f | %s | %s%n",
            slow.seconds, slow.interval / 1e6, times(slow.scopes), slow.position);
        out.println("Elapsed scopes include driver waits and GC pauses inside a scope; they do not measure GPU execution time.");
        out.println("Server work runs on a different thread and overlaps client work. Frame intervals end at RenderTick.END, before flush/display/limiter work for that loop; adjacent frames can share causes. First/last intervals and timeline bins can be partial.");
        out.println("Only normally completed calls are counted. Aborted calls are omitted and client scope nesting resets at the next RenderTick.START; nested same-scope calls count only the outer call. Sampling and hooks add overhead.");
        if (contextError != null) out.println("Location lookup: " + contextError);
    }

    private static String times(long[] values) {
        StringBuilder result = new StringBuilder();
        for (long value : values) { if (result.length() > 0) result.append('/'); result.append(String.format(Locale.ROOT, "%.3f", value / 1e6)); }
        return result.toString();
    }
    private static String position() {
        if (minecraft == null) return "unavailable";
        try {
            Object player = field(minecraft, "field_71439_g", "thePlayer");
            if (player == null) return "no player (menu/loading)";
            return String.format(Locale.ROOT, "dim=%d xyz=%.1f,%.1f,%.1f yaw=%.1f pitch=%.1f",
                ((Number) field(player, "field_71093_bK", "dimension")).intValue(),
                number(player, "field_70165_t", "posX"), number(player, "field_70163_u", "posY"), number(player, "field_70161_v", "posZ"),
                number(player, "field_70177_z", "rotationYaw"), number(player, "field_70125_A", "rotationPitch"));
        } catch (ReflectiveOperationException | RuntimeException failure) { contextError = failure.toString(); return "unavailable"; }
    }
    private static double number(Object value, String srg, String mcp) throws ReflectiveOperationException {
        return ((Number) field(value, srg, mcp)).doubleValue();
    }
    private static Object field(Object value, String... names) throws ReflectiveOperationException {
        for (Class<?> type = value.getClass(); type != null; type = type.getSuperclass()) for (String name : names) {
            try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(value); }
            catch (NoSuchFieldException ignored) {}
        }
        throw new NoSuchFieldException(Arrays.toString(names));
    }
}
