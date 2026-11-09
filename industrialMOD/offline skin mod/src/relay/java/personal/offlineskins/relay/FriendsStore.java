package personal.offlineskins.relay;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import personal.offlineskins.SkinImage;
import personal.offlineskins.friends.FriendsApi;

/** Private account/skin store. All operations are serialized; files are replaced atomically. */
public final class FriendsStore {
    public static final class Account {
        public final String id, secretHash;
        public String name, skinHash="";
        Account(String id,String name,String secretHash){this.id=id;this.name=name;this.secretHash=secretHash;}
    }
    public static final class Registration {
        public final Account account; public final String token;
        Registration(Account account,String token){this.account=account;this.token=token;}
    }
    private final Path root;
    private final Map<String,Account> accounts=new HashMap<String,Account>();
    private final Set<String> friends=new HashSet<String>(), requests=new HashSet<String>();
    private final SecureRandom random=new SecureRandom();

    public FriendsStore(Path root) throws IOException {
        this.root=root; Files.createDirectories(root.resolve("accounts")); Files.createDirectories(root.resolve("skins"));
        try(DirectoryStream<Path> files=Files.newDirectoryStream(root.resolve("accounts"),"*.properties")) {
            for(Path path:files) {
                Properties properties=load(path); String id=properties.getProperty("id",""),name=properties.getProperty("name",""),hash=properties.getProperty("secretHash","");
                if(!id.matches("[0-9a-f]{16}") || !validName(name) || !hash.matches("[0-9a-f]{64}")) throw new IOException("Invalid saved service account.");
                Account account=new Account(id,name,hash);
                Path skin=root.resolve("skins/"+id+".png");
                if(Files.isRegularFile(skin)) account.skinHash=FriendsApi.hash(Files.readAllBytes(skin));
                accounts.put(id,account);
            }
        }
        Properties relationships=load(root.resolve("relationships.properties"));
        for(String key:relationships.stringPropertyNames()) {
            if(key.matches("friend\\.[0-9a-f]{16}\\.[0-9a-f]{16}")) friends.add(key.substring(7));
            else if(key.matches("request\\.[0-9a-f]{16}\\.[0-9a-f]{16}")) requests.add(key.substring(8));
        }
    }
    public synchronized Registration register(String name) throws IOException {
        checkName(name); if(accounts.size()>=1000) throw new IOException("This friends service has reached its account limit.");
        String id; do {id=random(8);} while(accounts.containsKey(id));
        String token=random(32); Account account=new Account(id,name,FriendsApi.hash(token.getBytes(StandardCharsets.UTF_8)));
        saveAccount(account); accounts.put(id,account); return new Registration(account,token);
    }
    public synchronized Account authenticate(String authorization) throws IOException {
        if(authorization==null || !authorization.matches("Bearer [0-9a-f]{16}\\.[0-9a-f]{64}")) throw new SecurityException("Service login required.");
        String id=authorization.substring(7,23),token=authorization.substring(24);
        Account account=accounts.get(id);
        if(account==null || !MessageDigest.isEqual(account.secretHash.getBytes(StandardCharsets.US_ASCII),FriendsApi.hash(token.getBytes(StandardCharsets.UTF_8)).getBytes(StandardCharsets.US_ASCII))) throw new SecurityException("Service account could not be verified.");
        return account;
    }
    public synchronized void profile(Account owner,String name) throws IOException { checkName(name); if(!owner.name.equals(name)){owner.name=name;saveAccount(owner);} }
    public synchronized void upload(Account owner,byte[] data) throws IOException {
        byte[] png=data.length==0?data:SkinImage.decode(data).png;
        Path file=root.resolve("skins/"+owner.id+".png");
        if(png.length==0) Files.deleteIfExists(file); else atomic(file,png);
        owner.skinHash=png.length==0?"":FriendsApi.hash(png);
    }
    public synchronized byte[] skin(Account viewer,String target) throws IOException {
        Account subject=target(target);
        if(!viewer.id.equals(target) && !friends.contains(pair(viewer.id,target))) throw new SecurityException("This player has not accepted you as a friend.");
        if(subject.skinHash.isEmpty()) return new byte[0];
        return Files.readAllBytes(root.resolve("skins/"+target+".png"));
    }
    public synchronized FriendsApi.Snapshot snapshot(Account owner) {
        List<FriendsApi.Person> accepted=new ArrayList<FriendsApi.Person>(), incoming=new ArrayList<FriendsApi.Person>(),outgoing=new ArrayList<FriendsApi.Person>();
        for(Account account:accounts.values()) {
            if(friends.contains(pair(owner.id,account.id))) accepted.add(person(account,true));
            if(requests.contains(account.id+"."+owner.id)) incoming.add(person(account,false));
            if(requests.contains(owner.id+"."+account.id)) outgoing.add(person(account,false));
        }
        Comparator<FriendsApi.Person> sort=Comparator.comparing(p->p.name.toLowerCase(Locale.ROOT));
        accepted.sort(sort);incoming.sort(sort);outgoing.sort(sort);
        return new FriendsApi.Snapshot(accepted,incoming,outgoing);
    }
    public synchronized void action(Account owner,String action,String id) throws IOException {
        Account other=target(id); if(owner.id.equals(id)) throw new IOException("That is your own friend code.");
        String sent=owner.id+"."+id,received=id+"."+owner.id,pair=pair(owner.id,id);
        if(action.equals("request")) {
            if(friends.contains(pair)) return;
            if(requests.contains(received)) throw new IOException("This player already invited you. Choose Accept in Incoming.");
            if(snapshot(owner).outgoing.size()>=FriendsApi.MAX_CONTACTS || snapshot(other).incoming.size()>=FriendsApi.MAX_CONTACTS) throw new IOException("Too many pending requests.");
            requests.add(sent);
        } else if(action.equals("accept")) {
            if(!requests.contains(received)) throw new IOException("No incoming request from this player.");
            if(snapshot(owner).friends.size()>=FriendsApi.MAX_CONTACTS || snapshot(other).friends.size()>=FriendsApi.MAX_CONTACTS) throw new IOException("Friend list is full.");
            requests.remove(received);requests.remove(sent);friends.add(pair);
        } else if(action.equals("decline")) requests.remove(received);
        else if(action.equals("cancel")) requests.remove(sent);
        else if(action.equals("remove")) {friends.remove(pair);requests.remove(sent);requests.remove(received);}
        else throw new IOException("Unknown friend action.");
        saveRelationships();
    }
    private Account target(String id) throws IOException { if(!id.matches("[0-9a-f]{16}") || !accounts.containsKey(id)) throw new IOException("Friend code not found on this service."); return accounts.get(id); }
    private FriendsApi.Person person(Account account,boolean includeHash){return new FriendsApi.Person(account.id,account.name,includeHash?account.skinHash:"");}
    private String pair(String a,String b){return a.compareTo(b)<0?a+"."+b:b+"."+a;}
    private static boolean validName(String name){return name!=null && name.matches("[A-Za-z0-9_]{1,16}");}
    private static void checkName(String name) throws IOException{if(!validName(name))throw new IOException("Use your Minecraft player name (1-16 letters, numbers or underscores).");}
    private String random(int size){byte[] data=new byte[size];random.nextBytes(data);StringBuilder text=new StringBuilder();for(byte b:data)text.append(String.format("%02x",b&255));return text.toString();}
    private void saveAccount(Account account) throws IOException {
        Properties p=new Properties();p.setProperty("id",account.id);p.setProperty("name",account.name);p.setProperty("secretHash",account.secretHash);save(root.resolve("accounts/"+account.id+".properties"),p);
    }
    private void saveRelationships() throws IOException {
        Properties p=new Properties();for(String key:friends)p.setProperty("friend."+key,"true");for(String key:requests)p.setProperty("request."+key,"true");save(root.resolve("relationships.properties"),p);
    }
    private static Properties load(Path path) throws IOException{Properties p=new Properties();if(Files.isRegularFile(path))try(InputStream in=Files.newInputStream(path)){p.load(in);}return p;}
    private static void save(Path path,Properties p) throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();p.store(out,"Offline Skins private service data");atomic(path,out.toByteArray());}
    private static void atomic(Path path,byte[] data) throws IOException {
        Path temp=path.resolveSibling(path.getFileName()+".tmp");Files.write(temp,data);
        try{Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
        catch(AtomicMoveNotSupportedException ex){Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING);}
    }
}
