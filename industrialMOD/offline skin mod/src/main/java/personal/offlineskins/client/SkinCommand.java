package personal.offlineskins.client;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public final class SkinCommand extends CommandBase {
    private final ClientSkins skins;
    public SkinCommand(ClientSkins skins) { this.skins = skins; }
    @Override public String getCommandName() { return "offlineskin"; }
    @Override public String getCommandUsage(ICommandSender sender) { return "/offlineskin [reload|folder|clear|use filename.png|friends|connect address|request code|accept code|decline code|remove code|disconnect|server]"; }
    @Override public int getRequiredPermissionLevel() { return 0; }
    @Override public void processCommand(ICommandSender sender, String[] args) {
        try {
            if (args.length == 0) Minecraft.getMinecraft().displayGuiScreen(new SkinScreen(null));
            else if(args[0].equalsIgnoreCase("friends"))Minecraft.getMinecraft().displayGuiScreen(new FriendsScreen(null));
            else if(args[0].equalsIgnoreCase("connect") && args.length==2)skins.connectFriends(args[1]);
            else if(java.util.Arrays.asList("request","accept","decline","remove","cancel").contains(args[0].toLowerCase(java.util.Locale.ROOT)) && args.length==2)skins.friendAction(args[0].toLowerCase(java.util.Locale.ROOT),args[1]);
            else if(args[0].equalsIgnoreCase("disconnect"))skins.disconnectFriends();
            else if(args[0].equalsIgnoreCase("server"))skins.useServerSharing();
            else if (args[0].equalsIgnoreCase("reload")) skins.reload(true);
            else if (args[0].equalsIgnoreCase("folder")) skins.openFolder();
            else if (args[0].equalsIgnoreCase("clear")) skins.select("");
            else if (args[0].equalsIgnoreCase("use") && args.length >= 2) {
                skins.select(String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)));
            } else skins.chat(getCommandUsage(sender));
        } catch (IOException ex) { skins.chat(ex.getMessage()); }
    }
    @Override public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "reload", "folder", "clear", "use", "friends", "connect", "request", "accept", "decline", "remove", "disconnect", "server");
        if (args.length == 2 && args[0].equalsIgnoreCase("use")) return getListOfStringsFromIterableMatchingLastWord(args, skins.files().list());
        return null;
    }
}
