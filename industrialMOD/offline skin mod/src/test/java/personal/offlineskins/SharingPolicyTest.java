package personal.offlineskins;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.junit.Test;
import static org.junit.Assert.*;

public class SharingPolicyTest {
    private InetSocketAddress address(String value) throws Exception {
        return new InetSocketAddress(InetAddress.getByName(value), 25565);
    }

    @Test public void privateAndLoopbackGuestsAcceptAnExplicitLanGreeting() throws Exception {
        for (String value : new String[] {"192.168.0.178", "10.0.0.1", "172.16.1.1", "127.0.0.1", "::1", "fd12::1", "fe80::1"}) {
            assertTrue(value, SharingPolicy.acceptLan(true, false, false, address(value)));
            assertFalse(value, SharingPolicy.acceptLan(false, false, false, address(value)));
        }
    }

    @Test public void internetServersAndUnresolvedAddressesCannotOverridePrivateFriendsMode() throws Exception {
        assertFalse(SharingPolicy.acceptLan(true, false, false, address("8.8.8.8")));
        assertFalse(SharingPolicy.acceptLan(true, false, false, address("2001:4860:4860::8888")));
        assertFalse(SharingPolicy.acceptLan(true, false, false, InetSocketAddress.createUnresolved("unresolved.invalid", 25565)));
        assertFalse(SharingPolicy.acceptLan(true, false, false, null));
    }

    @Test public void hostSharesOnlyAfterItsIntegratedWorldIsOpenedToLan() {
        assertFalse(SharingPolicy.acceptLan(true, true, false, null));
        assertTrue(SharingPolicy.acceptLan(true, true, true, null));
        assertFalse(SharingPolicy.mayUpload(false, true, false));
        assertFalse(SharingPolicy.mayUpload(true, true, false));
        assertTrue(SharingPolicy.mayUpload(true, true, true));
    }

    @Test public void lanGuestCanShareWhileOrdinaryFriendsModeStaysPrivate() {
        assertTrue(SharingPolicy.mayUpload(true, false, true));
        assertFalse(SharingPolicy.mayUpload(true, false, false));
        assertTrue(SharingPolicy.mayUpload(false, false, false));
    }
}
