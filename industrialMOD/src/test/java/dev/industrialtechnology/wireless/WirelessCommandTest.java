package dev.industrialtechnology.wireless;
import org.junit.Test;
import static org.junit.Assert.*;
import io.netty.buffer.*;
public class WirelessCommandTest {
    @Test public void boundedCommandRoundTripsExactly(){ByteBuf a=Unpooled.buffer(),b=Unpooled.buffer();new WirelessCommand(8,WirelessCommand.RENAME,1,2,"Main Arc Furnace").toBytes(a);byte[] expected=new byte[a.readableBytes()];a.getBytes(0,expected);WirelessCommand decoded=new WirelessCommand();decoded.fromBytes(a);decoded.toBytes(b);byte[] actual=new byte[b.readableBytes()];b.readBytes(actual);assertArrayEquals(expected,actual);}
    @Test(expected=IllegalArgumentException.class) public void rejectsOversizedIncomingCommand(){new WirelessCommand().fromBytes(Unpooled.buffer().writeZero(144));}
    @Test(expected=IllegalArgumentException.class) public void rejectsTruncatedCommand(){new WirelessCommand().fromBytes(Unpooled.buffer().writeZero(14));}
    @Test(expected=IllegalArgumentException.class) public void rejectsOversizedTextBeforeEncoding(){StringBuilder text=new StringBuilder();for(int i=0;i<129;i++)text.append('x');new WirelessCommand(1,WirelessCommand.RENAME,0,0,text.toString()).toBytes(Unpooled.buffer());}
}
