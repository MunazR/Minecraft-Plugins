package me.PocketIsland.PocketBlocks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Core plugin;
	
	List<Material> allowedPlace;
	List<Material> allowedBreak;
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled!");
	}

	@SuppressWarnings("deprecation")
	public void onEnable() {	
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		
		if(!(new File(getDataFolder(), "config.yml")).exists())
			saveDefaultConfig();
		
		List<String> listBlocks = getConfig().getStringList("whitelisted-blocks-place");
		
		allowedPlace = new ArrayList<Material>();
		
		for(String s : listBlocks){
			allowedPlace.add(Material.getMaterial(Integer.parseInt(s)));
		}
		
		listBlocks = getConfig().getStringList("whitelisted-blocks-break");
		
		allowedBreak = new ArrayList<Material>();
		
		for(String s : listBlocks){
			allowedBreak.add(Material.getMaterial(Integer.parseInt(s)));
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event){
		if(event.getPlayer().isOp() || event.isCancelled())
			return;
		
		if(!allowedBreak.contains(event.getBlock().getType())){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to break that block!");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event){
		if(event.getPlayer().isOp() || event.isCancelled())
			return;
		
		if(!allowedPlace.contains(event.getBlock().getType())){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to place that block!");
		}
	}
}
