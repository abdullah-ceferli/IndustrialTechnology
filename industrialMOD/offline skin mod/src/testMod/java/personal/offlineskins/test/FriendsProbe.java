package personal.offlineskins.test;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.renderer.texture.DynamicTexture;
import personal.offlineskins.OfflineSkins;
import personal.offlineskins.client.*;
import personal.offlineskins.friends.FriendsApi;

public final class FriendsProbe extends Probe {
    private final File root=new File(System.getProperty("skin.test.root"));
    private final boolean a=System.getProperty("skin.test.role").equals("A");
    private int stage,ticks,frames;
    private final long started=System.currentTimeMillis();
    private long reconnectAt;
    @Override public void init(){FMLCommonHandler.instance().bus().register(this);}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;Minecraft mc=Minecraft.getMinecraft();
        try {
            if(System.currentTimeMillis()-started>150000)throw new AssertionError("Friends test timeout at stage "+stage);
            if(stage==90){if(System.currentTimeMillis()>=reconnectAt){mc.displayGuiScreen(new GuiConnecting(new GuiMainMenu(),mc,"127.0.0.1",Integer.getInteger("skin.test.port")));stage=10;}return;}
            if(mc.thePlayer==null || ++ticks%5!=0)return;
            ClientSkins skins=(ClientSkins)OfflineSkins.proxy;
            if(stage==0){if(skins.sharing())throw new AssertionError("Test server unexpectedly has the skin mod");skins.connectFriends(System.getProperty("skin.test.service"));stage=1;}
            FriendsLink.State state=skins.friendsState();
            if(state==null || state.code.isEmpty())return;
            File codeFile=new File(root,(a?"a":"b")+"-code.txt");if(!codeFile.exists())Files.write(codeFile.toPath(),state.code.getBytes(StandardCharsets.UTF_8));
            AbstractClientPlayer other=null;for(Object p:mc.theWorld.playerEntities)if(p instanceof AbstractClientPlayer && ((AbstractClientPlayer)p).getCommandSenderName().equals(a?"SkinTestB":"SkinTestA"))other=(AbstractClientPlayer)p;
            if(other==null)return;
            String otherCode=read((a?"b":"a")+"-code");if(otherCode.isEmpty())return;
            if(a){
                if(stage==1 && plain(other)){skins.friendAction("request",otherCode);mark("a-request");stage=2;}
                if(stage==2 && color(other)==0xff3366cc){mark("a-accepted");stage=3;}
                if(stage==3 && has("b-accepted")){skins.select("alternate.png");mark("a-change");stage=4;}
                if(stage==4 && has("b-remove") && state.contacts.friends.isEmpty() && plain(other)){mark("a-revoked");stage=5;}
                if(stage==5 && has("b-revoked")){skins.friendAction("request",otherCode);stage=6;}
                if(stage==6 && color(other)==0xff3366cc && has("b-reaccepted")){
                    mark("a-reaccepted");mc.theWorld.sendQuittingDisconnectingPacket();mc.loadWorld(null);mc.displayGuiScreen(new GuiMainMenu());reconnectAt=System.currentTimeMillis()+1500;stage=90;return;
                }
                if(stage==10 && color(other)==0xff3366cc && color(mc.thePlayer)==0xff33cc66){mark("a-rejoined");stage=11;}
                if(stage==11 && has("b-rejoined")){mc.displayGuiScreen(new FriendsScreen(null));stage=12;}
            }else{
                if(stage==1 && !state.contacts.incoming.isEmpty() && plain(other)){
                    mark("b-private-before-accept");skins.friendAction("accept",otherCode);stage=2;
                }
                if(stage==2 && color(other)==0xffcc3333){mark("b-accepted");stage=3;}
                if(stage==3 && color(other)==0xff33cc66 && has("a-change")){mark("b-change");skins.friendAction("remove",otherCode);mark("b-remove");stage=4;}
                if(stage==4 && plain(other) && state.contacts.friends.isEmpty()){mark("b-revoked");stage=5;}
                if(stage==5 && !state.contacts.incoming.isEmpty()){skins.friendAction("accept",otherCode);stage=6;}
                if(stage==6 && color(other)==0xff33cc66){mark("b-reaccepted");stage=7;}
                if(stage==7 && has("a-rejoined") && color(other)==0xff33cc66){mark("b-rejoined");stage=8;}
                if(stage==8 && has("a-complete")){mark("b-complete");mc.shutdown();}
            }
        }catch(Throwable ex){fail(ex);}
    }
    @SubscribeEvent public void render(TickEvent.RenderTickEvent event){
        if(!a || stage!=12 || event.phase!=TickEvent.Phase.END || ++frames<10)return;
        Minecraft mc=Minecraft.getMinecraft();try{net.minecraft.util.ScreenShotHelper.saveScreenshot(root,"friends-screen.png",mc.displayWidth,mc.displayHeight,mc.getFramebuffer());mark("a-complete");stage=13;mc.shutdown();}catch(Throwable ex){fail(ex);}
    }
    private boolean plain(AbstractClientPlayer player){return !player.getLocationSkin().getResourceDomain().equals(OfflineSkins.ID);}
    private int color(AbstractClientPlayer player){Object texture=Minecraft.getMinecraft().getTextureManager().getTexture(player.getLocationSkin());return texture instanceof DynamicTexture?((DynamicTexture)texture).getTextureData()[8+8*64]:0;}
    private boolean has(String name){return new File(root,name+".txt").exists();}
    private String read(String name)throws Exception{File file=new File(root,name+".txt");return file.exists()?new String(Files.readAllBytes(file.toPath()),StandardCharsets.UTF_8):"";}
    private void mark(String name)throws Exception{Files.write(new File(root,name+".txt").toPath(),("PASS "+name).getBytes(StandardCharsets.UTF_8));System.out.println("FRIENDS_SKINS_TEST PASS "+name);}
    private void fail(Throwable ex){try{Files.write(new File(root,(a?"a":"b")+"-FAILED.txt").toPath(),ex.toString().getBytes(StandardCharsets.UTF_8));}catch(Exception ignored){}ex.printStackTrace();Minecraft.getMinecraft().shutdown();}
}
