package dev.industrialtechnology.capacitor;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import dev.industrialtechnology.core.IndustrialTechnology;
public final class BankSnapshot implements IMessage {
    private int x,y,z,window;private NBTTagCompound state;
    public BankSnapshot(){}
    public BankSnapshot(TileCapacitorBank b,int w){this(b,w,b.snapshot());}
    public BankSnapshot(TileCapacitorBank b,int w,NBTTagCompound s){x=b.xCoord;y=b.yCoord;z=b.zCoord;window=w;state=s;}
    @Override public void toBytes(ByteBuf b){b.writeInt(x);b.writeInt(y);b.writeInt(z);b.writeInt(window);ByteBufUtils.writeTag(b,state);}
    @Override public void fromBytes(ByteBuf b){x=b.readInt();y=b.readInt();z=b.readInt();window=b.readInt();state=ByteBufUtils.readTag(b);}
    public static final class Handler implements IMessageHandler<BankSnapshot,IMessage>{public IMessage onMessage(BankSnapshot m,MessageContext c){if(m.state!=null)IndustrialTechnology.proxy.acceptBankSnapshot(m.x,m.y,m.z,m.window,m.state);return null;}}
}
