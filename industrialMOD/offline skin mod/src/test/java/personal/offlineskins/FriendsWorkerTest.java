package personal.offlineskins;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import org.junit.Test;
import personal.offlineskins.client.FriendsLink;
import personal.offlineskins.relay.FriendsRelay;
import static org.junit.Assert.*;

public class FriendsWorkerTest {
    @Test public void disabledWorkerDoesNoNetworkingAndResumesWithTheSameIdentity() throws Exception {
        try(FriendsRelay relay=new FriendsRelay(new InetSocketAddress("127.0.0.1",0),Files.createTempDirectory("friends-paused-service"),null)) {
            relay.start();
            try(FriendsLink link=new FriendsLink(Files.createTempDirectory("friends-paused-client").toFile(),"http://127.0.0.1:"+relay.port(),"PlayerA",new byte[0])) {
                Thread.sleep(500);assertEquals(0,relay.requestsHandled());
                link.active(true);long deadline=System.currentTimeMillis()+5000;String code="";
                while(System.currentTimeMillis()<deadline){FriendsLink.State state=link.poll();if(state!=null && !state.code.isEmpty()){code=state.code;break;}Thread.sleep(50);}
                assertFalse(code.isEmpty());link.active(false);Thread.sleep(500);long count=relay.requestsHandled();
                Thread.sleep(500);assertEquals(count,relay.requestsHandled());
                assertFalse(link.action("request","0123456789abcdef"));
                link.active(true);Thread.sleep(3000);
                FriendsLink.State state=link.poll();assertNotNull(state);assertEquals(code,state.code);
            }
        }
    }
}
