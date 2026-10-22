package dev.industrialtechnology.wireless;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import net.minecraft.nbt.*;
import java.io.IOException;
import dev.industrialtechnology.core.IndustrialTechnology;
/** Bounded chunks keep hundreds of owned device names out of the periodic detail snapshot. */
public final class WirelessCatalog implements IMessage {
    public int window,revision,part,parts;public NBTTagCompound data=new NBTTagCompound();
    public WirelessCatalog(){}
    public WirelessCatalog(int window,int revision,int part,int parts,NBTTagList rows){this.window=window;this.revision=revision;this.part=part;this.parts=parts;data.setTag("Rows",rows);}
    private void validate(){
        if(revision<1||parts<1||parts>8||part<0||part>=parts||data.getTagList("Rows",10).tagCount()>64)throw new IllegalArgumentException("Invalid catalog chunk");
        NBTTagList rows=data.getTagList("Rows",10);for(int i=0;i<rows.tagCount();i++){
            NBTTagCompound n=rows.getCompoundTagAt(i);if(WirelessData.uuid(n.getString("Id"))==null||n.getString("Name").length()>32||n.getString("Machine").length()>64||n.getString("Status").length()>32||n.getInteger("Kind")<0||n.getInteger("Kind")>2)throw new IllegalArgumentException("Invalid catalog row");
        }
    }
    @Override public void toBytes(ByteBuf out){validate();try{if(CompressedStreamTools.compress(data).length>28000)throw new IllegalArgumentException("Catalog chunk too large");}catch(IOException e){throw new IllegalStateException(e);}out.writeInt(window);out.writeInt(revision);out.writeInt(part);out.writeInt(parts);ByteBufUtils.writeTag(out,data);}
    @Override public void fromBytes(ByteBuf in){if(in.readableBytes()<18||in.readableBytes()>28018)throw new IllegalArgumentException("Invalid catalog packet");window=in.readInt();revision=in.readInt();part=in.readInt();parts=in.readInt();data=ByteBufUtils.readTag(in);if(data==null||in.isReadable())throw new IllegalArgumentException("Invalid catalog payload");validate();}
    public static final class Handler implements IMessageHandler<WirelessCatalog,IMessage>{
        @Override public IMessage onMessage(WirelessCatalog packet,MessageContext context){IndustrialTechnology.proxy.acceptCatalog(packet);return null;}
    }
}
