package personal.offlineskins;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;

public class PacketsTest {
    @Test public void uploadAndClearRoundTripWithNoClientSuppliedPlayerIdentity() {
        for (byte[] png : new byte[][] {new byte[0], new byte[] {1, 2, 3}}) {
            ByteBuf buffer = Unpooled.buffer();
            new Packets.Upload(png).toBytes(buffer);
            assertEquals(4 + png.length, buffer.readableBytes());
            Packets.Upload decoded = new Packets.Upload();
            decoded.fromBytes(buffer);
            assertArrayEquals(png, decoded.png);
            buffer.release();
        }
    }
    @Test public void broadcastPreservesIdentityAndStaysWithinLegacyPayloadLimit() {
        UUID id = UUID.randomUUID();
        byte[] png = new byte[SkinImage.MAX_BYTES];
        ByteBuf buffer = Unpooled.buffer();
        new Packets.Skin(id, png).toBytes(buffer);
        assertTrue(buffer.readableBytes() < 32767);
        Packets.Skin decoded = new Packets.Skin();
        decoded.fromBytes(buffer);
        assertEquals(id, decoded.player);
        assertArrayEquals(png, decoded.png);
        buffer.release();
    }
    @Test public void rejectsNegativeOversizedTruncatedAndTrailingPacketData() {
        for (int size : new int[] {-1, Integer.MAX_VALUE, SkinImage.MAX_BYTES + 1, 2, 0}) {
            ByteBuf buffer = Unpooled.buffer().writeInt(size).writeByte(1);
            try { new Packets.Upload().fromBytes(buffer); fail("Invalid length accepted: " + size); }
            catch (IllegalArgumentException expected) { }
            finally { buffer.release(); }
        }
    }
    @Test public void helloRoundTripAndRejectsWrongLength() {
        ByteBuf buffer = Unpooled.buffer();
        new Packets.Hello().toBytes(buffer);
        Packets.Hello hello = new Packets.Hello();
        hello.fromBytes(buffer);
        assertEquals(OfflineSkins.PROTOCOL, hello.protocol);
        buffer.clear().writeByte(1);
        try { hello.fromBytes(buffer); fail(); } catch (IllegalArgumentException expected) { }
        buffer.release();
    }

    @Test public void lanGreetingPreservesLegacyPacketSizeAndAdvertisesItsCapability() {
        ByteBuf buffer = Unpooled.buffer();
        new Packets.Hello(true).toBytes(buffer);
        assertEquals(4, buffer.readableBytes());
        Packets.Hello hello = new Packets.Hello();
        hello.fromBytes(buffer);
        assertTrue(Packets.Hello.compatible(hello.protocol));
        assertTrue(Packets.Hello.isLan(hello.protocol));
        assertNotEquals(OfflineSkins.PROTOCOL, hello.protocol);
        assertTrue(Packets.Hello.compatible(new Packets.Hello(false).protocol));
        assertFalse(Packets.Hello.isLan(new Packets.Hello(false).protocol));
        buffer.release();
    }

    @Test public void unknownProtocolOrFlagsCannotEnableLanSharing() {
        for (int value : new int[] {0, 2, -1, OfflineSkins.PROTOCOL | 0x20000}) {
            assertFalse(Packets.Hello.compatible(value));
            assertFalse(Packets.Hello.isLan(value));
        }
    }
}
