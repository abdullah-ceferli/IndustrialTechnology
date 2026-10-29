package personal.factory;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class FrameSpikeCaptureTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        require(FrameSpikeCapture.percentile(new long[0], 0.99) == 0, "Empty capture has no percentile");
        FrameSpikeCapture.reset();
        for (int i = 1; i <= 100; i++) {
            FrameSpikeCapture.record(i * 1000000L, 2000000L, 1, 2, 500000L, 100000L, 3, i == 100 ? 40 : 0);
        }
        StringWriter buffer = new StringWriter();
        PrintWriter out = new PrintWriter(buffer);
        FrameSpikeCapture.finish(out);
        out.flush();
        String text = buffer.toString();
        require(text.contains("p50=50.000 ms; p95=95.000 ms; p99=99.000 ms; worst=100.000 ms"), "Percentiles and worst frame use actual intervals");
        require(text.contains(">=33.333ms=67/100"), "Slow-frame threshold count");
        require(text.contains("100.000 | 2.000 / 2 | 0.100 / 3 | 0.500 | 40"), "Slow frames retain correlated, overlapping timings");
        require(text.contains("uploadTotalNanos: unavailable"), "An absent metric is not reported as measured zero");
        System.out.println("Frame spike report: percentile, slow-frame, correlation and unavailable-metric checks passed");
    }
}
