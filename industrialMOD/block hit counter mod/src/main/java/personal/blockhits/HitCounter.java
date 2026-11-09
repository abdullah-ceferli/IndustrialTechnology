package personal.blockhits;

/** Session state only: no networking, world changes, or disk writes. */
public final class HitCounter {
    private boolean enabled;
    private long hits;

    public void toggle() {
        enabled = !enabled;
        hits = 0;
    }

    public void hit() {
        if (enabled && hits < Long.MAX_VALUE) hits++;
    }

    public void reset() { enabled = false; hits = 0; }
    public boolean enabled() { return enabled; }
    public long hits() { return hits; }
}
