package me.wazup.skyblock.commands.admin;

import me.wazup.skyblock.PlayerCondition;
import me.wazup.skyblock.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommand extends SubCommand {

    public TestCommand() {
        super("Command used by Wazup92 for testing purposes", "skyblockx.wazup92", false, null);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        Player p = (Player) sender;

        if(args.length == 1) {

            PlayerCondition pc = new PlayerCondition();
            pc.extractCondition(p);
            pc.saveConditionToFile(p);

            p.sendMessage("Saved");
        } else {
            PlayerCondition pc = new PlayerCondition();
            pc.loadConditionFromFile(p);
            pc.applyCondition(p);

            p.sendMessage("Loaded");

        }

        return true;
    }
}
