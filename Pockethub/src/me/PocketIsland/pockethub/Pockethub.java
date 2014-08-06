package me.PocketIsland.pockethub;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Pockethub extends JavaPlugin{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Pockethub plugin;

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion()
				+ " has been disabled!");
	}

	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion()
				+ " has been enabled!");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new MyPlayerListener(this), this);
		
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("help")) {
			sender.sendMessage(ChatColor.GREEN + "Welcome to " + ChatColor.AQUA
					+ "The MineJam Server");
			sender.sendMessage(ChatColor.GOLD
					+ "Join an arena by using your compass or the available portals!");
			return true;
		}
		return false;
	}
}
