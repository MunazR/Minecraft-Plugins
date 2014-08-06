package me.PocketIsland.pocketbedrock;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Pocketbedrock extends JavaPlugin{
		public final Logger logger = Logger.getLogger("Minecraft");
		public static Pocketbedrock plugin;
		
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
}
