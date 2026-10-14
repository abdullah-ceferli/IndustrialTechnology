package dev.industrialtechnology.quarry;

/** Each chunk scans descending Y, then Z rows and X columns; saves an exact next position. */
public final class MiningCursor {
    public static final int HEIGHT = 256;
    public static final int CELLS_PER_CHUNK = HEIGHT * 256;
    private final int[] positions = new int[25];
    public int cell = 12;
    public int position(int index) { return positions[index]; }
    public void set(int index, int value) { positions[index] = Math.max(0, Math.min(CELLS_PER_CHUNK, value)); }
    public int[] copy() { return positions.clone(); }
    public void clampBelow(int ceiling) {
        int start = (256 - Math.max(0, Math.min(256, ceiling))) * 256;
        for (int i = 0; i < 25; i++) if (positions[i] < start) positions[i] = start;
    }
    public long scannedBelow(int mask, int ceiling) {
        int start = (256 - Math.max(0, Math.min(256, ceiling))) * 256;
        long result = 0;
        for (int i = 0; i < 25; i++) if ((mask & (1 << i)) != 0) result += Math.max(0, positions[i] - start);
        return result;
    }
    public void read(int[] values) {
        for (int i = 0; i < 25; i++) set(i, i < values.length ? values[i] : 0);
    }
    public boolean choose(int mask) {
        if (cell >= 0 && cell < 25 && (mask & (1 << cell)) != 0 && positions[cell] < CELLS_PER_CHUNK) return true;
        for (int i = 0; i < 25; i++) if ((mask & (1 << i)) != 0 && positions[i] < CELLS_PER_CHUNK) { cell = i; return true; }
        return false;
    }
    public int x(int quarryX) { return ((quarryX >> 4) + ChunkArea.dx(cell)) * 16 + (positions[cell] & 15); }
    public int z(int quarryZ) { return ((quarryZ >> 4) + ChunkArea.dz(cell)) * 16 + ((positions[cell] >> 4) & 15); }
    public int y() { return 255 - (positions[cell] >> 8); }
    public void advance() { set(cell, positions[cell] + 1); }
    public int completedMask() {
        int mask = 0;
        for (int i = 0; i < 25; i++) if (positions[i] == CELLS_PER_CHUNK) mask |= 1 << i;
        return mask;
    }
    public long scanned(int mask) {
        long result = 0;
        for (int i = 0; i < 25; i++) if ((mask & (1 << i)) != 0) result += positions[i];
        return result;
    }
}
