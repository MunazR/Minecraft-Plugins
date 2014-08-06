package me.PocketIsland.PocketPerks;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Core extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Economy econ = null;
	public Connection conn;

	public HashMap<String, Boolean> tnt;
	public HashMap<String, Boolean> tacticalMask;
	public HashMap<String, Boolean> martyrdom;
	public HashMap<String, Boolean> scavenger;
	public HashMap<String, Boolean> rush;
	public HashMap<String, Boolean> commando;
	public HashMap<String, Boolean> hardline;
	public HashMap<String, Boolean> punch;
	public HashMap<String, Boolean> juggernaut;

	public HashMap<String, Integer> killstreak;
	public boolean killstreaksEnabled;

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled!");

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
		pm.registerEvents(new EventListener(this), this);
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

		tnt = new HashMap<String, Boolean>();
		tacticalMask = new HashMap<String, Boolean>();
		martyrdom = new HashMap<String, Boolean>();
		scavenger = new HashMap<String, Boolean>();
		rush = new HashMap<String, Boolean>();
		commando = new HashMap<String, Boolean>();
		hardline = new HashMap<String, Boolean>();
		punch = new HashMap<String, Boolean>();
		juggernaut = new HashMap<String, Boolean>();

		killstreak = new HashMap<String, Integer>();

		killstreaksEnabled = getConfig().getBoolean("killstreaks-enabled");
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
						"playerperks (player VARCHAR(255), tnt INT, tacticalmask INT, martyrdom INT, scavenger INT, rush INT, commando INT, hardline INT, punch INT, juggernaut INT);");

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

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {

		if((cmd.getName().equals("buyperk") || cmd.getName().equalsIgnoreCase("bp")) && sender instanceof Player){
			BuyPerk((Player)sender);
			return true;
		}

		return false;
	}

	public void checkForKillstreak(Player player){
		if(killstreaksEnabled){
			int kills = killstreak.get(player.getName());

			if(hardline.containsKey(player.getName()) && hardline.get(player.getName())){
				if(kills == 4){
					giveSpeedBoost(player);
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "]" + ChatColor.GREEN + " You earned the Speed Boost killstreak!");
				}else if(kills == 9){
					giveStrengthBoost(player);
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "]" + ChatColor.GREEN + " You earned the Strength Boost killstreak!");
				}else if(kills == 14){
					spawnWolves(player);
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "]" + ChatColor.GREEN + " You earned the Dogs killstreak!");
					Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " is on a killstreak of 14!");
				}else if(kills == 24){
					giveSuperStrengthBoost(player);
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "]" + ChatColor.GREEN + " You earned the Super Strength Boost killstreak!");
					Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " is on a killstreak of 24!");
				}else if(kills == 49){
					nuke(player);
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "]" + ChatColor.GREEN + " You earned the Tactical Nuke killstreak!");
					Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " is on a killstreak of 49!");
					killstreak.put(player.getName(), 0);
				}
			}else{
				if(kills == 5){
					giveSpeedBoost(player);
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "]" + ChatColor.GREEN + " You earned the Speed Boost killstreak!");
				}else if(kills == 10){
					giveStrengthBoost(player);
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "]" + ChatColor.GREEN + " You earned the Strength Boost killstreak!");
				}else if(kills == 15){
					spawnWolves(player);
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "]" + ChatColor.GREEN + " You earned the Dogs killstreak!");
					Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " is on a killstreak of 15!");
				}else if(kills == 25){
					giveSuperStrengthBoost(player);
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "]" + ChatColor.GREEN + " You earned the Super Strength Boost killstreak!");
					Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " is on a killstreak of 25!");
				}else if(kills == 50){
					nuke(player);
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "]" + ChatColor.GREEN + " You earned the Tactical Nuke killstreak!");
					Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "KitPvP" + ChatColor.GRAY + "] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " is on a killstreak of 50!");
					killstreak.put(player.getName(), 0);
				}
			}
		}
	}

	private void spawnWolves(Player player){
		Wolf wolf;
		Location location;

		for(int x = 1; x <= 5; x++){
			location = player.getLocation();
			location.setX(location.getX() - 2 + (Math.random() * 4));
			location.setZ(location.getZ() - 2 + (Math.random() * 4));

			wolf = player.getWorld().spawn(location, Wolf.class);
			wolf.setOwner(player);
			wolf.setTamed(true);
			wolf.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3600, 3));
			wolf.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 3600, 3));
			wolf.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 3600, 3));
			wolf.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 3600, 3));
			wolf.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 3600, 3));
		}
	}

	private void giveSpeedBoost(Player player){
		PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 1200, 1);

		player.addPotionEffect(speed);
	}

	private void giveStrengthBoost(Player player){
		PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1200, 1);
		PotionEffect regen = new PotionEffect(PotionEffectType.REGENERATION, 1200, 1);

		player.addPotionEffect(regen);
		player.addPotionEffect(strength);
	}

	private void giveSuperStrengthBoost(Player player){
		PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1200, 2);
		PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 1200, 1);

		player.addPotionEffect(speed);
		player.addPotionEffect(strength);
	}

	private void nuke(final Player player){
		Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
		Bukkit.broadcastMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + player.getName() + ChatColor.DARK_RED + " has earned a Tactical Nuke!");
		Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");

		for(Player enemy : Bukkit.getOnlinePlayers()){
			if(enemy.getName() != player.getName()){
				enemy.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 2));
			}
		}
		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage(ChatColor.DARK_RED + ""  + ChatColor.BOLD + "Tactical Nuke in " + ChatColor.GREEN + "" + ChatColor.MAGIC + "III" + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + " 5 " + ChatColor.GREEN + "" + ChatColor.MAGIC + "III" );
			}
		}, 20L);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Tactical Nuke in " + ChatColor.GREEN + "" + ChatColor.MAGIC + "III" + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + " 4 " + ChatColor.GREEN + "" + ChatColor.MAGIC + "III" );
			}
		}, 40L);
		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage(ChatColor.DARK_RED + ""  + ChatColor.BOLD + "Tactical Nuke in " + ChatColor.GREEN + "" + ChatColor.MAGIC + "III" + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + " 3 " + ChatColor.GREEN + "" + ChatColor.MAGIC + "III" );
			}
		}, 60L);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage(ChatColor.DARK_RED + ""  + ChatColor.BOLD + "Tactical Nuke in " + ChatColor.GREEN + "" + ChatColor.MAGIC + "III" + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + " 2 " + ChatColor.GREEN + "" + ChatColor.MAGIC + "III" );
			}
		}, 80L);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage(ChatColor.DARK_RED + ""  + ChatColor.BOLD + "Tactical Nuke in " + ChatColor.GREEN + "" + ChatColor.MAGIC + "III" + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + " 1 " + ChatColor.GREEN + "" + ChatColor.MAGIC + "III" );
			}
		}, 100L);
		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				for(Player enemy : Bukkit.getOnlinePlayers()){
					if(enemy.getName() != player.getName()){
						enemy.setHealth(0);
					}
				}
			}
		}, 120L);
	}

	private void BuyPerk(Player player){
		Inventory inventory = Bukkit.createInventory(player, 9, "Purchase a perk!");
		ItemStack item;
		ItemMeta meta;
		String[] lore;

		item = new ItemStack(Material.TNT, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "TNT");
		if(tnt.containsKey(player.getName())){
			if(tnt.get(player.getName())){
				lore = new String[]{ ChatColor.DARK_RED + "TNT detonates instantly", ChatColor.GREEN + "Enabled"};
			}else{
				lore = new String[]{ ChatColor.DARK_RED + "TNT detonates instantly", ChatColor.RED + "Disabled"};
			}
		}else{
			lore = new String[]{ ChatColor.DARK_RED + "TNT detonates instantly", ChatColor.GREEN + "Cost: 2500 coins"};
		}
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.POISONOUS_POTATO, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Tactical Mask");
		if(tacticalMask.containsKey(player.getName())){
			if(tacticalMask.get(player.getName())){
				lore = new String[]{ ChatColor.DARK_GREEN + "Reduced flash and stun time", ChatColor.GREEN + "Enabled"};
			}else{
				lore = new String[]{ ChatColor.DARK_GREEN + "Reduced flash and stun time", ChatColor.RED + "Disabled"};
			}
		}else{
			lore = new String[]{ ChatColor.DARK_GREEN + "Reduced flash and stun time", ChatColor.GREEN + "Cost: 2500 coins"};
		}
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.MONSTER_EGG, 1);
		item.setDurability((short) 50);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED + "Martyrdom");
		if(martyrdom.containsKey(player.getName())){
			if(martyrdom.get(player.getName())){
				lore = new String[]{ ChatColor.RED + "Explode upon death", ChatColor.GREEN + "Enabled"};
			}else{
				lore = new String[]{ ChatColor.RED + "Explode upon death", ChatColor.RED + "Disabled"};
			}
		}else{
			lore = new String[]{ ChatColor.RED + "Explode upon death", ChatColor.GREEN + "Cost: 5000 coins"};
		}
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.MUSHROOM_SOUP, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_BLUE + "Scavenger");
		if(scavenger.containsKey(player.getName())){
			if(scavenger.get(player.getName())){
				lore = new String[]{ ChatColor.BLUE + "Receive soup for every kill", ChatColor.GREEN + "Enabled"};
			}else{
				lore = new String[]{ ChatColor.BLUE + "Receive soup for every kill", ChatColor.RED + "Disabled"};
			}
		}else{
			lore = new String[]{ ChatColor.BLUE + "Receive soup for every kill", ChatColor.GREEN + "Cost: 5000 coins"};
		}
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.SUGAR, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Rush");
		if(rush.containsKey(player.getName())){
			if(rush.get(player.getName())){
				lore = new String[]{ ChatColor.DARK_AQUA + "Increased health and speed after respawning", ChatColor.GREEN + "Enabled"};
			}else{
				lore = new String[]{ ChatColor.DARK_AQUA + "Increased health and speed after respawning", ChatColor.RED + "Disabled"};
			}
		}else{
			lore = new String[]{ ChatColor.DARK_AQUA + "Increased health and speed after respawning", ChatColor.GREEN + "Cost: 5000 coins"};
		}
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.FEATHER, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "Commando");
		if(commando.containsKey(player.getName())){
			if(commando.get(player.getName())){
				lore = new String[]{ ChatColor.YELLOW + "No fall damage", ChatColor.GREEN + "Enabled"};
			}else{
				lore = new String[]{ ChatColor.YELLOW + "No fall damage", ChatColor.RED + "Disabled"};
			}
		}else{
			lore = new String[]{ ChatColor.YELLOW + "No fall damage", ChatColor.GREEN + "Cost: 5000 coins"};
		}
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.GOLD_INGOT, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "Hardline");
		if(hardline.containsKey(player.getName())){
			if(hardline.get(player.getName())){
				lore = new String[]{ ChatColor.WHITE + "Earn KillStreaks faster", ChatColor.GREEN + "Enabled"};
			}else{
				lore = new String[]{ ChatColor.WHITE + "Earn KillStreaks faster", ChatColor.RED + "Disabled"};
			}
		}else{
			lore = new String[]{ ChatColor.WHITE + "Earn KillStreaks faster", ChatColor.GREEN + "Cost: 5000 coins"};
		}
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.DIAMOND_SWORD, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Punch");
		if(punch.containsKey(player.getName())){
			if(punch.get(player.getName())){
				lore = new String[]{ ChatColor.BLUE + "Increased damage", ChatColor.GREEN + "Enabled"};
			}else{
				lore = new String[]{ ChatColor.BLUE + "Increased damage", ChatColor.RED + "Disabled"};
			}
		}else{
			lore = new String[]{ ChatColor.BLUE + "Increased damage", ChatColor.GREEN + "Cost: 10 000 coins"};
		}
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED + "Juggernaut");
		if(juggernaut.containsKey(player.getName())){
			if(juggernaut.get(player.getName())){
				lore = new String[]{ ChatColor.RED + "Reduced damage taken", ChatColor.GREEN + "Enabled"};
			}else{
				lore = new String[]{ ChatColor.RED + "Reduced damage taken", ChatColor.RED + "Disabled"};
			}
		}
		else{
			lore = new String[]{ ChatColor.RED + "Reduced damage taken", ChatColor.GREEN + "Cost: 10 000 coins"};
		}
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		player.openInventory(inventory);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();

		killstreak.put(player.getName(), 0);

		try{
			PreparedStatement check = conn.prepareStatement("SELECT * FROM playerperks WHERE player = ?;");
			check.setString(1, player.getName());
			ResultSet set = check.executeQuery();

			if (set.next()){
				if(set.getInt("tnt") == 1){
					tnt.put(player.getName(), true);
				}else if(set.getInt("tnt") == 2){
					juggernaut.put(player.getName(), false);
				}

				if(set.getInt("tacticalmask") == 1){
					tacticalMask.put(player.getName(), true);
				}else if(set.getInt("tacticalmask") == 2){
					juggernaut.put(player.getName(), false);
				}

				if(set.getInt("martyrdom") == 1){
					martyrdom.put(player.getName(), true);
				}else if(set.getInt("martyrdom") == 2){
					juggernaut.put(player.getName(), false);
				}

				if(set.getInt("scavenger") == 1){
					scavenger.put(player.getName(), true);
				}else if(set.getInt("scavenger") == 2){
					juggernaut.put(player.getName(), false);
				}

				if(set.getInt("rush") == 1){
					rush.put(player.getName(), true);
				}else if(set.getInt("rush") == 2){
					juggernaut.put(player.getName(), false);
				}

				if(set.getInt("commando") == 1){
					commando.put(player.getName(), true);
				}else if(set.getInt("commando") == 2){
					juggernaut.put(player.getName(), false);
				}

				if(set.getInt("hardline") == 1){
					hardline.put(player.getName(), true);
				}else if(set.getInt("hardline") == 2){
					juggernaut.put(player.getName(), false);
				}

				if(set.getInt("punch") == 1){
					punch.put(player.getName(), true);
				}else if(set.getInt("punch") == 2){
					juggernaut.put(player.getName(), false);
				}

				if(set.getInt("juggernaut") == 1){
					juggernaut.put(player.getName(), true);
				}else if(set.getInt("juggernaut") == 2){
					juggernaut.put(player.getName(), false);
				}

				set.close();
			}else{
				PreparedStatement insert;
				insert = conn.prepareStatement("INSERT INTO playerperks (player, tnt, tacticalmask, martyrdom, scavenger, rush, commando, hardline, punch, juggernaut)" +
						" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
				insert.setString(1, player.getName());
				insert.setInt(2, 0);
				insert.setInt(3, 0);
				insert.setInt(4, 0);
				insert.setInt(5, 0);
				insert.setInt(6, 0);
				insert.setInt(7, 0);
				insert.setInt(8, 0);
				insert.setInt(9, 0);
				insert.setInt(10, 0);
				insert.execute();
			}
		}
		catch(SQLException e){
			print(Level.SEVERE, "MySQL Error occured with player " + player.getName());
		}
	}

	@EventHandler
	public void playerInventoryClick(InventoryClickEvent event){
		if(event.isCancelled())
			return;

		HumanEntity entity = event.getWhoClicked();

		if(event.getInventory().getName().equalsIgnoreCase("Purchase a perk!") && entity instanceof Player){
			Player player = (Player)entity;

			if(event.getCurrentItem().getType() == Material.TNT){
				if(tnt.containsKey(player.getName())){
					if(tnt.get(player.getName())){
						player.sendMessage(Tag() + ChatColor.RED + " Perk TNT has been disabled!");
						tnt.put(player.getName(), false);
						updatePerk(player.getName(), "tnt", 2);
					}else{
						player.sendMessage(Tag() + ChatColor.GREEN + " Perk TNT has been enabled!");
						tnt.put(player.getName(), true);
						updatePerk(player.getName(), "tnt", 1);
					}
				}else if(econ.getBalance(player.getName()) >= 2500){
					updatePerk(player.getName(), "flak", 1);
					econ.withdrawPlayer(player.getName(), 2500);
					tnt.put(player.getName(), true);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "TNT" + ChatColor.AQUA + " has been successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough money to purchase that perk. Play more to earn money!");
				}
			}else if(event.getCurrentItem().getType() == Material.POISONOUS_POTATO){
				if(tacticalMask.containsKey(player.getName())){
					if(tacticalMask.get(player.getName())){
						player.sendMessage(Tag() + ChatColor.RED + " Perk Tactical Mask has been disabled!");
						tacticalMask.put(player.getName(), false);
						updatePerk(player.getName(), "tacticalmask", 2);
					}else{
						player.sendMessage(Tag() + ChatColor.GREEN + " Perk Tactical Mask has been enabled!");
						tacticalMask.put(player.getName(), true);
						updatePerk(player.getName(), "tacticalmask", 1);
					}
				}else if(econ.getBalance(player.getName()) >= 2500){
					updatePerk(player.getName(), "tacticalmask", 1);
					econ.withdrawPlayer(player.getName(), 2500);
					tacticalMask.put(player.getName(), true);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Tactical Mask" + ChatColor.AQUA + " has been successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough money to purchase that perk. Play more to earn money!");
				}
			}else if(event.getCurrentItem().getType() == Material.MONSTER_EGG){
				if(martyrdom.containsKey(player.getName())){
					if(martyrdom.get(player.getName())){
						player.sendMessage(Tag() + ChatColor.RED + " Perk Martyrdom has been disabled!");
						martyrdom.put(player.getName(), false);
						updatePerk(player.getName(), "martyrdom", 2);
					}else{
						player.sendMessage(Tag() + ChatColor.GREEN + " Perk Martyrdom has been enabled!");
						martyrdom.put(player.getName(), true);
						updatePerk(player.getName(), "martyrdom", 1);
					}
				}else if(econ.getBalance(player.getName()) >= 5000){
					updatePerk(player.getName(), "martyrdom", 1);
					econ.withdrawPlayer(player.getName(), 5000);
					martyrdom.put(player.getName(), true);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Martyrdom" + ChatColor.AQUA + " has been successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough money to purchase that perk. Play more to earn money!");
				}
			}else if(event.getCurrentItem().getType() == Material.MUSHROOM_SOUP){
				if(scavenger.containsKey(player.getName())){
					if(scavenger.get(player.getName())){
						player.sendMessage(Tag() + ChatColor.RED + " Perk Scavenger has been disabled!");
						scavenger.put(player.getName(), false);
						updatePerk(player.getName(), "scavenger", 2);
					}else{
						player.sendMessage(Tag() + ChatColor.GREEN + " Perk Scavenger has been enabled!");
						scavenger.put(player.getName(), true);
						updatePerk(player.getName(), "scavenger", 1);
					}
				}else if(econ.getBalance(player.getName()) >= 5000){
					updatePerk(player.getName(), "scavenger", 1);
					econ.withdrawPlayer(player.getName(), 5000);
					scavenger.put(player.getName(), true);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Scavenger" + ChatColor.AQUA + " has been successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough money to purchase that perk. Play more to earn money!");
				}
			}else if(event.getCurrentItem().getType() == Material.SUGAR){
				if(rush.containsKey(player.getName())){
					if(rush.get(player.getName())){
						player.sendMessage(Tag() + ChatColor.RED + " Perk Rush has been disabled!");
						rush.put(player.getName(), false);
						updatePerk(player.getName(), "rush", 2);
					}else{
						player.sendMessage(Tag() + ChatColor.GREEN + " Perk Rush has been enabled!");
						rush.put(player.getName(), true);
						updatePerk(player.getName(), "rush", 1);
					}
				}else if(econ.getBalance(player.getName()) >= 5000){
					updatePerk(player.getName(), "rush", 1);
					econ.withdrawPlayer(player.getName(), 5000);
					rush.put(player.getName(), true);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Rush" + ChatColor.AQUA + " has been successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough money to purchase that perk. Play more to earn money!");
				}
			}else if(event.getCurrentItem().getType() == Material.FEATHER){
				if(commando.containsKey(player.getName())){
					if(commando.get(player.getName())){
						player.sendMessage(Tag() + ChatColor.RED + " Perk Commando has been disabled!");
						commando.put(player.getName(), false);
						updatePerk(player.getName(), "commando", 2);
					}else{
						player.sendMessage(Tag() + ChatColor.GREEN + " Perk Commando has been enabled!");
						commando.put(player.getName(), true);
						updatePerk(player.getName(), "commando", 1);
					}
				}else if(econ.getBalance(player.getName()) >= 5000){
					updatePerk(player.getName(), "commando", 1);
					econ.withdrawPlayer(player.getName(), 5000);
					commando.put(player.getName(), true);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Commando" + ChatColor.AQUA + " has been successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough money to purchase that perk. Play more to earn money!");
				}
			}else if(event.getCurrentItem().getType() == Material.GOLD_INGOT){
				if(hardline.containsKey(player.getName())){
					if(hardline.get(player.getName())){
						player.sendMessage(Tag() + ChatColor.RED + " Perk Hardline has been disabled!");
						rush.put(player.getName(), false);
						updatePerk(player.getName(), "hardline", 2);
					}else{
						player.sendMessage(Tag() + ChatColor.GREEN + " Perk Hardline has been enabled!");
						hardline.put(player.getName(), true);
						updatePerk(player.getName(), "hardline", 1);
					}
				}else if(econ.getBalance(player.getName()) >= 5000){
					updatePerk(player.getName(), "hardline", 1);
					econ.withdrawPlayer(player.getName(), 5000);
					hardline.put(player.getName(), true);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Hardline" + ChatColor.AQUA + " has been successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough money to purchase that perk. Play more to earn money!");
				}
			}else if(event.getCurrentItem().getType() == Material.DIAMOND_SWORD){
				if(punch.containsKey(player.getName())){
					if(punch.get(player.getName())){
						player.sendMessage(Tag() + ChatColor.RED + " Perk Punch has been disabled!");
						punch.put(player.getName(), false);
						updatePerk(player.getName(), "punch", 2);
					}else{
						player.sendMessage(Tag() + ChatColor.GREEN + " Perk Punch has been enabled!");
						punch.put(player.getName(), true);
						updatePerk(player.getName(), "punch", 1);
					}
				}else if(econ.getBalance(player.getName()) >= 10000){
					updatePerk(player.getName(), "punch", 1);
					econ.withdrawPlayer(player.getName(), 10000);
					punch.put(player.getName(), true);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Punch" + ChatColor.AQUA + " has been successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough money to purchase that perk. Play more to earn money!");
				}
			}else if(event.getCurrentItem().getType() == Material.DIAMOND_CHESTPLATE){
				if(juggernaut.containsKey(player.getName())){
					if(juggernaut.get(player.getName())){
						player.sendMessage(Tag() + ChatColor.RED + " Perk Juggernaut has been disabled!");
						juggernaut.put(player.getName(), false);
						updatePerk(player.getName(), "juggernaut", 2);
					}else{
						player.sendMessage(Tag() + ChatColor.GREEN + " Perk Juggernaut has been enabled!");
						juggernaut.put(player.getName(), true);
						updatePerk(player.getName(), "juggernaut", 1);
					}
				}else if(econ.getBalance(player.getName()) >= 10000){
					updatePerk(player.getName(), "juggernaut", 1);
					econ.withdrawPlayer(player.getName(), 10000);
					juggernaut.put(player.getName(), true);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Juggernaut" + ChatColor.AQUA + " has been successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough money to purchase that perk. Play more to earn money!");
				}
			}

			event.setCancelled(true);
			player.closeInventory();
		}
	}

	private String Tag(){
		return ChatColor.GRAY + "[" + ChatColor.AQUA + "PocketPerk" + ChatColor.GRAY + "]";
	}

	private void updatePerk(String playerName, String perkName, int newValue){
		try{
			PreparedStatement check = conn.prepareStatement("SELECT * FROM playerperks WHERE player = ?;");
			check.setString(1, playerName);
			ResultSet set = check.executeQuery();

			if (set.next()){
				PreparedStatement update = conn.prepareStatement("UPDATE playerperks" +
						" SET " + perkName + " = ? WHERE player = ?;");

				update.setString(2, playerName);
				update.setInt(1, newValue);
				update.execute();
			}

			set.close();
		}catch(SQLException e){
			print(Level.SEVERE, "MySQL error occured when updating perk for player " + playerName);
		}
	}
}

