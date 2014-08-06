package me.PocketIsland.PocketEgg;

import java.util.logging.Logger;


import org.bukkit.entity.Egg;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PocketEgg extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static PocketEgg plugin;
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled!");
	}

	public void onEnable() {	
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
	}
	
	@EventHandler
	public void PlayerEggThrow(PlayerEggThrowEvent event){
		Egg egg = event.getEgg();
		
		event.setHatching(false);
		
		egg.getWorld().createExplosion(egg.getLocation().getX(), egg.getLocation().getY(), egg.getLocation().getZ(), 4F, false, false);
	}
}
