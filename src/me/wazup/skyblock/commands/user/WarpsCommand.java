package me.wazup.skyblock.commands.user;

import me.wazup.skyblock.Skyblock;
import me.wazup.skyblock.commands.SubCommand;
import me.wazup.skyblock.managers.Customization;
import me.wazup.skyblock.utils.Enums;
import me.wazup.skyblock.utils.Utils;
import me.wazup.skyblock.utils.WarpContainer;
import me.wazup.skyblock.utils.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WarpsCommand extends SubCommand {

    public WarpsCommand() {
        super("Manage warps", "skyblockx.admin", false, null);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        Player p = (Player) sender;

        if(args.length == 1){
            ChatColor c = Utils.getRandomColor();
            sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "------------" + ChatColor.YELLOW + " Skyblock X " + ChatColor.AQUA + "Warps " + c + "" + ChatColor.STRIKETHROUGH + "------------");
            sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sb Warps Set <Name>" + c + Enums.SPECIAL_CHARACTER.ARROW + ChatColor.GREEN + " Sets a server warp location");
            sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sb Warps Delete <Name> " + c + Enums.SPECIAL_CHARACTER.ARROW + ChatColor.GREEN + " Deletes an existing server warp");
            sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sb Warps List " + c + Enums.SPECIAL_CHARACTER.ARROW + ChatColor.GREEN + " Shows a list of server warps");
            sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------");
            return true;
        }
        String subCommand = args[1].toLowerCase();

        if(subCommand.equals("set")){

            if(args.length < 4){
                Utils.sendCommandUsage(sender, "Warps Set", "<Name> <Material> [Optional: Description]");
                return true;
            }

            WarpContainer serverWarps = Skyblock.getInstance().serverWarps;

            String warpName = args[2];
            if(serverWarps.warps.containsKey(warpName.toLowerCase())){
                p.sendMessage(Customization.getInstance().prefix + "There is already a warp created with that name!");
                return true;
            }

            Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(args[3]);
            if(!xMaterial.isPresent()){
                p.sendMessage(Customization.getInstance().prefix + "Could not find a material with that name!");
                return true;
            }

            List<String> descriptionList;
            if(args.length > 4){
                StringBuilder description = new StringBuilder(ChatColor.GRAY.toString());
                for(int i = 4; i < args.length; i++) description.append(args[i]).append(" ");
                descriptionList = Collections.singletonList(description.substring(0, description.length() - 1));
            } else descriptionList = Collections.emptyList();

            File file = new File(Skyblock.getInstance().getDataFolder(), "warps.yml");
            FileConfiguration editor = YamlConfiguration.loadConfiguration(file);

            String path = "Warps." + warpName.toLowerCase() + ".";
            editor.set(path + "Location", Utils.getStringFromLocation(p.getLocation(), true));
            editor.set(path + "Displayed-Name", "&e" + warpName);
            editor.set(path + "Displayed-Item", xMaterial.get().name());
            editor.set(path + "Displayed-Lore", descriptionList);
            try {
                editor.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            serverWarps.addWarp(warpName.toLowerCase(), ChatColor.YELLOW + warpName, p.getLocation(), xMaterial.get().parseItem(), descriptionList);
            p.sendMessage(Customization.getInstance().prefix + "Warp location has been set successfully!");
            return true;
        }

        if(subCommand.equals("delete")){
            if(args.length == 2){
                Utils.sendCommandUsage(sender, "Warps Delete", "<Warp Name>");
                return true;
            }

            String warpName = args[2].toLowerCase();
            if(!Skyblock.getInstance().serverWarps.warps.containsKey(warpName)){
                p.sendMessage(Customization.getInstance().prefix + "Could not find a warp with that name!");
                return true;
            }

            Skyblock.getInstance().serverWarps.removeWarp(warpName);

            File file = new File(Skyblock.getInstance().getDataFolder(), "warps.yml");
            FileConfiguration editor = YamlConfiguration.loadConfiguration(file);
            editor.set("Warps." + warpName.toLowerCase(), null);
            try {
                editor.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            p.sendMessage(Customization.getInstance().prefix + "Warp has been deleted successfully!");
            return true;
        }

        if(subCommand.equals("list")){
            ChatColor c = Utils.getRandomColor();
            p.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "-----------" + ChatColor.YELLOW + " Skyblock X " + ChatColor.AQUA + "Warps " + c + "" + ChatColor.STRIKETHROUGH + "-----------");
            p.sendMessage(ChatColor.GRAY + "Loaded warps: " + ChatColor.LIGHT_PURPLE + Skyblock.getInstance().serverWarps.warps.size());
            for(String name: Skyblock.getInstance().serverWarps.warps.keySet()){
                p.sendMessage(ChatColor.AQUA + "- " + ChatColor.LIGHT_PURPLE + name
                );
            }
            p.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "---------------------------------------");
            return true;
        }

        return true;
    }
}
