package me.wazup.skyblock.commands.admin;

import me.wazup.skyblock.PlayerCondition;
import me.wazup.skyblock.commands.SubCommand;
import me.wazup.skyblock.utils.XMaterial;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommand extends SubCommand {

    public TestCommand() {
        super("Command used by Wazup92 for testing purposes", "skyblockx.wazup92", false, null);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        Player p = (Player) sender;

        p.getLocation().getBlock().setType(XMaterial.GRASS.parseMaterial());

        return true;
    }
}
