package me.PocketIsland.SkyWars;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import me.PocketIsland.SkyWars.Arena.Status;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.mcsg.double0negative.tabapi.TabAPI;

public class Core extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Economy econ = null;
	public static CoreProtectAPI coreProtect = null;

	public static Location lobby;
	public static int minStart;
	public static double rewardAmount;
	public static Scoreboard sb;
	public static ItemStack[] T1Loot;
	public static ItemStack[] T2Loot;

	public static Map<String, Boolean> perkTNT;
	public static Map<String, Boolean> perkBlaze;
	public static Map<String, Boolean> perkFlak;
	public static Map<String, Boolean> perkSpeed;
	public static Map<String, Boolean> perkScavenger;
	public static Map<String, Boolean> perkDexterity;
	public static Map<String, Boolean> perkTacMask;
	public static Map<String, Boolean> perkResistance;
	//public static Map<String, Boolean> perkTrigger;

	public static Map<String, Integer> playerScore;
	public static Map<String, Integer> playerKills;
	public static Map<String, Integer> playerDeaths;
	public static Map<String, Integer> playerGames;
	public static Map<String, Integer> playerWins;

	public static Connection conn;

	int arenaCount;
	Arena[] arena;
	Map<String, Integer> wand;

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();

		for(int x = 0; x < arena.length; x++){
			if(arena[x].GetStatus() != Status.Disabled){
				arena[x].Reset();
			}
		}

		try {
			conn.close();
			logger.info("Connection closed!");
		} catch (SQLException e) {
			logger.info(("An SQL error occurred! " + e.getMessage()));
		}

		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion()
				+ " has been disabled!");
	}


	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);

		if (!setupEconomy() ) {
			logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		coreProtect = getCoreProtect();
		
		if (coreProtect!=null){
			coreProtect.testAPI();
		}else{
			logger.severe(String.format("[%s] - Disabled due to no CoreProtect!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (!(new File(getDataFolder(), "config.yml")).exists())
			saveDefaultConfig();
		
		if(!setupSQL()){
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		perkTNT = new HashMap<String, Boolean>();
		perkBlaze = new HashMap<String, Boolean>();
		perkFlak = new HashMap<String, Boolean>();
		perkSpeed = new HashMap<String, Boolean>();
		perkScavenger = new HashMap<String, Boolean>();
		perkDexterity = new HashMap<String, Boolean>();
		perkTacMask = new HashMap<String, Boolean>();
		perkResistance = new HashMap<String, Boolean>();
		//perkTrigger = new HashMap<String, Boolean>();

		playerScore = new HashMap<String, Integer>();
		playerKills = new HashMap<String, Integer>();
		playerDeaths = new HashMap<String, Integer>();
		playerGames = new HashMap<String, Integer>();
		playerWins = new HashMap<String, Integer>();


		sb = Bukkit.getScoreboardManager().getNewScoreboard();
		wand = new HashMap<String, Integer>();

		int radius = 0;
		Location[] spawnLocations = new Location[8];
		Location center;
		World world;
		List<String> strChestLocations;
		List<Location> chestLocationsT1 = new ArrayList<Location>();
		List<Location> chestLocationsT2 = new ArrayList<Location>();
		List<String> strLoot;

		String[] strSplit;
		List<ItemStack> chestLoot = new ArrayList<ItemStack>();
		strLoot = getConfig().getStringList("ChestLoot.T1");

		for(int x = 0; x < strLoot.size(); x++){
			strSplit = strLoot.get(x).split(">");
			chestLoot.add(GetItem(Integer.parseInt(strSplit[0]), Integer.parseInt(strSplit[1])));
		}

		T1Loot = new ItemStack[chestLoot.size()];
		chestLoot.toArray(T1Loot);

		strLoot = getConfig().getStringList("ChestLoot.T2");

		chestLoot.clear();

		for(int x = 0; x < strLoot.size(); x++){
			strSplit = strLoot.get(x).split(">");
			chestLoot.add(GetItem(Integer.parseInt(strSplit[0]), Integer.parseInt(strSplit[1])));
		}

		T2Loot = new ItemStack[chestLoot.size()];
		chestLoot.toArray(T2Loot);

		arenaCount = getConfig().getInt("Arena-Count");
		minStart = getConfig().getInt("Minimum-Players-To-Start");
		rewardAmount = getConfig().getDouble("Win-Reward");
		lobby = GetLocation(getConfig().getString("Lobby"));

		arena = new Arena[arenaCount];

		for(int x = 0; x < arenaCount; x++){
			if(getConfig().isInt("Arenas." + x + ".Radius"))
				radius = getConfig().getInt("Arenas." + x + ".Radius");
			else{
				logger.severe(Tag() + ChatColor.RED + " Error occured when loading plugin. Plugin disabled!");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}

			world = Bukkit.getWorld(getConfig().getString(("Arenas." + x + ".World")));
			center = GetLocation(getConfig().getString("Arenas." + x + ".Center"), world);

			strChestLocations = getConfig().getStringList("Arenas." + x + ".ChestLoot.T1");

			for(int y = 0; x < strChestLocations.size(); y++){
				chestLocationsT1.add(GetLocation(strChestLocations.get(y), world));
			}

			strChestLocations = getConfig().getStringList("Arenas." + x + ".ChestLoot.T2");

			for(int y = 0; x < strChestLocations.size(); y++){
				chestLocationsT2.add(GetLocation(strChestLocations.get(y), world));
			}

			for(int i = 1; i <= spawnLocations.length; i++){
				spawnLocations[i - 1] = GetLocation(getConfig().getString("Arenas." + x + ".Spawns." + i), world);
			}

			arena[x] = new Arena(this, x + 1, spawnLocations, radius, world, center, chestLocationsT1, chestLocationsT2);

			pm.registerEvents(arena[x], this);
		}

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				UpdateGame();
			}
		}, (20), (20));

		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion()
				+ " has been enabled!");
	}

	private CoreProtectAPI getCoreProtect() {
		Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

		if (plugin == null || !(plugin instanceof CoreProtect)) {
			return null;
		}

		CoreProtectAPI CoreProtect = ((CoreProtect)plugin).getAPI();
		if (CoreProtect.isEnabled()==false){
			return null;
		}

		if (CoreProtect.APIVersion() < 2){
			return null;
		}

		return CoreProtect;
	}

	private boolean setupSQL(){
		try {
			Class.forName("com.mysql.jdbc.Driver");

			conn = DriverManager.getConnection("jdbc:mysql://" + getConfig().getString("MySQL.host") + ":" + getConfig().getString("MySQL.port") + "/" + getConfig().getString("MySQL.database"), getConfig().getString("MySQL.user"), getConfig().getString("MySQL.password"));

			if (!conn.isClosed()){

				PreparedStatement stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " +
						"SW_stats (player VARCHAR(255), score INT, kills INT, deaths INT, games INT, wins INT);");

				stmt.execute();

				stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " +
						"SW_perks (player VARCHAR(255), tnt INT, blaze INT, flak INT, speed INT, scavenger INT, dexterity INT, tacmask INT, resistance INT);");

				stmt.execute();
			}
			else{
				logger.info("Connection isn't open!");
				return false;
			}

		} catch (ClassNotFoundException e) {
			logger.info("Where is your MySQL driver???");
			return false;
		} catch (SQLException e) {
			logger.info(("An SQL error occurred! " + e.getMessage()));
			return false;
		}
		
		return true;
	}

	enum Kit{
		creeper,
		tnt,
		troll,
		scout,
		swordsman,
		knight,
		witch,
		archer,
	}
	
	public void openKitInventory(Player player){
		Inventory inventory = Bukkit.createInventory(player, 9, "Select a kit!");
		ItemStack item;
		ItemMeta meta;
		String[] lore;
		
		item = new ItemStack(Material.MONSTER_EGG, 1);
		item.setDurability((short) 50);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GREEN + "Kit Creeper");
		lore = new String[]{ChatColor.GREEN + "5 creeper eggs"};
		meta.setLore(Arrays.asList(lore));
		inventory.addItem(item);
		
		item = new ItemStack(Material.TNT, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED + "TNT");
		lore = new String[]{ChatColor.GREEN + "10 TNT with flint and steel"};
		meta.setLore(Arrays.asList(lore));
		inventory.addItem(item);
		
		item = new ItemStack(Material.EGG, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Troll");
		lore = new String[]{ChatColor.GREEN + "8 eggs and 8 snowballs"};
		meta.setLore(Arrays.asList(lore));
		inventory.addItem(item);
		
		item = new ItemStack(Material.CARROT, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Scout");
		lore = new String[]{ChatColor.GREEN + "60 seconds of speed, haste and jump boost"};
		meta.setLore(Arrays.asList(lore));
		inventory.addItem(item);
		
		item = new ItemStack(Material.STONE_SWORD, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Swordsman");
		lore = new String[]{ChatColor.GREEN + "Stone sword"};
		meta.setLore(Arrays.asList(lore));
		inventory.addItem(item);
		
		item = new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_BLUE + "Knight");
		lore = new String[]{ChatColor.GREEN + "Chainmail chestplate"};
		meta.setLore(Arrays.asList(lore));
		inventory.addItem(item);
		
		item = new ItemStack(Material.POTION, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_PURPLE + "Witch");
		lore = new String[]{ChatColor.GREEN + "Potion pack"};
		meta.setLore(Arrays.asList(lore));
		inventory.addItem(item);
		
		item = new ItemStack(Material.BOW, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "Archer");
		lore = new String[]{ChatColor.GREEN + "Bow and 10 arrows"};
		meta.setLore(Arrays.asList(lore));
		inventory.addItem(item);
		
		player.openInventory(inventory);
	}
	
	static void incrementScore(String player, int x){

		try{
			PreparedStatement check = conn.prepareStatement("SELECT * FROM SW_stats WHERE player = ?;");
			check.setString(1, player);
			ResultSet set = check.executeQuery();

			if (set.next()){

				PreparedStatement update = conn.prepareStatement("UPDATE SW_stats" +
						" SET score = ? WHERE player = ?;");
				String n = set.getString("player");
				int points = set.getInt("score");
				points += x;

				update.setString(2, n);
				update.setInt(1, points);
				update.execute();

				playerScore.put(player, points);
			}
			else{
				createPlayerStats(player);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		RefreshScoreboard(Bukkit.getPlayer(player));
	}

	public static void incrementKills(String player, int x){
		try{
			PreparedStatement check = conn.prepareStatement("SELECT * FROM SW_stats WHERE player = ?;");
			check.setString(1, player);
			ResultSet set = check.executeQuery();

			if (set.next()){

				PreparedStatement update = conn.prepareStatement("UPDATE SW_stats" +
						" SET kills = ? WHERE player = ?;");
				String n = set.getString("player");
				int kills = set.getInt("kills");
				kills += x;

				update.setString(2, n);
				update.setInt(1, kills);
				update.execute();

				playerKills.put(player, kills);
			}
			else{
				createPlayerStats(player);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		RefreshScoreboard(Bukkit.getPlayer(player));
	}

	public static void incrementDeaths(String player, int x){

		try{
			PreparedStatement check = conn.prepareStatement("SELECT * FROM SW_stats WHERE player = ?;");
			check.setString(1, player);
			ResultSet set = check.executeQuery();

			if (set.next()){

				PreparedStatement update = conn.prepareStatement("UPDATE SW_stats" +
						" SET deaths = ? WHERE player = ?;");
				String n = set.getString("player");
				int deaths = set.getInt("deaths");
				deaths += + x;

				update.setString(2, n);
				update.setInt(1, deaths);
				update.execute();

				playerDeaths.put(player, deaths);
			}
			else{
				createPlayerStats(player);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		RefreshScoreboard(Bukkit.getPlayer(player));
	}

	public static void incrementGames(String player, int x){

		try{
			PreparedStatement check = conn.prepareStatement("SELECT * FROM SW_stats WHERE player = ?;");
			check.setString(1, player);
			ResultSet set = check.executeQuery();

			if (set.next()){

				PreparedStatement update = conn.prepareStatement("UPDATE SW_stats" +
						" SET games = ? WHERE player = ?;");
				String n = set.getString("player");
				int games = set.getInt("games");
				games += x;

				update.setString(2, n);
				update.setInt(1, games);
				update.execute();

				playerGames.put(player, games);
			}
			else{
				createPlayerStats(player);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		RefreshScoreboard(Bukkit.getPlayer(player));
	}

	public static void incrementWins(String player, int x){

		try{
			PreparedStatement check = conn.prepareStatement("SELECT * FROM SW_stats WHERE player = ?;");
			check.setString(1, player);
			ResultSet set = check.executeQuery();

			if (set.next()){

				PreparedStatement update = conn.prepareStatement("UPDATE SW_stats" +
						" SET wins = ? WHERE player = ?;");
				String n = set.getString("player");
				int wins = set.getInt("wins");
				wins += x;

				update.setString(2, n);
				update.setInt(1, wins);
				update.execute();

				playerWins.put(player, wins);
			}
			else{
				createPlayerStats(player);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		RefreshScoreboard(Bukkit.getPlayer(player));
	}

	public static void UpdatePerk(String player, String perk, int updateTo){
		try{
			PreparedStatement check = conn.prepareStatement("SELECT * FROM SW_perks WHERE player = ?;");
			check.setString(1, player);
			ResultSet set = check.executeQuery();

			if (set.next()){

				PreparedStatement update = conn.prepareStatement("UPDATE SW_perks" +
						" SET " + perk + " = ? WHERE player = ?;");
				String n = set.getString("player");

				update.setString(2, n);
				update.setInt(1, updateTo);
				update.execute();

				if(perk == "tnt" && updateTo == 1)
					perkTNT.put(player, true);
				else if(perk == "tnt" && updateTo == 2)
					perkTNT.put(player, false);
				else if(perk == "blaze" && updateTo == 1)
					perkBlaze.put(player, true);
				else if(perk == "blaze" && updateTo == 2)
					perkBlaze.put(player, false);
				else if(perk == "flak" && updateTo == 1)
					perkFlak.put(player, true);
				else if(perk == "flak" && updateTo == 2)
					perkFlak.put(player, false);
				else if(perk == "speed" && updateTo == 1)
					perkSpeed.put(player, true);
				else if(perk == "speed" && updateTo == 2)
					perkSpeed.put(player, false);
				else if(perk == "scavenger" && updateTo == 1)
					perkScavenger.put(player, true);
				else if(perk == "scavenger" && updateTo == 2)
					perkScavenger.put(player, false);
				else if(perk == "dexterity" && updateTo == 1)
					perkDexterity.put(player, true);
				else if(perk == "dexterity" && updateTo == 2)
					perkDexterity.put(player, false);
				else if(perk == "tacmask" && updateTo == 1)
					perkTacMask.put(player, true);
				else if(perk == "tacmask" && updateTo == 2)
					perkTacMask.put(player, false);
				else if(perk == "resistance" && updateTo == 1)
					perkResistance.put(player, true);
				else if(perk == "resistance" && updateTo == 2)
					perkResistance.put(player, false);
			}
			else{
				createPlayerPerks(player);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	private static void createPlayerStats(String player){
		PreparedStatement insert;
		try {
			insert = conn.prepareStatement("INSERT INTO SW_stats (player, score, kills, deaths, games, wins)" +
					" VALUES (?, ?, ?, ?, ?, ?);");
			insert.setString(1, player);
			insert.setInt(2, 0);
			insert.setInt(3, 0);
			insert.setInt(4, 0);
			insert.setInt(5, 0);
			insert.setInt(6, 0);
			insert.execute();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	private static void createPlayerPerks(String player){
		PreparedStatement insert;
		try {
			insert = conn.prepareStatement("INSERT INTO SW_perks (player, tnt, blaze, flak, speed, scavenger, dexterity, tacmask, resistance)" +
					" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
			insert.setString(1, player);
			insert.setInt(2, 0);
			insert.setInt(3, 0);
			insert.setInt(4, 0);
			insert.setInt(5, 0);
			insert.setInt(6, 0);
			insert.setInt(7, 0);
			insert.setInt(8, 0);
			insert.setInt(9, 0);
			insert.execute();
		}catch(SQLException e){
			e.printStackTrace();
		}
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
	
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {

		if(!cmd.getName().equalsIgnoreCase("sw") && !cmd.getName().equalsIgnoreCase("skywars"))
			return false;

		if(!(sender instanceof Player))
			return false;

		Player player = (Player)sender;


		if(args.length == 0){
			player.sendMessage(Tag() + ChatColor.AQUA + " Welcome to SkyWars on iPocketIsland's Server!");
			player.sendMessage(Tag() + ChatColor.RED + " Use " + ChatColor.BLUE + "/sw join #" + ChatColor.RED + " to join an arena!");
			player.sendMessage(Tag() + ChatColor.RED + " Use " + ChatColor.BLUE + "/sw spectate #" + ChatColor.RED + " to spectate an arena!");
			player.sendMessage(Tag() + ChatColor.RED + " Use " + ChatColor.BLUE + "/sw leave" + ChatColor.RED + " to leave an arena!");
			player.sendMessage(Tag() + ChatColor.RED + " Use " + ChatColor.BLUE + "/sw buyperk" + ChatColor.RED + " to purchase perks!");
			player.sendMessage(Tag() + ChatColor.RED + " Use " + ChatColor.BLUE + "/balance" + ChatColor.RED + " to view your balance!");
		}
		else{
			if(player.hasPermission("SkyWars.Admin") || player.isOp()){
				if(args[0].equalsIgnoreCase("setarena") || args[0].equalsIgnoreCase("sa")){
					if(args.length != 3){
						player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw SetArena <Arena #> <Radius>");
					}else{
						if(!TryParse(args[1]) || !TryParse(args[2])){
							player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw SetArena <Arena #> <Radius>");
						}else{
							int ID = Integer.parseInt(args[1]) - 1;
							int radius = Integer.parseInt(args[2]);

							if(!(ID >= 0 && ID < arenaCount && radius > 0)){
								player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw SetArena <Arena #> <Radius>");
								return true;
							}

							getConfig().set("Arenas." + ID + ".Radius", radius);
							getConfig().set("Arenas." + ID + ".World", player.getWorld().getName());
							getConfig().set("Arenas." + ID + ".Center", player.getLocation().getX() + ">" + player.getLocation().getY() + ">" + player.getLocation().getZ());

							arena[ID].SetCenter(player.getLocation());
							arena[ID].SetRadius(radius);
							arena[ID].SetWorld(player.getWorld());

							player.sendMessage(Tag() + ChatColor.RED + " Arena " + (ID + 1) + " set successfully!");
							saveConfig();
						}
					}
				}else if(args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("ss")){
					if(args.length != 3){
						player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw SetSpawn <Arena #> <Radius>");
					}
					else{
						if(!TryParse(args[1]) || !TryParse(args[2])){
							player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw SetSpawn <Arena #> <Spawn #>");
						}else{
							int ID = Integer.parseInt(args[1]) - 1;
							int spawnNum = Integer.parseInt(args[2]);
							Location loc = player.getLocation();

							if(!(ID >= 0 && ID < arenaCount && spawnNum > 0 && spawnNum <= 8)){
								player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw SetSpawn <Arena #> <Spawn #>");
								return true;
							}

							getConfig().set("Arenas." + ID + ".Spawns." + spawnNum, loc.getX() + ">" + loc.getY() + ">" + loc.getZ());
							arena[ID].SetSpawn(spawnNum, loc);
							player.sendMessage(Tag() + ChatColor.RED + " Spawn " + spawnNum + " in Arena " + (ID + 1) + " set successfully");
							saveConfig();
						}
					}
				}else if(args[0].equalsIgnoreCase("setlobby") || args[0].equalsIgnoreCase("sl")){
					if(args.length != 1){
						player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw SetLobby");
					}
					else{
						lobby = player.getLocation();
						getConfig().set("Lobby", player.getWorld().getName() + ">" + player.getLocation().getX() + ">" + player.getLocation().getY() + ">" + player.getLocation().getZ());
						player.sendMessage(Tag() + ChatColor.RED + " Lobby set succesfully at your location!");
						saveConfig();
					}
				}else if(args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("e")){
					if(args.length != 2){
						player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw Enable <Arena #> <Radius>");
					}
					else{
						if(!TryParse(args[1])){
							player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw Enable <Arena #>");
						}else{
							int ID = Integer.parseInt(args[1]) - 1;

							if(!(ID >= 0 && ID < arenaCount)){
								player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw Enable <Arena #>");
								return true;
							}

							if(arena[ID].GetStatus() == Status.Disabled){
								arena[ID].setStatus(Status.Waiting);
								player.sendMessage(Tag() + ChatColor.RED + " Arena " + (ID + 1) + " has been enabled!");
							}
							else
								player.sendMessage(Tag() + ChatColor.RED + " Arena " + (ID + 1) + " is already enabled!");
						}
					}
				}else if(args[0].equalsIgnoreCase("disable") || args[0].equalsIgnoreCase("d")){
					if(args.length != 2){
						player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw Disable <Arena #>");
					}
					else{
						if(!TryParse(args[1])){
							player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw Disable <Arena #>");
						}else{
							int ID = Integer.parseInt(args[1]) - 1;

							if(!(ID >= 0 && ID < arenaCount)){
								player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw Disable <Arena #>");
								return true;
							}

							if(arena[ID].GetStatus() != Status.Disabled){
								arena[ID].Reset();
								arena[ID].setStatus(Status.Disabled);
								player.sendMessage(Tag() + ChatColor.RED + " Arena " + (ID + 1) + " has been disabled!");
							}			
							else
								player.sendMessage(Tag() + ChatColor.RED + " Arena " + (ID + 1) + " is already disaabled!");
						}
					}
				}else if(args[0].equalsIgnoreCase("wand") || args[0].equalsIgnoreCase("w")){
					if(args.length != 3){
						player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw wand <Arena #> <Tier #>");
					}
					else{
						if(!TryParse(args[1]) || !TryParse(args[2])){
							player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw wand <Arena #> <Tier #>");
						}else{
							int ID = Integer.parseInt(args[1]) - 1;
							int tier = Integer.parseInt(args[2]);

							if(!(ID >= 0 && ID < arenaCount) || !(tier == 1 || tier == 2)){
								player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw wand <Arena #> <Tier #>");
								return true;
							}

							ItemStack magicWand = new ItemStack(Material.GOLD_SPADE, 1);
							ItemMeta wandMeta = magicWand.getItemMeta();
							List<String> lore = new ArrayList<String>();
							lore.add("Wand for Arena " + (ID + 1) + " Tier " + tier);

							wandMeta.setDisplayName(ChatColor.RED + "SkyWars Magic Wand");
							wandMeta.setLore(lore);

							wand.put(player.getName(), ID);

							player.sendMessage(Tag() + ChatColor.DARK_RED + " You have the magic wand! Use it to ID chests");
						}
					}
				}
			}

			if(args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("j")){
				if(args.length != 2){
					player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw join <Arena #>");
				}else{
					if(!TryParse(args[1])){
						player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw join <Arena #>");
					}else{
						int ID = Integer.parseInt(args[1]) - 1;;

						if(!(ID >= 0 && ID < arenaCount)){
							player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw join <Arena #>");
							return true;
						}

						if(arena[ID].GetStatus() == Status.Disabled)
							player.sendMessage(Tag() + ChatColor.RED + " That arena is disabled!");
						else if(arena[ID].GetStatus() == Status.InGame)
							player.sendMessage(Tag() + ChatColor.RED + " That arena has already begun!");
						else if(arena[ID].GetStatus() == Status.Waiting || arena[ID].GetStatus() == Status.Starting){
							if(arena[ID].GetPlayers().size() < 8)
								PlayerJoin(player, ID);
							else
								player.sendMessage(Tag() + ChatColor.RED + " That arena is full!");
						}
						else
							player.sendMessage(Tag() + ChatColor.RED + " Could not join arena!");
					}
				}
			}else if(args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("l")){
				if(args.length != 1){
					player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw leave");
				}else{
					if(InGame(player.getName())){
						arena[GetPlayerArena(player.getName())].RemovePlayer(player);
						player.teleport(lobby);
					}else{
						player.sendMessage(Tag() + ChatColor.RED + " You are not in a game!");
					}
				}
			}else if(args[0].equalsIgnoreCase("fstart") || args[0].equalsIgnoreCase("fs") || args[0].equalsIgnoreCase("forcestart")){
				if(args.length != 1){
					player.sendMessage(Tag() + ChatColor.RED + " Usage " + ChatColor.BLUE + "/sw fstart");
				}else{
					if(InGame(player.getName())){
						int ID = GetPlayerArena(player.getName());

						if(arena[ID].GetStatus() == Status.Waiting && arena[ID].GetPlayers().size() >= 2)
							arena[ID].StartCountdown();
						else if(arena[ID].GetPlayers().size() <= 1)
							player.sendMessage(Tag() + ChatColor.RED + " Not enough players to start game!");
						else
							player.sendMessage(Tag() + ChatColor.RED + " Can not force start game!");
					}else{
						player.sendMessage(Tag() + ChatColor.RED + " You are not in a game!");
					}
				}
			}else if(args[0].equalsIgnoreCase("buyperk") || args[0].equalsIgnoreCase("bp")){
				if(InGame(player.getName())){
					player.sendMessage(Tag() + ChatColor.RED + " You may not purchase perks while in-game!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " Opening perk purchase menu!");
					BuyPerk(player);
				}
			}
		}
		return true;
	}

	public Location GetLocation(String i){
		String[] data = i.split(">");
		Location loc;
		World world = Bukkit.getWorld(data[0]);
		double x = Double.parseDouble(data[1]);
		double y = Double.parseDouble(data[2]);
		double z = Double.parseDouble(data[3]);

		loc = new Location(world, x, y, z);

		return loc;
	}

	public Location GetLocation(String i, World world){
		String[] data = i.split(">");
		Location loc;
		double x = Double.parseDouble(data[0]);
		double y = Double.parseDouble(data[1]);
		double z = Double.parseDouble(data[2]);

		loc = new Location(world, x, y, z);

		return loc;
	}

	private void PlayerJoin(Player player, int ID){
		arena[ID].AddPlayer(player);
	}

	public static String Tag(){
		return ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "SW" + ChatColor.DARK_GREEN + "]";
	}

	public boolean TryParse(String x){
		try{
			Integer.parseInt(x);
			return true;
		}catch (NumberFormatException exc){
			return false;
		}
	}

	private void UpdateGame(){
		for(int x = 0; x < arena.length; x++){
			arena[x].UpdateArena();
		}
	}

	private ItemStack GetItem(int ID, int amount){
		@SuppressWarnings("deprecation")
		ItemStack i = new ItemStack(Material.getMaterial(ID), amount);
		return i;
	}

	public static void GiveLobbyItems(Player player){

	}

	public boolean InGame(String playerName){
		for(int x = 0; x < arena.length; x++)
			if(arena[x].GetPlayers().contains(playerName))
				return true;

		return false;
	}

	public int GetPlayerArena(String playerName){
		for(int x = 0; x < arena.length; x++)
			if(arena[x].GetPlayers().contains(playerName))
				return x;

		return -1;
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event){
		if(event.isCancelled())
			return;

		Player player = event.getPlayer();

		if(player.isOp() || player.hasPermission("SkyWars.Admin")){
			if(player.getInventory().getItemInHand().getType() == Material.GOLD_SPADE && wand.containsKey(player.getName())){
				Block b = event.getBlock();

				if(b.getType()  == Material.CHEST){
					int ID = wand.get(player.getName());
					List<Location> locations = arena[ID].GetChestLocationsT1();

					locations.add(b.getLocation());

					arena[ID].SetChestLocationsT1(locations);
					getConfig().getStringList("Arenas." + ID + ".ChestLoot.T1").add(b.getLocation().getX() + ">" + b.getLocation().getY() + ">" + b.getLocation().getZ());
					saveConfig();

					player.sendMessage(Tag() + ChatColor.RED + " Chest registered successfully!");
					event.setCancelled(true);
				}
			}
		}
	}

	public static void SetupScoreboard(Player player){
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		Objective stats = scoreboard.registerNewObjective("stats", "dummy");

		stats.setDisplayName(ChatColor.RED + "Sky Wars");
		stats.setDisplaySlot(DisplaySlot.SIDEBAR);

		player.setScoreboard(scoreboard);
	}

	public static void RefreshScoreboard(Player player){
		Scoreboard scoreboard = player.getScoreboard();
		Objective stats;

		stats = scoreboard.getObjective("stats");

		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.YELLOW + "Coins")).setScore((int) Math.round(econ.getBalance(player.getName())));
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Score:")).setScore(playerScore.get(player.getName()));
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.AQUA + "Kills:")).setScore(playerKills.get(player.getName()));
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_RED + "Deaths:")).setScore(playerDeaths.get(player.getName()));
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "Wins:")).setScore(playerWins.get(player.getName()));
		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.BLUE + "Games:")).setScore(playerGames.get(player.getName()));
	}

	public void RefreshTABAll(){
		for(Player player : Bukkit.getOnlinePlayers()){
			RefreshTAB(player);
		}
	}

	public void RefreshTAB(Player player){
		int h = 1;
		int v = 3;

		TabAPI.setTabString(this, player, 0, 0, ChatColor.DARK_GREEN + "----------");
		TabAPI.setTabString(this, player, 0, 1, ChatColor.GREEN + "----------");
		TabAPI.setTabString(this, player, 0, 2, ChatColor.DARK_GREEN + "----------");

		TabAPI.setTabString(this, player, 1, 0, ChatColor.DARK_RED + "SkyWars");
		TabAPI.setTabString(this, player, 1, 1, ChatColor.DARK_RED + "PocketIsland");
		TabAPI.setTabString(this, player, 1, 2, ChatColor.DARK_RED + "Online" + Bukkit.getOnlinePlayers().length + "/" + Bukkit.getServer().getMaxPlayers());

		TabAPI.setTabString(this, player, 2, 0, ChatColor.DARK_GREEN + "----------");
		TabAPI.setTabString(this, player, 2, 1, ChatColor.GREEN + "----------");
		TabAPI.setTabString(this, player, 2, 2, ChatColor.DARK_GREEN + "----------");

		TabAPI.setTabString(this, player, 3, 0, ChatColor.BLUE + "" + ChatColor.BOLD + "Players:");

		for(Player user : Bukkit.getOnlinePlayers()){
			if(h > 2){
				h = 0;
				v++;
			}

			TabAPI.setTabString(this, player, v, h++, GetUserColor(user) + user.getName());
		}

		TabAPI.updatePlayer(player);
	}

	public static void BuyPerk(Player player){
		Inventory inventory = Bukkit.createInventory(player, 9, "Purchase a perk!");
		ItemStack item;
		ItemMeta meta;
		String[] lore;

		item = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Flak Jacket");
		lore = new String[]{ ChatColor.AQUA + "Reduced explosive damage", ChatColor.GREEN + "Cost: 2500 coins"};
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.BLAZE_ROD, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED + "Resistance");
		lore = new String[]{ ChatColor.RED + "Reduced fire damage", ChatColor.GREEN + "Cost: 2500 coins"};
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.DIAMOND_HELMET, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GREEN + "Tactical Mask");
		lore = new String[]{ ChatColor.AQUA + "Immune to stuns and blinds", ChatColor.GREEN + "Cost: 5000 coins"};
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		//item = new ItemStack(Material.BOW, 1);
		//meta = item.getItemMeta();
		//meta.setDisplayName(ChatColor.DARK_PURPLE + "Trigger Finger");
		//lore = new String[]{ ChatColor.LIGHT_PURPLE + "Arrows shoot with greater force", ChatColor.GREEN + "Cost: 5000 coins"};
		//meta.setLore(Arrays.asList(lore));
		//item.setItemMeta(meta);
		//inventory.addItem(item);

		item = new ItemStack(Material.TNT, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Instant TNT");
		lore = new String[]{ ChatColor.GOLD + "TNT you place instantly detonated", ChatColor.GREEN + "Cost: 5000 coins"};
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.ARROW, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_BLUE + "Scavenger");
		lore = new String[]{ ChatColor.BLUE + "Receive arrows for every kill", ChatColor.GREEN + "Cost: 5000 coins"};
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.BLAZE_POWDER, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "Blaze");
		lore = new String[]{ ChatColor.RED + "All your arrows set your foes on fire", ChatColor.GREEN + "Cost: 7500 coins"};
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.SUGAR, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Speed");
		lore = new String[]{ ChatColor.BLUE + "Faster movement", ChatColor.GREEN + "Cost: 10 000 coins"};
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.CARROT, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "Dexterity");
		lore = new String[]{ ChatColor.GOLD + "Jump higher", ChatColor.GREEN + "Cost: 10 000 coins"};
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		player.openInventory(inventory);
	}

	public static ChatColor GetUserColor(Player player){

		if(player.hasPermission("SkyWars.Staff"))
			return ChatColor.GRAY;
		else if(player.hasPermission("SkyWars.Immortal"))
			return ChatColor.DARK_RED;
		else if(player.hasPermission("SkyWars.Legend"))
			return ChatColor.RED;
		else if(player.hasPermission("SkyWars.Elite"))
			return ChatColor.GOLD;
		else if(player.hasPermission("SkyWars.Veteran"))
			return ChatColor.YELLOW;
		else if(player.hasPermission("SkyWars.Hero"))
			return ChatColor.DARK_GREEN;
		else if(player.hasPermission("SkyWars.Warrior"))
			return ChatColor.GREEN;
		else
			return ChatColor.WHITE;
	}

	@EventHandler
	public void playerJoinEvent(PlayerJoinEvent event){
		Player player = event.getPlayer();

		try{
			PreparedStatement check = conn.prepareStatement("SELECT * FROM SW_stats WHERE player = ?;");
			check.setString(1, player.getName());
			ResultSet set = check.executeQuery();

			if (set.next()){
				playerScore.put(player.getName(), set.getInt("score"));
				playerKills.put(player.getName(), set.getInt("kills"));
				playerDeaths.put(player.getName(), set.getInt("deaths"));
				playerWins.put(player.getName(), set.getInt("wins"));
				playerGames.put(player.getName(), set.getInt("games"));

			}
			else{
				createPlayerStats(player.getName());
				playerScore.put(player.getName(), 0);
				playerKills.put(player.getName(), 0);
				playerDeaths.put(player.getName(), 0);
				playerWins.put(player.getName(), 0);
				playerGames.put(player.getName(), 0);
			}

			check = conn.prepareStatement("SELECT * FROM SW_perks WHERE player = ?;");
			check.setString(1, player.getName());
			set = check.executeQuery();

			if (set.next()){
				if(set.getInt("tnt") == 1)
					perkTNT.put(player.getName(), true);
				else if(set.getInt("tnt") == 2)
					perkTNT.put(player.getName(), false);

				if(set.getInt("blaze") == 1)
					perkBlaze.put(player.getName(), true);
				else if(set.getInt("blaze") == 2)
					perkBlaze.put(player.getName(), false);

				if(set.getInt("flak") == 1)
					perkFlak.put(player.getName(), true);
				else if(set.getInt("flak") == 2)
					perkFlak.put(player.getName(), false);

				if(set.getInt("speed") == 1)
					perkSpeed.put(player.getName(), true);
				else if(set.getInt("speed") == 2)
					perkSpeed.put(player.getName(), false);

				if(set.getInt("scavenger") == 1)
					perkScavenger.put(player.getName(), true);
				else if(set.getInt("scavenger") == 2)
					perkScavenger.put(player.getName(), false);

				if(set.getInt("dexterity") == 1)
					perkDexterity.put(player.getName(), true);
				else if(set.getInt("dexterity") == 2)
					perkDexterity.put(player.getName(), false);

				if(set.getInt("tacmask") == 1)
					perkTacMask.put(player.getName(), true);
				else if(set.getInt("tacmask") == 2)
					perkTacMask.put(player.getName(), false);

				if(set.getInt("resistance") == 1)
					perkResistance.put(player.getName(), true);
				else if(set.getInt("resistance") == 2)
					perkResistance.put(player.getName(), false);

				//if(set.getInt("trigger") == 1)
				//	perkTrigger.put(player.getName(), true);
				//else if(set.getInt("trigger") == 2)
				//	perkTrigger.put(player.getName(), false);
			}
			else{
				createPlayerPerks(player.getName());
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		SetupScoreboard(player);
		RefreshScoreboard(player);

		TabAPI.setPriority(this, player, 0);
		RefreshTABAll();

		if(perkSpeed.get(player.getName())){
			PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 2147483647, 1);

			player.addPotionEffect(speed);
		}

		if(perkDexterity.get(player.getName())){
			PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 2147483647, 1);

			player.addPotionEffect(jump);
		}

		if(perkResistance.get(player.getName())){
			PotionEffect resistance = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 2147483647, 1);

			player.addPotionEffect(resistance);
		}
	}

	@EventHandler
	public void playerLeaveEvent(PlayerQuitEvent event){
		String playerName = event.getPlayer().getName();

		if(perkTNT.containsKey(playerName))
			perkTNT.remove(playerName);

		if(perkBlaze.containsKey(playerName))
			perkBlaze.remove(playerName);

		if(perkFlak.containsKey(playerName))
			perkFlak.remove(playerName);

		if(perkSpeed.containsKey(playerName))
			perkSpeed.remove(playerName);

		if(perkScavenger.containsKey(playerName))
			perkScavenger.remove(playerName);

		if(perkDexterity.containsKey(playerName))
			perkDexterity.remove(playerName);

		if(perkTacMask.containsKey(playerName))
			perkTacMask.remove(playerName);

		if(perkResistance.containsKey(playerName))
			perkResistance.remove(playerName);

		//if(perkTrigger.containsKey(playerName))
		//	perkTrigger.remove(playerName);

		if(playerScore.containsKey(playerName))
			playerScore.remove(playerName);

		if(playerKills.containsKey(playerName))
			playerKills.remove(playerName);

		if(playerDeaths.containsKey(playerName))
			playerDeaths.remove(playerName);

		if(playerGames.containsKey(playerName))
			playerGames.remove(playerName);

		if(playerWins.containsKey(playerName))
			playerWins.remove(playerName);

		RefreshTABAll();
	}

	@EventHandler
	public void playerInventoryClick(InventoryClickEvent event){
		if(event.isCancelled())
			return;

		HumanEntity entity = event.getWhoClicked();

		if(event.getInventory().getName().equalsIgnoreCase("Purchase a perk!") && entity instanceof Player){
			Player player = (Player)entity;

			if(event.getCurrentItem().getType() == Material.DIAMOND_CHESTPLATE){
				if(perkFlak.containsKey(player.getName())){
					player.sendMessage(Tag() + ChatColor.RED + " You already own that perk!");
				}else if(econ.getBalance(player.getName()) >= 2500){
					UpdatePerk(player.getName(), "flak", 1);
					econ.withdrawPlayer(player.getName(), 2500);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Flak Jacket" + ChatColor.AQUA + " successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough coins to purchase that perk. Play more to earn coins!");
				}
			}else if(event.getCurrentItem().getType() == Material.BLAZE_ROD){
				if(perkResistance.containsKey(player.getName())){
					player.sendMessage(Tag() + ChatColor.RED + " You already own that perk!");
				}else if(econ.getBalance(player.getName()) >= 2500){
					UpdatePerk(player.getName(), "resistance", 1);
					econ.withdrawPlayer(player.getName(), 2500);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Resistance" + ChatColor.AQUA + " successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough coins to purchase that perk. Play more to earn coins!");
				}
			}else if(event.getCurrentItem().getType() == Material.DIAMOND_HELMET){
				if(perkTacMask.containsKey(player.getName())){
					player.sendMessage(Tag() + ChatColor.RED + " You already own that perk!");
				}else if(econ.getBalance(player.getName()) >= 5000){
					UpdatePerk(player.getName(), "tacmask", 1);
					econ.withdrawPlayer(player.getName(), 5000);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Tactical Mask" + ChatColor.AQUA + " successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough coins to purchase that perk. Play more to earn coins!");
				}
			//}else if(event.getCurrentItem().getType() == Material.BOW){
			//	if(perkTrigger.containsKey(player.getName())){
			//		player.sendMessage(Tag() + ChatColor.RED + " You already own that perk!");
			//	}else if(econ.getBalance(player.getName()) >= 5000){
			//		UpdatePerk(player.getName(), "trigger", 1);
			//		econ.withdrawPlayer(player.getName(), 5000);
			//		player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Trigger Finger" + ChatColor.AQUA + " successfully purchased!");
			//	}else{
			//		player.sendMessage(Tag() + ChatColor.RED + " You don't have enough coins to purchase that perk. Play more to earn coins!");
			//	}
			}else if(event.getCurrentItem().getType() == Material.TNT){
				if(perkTNT.containsKey(player.getName())){
					player.sendMessage(Tag() + ChatColor.RED + " You already own that perk!");
				}else if(econ.getBalance(player.getName()) >= 5000){
					UpdatePerk(player.getName(), "tnt", 1);
					econ.withdrawPlayer(player.getName(), 5000);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Instant TNT" + ChatColor.AQUA + " successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough coins to purchase that perk. Play more to earn coins!");
				}
			}else if(event.getCurrentItem().getType() == Material.ARROW){
				if(perkScavenger.containsKey(player.getName())){
					player.sendMessage(Tag() + ChatColor.RED + " You already own that perk!");
				}else if(econ.getBalance(player.getName()) >= 5000){
					UpdatePerk(player.getName(), "scavenger", 1);
					econ.withdrawPlayer(player.getName(), 5000);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Scavenger" + ChatColor.AQUA + " successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough coins to purchase that perk. Play more to earn coins!");
				}
			}else if(event.getCurrentItem().getType() == Material.BLAZE_POWDER){
				if(perkBlaze.containsKey(player.getName())){
					player.sendMessage(Tag() + ChatColor.RED + " You already own that perk!");
				}else if(econ.getBalance(player.getName()) >= 7500){
					UpdatePerk(player.getName(), "blaze", 1);
					econ.withdrawPlayer(player.getName(), 7500);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Blaze" + ChatColor.AQUA + " successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough coins to purchase that perk. Play more to earn coins!");
				}
			}else if(event.getCurrentItem().getType() == Material.BLAZE_POWDER){
				if(perkSpeed.containsKey(player.getName())){
					player.sendMessage(Tag() + ChatColor.RED + " You already own that perk!");
				}else if(econ.getBalance(player.getName()) >= 10000){
					UpdatePerk(player.getName(), "speed", 1);
					econ.withdrawPlayer(player.getName(), 10000);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Speed" + ChatColor.AQUA + " successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough coins to purchase that perk. Play more to earn coins!");
				}
			}else if(event.getCurrentItem().getType() == Material.CARROT){
				if(perkSpeed.containsKey(player.getName())){
					player.sendMessage(Tag() + ChatColor.RED + " You already own that perk!");
				}else if(econ.getBalance(player.getName()) >= 10000){
					UpdatePerk(player.getName(), "dexterotu", 1);
					econ.withdrawPlayer(player.getName(), 10000);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Dexterity" + ChatColor.AQUA + " successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough coins to purchase that perk. Play more to earn coins!");
				}
			}

			RefreshScoreboard(player);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void playerDeathEvent(PlayerRespawnEvent event){
		final Player player = (Player)event.getPlayer();

		if(perkSpeed.get(player.getName())){
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 2147483647, 1);
					player.addPotionEffect(speed);
				}
			}, 60L);
		}

		if(perkDexterity.get(player.getName())){
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 2147483647, 1);
					player.addPotionEffect(jump);
				}
			}, 60L);
		}

		if(perkResistance.get(player.getName())){
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					PotionEffect resistance = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 2147483647, 1);
					player.addPotionEffect(resistance);
				}
			}, 60L);
		}
	}

	@EventHandler (priority = EventPriority.LOW)
	public void blockPlace(BlockPlaceEvent event){
		if(event.isCancelled())
			return;

		Player player = event.getPlayer();

		if(event.getBlockPlaced().getType() == Material.TNT && perkTNT.get(player.getName())){
			player.getWorld().spawn(event.getBlock().getLocation(), TNTPrimed.class);
		}
	}

	@EventHandler
	public void onTNTExplosion(EntityExplodeEvent event) {
		if(event.getEntityType() == EntityType.PRIMED_TNT) {
			event.blockList().clear();
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void arrowShoot(EntityShootBowEvent event){
		if(event.isCancelled())
			return;

		if(event.getEntity() instanceof Player){
			Player player = (Player)event.getEntity();

			if(perkBlaze.get(player.getName()) && event.getProjectile().getType() == EntityType.ARROW){
				Entity arrow = event.getProjectile();
				arrow.setFireTicks(200);			
				event.setProjectile(arrow);
			//}else if((perkTrigger.get(player.getName()) && event.getProjectile().getType() == EntityType.ARROW)){
			//	Entity arrow = event.getProjectile();
			//	arrow.setVelocity(arrow.getVelocity().multiply(2));			
			//	event.setProjectile(arrow);
			}
		}
	}

	@EventHandler
	public void onPlayerEggThrow(PlayerEggThrowEvent event){
		Egg egg = event.getEgg();

		event.setHatching(false);

		List<Entity> near = egg.getNearbyEntities(3.0D, 3.0D, 3.0D);
		for (Entity entry : near) {
			if ((entry instanceof Player)) {
				Player victim = (Player)entry;

				if(perkTacMask.get(victim.getName())){
					victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 0));
					victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 50, 0));
				}else{
					victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
					victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 0));
				}
			}
		}

		egg.getWorld().createExplosion(egg.getLocation(), 0.0F);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDamage(EntityDamageByEntityEvent event){
		if(event.isCancelled())
			return;

		if(event.getEntity() instanceof Player){
			Player player = (Player)event.getEntity();

			if(perkFlak.get(player.getName())){
				if(event.getCause() == DamageCause.BLOCK_EXPLOSION || event.getCause() == DamageCause.ENTITY_EXPLOSION){
					event.setDamage(event.getDamage() / 2);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(EntityDeathEvent event){
		if(event.getEntity() instanceof Player && event.getEntity().getKiller() instanceof Player){
			Player player = (Player)event.getEntity().getKiller();

			if(perkScavenger.get(player.getName())){
				player.getInventory().addItem(new ItemStack(Material.ARROW, 5));
			}

			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
		}
	}
}
