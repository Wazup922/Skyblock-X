package me.wazup.skyblock.commands.admin;

import me.wazup.skyblock.commands.MainCommand;
import me.wazup.skyblock.commands.SubCommand;
import me.wazup.skyblock.managers.Customization;
import me.wazup.skyblock.managers.SoundsManager;
import me.wazup.skyblock.utils.Enums;
import me.wazup.skyblock.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class AdminCommand extends SubCommand {

    private final LinkedHashMap<String, String> adminCommands;

    public AdminCommand() {
        super("Shows a list of admin commands", "skyblockx.admin", true, null);

        adminCommands = new LinkedHashMap<>();

        for(String command: MainCommand.commands.keySet()){
            SubCommand subCommand = MainCommand.commands.get(command);
            if(subCommand.permission != null){
                adminCommands.put(command, subCommand.description);
            }
        }

    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        ChatColor c = Utils.getRandomColor();

        sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "-------" + ChatColor.YELLOW + " Skyblock X " + ChatColor.RED + "Admin" + ChatColor.GRAY + " " + c + "" + ChatColor.STRIKETHROUGH + "-------");
        for(String command: adminCommands.keySet()){
            String commandName = command.substring(0, 1).toUpperCase() + command.substring(1);
            sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sb " + commandName + " " + c + Enums.SPECIAL_CHARACTER.ARROW + ChatColor.GREEN + " " + adminCommands.get(command));
        }
        sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "-----------------------------");
        return true;
    }
}
