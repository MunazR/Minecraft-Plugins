package me.PocketIsland.DisguisePvP;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;

public class Core extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	DisguiseCraftAPI dcAPI;

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
		pm.registerEvents(this, this);
		setupDisguiseCraft();
	}

	public void setupDisguiseCraft() {
		dcAPI = DisguiseCraft.getAPI();
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event){
		if(event.getEntity() instanceof Player){
			Player player = (Player)event.getEntity();

			if(!player.hasPermission("disguise.pvp") && !player.isOp()){
				if(dcAPI.getDisguise(player) != null){
					dcAPI.undisguisePlayer(player);
					player.sendMessage(ChatColor.RED + "Your disguise has been blown!");
				}
			}
		}
	}


}
