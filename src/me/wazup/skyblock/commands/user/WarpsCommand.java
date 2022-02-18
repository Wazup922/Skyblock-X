package me.wazup.skyblock.commands.user;

import me.wazup.skyblock.Skyblock;
import me.wazup.skyblock.commands.SubCommand;
import me.wazup.skyblock.managers.Customization;
import me.wazup.skyblock.managers.FilesManager;
import me.wazup.skyblock.managers.SoundsManager;
import me.wazup.skyblock.managers.ThemeManager;
import me.wazup.skyblock.utils.Cuboid;
import me.wazup.skyblock.utils.Enums;
import me.wazup.skyblock.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class WarpsCommand extends SubCommand {

    public WarpsCommand() {
        super("Manage warps", null, false, null);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        Player p = (Player) sender;

        if(args.length == 1){
            ChatColor c = Utils.getRandomColor();
            sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "------------" + ChatColor.YELLOW + " Skyblock X " + ChatColor.AQUA + "Warps " + c + "" + ChatColor.STRIKETHROUGH + "------------");
            sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sb Warps Create <Name>" + c + Enums.SPECIAL_CHARACTER.ARROW + ChatColor.GREEN + " Gives the theme selection wand");
            sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sb Warps Delete <Name> " + c + Enums.SPECIAL_CHARACTER.ARROW + ChatColor.GREEN + " Deletes an existing theme");
            sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sb Warps List <Name> " + c + Enums.SPECIAL_CHARACTER.ARROW + ChatColor.GREEN + " Lists the loaded themes");
            sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "------------------------------------------------");
            return true;
        }
        String subCommand = args[1].toLowerCase();

        if(subCommand.equals("create")){

            if(args.length == 2){
                Utils.sendCommandUsage(sender, "Themes Create", "<Theme Name>");
                return true;
            }

            String themeName = args[2];
            if(ThemeManager.getInstance().themes.containsKey(themeName.toLowerCase())){
                p.sendMessage(Customization.getInstance().prefix + "There is already a theme created with that name!");
                return true;
            }

            Location[] selection = Skyblock.getInstance().adminSelection.getOrDefault(p.getUniqueId(), new Location[2]);
            if(selection[0] == null || selection[1] == null){
                p.sendMessage(Customization.getInstance().prefix + "You haven't selected the 2 corners yet!");
                return true;
            }

            Cuboid cuboid = new Cuboid(selection[0], selection[1]);

            boolean created = ThemeManager.getInstance().createTheme(cuboid, themeName, p);

            if(created) p.sendMessage(Customization.getInstance().prefix + "Theme has been created successfully!");
            else p.sendMessage(Customization.getInstance().prefix + "Could not create theme!");

            return true;
        }

        if(subCommand.equals("delete")){
            if(args.length == 2){
                Utils.sendCommandUsage(sender, "Themes Delete", "<Theme Name>");
                return true;
            }

            String themeName = args[2];
            if(!ThemeManager.getInstance().themes.containsKey(themeName.toLowerCase())){
                p.sendMessage(Customization.getInstance().prefix + "Could not find a theme with that name!");
                return true;
            }

            ThemeManager.getInstance().themes.remove(themeName.toLowerCase());
            File mainDirectory = new File(Skyblock.getInstance().getDataFolder(), "themes");
            if(mainDirectory.exists()){
                for(File themeFolder: mainDirectory.listFiles()){
                    if(themeFolder.isDirectory()){
                        if(themeFolder.getName().equalsIgnoreCase(themeName)){
                            FilesManager.deleteFile(themeFolder);
                            break;
                        }
                    }
                }
            }

            p.sendMessage(Customization.getInstance().prefix + "Theme has been deleted successfully!");
            return true;
        }

        if(subCommand.equals("build")){

            if(args.length == 2){
                Utils.sendCommandUsage(sender, "Themes Build", "<Theme Name>");
                return true;
            }

            String themeName = args[2];
            if(!ThemeManager.getInstance().themes.containsKey(themeName.toLowerCase())){
                p.sendMessage(Customization.getInstance().prefix + "Could not find a theme with that name!");
                return true;
            }

            ThemeManager.Theme theme = ThemeManager.getInstance().themes.get(themeName.toLowerCase());
            theme.build(p.getLocation());
            Location spawnpoint = theme.getSpawn(p.getLocation());
            p.teleport(spawnpoint);

            p.sendMessage(Customization.getInstance().prefix + "Theme has been built successfully!");
            return true;
        }

        if(subCommand.equals("list")){
            ChatColor c = Utils.getRandomColor();
            p.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "-----------" + ChatColor.YELLOW + " Skyblock X " + ChatColor.AQUA + "Themes " + c + "" + ChatColor.STRIKETHROUGH + "-----------");
            p.sendMessage(ChatColor.GRAY + "Loaded themes: " + ChatColor.LIGHT_PURPLE + ThemeManager.getInstance().themes.size());
            for(ThemeManager.Theme theme: ThemeManager.getInstance().themes.values()){
                p.sendMessage(ChatColor.AQUA + "- " + ChatColor.LIGHT_PURPLE + theme.name
//                        + ChatColor.DARK_AQUA + " -> " + ChatColor.RED + "Damage: " + ChatColor.AQUA + tower.damage +
                );
            }
            p.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "---------------------------------------");
            return true;
        }

        return true;
    }
}
