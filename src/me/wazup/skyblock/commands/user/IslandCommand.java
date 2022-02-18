package me.wazup.skyblock.commands.user;

import me.wazup.skyblock.Skyblock;
import me.wazup.skyblock.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IslandCommand extends SubCommand {

    public IslandCommand() {
        super("Teleports you to your island!", null, false, null);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Skyblock.getInstance().join((Player) sender);
        return true;
    }
}
