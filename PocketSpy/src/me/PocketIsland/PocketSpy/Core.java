package me.PocketIsland.PocketSpy;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Core plugin;
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled!");
	}

	public void onEnable() {	
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");
		
		if(!(new File(getDataFolder(), "config.yml")).exists())
			saveDefaultConfig();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().equalsIgnoreCase("manuadd") || cmd.getName().equalsIgnoreCase("manuaddsub")){
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			String command = "";
			
			for(int x = 0; x < args.length; x++){
				command += args[x] + " ";
			}
			
			getConfig().set(dateFormat.format(date), sender.getName() + " used command " + command);
			saveConfig();
			
			logger.info(dateFormat.format(date) + ": " + sender.getName() + " used command " + command);
			
			return true;
		}
		return false;
	}
}
