package me.PocketIsland.PocketTABKitPvP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcsg.double0negative.tabapi.TabAPI;

public class Core extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Economy econ = null;

	List<String> hero;
	List<String> legend;
	List<String> immortal;
	List<String> staff;
	Map<String, Double> balance;

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

		if (!setupEconomy() ) {
			logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		hero = new ArrayList<String>();
		legend = new ArrayList<String>();
		immortal = new ArrayList<String>();
		staff = new ArrayList<String>();

		balance = new HashMap<String, Double>();

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				checkPlayerBalance();
			}
		}, (20), (600));

	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	private void checkPlayerBalance(){
		for(Player player : Bukkit.getOnlinePlayers()){
			if(econ.getBalance(player.getName()) != balance.get(player.getName())){
				UpdatePlayerBalance(player);
				balance.put(player.getName(), econ.getBalance(player.getName()));
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();

		TabAPI.setPriority(this, player, 2);

		TabAPI.setTabString(this, player, 0, 0, ChatColor.DARK_GREEN + "----------" + TabAPI.nextNull());
		TabAPI.setTabString(this, player, 0, 1, ChatColor.GREEN + "----------" + TabAPI.nextNull());
		TabAPI.setTabString(this, player, 0, 2, ChatColor.DARK_GREEN + "----------" + TabAPI.nextNull());

		TabAPI.setTabString(this, player, 1, 0, ChatColor.DARK_RED + "" + ChatColor.BOLD + "KitPvP");
		TabAPI.setTabString(this, player, 1, 1, ChatColor.DARK_RED + "MineJam");
		TabAPI.setTabString(this, player, 1, 2, ChatColor.DARK_RED  + "" + ChatColor.BOLD + "Server");

		TabAPI.setTabString(this, player, 2, 0, ChatColor.GREEN + "----------" + TabAPI.nextNull());
		TabAPI.setTabString(this, player, 2, 1, ChatColor.DARK_GREEN + "----------" + TabAPI.nextNull());
		TabAPI.setTabString(this, player, 2, 2, ChatColor.GREEN + "----------" + TabAPI.nextNull());

		TabAPI.setTabString(this, player, 3, 0, ChatColor.BLUE + "Server Name");
		TabAPI.setTabString(this, player, 3, 1, ChatColor.BLUE + "Online");
		TabAPI.setTabString(this, player, 3, 2, ChatColor.BLUE + "Balance");

		TabAPI.setTabString(this, player, 4, 0, ChatColor.RED + "KITPVP");
		TabAPI.setTabString(this, player, 4, 1, ChatColor.AQUA + "" + Bukkit.getOnlinePlayers().length + "/" + Bukkit.getServer().getMaxPlayers());
		TabAPI.setTabString(this, player, 4, 2, ChatColor.GREEN + "$" + Math.round(econ.getBalance(player.getName())* 10) / 10);

		TabAPI.updatePlayer(player);

		if(player.hasPermission("pockettab.staff") || player.isOp()){
			staff.add(player.getName());
			UpdateTABAll();
		}else if(player.hasPermission("pockettab.immortal")){
			immortal.add(player.getName());
			UpdateTABAll();
		}else if(player.hasPermission("pockettab.legend")){
			legend.add(player.getName());
			UpdateTABAll();
		}else if(player.hasPermission("pockettab.hero")){
			hero.add(player.getName());
			UpdateTABAll();
		}

		UpdateTabPlayerList(player);
		TabAPI.updatePlayer(player);
		UpdatePlayerCount();
		balance.put(player.getName(), econ.getBalance(player.getName()));
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event){
		Player player = event.getPlayer();

		if(player.hasPermission("pockettab.staff") || player.isOp()){
			staff.remove(player.getName());
			UpdateTABAll();
		}else if(player.hasPermission("pockettab.immortal")){
			immortal.remove(player.getName());
			UpdateTABAll();
		}else if(player.hasPermission("pockettab.legend")){
			legend.remove(player.getName());
			UpdateTABAll();
		}else if(player.hasPermission("pockettab.hero")){
			hero.remove(player.getName());
			UpdateTABAll();
		}
		
		UpdatePlayerCount();
		balance.remove(player.getName());
	}

	private void UpdateTABAll(){
		for(Player player : Bukkit.getOnlinePlayers()){
			UpdateTabPlayerList(player);
		}

		TabAPI.updateAll();
	}

	private void UpdatePlayerBalance(Player player){
		TabAPI.setTabString(this, player, 4, 2, ChatColor.GREEN + "$" + Math.round(econ.getBalance(player.getName())* 10) / 10);

		TabAPI.updatePlayer(player);
	}

	private void UpdatePlayerCount(){
		for(Player player : Bukkit.getOnlinePlayers()){
			TabAPI.setTabString(this, player, 4, 1, ChatColor.AQUA + "" + Bukkit.getOnlinePlayers().length + "/" + Bukkit.getServer().getMaxPlayers());
		}

		TabAPI.updateAll();
	}

	private void UpdateTabPlayerList(Player player){
		int h = 0;
		int v = 6;

		if(staff.size() > 0){
			TabAPI.setTabString(this, player, v, h++, ChatColor.DARK_GREEN + "Staff:");
			for(int x = 0; x < staff.size(); x++){
				if(h > 2){
					h = 0;
					v++;
				}

				TabAPI.setTabString(this, player, v, h++, ChatColor.GREEN + staff.get(x) + TabAPI.nextNull());
			}
		}

		if(hero.size() > 0){
			h = 0;
			v++;
			TabAPI.setTabString(this, player, ++v, h++, ChatColor.DARK_BLUE + "Heroes:" + TabAPI.nextNull());
			for(int x = 0; x < hero.size(); x++){
				if(h > 2){
					h = 0;
					v++;
				}

				TabAPI.setTabString(this, player, v, h++, ChatColor.BLUE + hero.get(x) + TabAPI.nextNull());
			}
		}

		if(legend.size() > 0){
			h = 0;
			v++;
			TabAPI.setTabString(this, player, ++v, h++, ChatColor.DARK_AQUA + "Legends:" + TabAPI.nextNull());
			for(int x = 0; x < legend.size(); x++){
				if(h > 2){
					h = 0;
					v++;
				}

				TabAPI.setTabString(this, player, v, h++, ChatColor.AQUA + legend.get(x) + TabAPI.nextNull());
			}
		}
		
		if(immortal.size() > 0){
			h = 0;
			v++;
			TabAPI.setTabString(this, player, ++v, h++, ChatColor.DARK_RED + "Immortals:" + TabAPI.nextNull());
			for(int x = 0; x < immortal.size(); x++){
				if(h > 2){
					h = 0;
					v++;
				}

				TabAPI.setTabString(this, player, v, h++, ChatColor.RED + immortal.get(x) + TabAPI.nextNull());
			}
		}
	}
}
