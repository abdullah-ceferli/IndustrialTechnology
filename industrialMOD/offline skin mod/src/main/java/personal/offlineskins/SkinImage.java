package personal.offlineskins;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import javax.imageio.ImageIO;

/** Bounded PNG input, normalized to the native 1.7.10 64x32 skin layout. */
public final class SkinImage {
    public static final int MAX_BYTES = 24576;
    private static final byte[] PNG_SIGNATURE = {(byte)137,80,78,71,13,10,26,10};
    public final BufferedImage image;
    public final byte[] png;

    private SkinImage(BufferedImage image, byte[] png) {
        this.image = image;
        this.png = png;
    }

    public static SkinImage read(File file) throws IOException {
        if (!file.isFile()) throw new IOException("Skin file not found: " + file.getName() + ". Use Open skin folder to add it.");
        long size = Files.size(file.toPath());
        if (size < 33 || size > MAX_BYTES) throw new IOException("Skin must be a PNG smaller than 24 KiB.");
        // A bounded stream also handles a file being replaced between size() and read().
        try (java.io.InputStream in = Files.newInputStream(file.toPath())) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] block = new byte[4096];
            int count;
            while ((count = in.read(block)) != -1) {
                if (out.size() + count > MAX_BYTES) throw new IOException("Skin file is too large.");
                out.write(block, 0, count);
            }
            return decode(out.toByteArray());
        }
    }

    public static SkinImage decode(byte[] data) throws IOException {
        if (data == null || data.length < 33 || data.length > MAX_BYTES
                || !Arrays.equals(PNG_SIGNATURE, Arrays.copyOf(data, 8))) {
            throw new IOException("Use a PNG skin (64x32 or 64x64, maximum 24 KiB).");
        }
        ByteBuffer header = ByteBuffer.wrap(data);
        if (header.getInt(8) != 13 || header.getInt(12) != 0x49484452) {
            throw new IOException("PNG header is invalid.");
        }
        int width = header.getInt(16), height = header.getInt(20);
        if (width != 64 || (height != 32 && height != 64)) {
            throw new IOException("Skin must be exactly 64x32 or 64x64 pixels.");
        }
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(data));
        if (source == null || source.getWidth() != width || source.getHeight() != height) {
            throw new IOException("Cannot read this PNG.");
        }
        BufferedImage classic = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        classic.setRGB(0, 0, 64, 32, source.getRGB(0, 0, 64, 32, null, 0, 64), 0, 64);
        opaque(classic, 0, 0, 32, 16);
        opaque(classic, 0, 16, 64, 32);
        // Legacy editors fill the unused hat area with opaque pixels. Match vanilla's
        // transparency rule on both input layouts, so normalization is idempotent
        // when the server validates an already converted skin a second time.
        {
            boolean transparent = false;
            for (int y = 0; y < 16; y++) for (int x = 32; x < 64; x++) {
                if ((classic.getRGB(x, y) >>> 24) < 128) transparent = true;
            }
            if (!transparent) {
                for (int y = 0; y < 16; y++) for (int x = 32; x < 64; x++) {
                    classic.setRGB(x, y, classic.getRGB(x, y) & 0xffffff);
                }
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!ImageIO.write(classic, "png", out)) throw new IOException("PNG encoder is unavailable.");
        return new SkinImage(classic, out.toByteArray());
    }

    private static void opaque(BufferedImage image, int x0, int y0, int x1, int y1) {
        for (int y = y0; y < y1; y++) for (int x = x0; x < x1; x++) {
            image.setRGB(x, y, image.getRGB(x, y) | 0xff000000);
        }
    }
}
