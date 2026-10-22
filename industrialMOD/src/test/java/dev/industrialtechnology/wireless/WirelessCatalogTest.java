package dev.industrialtechnology.wireless;
import org.junit.Test;import static org.junit.Assert.*;import net.minecraft.nbt.*;import io.netty.buffer.*;import java.util.UUID;
public class WirelessCatalogTest {
    private NBTTagList rows(String name){NBTTagList l=new NBTTagList();NBTTagCompound n=new NBTTagCompound();n.setString("Id",UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString());n.setString("Name",name);n.setInteger("Kind",0);l.appendTag(n);return l;}
    @Test public void packetRoundTripRetainsBoundedMetadata(){ByteBuf b=Unpooled.buffer();new WirelessCatalog(7,2,0,1,rows("Reactor")).toBytes(b);WirelessCatalog p=new WirelessCatalog();p.fromBytes(b);assertEquals(7,p.window);assertEquals("Reactor",p.data.getTagList("Rows",10).getCompoundTagAt(0).getString("Name"));}
    @Test public void outOfOrderChunksPublishOnlyWhenComplete(){CatalogAssembler a=new CatalogAssembler();a.accept(new WirelessCatalog(1,1,1,2,rows("B")));assertFalse(a.ready);a.accept(new WirelessCatalog(1,1,0,2,rows("A")));assertTrue(a.ready);assertEquals(2,a.rows.tagCount());assertEquals("A",a.rows.getCompoundTagAt(0).getString("Name"));a.accept(new WirelessCatalog(1,2,0,2,rows("C")));assertEquals(2,a.rows.tagCount());a.accept(new WirelessCatalog(1,1,0,2,rows("stale")));a.accept(new WirelessCatalog(1,2,1,2,rows("D")));assertEquals("C",a.rows.getCompoundTagAt(0).getString("Name"));}
    @Test(expected=IllegalArgumentException.class)public void oversizedPacketRejected(){new WirelessCatalog().fromBytes(Unpooled.buffer().writeZero(28019));}
    @Test(expected=IllegalArgumentException.class)public void excessiveChunksRejected(){new WirelessCatalog(1,1,0,9,rows("A")).toBytes(Unpooled.buffer());}
    @Test(expected=IllegalArgumentException.class)public void oversizedNameRejected(){new WirelessCatalog(1,1,0,1,rows("01234567890123456789012345678901234")).toBytes(Unpooled.buffer());}
}
