package dev.industrialtechnology.automation;
import org.junit.Test;import static org.junit.Assert.*;import io.netty.buffer.*;import java.util.UUID;
public class ScriptPacketTest {
    @Test public void multilineUploadAndDocumentRoundTrip(){UUID id=UUID.randomUUID();String text="WHEN TANK EMPTY\nTHEN ON \"Pump\"\nEND";ByteBuf b=Unpooled.buffer();new ScriptUpload(4,id,text).toBytes(b);ScriptUpload u=new ScriptUpload();u.fromBytes(b);assertEquals(text,u.text);assertEquals(id,u.device);b.clear();new ScriptDocument(4,id,9,text).toBytes(b);ScriptDocument d=new ScriptDocument();d.fromBytes(b);assertEquals(text,d.text);assertEquals(9,d.revision);}
    @Test(expected=IllegalArgumentException.class)public void truncatedUploadIsRejected(){new ScriptUpload().fromBytes(Unpooled.buffer().writeZero(21));}
    @Test(expected=IllegalArgumentException.class)public void oversizedUploadIsRejected(){new ScriptUpload().fromBytes(Unpooled.buffer().writeZero(22+ScriptCompiler.MAX_BYTES+1));}
    @Test(expected=IllegalArgumentException.class)public void malformedUtf8IsRejected(){ScriptUpload.readText(Unpooled.buffer().writeShort(2).writeByte(0xc3).writeByte(0x28));}
    @Test(expected=IllegalArgumentException.class)public void wrongLengthIsRejected(){ScriptUpload.readText(Unpooled.buffer().writeShort(7).writeByte(1));}
}
