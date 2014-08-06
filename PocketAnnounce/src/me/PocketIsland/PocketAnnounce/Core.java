package me.PocketIsland.PocketAnnounce;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Core plugin;
	
	private List<String> readConfig;
	private String[] announcements;
	private int index;
	private int interval;
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled!");
	}

	public void onEnable() {	
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");
		readConfig = getConfig().getStringList("announcements");
		announcements = readConfig.toArray(new String[readConfig.size()]);
		interval = getConfig().getInt("interval");
		
		if(!(new File(getDataFolder(), "config.yml")).exists())
			saveDefaultConfig();
		
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				announce();
			}
			}, (20 * interval), (20 * interval));
	}
	
	private void announce()
	{
		if(index >= announcements.length)
			index = 0;
			
		Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "" + ChatColor.STRIKETHROUGH + "------------------" + ChatColor.RED + "[MineJam]" + ChatColor.DARK_RED + "" + ChatColor.STRIKETHROUGH + "------------------");
		Bukkit.getServer().broadcastMessage(ChatColor.GREEN + announcements[index++]);
		Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "" + ChatColor.STRIKETHROUGH + "------------------" + ChatColor.RED + "[MineJam]" + ChatColor.DARK_RED + "" + ChatColor.STRIKETHROUGH + "------------------");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().equalsIgnoreCase("announce") && sender.isOp()){
			announce();
			return true;
		}
		return false;
	}
}
