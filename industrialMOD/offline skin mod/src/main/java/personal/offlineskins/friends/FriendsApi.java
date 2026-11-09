package personal.offlineskins.friends;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import javax.net.ssl.*;

/** Small, bounded, authenticated protocol, independent of Minecraft/render threads. */
public final class FriendsApi {
    public static final int MAGIC = 0x4f534631;
    public static final int MAX_BODY = 32768;
    public static final int MAX_CONTACTS = 64;
    public static final class Person {
        public final String code, name, hash;
        public Person(String code, String name, String hash) { this.code=code; this.name=name; this.hash=hash; }
    }
    public static final class Snapshot {
        public final List<Person> friends, incoming, outgoing;
        public Snapshot(List<Person> friends, List<Person> incoming, List<Person> outgoing) {
            this.friends=Collections.unmodifiableList(friends);
            this.incoming=Collections.unmodifiableList(incoming);
            this.outgoing=Collections.unmodifiableList(outgoing);
        }
    }
    public final String endpoint;
    private final URL base;
    private final String pin;
    public String code = "", token = "";

    public FriendsApi(String address) throws IOException {
        try {
            URI uri = new URI(address.trim());
            String scheme=uri.getScheme(), host=uri.getHost(), path=uri.getPath();
            if (host == null || uri.getUserInfo()!=null || uri.getQuery()!=null
                    || (path!=null && !path.isEmpty() && !path.equals("/"))) throw new IOException("Use the service address, without a path or login.");
            if (!"https".equalsIgnoreCase(scheme) && !("http".equalsIgnoreCase(scheme)
                    && (host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1") || host.equals("[::1]") || host.equals("::1")))) {
                throw new IOException("Internet/LAN sharing requires HTTPS. HTTP is allowed only on this PC (localhost).");
            }
            pin = uri.getFragment()==null ? "" : uri.getFragment().toLowerCase(Locale.ROOT);
            if (!pin.isEmpty() && (!pin.matches("[0-9a-f]{64}") || !scheme.equalsIgnoreCase("https"))) throw new IOException("Invalid service certificate fingerprint.");
            base = new URI(scheme.toLowerCase(Locale.ROOT), null, host.toLowerCase(Locale.ROOT), uri.getPort(), "/", null, null).toURL();
            endpoint=base.toString()+(pin.isEmpty()?"":"#"+pin);
        } catch (URISyntaxException | IllegalArgumentException ex) { throw new IOException("Invalid service address.", ex); }
    }

    public void register(String name) throws IOException {
        DataInputStream reply=call("register", output -> output.writeUTF(name), false);
        code=reply.readUTF(); token=reply.readUTF();
        if (!code.matches("[0-9a-f]{16}") || !token.matches("[0-9a-f]{64}")) throw new IOException("Invalid service account response.");
        end(reply);
    }
    public void profile(String name) throws IOException { end(call("profile", out -> out.writeUTF(name), true)); }
    public void action(String action, String target) throws IOException {
        if (!Arrays.asList("request","accept","decline","remove","cancel").contains(action)) throw new IOException("Unknown friend action.");
        if (!target.matches("[0-9a-f]{16}")) throw new IOException("A friend code has 16 letters/numbers. Ask your friend to copy theirs.");
        end(call(action, out -> out.writeUTF(target), true));
    }
    public void upload(byte[] png) throws IOException {
        if (png.length>24576) throw new IOException("Skin too large.");
        end(call("upload", out -> { out.writeInt(png.length); out.write(png); }, true));
    }
    public byte[] download(String target) throws IOException {
        DataInputStream reply=call("skin", out -> out.writeUTF(target), true);
        int length=reply.readInt();
        if (length<0 || length>24576 || length!=reply.available()) throw new IOException("Invalid skin response.");
        byte[] png=new byte[length]; reply.readFully(png); end(reply); return png;
    }
    public Snapshot snapshot() throws IOException {
        DataInputStream reply=call("list", out -> {}, true);
        Snapshot snapshot=new Snapshot(people(reply),people(reply),people(reply)); end(reply); return snapshot;
    }
    private List<Person> people(DataInputStream input) throws IOException {
        int count=input.readInt();
        if (count<0 || count>MAX_CONTACTS) throw new IOException("Invalid contact list size.");
        List<Person> people=new ArrayList<Person>();
        for(int i=0;i<count;i++) {
            String id=input.readUTF(), name=input.readUTF(), hash=input.readUTF();
            if(!id.matches("[0-9a-f]{16}") || !name.matches("[A-Za-z0-9_]{1,16}") || !(hash.isEmpty() || hash.matches("[0-9a-f]{64}"))) throw new IOException("Invalid friend data.");
            people.add(new Person(id,name,hash));
        }
        return people;
    }
    private interface Body { void write(DataOutputStream out) throws IOException; }
    private DataInputStream call(String action, Body body, boolean authenticated) throws IOException {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        DataOutputStream output=new DataOutputStream(bytes); output.writeInt(MAGIC); body.write(output); output.flush();
        if(bytes.size()>MAX_BODY) throw new IOException("Request too large.");
        HttpURLConnection connection=(HttpURLConnection)new URL(base,"v1/"+action).openConnection(Proxy.NO_PROXY);
        try {
            if(connection instanceof HttpsURLConnection && !pin.isEmpty()) pin((HttpsURLConnection)connection);
            connection.setConnectTimeout(4000); connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(false); connection.setUseCaches(false);
            connection.setRequestMethod("POST"); connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","application/octet-stream");
            if(authenticated) {
                if(!code.matches("[0-9a-f]{16}") || !token.matches("[0-9a-f]{64}")) throw new IOException("Connect to create your service account first.");
                connection.setRequestProperty("Authorization","Bearer "+code+"."+token);
            }
            connection.setFixedLengthStreamingMode(bytes.size());
            try(OutputStream stream=connection.getOutputStream()) { bytes.writeTo(stream); }
            int status=connection.getResponseCode();
            if(status!=200) {
                InputStream error=connection.getErrorStream();
                String message=error==null ? "Service error "+status : new String(readBounded(error,1024),StandardCharsets.UTF_8);
                throw new IOException(message);
            }
            DataInputStream result=new DataInputStream(new ByteArrayInputStream(readBounded(connection.getInputStream(),MAX_BODY)));
            if(result.readInt()!=MAGIC) throw new IOException("This address is not an Offline Skins friends service.");
            return result;
        } finally { connection.disconnect(); }
    }
    public static byte[] readBounded(InputStream stream, int max) throws IOException {
        try(InputStream input=stream) {
            ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] block=new byte[4096]; int count;
            while((count=input.read(block))!=-1) { if(out.size()+count>max) throw new IOException("Response too large."); out.write(block,0,count); }
            return out.toByteArray();
        }
    }
    private static void end(DataInputStream input) throws IOException { if(input.available()!=0) throw new IOException("Unexpected response data."); }
    public static String hash(byte[] bytes) {
        try { StringBuilder hex=new StringBuilder(); for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes)) hex.append(String.format("%02x",b&255)); return hex.toString(); }
        catch(NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
    private void pin(HttpsURLConnection connection) throws IOException {
        try {
            SSLContext ssl=SSLContext.getInstance("TLSv1.2");
            ssl.init(null,new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers(){ return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] chain,String type) throws CertificateException { throw new CertificateException("Not a client certificate authority"); }
                public void checkServerTrusted(X509Certificate[] chain,String type) throws CertificateException {
                    if(chain==null || chain.length==0) throw new CertificateException("Missing service certificate");
                    chain[0].checkValidity();
                    if(!hash(chain[0].getEncoded()).equals(pin)) throw new CertificateException("Service certificate fingerprint changed");
                }
            }},new SecureRandom());
            connection.setSSLSocketFactory(ssl.getSocketFactory());
            // An exact, user-configured certificate fingerprint authenticates this
            // endpoint even if its IP changes or its certificate is self-signed.
            connection.setHostnameVerifier((host, session) -> {
                try { return hash(session.getPeerCertificates()[0].getEncoded()).equals(pin); }
                catch(Exception ex) { return false; }
            });
        } catch(GeneralSecurityException ex) { throw new IOException("Cannot initialize secure skin sharing.",ex); }
    }
}
