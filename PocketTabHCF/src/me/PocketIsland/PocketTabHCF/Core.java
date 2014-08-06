package me.PocketIsland.PocketTabHCF;

import java.util.ArrayList;
import java.util.List;
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

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;

public class Core extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Economy econ = null;

	List<String> defaultPlayers;
	List<String> warrior;
	List<String> hero;
	List<String> veteran;
	List<String> elite;
	List<String> legend;
	List<String> immortal;
	List<String> staff;

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

		defaultPlayers = new ArrayList<String>();
		warrior = new ArrayList<String>();
		hero = new ArrayList<String>();
		veteran = new ArrayList<String>();
		elite = new ArrayList<String>();
		legend = new ArrayList<String>();
		immortal = new ArrayList<String>();
		staff = new ArrayList<String>();

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				UpdateTABAll();
			}
		}, (20), (200));

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

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();

		if(player.hasPermission("pockettab.staff") || player.isOp())
			staff.add(player.getName());
		else if(player.hasPermission("pockettab.immortal"))
			immortal.add(player.getName());
		else if(player.hasPermission("pockettab.legend"))
			legend.add(player.getName());
		else if(player.hasPermission("pockettab.elite"))
			elite.add(player.getName());
		else if(player.hasPermission("pockettab.veteran"))
			veteran.add(player.getName());
		else if(player.hasPermission("pockettab.hero"))
			hero.add(player.getName());
		else if(player.hasPermission("pockettab.warrior"))
			warrior.add(player.getName());
		else
			defaultPlayers.add(player.getName());

		TabAPI.setPriority(this, player, 2);

		UpdateTAB(player);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event){
		Player player = event.getPlayer();

		if(player.hasPermission("pockettab.staff") || player.isOp())
			staff.remove(player.getName());
		else if(player.hasPermission("pockettab.immortal"))
			immortal.remove(player.getName());
		else if(player.hasPermission("pockettab.legend"))
			legend.remove(player.getName());
		else if(player.hasPermission("pockettab.elite"))
			elite.remove(player.getName());
		else if(player.hasPermission("pockettab.veteran"))
			veteran.remove(player.getName());
		else if(player.hasPermission("pockettab.hero"))
			hero.remove(player.getName());
		else if(player.hasPermission("pockettab.warrior"))
			warrior.remove(player.getName());
		else
			defaultPlayers.remove(player.getName());
	}

	private void UpdateTABAll(){
		for(Player player : Bukkit.getOnlinePlayers()){
			UpdateTAB(player);
		}
	}

	private void UpdateTAB(Player player){
		int h;
		int v;
		
	    FPlayer fPlayer = (FPlayer)FPlayers.i.get(player);
	    Faction faction = fPlayer.getFaction();
		    
		TabAPI.setTabString(this, player, 0, 0, ChatColor.DARK_GREEN + "----------" + TabAPI.nextNull());
		TabAPI.setTabString(this, player, 0, 1, ChatColor.GREEN + "----------" + TabAPI.nextNull());
		TabAPI.setTabString(this, player, 0, 2, ChatColor.DARK_GREEN + "----------" + TabAPI.nextNull());

		TabAPI.setTabString(this, player, 1, 0, ChatColor.DARK_RED + "" + ChatColor.BOLD + "HC Factions");
		TabAPI.setTabString(this, player, 1, 1, ChatColor.DARK_RED + "MineJam");
		TabAPI.setTabString(this, player, 1, 2, ChatColor.DARK_RED  + "" + ChatColor.BOLD + "Server");

		TabAPI.setTabString(this, player, 2, 0, ChatColor.GREEN + "----------" + TabAPI.nextNull());
		TabAPI.setTabString(this, player, 2, 1, ChatColor.DARK_GREEN + "----------" + TabAPI.nextNull());
		TabAPI.setTabString(this, player, 2, 2, ChatColor.GREEN + "----------" + TabAPI.nextNull());

		TabAPI.setTabString(this, player, 3, 0, ChatColor.BLUE + "Faction Name");
		TabAPI.setTabString(this, player, 3, 1, ChatColor.BLUE + "Online");
		TabAPI.setTabString(this, player, 3, 2, ChatColor.BLUE + "Balance");

		TabAPI.setTabString(this, player, 4, 0, ChatColor.RED + faction.getTag() + TabAPI.nextNull());
		TabAPI.setTabString(this, player, 4, 1, ChatColor.AQUA + "" + Bukkit.getOnlinePlayers().length + "/" + Bukkit.getServer().getMaxPlayers());
		TabAPI.setTabString(this, player, 4, 2, ChatColor.GREEN + "$" + Math.round(econ.getBalance(player.getName())* 100) / 100);

		h = 0;
		v = 6;

		if(staff.size() > 0){
			TabAPI.setTabString(this, player, v, h++, ChatColor.AQUA + "Staff:");

			for(int x = 0; x < staff.size(); x++){

				if(h > 2){
					h = 0;
					v++;
				}

				TabAPI.setTabString(this, player, v, h++, staff.get(x) + TabAPI.nextNull());
			}
		}
		h = 0;

		if(defaultPlayers.size() > 0){
			TabAPI.setTabString(this, player, v, h++, ChatColor.GRAY + "Players:");

			for(int x = 0; x < defaultPlayers.size(); x++){

				if(h > 2){
					h = 0;
					v++;
				}

				TabAPI.setTabString(this, player, v, h++, defaultPlayers.get(x) + TabAPI.nextNull());
			}
		}
		
		h = 0;

		if(warrior.size() > 0){
			TabAPI.setTabString(this, player, ++v, h++, ChatColor.GREEN + "Warriors:" + TabAPI.nextNull());

			for(int x = 0; x < warrior.size(); x++){

				if(h > 2){
					h = 0;
					v++;
				}

				TabAPI.setTabString(this, player, v, h++, warrior.get(x) + TabAPI.nextNull());
			}
		}
		
		h = 0;

		if(hero.size() > 0){
			TabAPI.setTabString(this, player, ++v, h++, ChatColor.DARK_GREEN + "Heroes:" + TabAPI.nextNull());

			for(int x = 0; x < hero.size(); x++){

				if(h > 2){
					h = 0;
					v++;
				}

				TabAPI.setTabString(this, player, v, h++, hero.get(x) + TabAPI.nextNull());
			}
		}
		
		h = 0;

		if(veteran.size() > 0){
			TabAPI.setTabString(this, player, ++v, h++, ChatColor.YELLOW + "Veterans:" + TabAPI.nextNull());

			for(int x = 0; x < veteran.size(); x++){

				if(h > 2){
					h = 0;
					v++;
				}

				TabAPI.setTabString(this, player, v, h++, veteran.get(x) + TabAPI.nextNull());
			}
		}
		h = 0;

		if(elite.size() > 0){
			TabAPI.setTabString(this, player, ++v, h++, ChatColor.GOLD + "Elites:" + TabAPI.nextNull());

			for(int x = 0; x < elite.size(); x++){

				if(h > 2){
					h = 0;
					v++;
				}

				TabAPI.setTabString(this, player, v, h++, elite.get(x) + TabAPI.nextNull());
			}
		}
		h = 0;

		if(legend.size() > 0){
			TabAPI.setTabString(this, player, ++v, h++, ChatColor.RED + "Legends:" + TabAPI.nextNull());

			for(int x = 0; x < legend.size(); x++){

				if(h > 2){
					h = 0;
					v++;
				}

				TabAPI.setTabString(this, player, v, h++, legend.get(x) + TabAPI.nextNull());
			}
		}
		h = 0;

		if(immortal.size() > 0){
			TabAPI.setTabString(this, player, ++v, h++, ChatColor.DARK_RED + "Immortals:" + TabAPI.nextNull());

			for(int x = 0; x < immortal.size(); x++){

				if(h > 2){
					h = 0;
					v++;
				}

				TabAPI.setTabString(this, player, v, h++, immortal.get(x) + TabAPI.nextNull());
			}
		}
		
		TabAPI.updatePlayer(player);
	}
}
