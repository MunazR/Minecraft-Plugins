package me.PocketIsland.PocketRankUp;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
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
	Connection conn;

	HashMap<String, Integer> killstreak;
	HashMap<String, Integer> kills;
	HashMap<String, Integer> deaths;
	HashMap<String, Integer> level;
	HashMap<String, Integer> killsNeeded;
	HashMap<String, Double> balance;

	String tag;
	
	HashMap<String, Scoreboard> sb;
	
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

		killstreak = new HashMap<String, Integer>();
		kills = new HashMap<String, Integer>();
		deaths = new HashMap<String, Integer>();
		level = new HashMap<String, Integer>();
		killsNeeded = new HashMap<String, Integer>();
		balance = new HashMap<String, Double>();
		sb = new HashMap<String, Scoreboard>();

		tag = getConfig().getString("Tag").replaceAll("&", "§");

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
		
		sb.put(player.getName(), scoreboard);

		refreshScoreboard(player);
	}

	private void refreshScoreboard(Player player){
		Scoreboard scoreboard = sb.get(player.getName());
		Objective stats = scoreboard.getObjective("stats");

		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Balance:")).setScore((int) Math.round(econ.getBalance(player.getName())));
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.YELLOW + "Level:")).setScore(level.get(player.getName()));
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.AQUA + "Kills:")).setScore(kills.get(player.getName()));
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_RED + "Deaths:")).setScore(deaths.get(player.getName()));
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_PURPLE + "Killstreak")).setScore(killstreak.get(player.getName()));
		if(deaths.get(player.getName()) != 0)
			stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "KDR:")).setScore(Math.round(kills.get(player.getName()) / deaths.get(player.getName()) * 100) / 100);
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.BLUE + "Kills Needed:")).setScore(killsNeeded.get(player.getName()));
		
		player.setScoreboard(scoreboard);
	}

	private void getStats(String player, Player recipient){
		int level = 0;
		int kills = 0;
		int deaths = 0;
		int levelRank = 0;
		int killRank = 0;
		int deathRank = 0;
		
		try{	
			PreparedStatement check = conn.prepareStatement("SELECT * FROM KITPVP_stats WHERE player = ?;");
			check.setString(1, player);
			ResultSet set = check.executeQuery();
			
			if(set.next()){
				level = set.getInt("level");
				kills = set.getInt("kills");
				deaths = set.getInt("deaths");
			}else{
				recipient.sendMessage(ChatColor.RED + "That player does not exist!");
				return;
			}
		}catch(SQLException e){
			print(Level.SEVERE, "MySQL error occured when fetching stats for player: " + player);
			return;
		}
		
		try{	
			PreparedStatement check = conn.prepareStatement("SELECT *, (SELECT COUNT(*) FROM TABLE KITPVP_stats WHERE x.name <= t.name) AS position t.name FROM TABLE t WHERE t.name = 'Beta';");
			check.setString(1, player);
			ResultSet set = check.executeQuery();
			
			if(set.next()){
				level = set.getInt("level");
				kills = set.getInt("kills");
				deaths = set.getInt("deaths");
			}else{
				recipient.sendMessage(ChatColor.RED + "That player does not exist!");
				return;
			}
		}catch(SQLException e){
			print(Level.SEVERE, "MySQL error occured when fetching stats for player: " + player);
			return;
		}
		
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta meta = (BookMeta) book.getItemMeta();
		meta.setTitle(ChatColor.GREEN + "Server Info");
		meta.setAuthor("PocketIsland");
		String[] stats = {
				ChatColor.GREEN + " " + ChatColor.BOLD + "Player stats" + "\n"
						+ ChatColor.GOLD + ChatColor.BOLD + player
						+ "\n" + ChatColor.BLACK
						+ ChatColor.STRIKETHROUGH + "-------------------"
						+ "\n" + ChatColor.AQUA + "Level: " + level
						+ "\n" + ChatColor.GREEN + "Kills: " + kills
						+ "\n" + ChatColor.RED + "Deaths: " + deaths};
		
		meta.setPages(stats);
		book.setItemMeta(meta);
		
		recipient.getInventory().addItem(book);
		recipient.sendMessage(ChatColor.GREEN + "I've placed a book in your inventory.");
	}
	
	private void levelUp(Player player){
		level.put(player.getName(), level.get(player.getName()) + 1);
		int killsToLevel = (int) (Math.pow(level.get(player.getName()), 2) - kills.get(player.getName()));
		killsNeeded.put(player.getName(), killsToLevel);
		player.sendMessage(tag + ChatColor.BLUE + " Congratulations! You've ranked up! Your new level is " + ChatColor.GREEN + "" + ChatColor.BOLD + level.get(player.getName()));
		Bukkit.broadcastMessage(tag + ChatColor.GREEN + " " + ChatColor.BOLD + player.getName() + ChatColor.BLUE + " has leveled up to " + ChatColor.GREEN + "" + ChatColor.BOLD + level.get(player.getName()));
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
		final Player player = event.getPlayer();

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

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				setupScoreboard(player);
			}
		}, 60L);
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
}
