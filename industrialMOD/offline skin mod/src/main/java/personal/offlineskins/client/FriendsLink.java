package personal.offlineskins.client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import personal.offlineskins.SkinImage;
import personal.offlineskins.friends.FriendsApi;

/** All HTTP and image decoding lives on this bounded, low-frequency worker. */
public final class FriendsLink implements AutoCloseable {
    public static final class Image {
        public final String code,name,hash; public final SkinImage skin;
        Image(FriendsApi.Person p,SkinImage skin){code=p.code;name=p.name;hash=p.hash;this.skin=skin;}
    }
    public static final class State {
        public final String code,message;
        public final FriendsApi.Snapshot contacts;
        public final Map<String,Image> images;
        State(String code,String message,FriendsApi.Snapshot contacts,Map<String,Image> images){
            this.code=code;this.message=message;this.contacts=contacts;this.images=Collections.unmodifiableMap(new HashMap<String,Image>(images));
        }
    }
    private final FriendsApi api;
    private final Path accountFile;
    private final AtomicReference<State> update=new AtomicReference<State>();
    private final ArrayBlockingQueue<String[]> actions=new ArrayBlockingQueue<String[]>(16);
    private volatile boolean closed,active;
    private volatile String name;
    private volatile byte[] local=new byte[0];
    private boolean saved;
    private final Thread worker;
    private static FriendsApi.Snapshot emptyContacts(){return new FriendsApi.Snapshot(new ArrayList<FriendsApi.Person>(),new ArrayList<FriendsApi.Person>(),new ArrayList<FriendsApi.Person>());}

    public FriendsLink(File directory,String address,String name,byte[] png) throws IOException {
        api=new FriendsApi(address);this.name=name;local=png.clone();
        String identity=api.endpoint.contains("#")?api.endpoint.substring(api.endpoint.indexOf('#')):api.endpoint;
        Path accounts=new File(directory,"relay-accounts").toPath();Files.createDirectories(accounts);
        accountFile=accounts.resolve(FriendsApi.hash(identity.getBytes(StandardCharsets.UTF_8))+".properties");
        if(Files.isRegularFile(accountFile)) {
            Properties saved=new Properties();try(InputStream input=Files.newInputStream(accountFile)){saved.load(input);}
            api.code=saved.getProperty("code","");api.token=saved.getProperty("token","");
            if(!api.code.matches("[0-9a-f]{16}") || !api.token.matches("[0-9a-f]{64}")) throw new IOException("Invalid saved friends account; restore its backup or choose another service.");
            this.saved=true;
        }
        publish("Sharing paused.",emptyContacts(),Collections.emptyMap());
        worker=new Thread(this::run,"Offline Skins friends networking");worker.setDaemon(true);worker.start();
    }
    public String endpoint(){return api.endpoint;}
    public void active(boolean active){this.active=active;}
    public void skin(String name,byte[] png){this.name=name;if(!Arrays.equals(local,png))local=png.clone();}
    public boolean action(String action,String code){return !closed && active && actions.offer(new String[]{action,code.toLowerCase(Locale.ROOT).trim()});}
    public State poll(){return update.getAndSet(null);}
    @Override public void close(){closed=true;active=false;worker.interrupt();}

    private void run() {
        Map<String,Image> images=new HashMap<String,Image>();FriendsApi.Snapshot contacts=emptyContacts();
        byte[] uploaded=null;String named="";long next=0;boolean wasActive=false;String notice="";
        while(!closed) {
            try {
                if(!active) {
                    if(wasActive){images.clear();publish("Sharing paused in single-player / LAN.",contacts,images);wasActive=false;}
                    Thread.sleep(250);continue;
                }
                wasActive=true;
                if(api.code.isEmpty()) api.register(name);
                if(!saved){saveAccount();saved=true;}
                String currentName=name;
                if(!named.equals(currentName)){api.profile(currentName);named=currentName;}
                byte[] desired=local;
                boolean changed=!Arrays.equals(uploaded,desired);
                if(changed){api.upload(desired);uploaded=desired;}
                String[] action=actions.poll();
                if(action!=null) {
                    try {api.action(action[0],action[1]);notice="Friend list updated.";}
                    catch(IOException ex){notice=shortError(ex);}
                }
                long now=System.currentTimeMillis();
                if(now>=next || action!=null || changed) {
                    contacts=api.snapshot();Map<String,Image> refreshed=new HashMap<String,Image>();
                    for(FriendsApi.Person friend:contacts.friends) {
                        if(closed || !active)break;
                        if(friend.hash.isEmpty())continue;
                        Image old=images.get(friend.code);
                        if(old!=null && old.hash.equals(friend.hash) && old.name.equals(friend.name)){refreshed.put(friend.code,old);continue;}
                        byte[] png=api.download(friend.code);
                        // Clear/remove can race a metadata fetch. Retry on the next poll.
                        if(png.length==0 || !FriendsApi.hash(png).equals(friend.hash))continue;
                        refreshed.put(friend.code,new Image(friend,SkinImage.decode(png)));
                    }
                    images=refreshed;publish(notice.isEmpty()?"Connected - accepted friends only.":notice,contacts,images);notice="";next=now+3000;
                }
                Thread.sleep(150);
            } catch(InterruptedException ex){if(closed)return;}
            catch(Exception ex) {
                images.clear();publish("Service unavailable: "+shortError(ex),contacts,images);
                uploaded=null;next=0;
                try{Thread.sleep(5000);}catch(InterruptedException ignored){if(closed)return;}
            }
        }
    }
    private void publish(String message,FriendsApi.Snapshot contacts,Map<String,Image> images){update.set(new State(api.code,message,contacts,images));}
    private String shortError(Exception ex){String message=ex.getMessage();if(message==null)message=ex.getClass().getSimpleName();return message.length()>150?message.substring(0,150):message;}
    private void saveAccount() throws IOException {
        Properties saved=new Properties();saved.setProperty("code",api.code);saved.setProperty("token",api.token);
        Path temporary=accountFile.resolveSibling(accountFile.getFileName()+".tmp");
        try(OutputStream out=Files.newOutputStream(temporary)){saved.store(out,"Private Offline Skins friends account. Do not share this file.");}
        try{Files.move(temporary,accountFile,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
        catch(AtomicMoveNotSupportedException ex){Files.move(temporary,accountFile,StandardCopyOption.REPLACE_EXISTING);}
    }
}
