package personal.offlineskins;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/** A remote Internet server cannot override the client's friends-only preference. */
public final class SharingPolicy {
    private SharingPolicy() { }

    public static boolean acceptLan(boolean announced, boolean localChannel,
            boolean integratedWorldIsPublic, SocketAddress remoteAddress) {
        if (!announced) return false;
        if (localChannel) return integratedWorldIsPublic;
        if (!(remoteAddress instanceof InetSocketAddress)) return false;
        InetAddress address = ((InetSocketAddress) remoteAddress).getAddress();
        if (address == null) return false;
        byte[] bytes = address.getAddress();
        boolean uniqueLocalV6 = bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
        return address.isLoopbackAddress() || address.isSiteLocalAddress()
                || address.isLinkLocalAddress() || uniqueLocalV6;
    }

    public static boolean mayUpload(boolean friendsOnly, boolean integrated, boolean lan) {
        return lan || (!friendsOnly && !integrated);
    }
}
