import java.io.*;
import java.util.Arrays;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import personal.factory.DuctPacketFix;

public final class VerifyDuctFilter {
    public static final class World {
        int marks;
        public void markBlockForUpdate(int x,int y,int z) { marks++; }
    }
    public static final class Packet {
        public DataInputStream datain;
        Packet(byte[] bytes) { datain=new DataInputStream(new ByteArrayInputStream(bytes)); }
    }
    static void packet(TileItemDuct tile,byte[] bytes,boolean server) throws Exception {
        Packet packet=new Packet(bytes);
        DuctPacketFix.beginPacket(tile,packet,server);
        byte[] consumed=new byte[bytes.length];packet.datain.readFully(consumed);
        if(!Arrays.equals(bytes,consumed))throw new AssertionError("Original packet cursor was changed");
        DuctPacketFix.innerMark(tile.getTestWorld(),1,2,3);
        DuctPacketFix.outerMark(tile.getTestWorld(),1,2,3);
        DuctPacketFix.endPacket();
    }
    public static void main(String[] args) throws Exception {
        World world=new World();TileItemDuct tile=new TileItemDuct(world);
        packet(tile,new byte[]{1,2,3},false);check(world.marks==1,"First state redraws once");
        packet(tile,new byte[]{1,2,3},false);check(world.marks==1,"Identical state does not redraw");
        packet(tile,new byte[]{1,2,4},false);check(world.marks==2,"Changed state redraws immediately");
        Thread.sleep(1100);
        packet(tile,new byte[]{1,2,4},false);check(world.marks==3,"Periodic refresh preserved");
        packet(tile,new byte[]{1,2,4},true);check(world.marks==5,"Server path unchanged");
        DuctPacketFix.enabled=false;
        packet(tile,new byte[]{1,2,4},false);check(world.marks==7,"Disabled filter preserves both original calls");
        DuctPacketFix.enabled=true;
        TileItemDuct replacement=new TileItemDuct(world);
        packet(replacement,new byte[]{1,2,4},false);check(world.marks==8,"Replacement tile gets first redraw");
        DuctPacketFix.beginPacket(tile,new Packet(new byte[]{1,2,4}),false);
        DuctPacketFix.innerMark(world,9,2,3);DuctPacketFix.endPacket();
        check(world.marks==9,"Different coordinate is never suppressed");
        System.out.println("PASS: packet cursor, initial/change/duplicate/periodic redraws, server behavior, disable, tile replacement, and coordinate guard");
    }
    static void check(boolean pass,String message) { if(!pass)throw new AssertionError(message); }
}
