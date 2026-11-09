package personal.offlineskins;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Random;
import javax.imageio.ImageIO;
import org.junit.Test;
import static org.junit.Assert.*;

public class SkinImageTest {
    static byte[] png(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    @Test public void legacyOpaqueHatIsRemovedButBodyStaysOpaque() throws Exception {
        BufferedImage source = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 32; y++) for (int x = 0; x < 64; x++) source.setRGB(x, y, 0xff123456);
        SkinImage result = SkinImage.decode(png(source));
        assertEquals(0xff123456, result.image.getRGB(8, 8));
        assertEquals(0xff123456, result.image.getRGB(44, 25));
        assertEquals(0x00123456, result.image.getRGB(40, 8));
    }

    @Test public void transparentLegacyHatIsPreserved() throws Exception {
        BufferedImage source = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(40, 8, 0xffff0000);
        SkinImage result = SkinImage.decode(png(source));
        assertEquals(0xffff0000, result.image.getRGB(40, 8));
        assertEquals(0, result.image.getRGB(41, 8));
        assertEquals(255, result.image.getRGB(22, 22) >>> 24);
    }

    @Test public void modernSkinUsesTopHalfAndPreservesOpaqueHat() throws Exception {
        BufferedImage source = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(40, 8, 0xff00ff00);
        source.setRGB(20, 20, 0xff0000ff);
        source.setRGB(20, 52, 0xffff0000);
        SkinImage result = SkinImage.decode(png(source));
        assertEquals(32, result.image.getHeight());
        assertEquals(0xff00ff00, result.image.getRGB(40, 8));
        assertEquals(0xff0000ff, result.image.getRGB(20, 20));
    }

    @Test public void randomSkinRemainsBelowPacketLimitAndCanonicalizationIsStable() throws Exception {
        BufferedImage source = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Random random = new Random(7);
        for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) source.setRGB(x, y, random.nextInt());
        SkinImage result = SkinImage.decode(png(source));
        assertTrue(result.png.length < SkinImage.MAX_BYTES);
        assertArrayEquals(result.png, SkinImage.decode(result.png).png);
    }

    @Test public void solidModernSkinAlsoRemainsStableAfterServerValidation() throws Exception {
        BufferedImage source = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) source.setRGB(x, y, 0xff123456);
        SkinImage result = SkinImage.decode(png(source));
        assertArrayEquals(result.png, SkinImage.decode(result.png).png);
    }

    @Test public void rejectsWrongFormatWrongDimensionsAndBombHeader() throws Exception {
        reject(new byte[40]);
        reject(png(new BufferedImage(128, 64, BufferedImage.TYPE_INT_ARGB)));
        byte[] oversized = png(new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB));
        ByteBuffer.wrap(oversized).putInt(16, Integer.MAX_VALUE).putInt(20, Integer.MAX_VALUE);
        reject(oversized);
        reject(new byte[SkinImage.MAX_BYTES + 1]);
    }

    @Test public void filenamesCannotEscapeSkinFolderAndMissingFileHasHelpfulFailure() throws Exception {
        File root = Files.createTempDirectory("skin-path-test").toFile();
        SkinFiles files = new SkinFiles(root);
        for (String name : new String[] {"../bad.png", "..\\bad.png", "C:bad.png", "bad.jpg", ""}) {
            try { files.resolve(name); fail(name); } catch (IOException expected) { }
        }
        assertEquals(new File(root, "my skin.png").getCanonicalFile(), files.resolve("my skin.png").getCanonicalFile());
        Files.delete(root.toPath());
    }

    private void reject(byte[] data) {
        try { SkinImage.decode(data); fail("Invalid input accepted"); }
        catch (IOException expected) { }
    }
}
