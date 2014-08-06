package me.PocketIsland.PocketRankUp;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class Core extends JavaPlugin implements Listener{
	final Logger logger = Logger.getLogger("Minecraft");
	static Economy econ = null;
	static Chat chat = null;
	Connection conn;

	HashMap<String, Integer> killstreak;
	HashMap<String, Integer> kills;
	HashMap<String, Integer> deaths;
	HashMap<String, Integer> level;
	HashMap<String, Integer> killsNeeded;
	HashMap<String, Double> balance;

	List<String> spam;
	List<String> muted;
	List<String> blacklist;

	String tag;
	String chatTag;

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled!");

		for(Player player: Bukkit.getOnlinePlayers()){
			savePlayerData(player);
		}

		try {
			conn.close();
			print(Level.INFO, "Connection closed!");
		} catch (SQLException e) {
			print(Level.SEVERE, "Connection failed to close!");
		}
	}

	public void onEnable() {	
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);

		if (!(new File(getDataFolder(), "config.yml")).exists())
			saveDefaultConfig();

		if (!setupEconomy()) {
			logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if(!setupSQL()){
			logger.severe(String.format("[%s] - Disabled due to MySQL setup failure!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		if(!setupChat()){
			logger.severe(String.format("[%s] - Chat failed to setup!", getDescription().getName()));
		}

		killstreak = new HashMap<String, Integer>();
		kills = new HashMap<String, Integer>();
		deaths = new HashMap<String, Integer>();
		level = new HashMap<String, Integer>();
		killsNeeded = new HashMap<String, Integer>();
		balance = new HashMap<String, Double>();
		spam = new ArrayList<String>();
		muted = new ArrayList<String>();

		tag = getConfig().getString("Tag").replaceAll("&", "§");
		chatTag = getConfig().getString("ChatTag").replaceAll("&", "§");
		
		blacklist = getConfig().getStringList("Blacklist");

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				checkPlayerBalance();
			}
		}, (20), (600));

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				saveAllPlayerData();
			}
		}, (12000), (12000));
	}
	
	private boolean setupChat(){
		RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
	    chat = (Chat)rsp.getProvider();
	    return chat != null;
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

	private boolean setupSQL(){
		try {
			Class.forName("com.mysql.jdbc.Driver");

			conn = DriverManager.getConnection("jdbc:mysql://" + getConfig().getString("MySQL.host") + ":" + getConfig().getString("MySQL.port") + "/" + getConfig().getString("MySQL.database"), getConfig().getString("MySQL.user"), getConfig().getString("MySQL.password"));

			if (!conn.isClosed()){
				print(Level.INFO, "Connected!");

				PreparedStatement stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " +
						"KITPVP_stats (player VARCHAR(255), kills INT, deaths INT, level INT);");

				stmt.execute();
				return true;
			}
			else{
				print(Level.SEVERE, "Connection isn't open!");
			}

		} catch (ClassNotFoundException e) {
			print(Level.SEVERE, "Where is your MySQL driver???");
		} catch (SQLException e) {
			print(Level.SEVERE, "An SQL error occurred! " + e.getMessage());
		}

		return false;
	}

	private void print(Level level, String msg){
		logger.log(level, String.format("[%s]" + msg, getDescription().getName()));
	}

	private boolean tryParse(String x){
		try{
			Integer.parseInt(x);
			return true;
		}catch (NumberFormatException exc){
			return false;
		}
	}

	private boolean containsSwearWords(String msg){
		for(String s : blacklist){
			if(msg.toLowerCase().contains(s)){
				return true;
			}
		}
		
		return false;
	}
	
	public String getPrefix(Player player) {
	    return chat.getPlayerPrefix(player).replaceAll("&", "§");
	  }
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("mute")){
			if(sender.hasPermission("chat.mod")){
				if(args.length != 2){
					sender.sendMessage(chatTag + ChatColor.RED + " Correct usage: " + ChatColor.BLUE + "/mute <Player> <Time in Seconds>");
				}else{
					final Player player = Bukkit.getPlayer(args[0]);

					if(player.isOnline()){
						if(player.hasPermission("chat.mod") || player.hasPermission("chat.admin")){
							sender.sendMessage(chatTag + ChatColor.RED + " You may not mute that player!");
						}else{
							if(tryParse(args[1])){
								int time = Integer.parseInt(args[1]);

								if(time > 900){
									sender.sendMessage(chatTag + ChatColor.RED + " Maximum mute time is 900 seconds!");
								}else{
									sender.sendMessage(chatTag + ChatColor.GREEN + " Mute successfully placed!");
									player.sendMessage(chatTag + ChatColor.RED + " You have been muted for " + ChatColor.DARK_RED + time + ChatColor.RED + " seconds by " + sender.getName());
									Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
										public void run() {
											muted.remove(player.getName());
										}
									}, 20 * time);
								}
							}else{
								sender.sendMessage(chatTag + ChatColor.RED + " Please enter a valid time!");
							}
						}
					}else{
						sender.sendMessage(chatTag + ChatColor.RED + " That player is not online!");
					}
				}
			}else{
				sender.sendMessage(chatTag + ChatColor.RED + " You do not have permission to do that!");
			}

			return true;
		}
		return false;
	}

	private void checkPlayerBalance(){
		for(Player player : Bukkit.getOnlinePlayers()){
			if(econ.getBalance(player.getName()) != balance.get(player.getName())){
				balance.put(player.getName(), econ.getBalance(player.getName()));
				refreshScoreboard(player);
			}
		}
	}

	private void saveAllPlayerData(){
		for(Player player : Bukkit.getOnlinePlayers()){
			savePlayerData(player);
		}
	}

	private void setupScoreboard(Player player){
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		Objective stats = scoreboard.registerNewObjective("stats", "dummy");

		stats.setDisplayName(ChatColor.RED + "Kit PvP");
		stats.setDisplaySlot(DisplaySlot.SIDEBAR);

		player.setScoreboard(scoreboard);
		refreshScoreboard(player);
	}

	private void refreshScoreboard(Player player){
		Scoreboard scoreboard = player.getScoreboard();
		Objective stats = scoreboard.getObjective("stats");

		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Coins")).setScore((int) Math.round(econ.getBalance(player.getName())));
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.YELLOW + "Level:")).setScore(level.get(player.getName()));
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.AQUA + "Kills:")).setScore(kills.get(player.getName()));
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_RED + "Deaths:")).setScore(deaths.get(player.getName()));
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "KDR:")).setScore(Math.round(kills.get(player.getName()) / deaths.get(player.getName()) * 100) / 100);
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.BLUE + "Kills Needed:")).setScore(killsNeeded.get(player.getName()));
	}

	private void levelUp(Player player){
		level.put(player.getName(), level.get(player.getName()) + 1);
		int killsToLevel = (int) (Math.pow(level.get(player.getName()), 2) - kills.get(player.getName()));
		killsNeeded.put(player.getName(), killsToLevel);
		player.sendMessage(tag + ChatColor.BLUE + "Congratulations! You've ranked up! Your new level is " + ChatColor.GREEN + "" + ChatColor.BOLD + level.get(player.getName()));
		Bukkit.broadcastMessage(tag + ChatColor.GREEN + "" + ChatColor.BOLD + player.getName() + ChatColor.BLUE + " has leveled up to " + ChatColor.GREEN + "" + ChatColor.BOLD + level.get(player.getName()));
	}

	private void savePlayerData(Player player){
		try{
			PreparedStatement check = conn.prepareStatement("SELECT * FROM KITPVP_stats WHERE player = ?;");
			check.setString(1, player.getName());
			ResultSet set = check.executeQuery();

			if (set.next()){
				PreparedStatement update = conn.prepareStatement("UPDATE KITPVP_stats" +
						" SET kills = ?, deaths = ?, level = ? WHERE player = ?;");

				update.setString(4, player.getName());
				update.setInt(3, level.get(player.getName()));
				update.setInt(2, deaths.get(player.getName()));
				update.setInt(1, kills.get(player.getName()));
				update.execute();
			}

			set.close();
		}catch(SQLException e){
			print(Level.SEVERE, "MySQL error occured when updating stats for player: " + player.getName());
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();

		try{
			PreparedStatement check = conn.prepareStatement("SELECT * FROM KITPVP_stats WHERE player = ?;");
			check.setString(1, player.getName());
			ResultSet set = check.executeQuery();

			if (set.next()){
				kills.put(player.getName(), set.getInt("kills"));
				deaths.put(player.getName(), set.getInt("deaths"));
				level.put(player.getName(), set.getInt("level"));
				set.close();
			}else{
				PreparedStatement insert;
				insert = conn.prepareStatement("INSERT INTO KITPVP_stats (player, kills, deaths, level)" +
						" VALUES (?, ?, ?, ?);");
				insert.setString(1, player.getName());
				insert.setInt(2, 0);
				insert.setInt(3, 0);
				insert.setInt(4, 0);
				insert.execute();

				kills.put(player.getName(), 0);
				deaths.put(player.getName(), 0);
				level.put(player.getName(), 1);
			}
		}
		catch(SQLException e){
			print(Level.SEVERE, "MySQL Error occured with player: " + player.getName());
		}

		killstreak.put(player.getName(), 0);
		balance.put(player.getName(), econ.getBalance(player.getName()));

		int killsToLevel = (int) (Math.pow(level.get(player.getName()), 2) - kills.get(player.getName()));
		killsNeeded.put(player.getName(), killsToLevel);

		setupScoreboard(player);
	}

	@EventHandler
	public void onPlayerDeath(EntityDeathEvent event){
		if(event.getEntity() instanceof Player){
			Player player = (Player)event.getEntity();
			deaths.put(player.getName(), deaths.get(player.getName()) + 1);
			killstreak.put(player.getName(), 0);

			if(player.getKiller() instanceof Player){
				Player killer = (Player)player.getKiller();
				kills.put(killer.getName(), kills.get(killer.getName()) + 1);
				killstreak.put(killer.getName(), killstreak.get(killer.getName()) + 1);
				killsNeeded.put(killer.getName(), killsNeeded.get(killer.getName()) - 1);

				if(killsNeeded.get(killer.getName()) <= 0){
					levelUp(killer);
				}

				refreshScoreboard(killer);
			}

			refreshScoreboard(player);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();
		savePlayerData(player);

		kills.remove(player.getName());
		deaths.remove(player.getName());
		level.remove(player.getName());
		killsNeeded.remove(player.getName());
		killstreak.remove(player.getName());
		balance.remove(player.getName());
	}

	@EventHandler
	public void onPlayerDeathChat(PlayerDeathEvent event){
		event.setDeathMessage(ChatColor.GRAY + event.getDeathMessage());
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event){
		final Player player = event.getPlayer();

		if(event.isCancelled())
			return;

		if(spam.contains(player.getName())){
			player.sendMessage(chatTag + ChatColor.RED + " Please do not spam the chat!");
			event.setCancelled(true);
			return;
		}

		if(muted.contains(player.getName())){
			player.sendMessage(chatTag + ChatColor.RED + " You may not speak in chat while muted!");
			event.setCancelled(true);
			return;
		}

		if(containsSwearWords(event.getMessage())){
			player.sendMessage(chatTag + ChatColor.RED + " Do not swear in chat! You've been fined $100!");
			econ.withdrawPlayer(player.getName(), 100);
			event.setCancelled(true);
			return;
		}
		
		if(player.hasPermission("chat.color")){
			event.setMessage(event.getMessage().replaceAll("&", "§"));
		}
		
		event.setMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + level.get(player.getName()) + ChatColor.GRAY + "][" + getPrefix(player) + ChatColor.GRAY + "] " + ChatColor.WHITE + player.getName() + ChatColor.GRAY + ">");
		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				spam.remove(player.getName());
			}
		}, 60);
	}
}
