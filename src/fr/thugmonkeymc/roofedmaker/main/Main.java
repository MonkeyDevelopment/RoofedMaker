package fr.thugmonkeymc.roofedmaker.main;

import org.bukkit.plugin.java.JavaPlugin;

import fr.thugmonkeymc.roofedmaker.commands.StartCommand;

public class Main extends JavaPlugin {
	
	@Override
	public void onEnable() {
		this.getCommand("forestgen").setExecutor(new StartCommand(this));
		this.saveDefaultConfig();
	}
	
}
