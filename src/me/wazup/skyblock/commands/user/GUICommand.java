package me.wazup.skyblock.commands.user;

import me.wazup.skyblock.PlayerData;
import me.wazup.skyblock.Skyblock;
import me.wazup.skyblock.commands.SubCommand;
import me.wazup.skyblock.managers.Customization;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class GUICommand extends SubCommand {

    private final List<String> options = Arrays.asList( "stats", "skills", "warps" );

    public GUICommand() {
        super("Opens interfaces directly", null, false, "<Stats/Skills/Warps>");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(args.length == 1 || !options.contains(args[1].toLowerCase())) return false;

        Player p = (Player) sender;
        if(!Skyblock.getInstance().players.contains(p.getUniqueId())){
            p.sendMessage(Customization.getInstance().messages.get("Not-In-Skyblock"));
            return true;
        }

        PlayerData data = Skyblock.getInstance().playerData.get(p.getUniqueId());

        String option = args[1].toLowerCase();
        if(option.equals("stats")) p.openInventory(data.getStatisticsMenu(false));
        else if(option.equals("skills")) p.openInventory(data.getSkillsMenu(false));
        else if(option.equals("warps")) Skyblock.getInstance().serverWarps.inventory.open(p);

        return true;
    }
}
