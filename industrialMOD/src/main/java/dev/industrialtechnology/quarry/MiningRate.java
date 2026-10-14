package dev.industrialtechnology.quarry;

/** Fractional block credit backed by actual RF at commit. Credit can never exceed one tick's work budget. */
public final class MiningRate {
    private MiningRate() {}
    public static double advance(double credit, int input, int targetInput, int maximum, double multiplier, int operations) {
        double rate = Math.min(operations, maximum * multiplier);
        double gain = rate * Math.max(0, Math.min(input, targetInput)) / Math.max(1.0, targetInput);
        return Math.max(0, Math.min(operations, credit + gain));
    }
}
