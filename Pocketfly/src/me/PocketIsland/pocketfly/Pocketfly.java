package me.PocketIsland.pocketfly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagApi.CombatTagApi;

public class Pocketfly extends JavaPlugin{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Pocketfly plugin;
	public final HashMap<Player, ArrayList<Block>> hashmap = new HashMap<Player, ArrayList<Block>>();
	public CombatTagApi combatApi;

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled!");
	}

	public void onEnable() {	
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new MyPlayerListener(this), this);
		if(getServer().getPluginManager().getPlugin("CombatTag") != null){
			combatApi = new CombatTagApi((CombatTag)getServer().getPluginManager().getPlugin("CombatTag")); 
		}
	}


	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().equalsIgnoreCase("fly")){
			Player player = (Player) sender;
			if(player.hasPermission("pocketfly.fly")){
				if (hashmap.containsKey(player)){
					hashmap.remove(player);
					player.setAllowFlight(false);
					player.setCanPickupItems(true);
					player.sendMessage(ChatColor.RED + "You are no longer flying!");
				}else{

					//Location location = player.getLocation();
					//Faction faction = BoardColls.get().getFactionAt(PS.valueOf(location));

					//if (faction.getId() == "-2"){
					//	player.sendMessage(ChatColor.RED + "You may not start flying in Warzone!");
					//}else{
					//if (combatApi.isInCombat(player) == false){
						hashmap.put(player, null);
						player.setAllowFlight(true);
						player.setCanPickupItems(false);
						player.sendMessage(ChatColor.GREEN + "You can now fly!");
					//}else if (combatApi.isInCombat(player) == true){
					//	player.sendMessage(ChatColor.RED + "You are in combat! You may not fly for " + ChatColor.BLUE + ((combatApi.getRemainingTagTime(player) / 1000) + 1) + ChatColor.RED + " seconds!");						
					//}
					//}
				}
			}else{
				player.sendMessage(ChatColor.RED + "You must purchase " + ChatColor.DARK_RED + "Legend" + ChatColor.RED + " to use that! Visit " + ChatColor.GREEN + "www.store.ipocketisland.com" + ChatColor.RED + " to purchase!");
			}
		}
		return false;
	}
}
