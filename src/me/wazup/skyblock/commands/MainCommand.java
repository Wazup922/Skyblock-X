package me.wazup.skyblock.commands;

import me.wazup.skyblock.Skyblock;
import me.wazup.skyblock.commands.admin.AdminCommand;
import me.wazup.skyblock.commands.admin.TestCommand;
import me.wazup.skyblock.commands.admin.ThemesCommand;
import me.wazup.skyblock.commands.user.GUICommand;
import me.wazup.skyblock.commands.user.IslandCommand;
import me.wazup.skyblock.managers.Customization;
import me.wazup.skyblock.managers.SoundsManager;
import me.wazup.skyblock.utils.Enums;
import me.wazup.skyblock.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;

public class MainCommand implements CommandExecutor {

    public static final LinkedHashMap<String, SubCommand> commands = new LinkedHashMap<>();

    public MainCommand() {

        //User commands
        commands.put("island", new IslandCommand());
        commands.put("gui", new GUICommand());

        //Admin commands
        commands.put("themes", new ThemesCommand());

        commands.put("admin", new AdminCommand()); //It is registered last so that it sees all admin commands before it, but it does not see itself
        commands.put("test", new TestCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Skyblock plugin = Skyblock.getInstance();

        if(sender instanceof Player) ((Player) sender).playSound(((Player) sender).getLocation(), SoundsManager.CLICK, 1, 1);

        if(args.length == 0){
            ChatColor c = Utils.getRandomColor();
            sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "-------------" + ChatColor.YELLOW + " Skyblock X " + ChatColor.GRAY + "[" + plugin.getDescription().getVersion() + "] " + c + "" + ChatColor.STRIKETHROUGH + "-------------");
            sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sb " + c + Enums.SPECIAL_CHARACTER.ARROW + ChatColor.GREEN + " Shows a list of commands");
            for(String command: commands.keySet()){
                SubCommand subCommand = commands.get(command);
                if(subCommand.permission != null) break; //We reached admin commands
                String commandName = command.substring(0, 1).toUpperCase() + command.substring(1);
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sb " + commandName + " " + c + Enums.SPECIAL_CHARACTER.ARROW + ChatColor.GREEN + " " + subCommand.description);
            }
            sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sb " + (sender.hasPermission("skyblockx.admin") ? ChatColor.GREEN : ChatColor.RED) + "Admin " + c + Enums.SPECIAL_CHARACTER.ARROW + ChatColor.GREEN + " Shows a list of admin commands");
            sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
            return true;
        }

        String subCommandName = args[0].toLowerCase();

        Customization customization = Customization.getInstance();

        if(!commands.containsKey(subCommandName)) {
            //Look if what is written is a shortcut
            for(String command: commands.keySet()){
                if(command.contains(subCommandName)){
                    subCommandName = command;
                    break;
                }
            }
            //Not a shortcut, unknown command!
            if(!commands.containsKey(subCommandName)) {
                sender.sendMessage(customization.messages.get("Unknown-Command"));
                return true;
            }
        }

        SubCommand subCommand = commands.get(subCommandName);

        if(!subCommand.allowConsole && !(sender instanceof Player)) {
            sender.sendMessage(customization.prefix + "You must be a player to use this command");
            return true;
        }

        if(subCommand.permission != null && !sender.hasPermission(subCommand.permission)){
            sender.sendMessage(customization.messages.get("No-Permission"));
            return false;
        }

        boolean executed = subCommand.execute(sender, args);

        if(!executed) {
            Utils.sendCommandUsage(sender, args[0], subCommand.arguments);
            for(String explaination: subCommand.argumentsExplaination) sender.sendMessage(customization.prefix + ChatColor.AQUA + "- " + ChatColor.GRAY + explaination);
        }

        return true;
    }



}
