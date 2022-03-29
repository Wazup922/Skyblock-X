package me.wazup.skyblock.commands.user;

import me.wazup.skyblock.Skyblock;
import me.wazup.skyblock.commands.SubCommand;
import me.wazup.skyblock.managers.Customization;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sun.java2d.loops.CustomComponent;

public class LeaveCommand extends SubCommand {

    public LeaveCommand() {
        super("Removes you from skyblock!", null, false, null);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;

        if(!Skyblock.getInstance().players.contains(p.getUniqueId())){
            p.sendMessage(Customization.getInstance().messages.get("Not-In-Skyblock"));
            return true;
        }

        Skyblock.getInstance().leave(p);
        return true;
    }
}
