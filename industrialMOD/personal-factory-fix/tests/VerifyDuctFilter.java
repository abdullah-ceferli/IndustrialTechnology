import java.io.*;
import java.util.Arrays;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TileItemDuctEnder;
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
        tile.handledPackets++;
        if(tile instanceof TileItemDuctEnder)((TileItemDuctEnder)tile).powered=consumed[consumed.length-1]!=0;
        DuctPacketFix.innerMark(tile.getTestWorld(),tile.getTestX(),tile.getTestY(),tile.getTestZ());
        DuctPacketFix.outerMark(tile.getTestWorld(),tile.getTestX(),tile.getTestY(),tile.getTestZ());
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
        testEnder();
        System.out.println("PASS: plain and Ender packet processing, initial/change/duplicate/periodic redraws, power/info invalidation, server/disabled behavior, replacement, world/coordinate identity, unsupported subclass and stream fallback");
    }
    private static void testEnder() throws Exception {
        DuctPacketFix.toggle();DuctPacketFix.toggle();
        World world=new World();TileItemDuctEnder ender=new TileItemDuctEnder(world);
        packet(ender,new byte[]{1,2,0},false);check(world.marks==1,"Ender first state redraws once");
        packet(ender,new byte[]{1,2,0},false);check(world.marks==1,"Ender identical full state skips redraw");
        check(ender.handledPackets==2,"Duplicate data is still consumed and processed");
        packet(ender,new byte[]{1,2,1},false);check(world.marks==2 && ender.powered,"Changed Ender power redraws immediately");
        DuctPacketFix.invalidate(ender);ender.powered=false; // Separate power/info packet.
        packet(ender,new byte[]{1,2,1},false);
        check(world.marks==3 && ender.powered,"Matching old description redraws after an intervening info/power update");
        Thread.sleep(1100);
        packet(ender,new byte[]{1,2,1},false);check(world.marks==4,"Ender periodic refresh remains bounded");
        packet(ender,new byte[]{1,2,1},true);check(world.marks==6,"Ender server path unchanged");
        DuctPacketFix.toggle();packet(ender,new byte[]{1,2,1},false);check(world.marks==8,"F10 disabled Ender path unchanged");DuctPacketFix.toggle();
        packet(ender,new byte[]{1,2,1},false);check(world.marks==9,"Reenabled filter starts with a redraw");
        ender.setTestPosition(4,5,6);packet(ender,new byte[]{1,2,1},false);check(world.marks==10,"Moved tile cannot reuse an old-position snapshot");
        World next=new World();ender.setTestWorld(next);packet(ender,new byte[]{1,2,1},false);check(next.marks==1,"Same tile in another world redraws");
        packet(new TileItemDuctEnder(next),new byte[]{1,2,1},false);check(next.marks==2,"Replacement Ender tile redraws");
        TileItemDuct unsupported=new TileItemDuctEnder(next){};
        packet(unsupported,new byte[]{1,2,1},false);packet(unsupported,new byte[]{1,2,1},false);
        check(next.marks==6,"Unrecognized subclasses preserve both original redraws");
        Packet noReset=new Packet(new byte[]{1,2,1});
        noReset.datain=new DataInputStream(new FilterInputStream(new ByteArrayInputStream(new byte[]{1,2,1})) {
            public boolean markSupported(){return false;}
        });
        DuctPacketFix.beginPacket(ender,noReset,false);
        check(noReset.datain.readByte()==1,"Unsupported stream cursor unchanged");
        DuctPacketFix.innerMark(next,4,5,6);DuctPacketFix.outerMark(next,4,5,6);DuctPacketFix.endPacket();
        check(next.marks==8,"Unsupported packet streams retain original redraws");
        check(DuctPacketFix.enderIdentical>=2 && DuctPacketFix.enderSuppressed>0,"Ender subset diagnostics count actual filtered work");
    }
    static void check(boolean pass,String message) { if(!pass)throw new AssertionError(message); }
}
