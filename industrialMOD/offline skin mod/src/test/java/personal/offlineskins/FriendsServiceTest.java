package personal.offlineskins;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import org.junit.Test;
import personal.offlineskins.friends.FriendsApi;
import personal.offlineskins.relay.FriendsRelay;
import personal.offlineskins.relay.FriendsStore;
import static org.junit.Assert.*;

public class FriendsServiceTest {
    private byte[] skin(int color) throws Exception {
        BufferedImage image=new BufferedImage(64,32,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<32;y++)for(int x=0;x<64;x++)image.setRGB(x,y,color);
        ByteArrayOutputStream out=new ByteArrayOutputStream();javax.imageio.ImageIO.write(image,"png",out);return SkinImage.decode(out.toByteArray()).png;
    }
    private FriendsApi api(FriendsRelay relay,String name) throws Exception {FriendsApi api=new FriendsApi("http://127.0.0.1:"+relay.port());api.register(name);return api;}
    @Test public void requestAloneCannotReadSkinAndAcceptSharesOnlyWithThatFriend() throws Exception {
        try(FriendsRelay relay=new FriendsRelay(new InetSocketAddress("127.0.0.1",0),Files.createTempDirectory("skin-relay"),null)) {
            relay.start();FriendsApi a=api(relay,"PlayerA"),b=api(relay,"PlayerB"),outsider=api(relay,"Other");
            byte[] red=skin(0xffcc3333);a.upload(red);b.action("request",a.code);
            assertEquals(1,a.snapshot().incoming.size());assertEquals(0,b.snapshot().friends.size());
            denied(()->b.download(a.code));denied(()->outsider.download(a.code));
            a.action("accept",b.code);assertEquals(1,b.snapshot().friends.size());assertArrayEquals(red,b.download(a.code));
            denied(()->outsider.download(a.code));assertTrue(outsider.snapshot().friends.isEmpty());
            byte[] green=skin(0xff33cc66);a.upload(green);assertArrayEquals(green,b.download(a.code));
            a.upload(new byte[0]);assertEquals(0,b.download(a.code).length);
            a.upload(red);a.action("remove",b.code);assertTrue(b.snapshot().friends.isEmpty());denied(()->b.download(a.code));
        }
    }
    @Test public void friendsAndPrivateAccountsSurviveServiceRestart() throws Exception {
        Path folder=Files.createTempDirectory("skin-relay-persistence");String aid,bid,token;
        FriendsStore original=new FriendsStore(folder);FriendsStore.Registration a=original.register("PlayerA"),b=original.register("PlayerB");
        original.upload(a.account,skin(0xff123456));original.action(a.account,"request",b.account.id);original.action(b.account,"accept",a.account.id);
        aid=a.account.id;bid=b.account.id;token=b.token;
        FriendsStore reopened=new FriendsStore(folder);FriendsStore.Account auth=reopened.authenticate("Bearer "+bid+"."+token);
        assertEquals(aid,reopened.snapshot(auth).friends.get(0).code);assertTrue(reopened.skin(auth,aid).length>0);
        String saved=new String(Files.readAllBytes(folder.resolve("accounts/"+bid+".properties")),java.nio.charset.StandardCharsets.ISO_8859_1);
        assertFalse(saved.contains(token));
    }
    @Test public void cancelDeclineAndNoSelfOrForgedAcceptance() throws Exception {
        try(FriendsRelay relay=new FriendsRelay(new InetSocketAddress("127.0.0.1",0),Files.createTempDirectory("skin-relay"),null)) {
            relay.start();FriendsApi a=api(relay,"PlayerA"),b=api(relay,"PlayerB");
            denied(()->a.action("accept",b.code));denied(()->a.action("request",a.code));
            a.action("request",b.code);a.action("cancel",b.code);assertTrue(b.snapshot().incoming.isEmpty());
            a.action("request",b.code);b.action("decline",a.code);assertTrue(a.snapshot().outgoing.isEmpty());
            b.token=String.join("",Collections.nCopies(64,"0"));denied(b::snapshot);
        }
    }
    @Test public void rejectsInvalidImagesAndInsecureRemoteEndpoints() throws Exception {
        for(String address:new String[]{"http://example.com","http://192.168.1.5:24464","https://user:pass@example.com","https://example.com/path","https://example.com#bad"}) {
            try{new FriendsApi(address);fail(address);}catch(IOException expected){}
        }
        try(FriendsRelay relay=new FriendsRelay(new InetSocketAddress("127.0.0.1",0),Files.createTempDirectory("skin-relay"),null)) {
            relay.start();FriendsApi a=api(relay,"PlayerA");denied(()->a.upload(new byte[200]));assertTrue(a.snapshot().friends.isEmpty());
        }
        try{new FriendsRelay(new InetSocketAddress("0.0.0.0",0),Files.createTempDirectory("skin-relay-tls"),null);fail();}catch(IOException expected){}
    }
    private interface Operation{void run() throws Exception;}
    private void denied(Operation operation) throws Exception {try{operation.run();fail("Operation should have been rejected");}catch(IOException expected){}}
}
