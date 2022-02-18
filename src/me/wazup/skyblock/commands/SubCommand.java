package me.wazup.skyblock.commands;

import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    public final String description;
    public final String permission;
    final boolean allowConsole;
	final String arguments;
	final String[] argumentsExplaination;
    
	public SubCommand(String description, String permission, boolean allowConsole, String arguments, String... argumentsExplaination) {
        this.description = description;
        this.permission = permission;
        this.allowConsole = allowConsole;
        this.arguments = arguments;
        this.argumentsExplaination = argumentsExplaination;
	}
	
	public abstract boolean execute(CommandSender sender, String[] args);
	
}
