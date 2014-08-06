package me.PocketIsland.pockethello;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Pockethello extends JavaPlugin{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Pockethello plugin;
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled!");
	}
	
	@Override
	public void onEnable() {	
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new MyPlayerListener(this), this);
	}
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		Player player = (Player) sender;
		if(cmd.getName().equalsIgnoreCase("hello") || cmd.getName().equalsIgnoreCase("hi")){
			int randomInt = 0;
			randomInt = 1 + (int)(Math.random()*5);
			switch (randomInt){
			case 1:
				player.sendMessage(ChatColor.LIGHT_PURPLE + "PM" + ChatColor.GRAY + ": " + ChatColor.BLACK + "[" + ChatColor.GREEN + "O" + ChatColor.AQUA + "W" + ChatColor.RED + "N" + ChatColor.LIGHT_PURPLE + "E" + ChatColor.YELLOW + "R" + ChatColor.BLACK + "]" + ChatColor.DARK_RED + "PocketIsland " + ChatColor.RED + "-" + ChatColor.BOLD + ">" + ChatColor.RESET + ChatColor.WHITE + " You " + ChatColor.DARK_GREEN + ">" + ChatColor.LIGHT_PURPLE + " Hello!");
			break;
			case 2:
				player.sendMessage(ChatColor.LIGHT_PURPLE + "PM" + ChatColor.GRAY + ": " + ChatColor.BLACK + "[" + ChatColor.GREEN + "O" + ChatColor.AQUA + "W" + ChatColor.RED + "N" + ChatColor.LIGHT_PURPLE + "E" + ChatColor.YELLOW + "R" + ChatColor.BLACK + "]" + ChatColor.DARK_RED + "PocketIsland " + ChatColor.RED + "-" + ChatColor.BOLD + ">" + ChatColor.RESET + ChatColor.WHITE + " You " + ChatColor.DARK_GREEN + ">" + ChatColor.LIGHT_PURPLE + " Hi there!");
			break;
			case 3:
				player.sendMessage(ChatColor.LIGHT_PURPLE + "PM" + ChatColor.GRAY + ": " + ChatColor.BLACK + "[" + ChatColor.GREEN + "O" + ChatColor.AQUA + "W" + ChatColor.RED + "N" + ChatColor.LIGHT_PURPLE + "E" + ChatColor.YELLOW + "R" + ChatColor.BLACK + "]" + ChatColor.DARK_RED + "PocketIsland " + ChatColor.RED + "-" + ChatColor.BOLD + ">" + ChatColor.RESET + ChatColor.WHITE + " You " + ChatColor.DARK_GREEN + ">" + ChatColor.LIGHT_PURPLE + " How are you?");
			break;
			case 4:
				player.sendMessage(ChatColor.LIGHT_PURPLE + "PM" + ChatColor.GRAY + ": " + ChatColor.BLACK + "[" + ChatColor.GREEN + "O" + ChatColor.AQUA + "W" + ChatColor.RED + "N" + ChatColor.LIGHT_PURPLE + "E" + ChatColor.YELLOW + "R" + ChatColor.BLACK + "]" + ChatColor.DARK_RED + "PocketIsland " + ChatColor.RED + "-" + ChatColor.BOLD + ">" + ChatColor.RESET + ChatColor.WHITE + " You " + ChatColor.DARK_GREEN + ">" + ChatColor.LIGHT_PURPLE + " What's up!");
			break;
			default:
				player.sendMessage(ChatColor.LIGHT_PURPLE + "PM" + ChatColor.GRAY + ": " + ChatColor.BLACK + "[" + ChatColor.GREEN + "O" + ChatColor.AQUA + "W" + ChatColor.RED + "N" + ChatColor.LIGHT_PURPLE + "E" + ChatColor.YELLOW + "R" + ChatColor.BLACK + "]" + ChatColor.DARK_RED + "PocketIsland " + ChatColor.RED + "-" + ChatColor.BOLD + ">" + ChatColor.RESET + ChatColor.WHITE + " You " + ChatColor.DARK_GREEN + ">" + ChatColor.LIGHT_PURPLE + " Having fun?");
			}
			return true;
		}
		return false;
	}
	
}