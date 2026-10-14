package dev.industrialtechnology.quarry;

/** Pure geometry; relative chunks are represented by a validated 25-bit mask. */
public final class ChunkArea {
    public static final int CENTER = 1 << 12;
    public static final int ALL = (1 << 25) - 1;
    private ChunkArea() {}
    public static int sanitize(int mask, int maximum) {
        mask = (mask & ALL) | CENTER;
        maximum = Math.max(1, Math.min(25, maximum));
        while (Integer.bitCount(mask) > maximum) {
            int bit = 31 - Integer.numberOfLeadingZeros(mask & ~CENTER);
            mask &= ~(1 << bit);
        }
        return mask;
    }
    public static boolean valid(int mask, int maximum) {
        return (mask & ~ALL) == 0 && (mask & CENTER) != 0 && Integer.bitCount(mask) <= maximum;
    }
    public static int dx(int cell) { return cell % 5 - 2; }
    public static int dz(int cell) { return cell / 5 - 2; }
    public static int square3() {
        int mask = 0;
        for (int z = 1; z <= 3; z++) for (int x = 1; x <= 3; x++) mask |= 1 << (z * 5 + x);
        return mask;
    }
    public static int first(int mask) { return Integer.numberOfTrailingZeros(mask); }
}
