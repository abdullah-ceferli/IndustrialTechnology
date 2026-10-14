package dev.industrialtechnology.quarry;

import java.util.Arrays;

/** Actual successful harvests in the last twenty game ticks, including the current tick. */
public final class MiningMetrics {
    private final int[] buckets = new int[20];
    private long lastTick = Long.MIN_VALUE;
    private int index, sum;
    public void advance(long tick) {
        if (lastTick == Long.MIN_VALUE || tick < lastTick || tick - lastTick >= 20) {
            Arrays.fill(buckets, 0); sum = index = 0;
        } else {
            for (long i = lastTick; i < tick; i++) { index = (index + 1) % 20; sum -= buckets[index]; buckets[index] = 0; }
        }
        lastTick = tick;
    }
    public void record(long tick) { advance(tick); buckets[index]++; sum++; }
    public int blocksPerSecond() { return sum; }
}
