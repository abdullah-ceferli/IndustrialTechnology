package personal.offlineskins.relay;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.*;
import personal.offlineskins.friends.FriendsApi;

/** Companion service, not a Minecraft server mod. Does not open any firewall rules. */
public final class FriendsRelay implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService executor;
    private final FriendsStore store;
    private final Map<String,long[]> rates=new HashMap<String,long[]>();
    private final java.util.concurrent.atomic.AtomicBoolean closed=new java.util.concurrent.atomic.AtomicBoolean();
    private final java.util.concurrent.atomic.AtomicLong requestsHandled=new java.util.concurrent.atomic.AtomicLong();
    public FriendsRelay(InetSocketAddress address,Path data,SSLContext tls) throws IOException {
        if(tls==null && !address.getAddress().isLoopbackAddress()) throw new IOException("Non-local connections require HTTPS.");
        store=new FriendsStore(data);
        if(tls==null) server=HttpServer.create(address,32);
        else { HttpsServer secure=HttpsServer.create(address,32); secure.setHttpsConfigurator(new HttpsConfigurator(tls)); server=secure; }
        executor=new ThreadPoolExecutor(2,8,30,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(64),r->{Thread t=new Thread(r,"offline-skins-service");t.setDaemon(true);return t;});
        server.setExecutor(executor);server.createContext("/",this::handle);
    }
    public void start(){server.start();}
    public int port(){return server.getAddress().getPort();}
    public long requestsHandled(){return requestsHandled.get();}
    @Override public void close(){if(closed.compareAndSet(false,true)){server.stop(0);executor.shutdownNow();}}
    private void handle(HttpExchange exchange) throws IOException {
        requestsHandled.incrementAndGet();
        try {
            if(exchange.getRequestURI().getPath().equals("/health") && exchange.getRequestMethod().equals("GET")) {respond(exchange,200,"Offline Skins Friends Service 1.1.0".getBytes(StandardCharsets.UTF_8));return;}
            if(!exchange.getRequestMethod().equals("POST") || exchange.getRequestURI().getQuery()!=null) {respond(exchange,404,"Not found".getBytes(StandardCharsets.UTF_8));return;}
            String path=exchange.getRequestURI().getPath();
            if(!path.matches("/v1/(register|profile|list|upload|skin|request|accept|decline|remove|cancel)")) {respond(exchange,404,"Not found".getBytes(StandardCharsets.UTF_8));return;}
            String action=path.substring(4);
            if(!rate(exchange.getRemoteAddress().getAddress().getHostAddress(),action.equals("register"))) {respond(exchange,429,"Too many requests. Wait a minute and try again.".getBytes(StandardCharsets.UTF_8));return;}
            String declared=exchange.getRequestHeaders().getFirst("Content-Length");
            if(declared!=null && (Long.parseLong(declared)>FriendsApi.MAX_BODY || Long.parseLong(declared)<4)) throw new IOException("Request too large or incomplete.");
            DataInputStream input=new DataInputStream(new ByteArrayInputStream(FriendsApi.readBounded(exchange.getRequestBody(),FriendsApi.MAX_BODY)));
            if(input.readInt()!=FriendsApi.MAGIC) throw new IOException("Unsupported friends protocol.");
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream output=new DataOutputStream(bytes);output.writeInt(FriendsApi.MAGIC);
            if(action.equals("register")) {
                String name=input.readUTF();end(input);FriendsStore.Registration created=store.register(name);output.writeUTF(created.account.id);output.writeUTF(created.token);
            } else {
                FriendsStore.Account owner=store.authenticate(exchange.getRequestHeaders().getFirst("Authorization"));
                if(action.equals("profile")){String name=input.readUTF();end(input);store.profile(owner,name);}
                else if(action.equals("list")){end(input);FriendsApi.Snapshot snapshot=store.snapshot(owner);people(output,snapshot.friends);people(output,snapshot.incoming);people(output,snapshot.outgoing);}
                else if(action.equals("upload")) {
                    int count=input.readInt();if(count<0 || count>24576 || input.available()!=count)throw new IOException("Invalid skin size.");
                    byte[] png=new byte[count];input.readFully(png);end(input);store.upload(owner,png);
                } else if(action.equals("skin")) {String id=input.readUTF();end(input);byte[] png=store.skin(owner,id);output.writeInt(png.length);output.write(png);}
                else {String id=input.readUTF();end(input);store.action(owner,action,id);}
            }
            output.flush();respond(exchange,200,bytes.toByteArray());
        } catch(SecurityException ex) {respond(exchange,403,ex.getMessage().getBytes(StandardCharsets.UTF_8));}
        catch(IOException | IllegalArgumentException ex) {respond(exchange,400,(ex.getMessage()==null?"Invalid request.":ex.getMessage()).getBytes(StandardCharsets.UTF_8));}
        catch(Exception ex) {respond(exchange,500,"Service error. Check the service's data directory.".getBytes(StandardCharsets.UTF_8));}
        finally {exchange.close();}
    }
    private synchronized boolean rate(String address,boolean register) {
        long now=System.currentTimeMillis()/60000;String key=address+(register?":register":":api");long[] entry=rates.get(key);
        if(entry==null || entry[0]!=now){entry=new long[]{now,0};rates.put(key,entry);}
        if(rates.size()>4096) rates.entrySet().removeIf(e->e.getValue()[0]!=now);
        return ++entry[1]<=(register?8:600);
    }
    private static void end(DataInputStream input) throws IOException{if(input.available()!=0)throw new IOException("Unexpected request data.");}
    private static void people(DataOutputStream output,List<FriendsApi.Person> list) throws IOException{output.writeInt(list.size());for(FriendsApi.Person p:list){output.writeUTF(p.code);output.writeUTF(p.name);output.writeUTF(p.hash);}}
    private static void respond(HttpExchange exchange,int status,byte[] data) throws IOException{exchange.getResponseHeaders().set("Content-Type",status==200?"application/octet-stream":"text/plain; charset=utf-8");exchange.getResponseHeaders().set("Cache-Control","no-store");exchange.sendResponseHeaders(status,data.length);try(OutputStream out=exchange.getResponseBody()){out.write(data);}}

    public static void main(String[] args) throws Exception {
        Map<String,String> options=new HashMap<String,String>();for(int i=0;i<args.length;i+=2){if(i+1>=args.length)throw new IOException("Options require values.");options.put(args[i],args[i+1]);}
        String bind=options.getOrDefault("--bind","127.0.0.1");int port=Integer.parseInt(options.getOrDefault("--port","24464"));
        SSLContext ssl=null;String fingerprint="";
        if(options.containsKey("--keystore")) {
            char[] password=new String(Files.readAllBytes(Paths.get(options.get("--password-file"))),StandardCharsets.UTF_8).trim().toCharArray();
            KeyStore keys=KeyStore.getInstance("JKS");try(InputStream in=Files.newInputStream(Paths.get(options.get("--keystore")))){keys.load(in,password);}
            KeyManagerFactory managers=KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());managers.init(keys,password);Arrays.fill(password,'\0');
            ssl=SSLContext.getInstance("TLSv1.2");ssl.init(managers.getKeyManagers(),null,new SecureRandom());fingerprint=FriendsApi.hash(keys.getCertificate("offlineskins").getEncoded());
        }
        FriendsRelay relay=new FriendsRelay(new InetSocketAddress(InetAddress.getByName(bind),port),Paths.get(options.getOrDefault("--data","friends-data")),ssl);
        Runtime.getRuntime().addShutdownHook(new Thread(relay::close));relay.start();
        System.out.println("Offline Skins friends service is running. Stop it with Ctrl+C.");
        String protocol=ssl==null?"http":"https";String suffix=fingerprint.isEmpty()?"":"#"+fingerprint;
        System.out.println("This PC: "+protocol+"://127.0.0.1:"+relay.port()+"/"+suffix);
        if(!bind.equals("127.0.0.1") && ssl!=null) {
            Enumeration<NetworkInterface> interfaces=NetworkInterface.getNetworkInterfaces();while(interfaces.hasMoreElements()){NetworkInterface card=interfaces.nextElement();Enumeration<InetAddress> addresses=card.getInetAddresses();while(addresses.hasMoreElements()){InetAddress address=addresses.nextElement();if(address instanceof Inet4Address && !address.isLoopbackAddress() && address.isSiteLocalAddress())System.out.println("Local network: https://"+address.getHostAddress()+":"+relay.port()+"/"+suffix);}}
            System.out.println("For internet friends, replace the IP above with your reachable public address. The #fingerprint stays the same.");
        }
        CountDownLatch stop=new CountDownLatch(1);
        Thread console=new Thread(()->{try{BufferedReader input=new BufferedReader(new InputStreamReader(System.in,StandardCharsets.UTF_8));String line;while((line=input.readLine())!=null)if(line.trim().equalsIgnoreCase("stop")){stop.countDown();break;}}catch(IOException ignored){}},"friends-service-console");
        console.setDaemon(true);console.start();stop.await();relay.close();
    }
}
