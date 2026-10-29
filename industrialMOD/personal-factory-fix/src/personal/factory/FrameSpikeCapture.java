package personal.factory;

import java.io.PrintWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Optional F9 diagnostics. Counter binding/sampling is inactive during normal play. */
final class FrameSpikeCapture {
    private static final String ROOT = "com.gtnewhorizons.angelica.rendering.";
    private static final String[] CLASSES = {"ChunkUploadBudget", "ChunkUploadBudget", "ChunkUploadBudget",
        "HbmFactoryRenderFallback", "HbmFactoryRenderFallback", "HbmFactoryRenderFallback"};
    private static final String[] NAMES = {"uploadTotalNanos", "uploadCalls", "uploadResults",
        "fallbackTotalNanos", "discoveryTotalNanos", "discoveryTotalChunks"};
    private static final Field[] fields = new Field[NAMES.length];
    private static final long[] previous = new long[NAMES.length];
    private static final long[] totals = new long[NAMES.length];
    private static final long[] delta = new long[NAMES.length];
    private static final long[] intervals = new long[100000];
    // Columns: interval, upload time, dequeued results, discovery time, fallback time, GC ms, discovered chunks.
    private static final long[][] slowest = new long[10][7];
    private static List<GarbageCollectorMXBean> collectors;
    private static int samples;
    private static long intervalCount, worst, over33ms, previousGcMs, totalGcMs;

    static void start() {
        reset();
        for (int i = 0; i < fields.length; i++) {
            fields[i] = null;
            try {
                Class<?> type = Class.forName(ROOT + CLASSES[i], false, FrameSpikeCapture.class.getClassLoader());
                fields[i] = type.getField(NAMES[i]);
                previous[i] = fields[i].getLong(null);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException unavailable) {
                fields[i] = null;
            }
        }
        try { collectors = ManagementFactory.getGarbageCollectorMXBeans(); }
        catch (RuntimeException unavailable) { collectors = null; }
        previousGcMs = gcMillis();
    }

    static void reset() {
        samples = 0;
        intervalCount = worst = over33ms = totalGcMs = 0;
        Arrays.fill(totals, 0);
        Arrays.fill(delta, 0);
        for (long[] row : slowest) Arrays.fill(row, 0);
    }

    static void frame(long intervalNs) {
        for (int i = 0; i < fields.length; i++) {
            delta[i] = 0;
            if (fields[i] == null) continue;
            try {
                long current = fields[i].getLong(null);
                delta[i] = Math.max(0, current - previous[i]);
                previous[i] = current;
            } catch (IllegalAccessException | RuntimeException unavailable) { fields[i] = null; }
        }
        long gc = gcMillis();
        long gcDelta = gc < 0 || previousGcMs < 0 ? 0 : Math.max(0, gc - previousGcMs);
        previousGcMs = gc;
        record(intervalNs, delta[0], delta[1], delta[2], delta[3], delta[4], delta[5], gcDelta);
    }

    // Kept separate so report arithmetic can be checked without loading Minecraft.
    static void record(long intervalNs, long uploadNs, long uploadCalls, long results,
                       long fallbackNs, long discoveryNs, long chunks, long gcMs) {
        if (intervalNs <= 0) return;
        intervalCount++;
        if (samples < intervals.length) intervals[samples++] = intervalNs;
        worst = Math.max(worst, intervalNs);
        if (intervalNs >= 33333333L) over33ms++;
        totals[0] += uploadNs;
        totals[1] += uploadCalls;
        totals[2] += results;
        totals[3] += fallbackNs;
        totals[4] += discoveryNs;
        totals[5] += chunks;
        totalGcMs += gcMs;
        int smallest = 0;
        for (int i = 1; i < slowest.length; i++) if (slowest[i][0] < slowest[smallest][0]) smallest = i;
        if (intervalNs > slowest[smallest][0]) {
            long[] row = slowest[smallest];
            row[0] = intervalNs; row[1] = uploadNs; row[2] = results;
            row[3] = discoveryNs; row[4] = fallbackNs; row[5] = gcMs; row[6] = chunks;
        }
    }

    private static long gcMillis() {
        if (collectors == null) return -1;
        long total = 0;
        boolean available = false;
        try {
            for (GarbageCollectorMXBean bean : collectors) {
                long value = bean.getCollectionTime();
                if (value >= 0) { available = true; total += value; }
            }
        } catch (RuntimeException unavailable) { return -1; }
        return available ? total : -1;
    }

    static void finish(PrintWriter out) {
        long[] sorted = Arrays.copyOf(intervals, samples);
        Arrays.sort(sorted);
        out.println("Frame spike diagnostics (F9 capture only):");
        out.printf(Locale.ROOT, "Frame intervals: p50=%.3f ms; p95=%.3f ms; p99=%.3f ms; worst=%.3f ms; >=33.333ms=%d/%d%n",
            percentile(sorted, 0.50) / 1e6, percentile(sorted, 0.95) / 1e6, percentile(sorted, 0.99) / 1e6,
            worst / 1e6, over33ms, intervalCount);
        if (intervalCount > samples) out.println("Percentiles use the first " + samples + " intervals; worst/count cover the full capture.");
        for (int i = 0; i < fields.length; i++) {
            out.println(NAMES[i] + ": " + (fields[i] == null ? "unavailable" : Long.toString(totals[i])));
        }
        out.println("Observed GC collection-time delta: " + totalGcMs + " ms (" + (previousGcMs < 0 ? "unavailable" : "supported") + ")");
        out.println("Slowest frames: interval ms | upload ms / dequeued results | discovery ms / chunk lookups | fallback ms | GC ms");
        long[][] ordered = slowest.clone();
        Arrays.sort(ordered, (a, b) -> Long.compare(b[0], a[0]));
        for (long[] row : ordered) if (row[0] > 0) {
            out.printf(Locale.ROOT, "%.3f | %.3f / %d | %.3f / %d | %.3f | %d%n",
                row[0] / 1e6, row[1] / 1e6, row[2], row[3] / 1e6, row[6], row[4] / 1e6, row[5]);
        }
        out.println("Discovery is included in fallback time; GC and CPU/driver times can overlap. These are correlations, not proof of cause.");
        out.println("Dequeued results include stale/cancelled work, not just uploaded sections. Region requests are not completed chunk rebuilds.");
    }

    static long percentile(long[] sorted, double fraction) {
        return sorted.length == 0 ? 0 : sorted[Math.max(0, (int) Math.ceil(sorted.length * fraction) - 1)];
    }
}
