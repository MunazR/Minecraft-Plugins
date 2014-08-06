package me.PocketIsland.SWPerks;

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
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
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

public class Core extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Economy econ = null;
	public Connection conn;

	public HashMap<String, Boolean> tnt;
	public HashMap<String, Boolean> tacticalMask;
	public HashMap<String, Boolean> martyrdom;
	public HashMap<String, Boolean> scavenger;
	public HashMap<String, Boolean> flak;
	public HashMap<String, Boolean> commando;
	public HashMap<String, Boolean> punch;
	public HashMap<String, Boolean> juggernaut;
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
		flak = new HashMap<String, Boolean>();
		commando = new HashMap<String, Boolean>();
		punch = new HashMap<String, Boolean>();
		juggernaut = new HashMap<String, Boolean>();

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
						"SWPerks (player VARCHAR(255), tnt INT, tacticalmask INT, martyrdom INT, scavenger INT, flak INT, commando INT, punch INT, juggernaut INT);");

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
			lore = new String[]{ ChatColor.DARK_RED + "TNT detonates instantly", ChatColor.GREEN + "Cost: 250 coins"};
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
			lore = new String[]{ ChatColor.DARK_GREEN + "Reduced flash and stun time", ChatColor.GREEN + "Cost: 250 coins"};
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
			lore = new String[]{ ChatColor.RED + "Explode upon death", ChatColor.GREEN + "Cost: 500 coins"};
		}
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.ARROW, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_BLUE + "Scavenger");
		if(scavenger.containsKey(player.getName())){
			if(scavenger.get(player.getName())){
				lore = new String[]{ ChatColor.BLUE + "Receive arrows for every kill", ChatColor.GREEN + "Enabled"};
			}else{
				lore = new String[]{ ChatColor.BLUE + "Receive arrows for every kill", ChatColor.RED + "Disabled"};
			}
		}else{
			lore = new String[]{ ChatColor.BLUE + "Receive arrows for every kill", ChatColor.GREEN + "Cost: 500 coins"};
		}
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		item = new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Flak Jacket");
		if(flak.containsKey(player.getName())){
			if(flak.get(player.getName())){
				lore = new String[]{ ChatColor.DARK_AQUA + "Significantly reduced explosive damage", ChatColor.GREEN + "Enabled"};
			}else{
				lore = new String[]{ ChatColor.DARK_AQUA + "Significantly reduced explosive damage", ChatColor.RED + "Disabled"};
			}
		}else{
			lore = new String[]{ ChatColor.DARK_AQUA + "Significantly reduced explosive damage", ChatColor.GREEN + "Cost: 500 coins"};
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
			lore = new String[]{ ChatColor.YELLOW + "No fall damage", ChatColor.GREEN + "Cost: 500 coins"};
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
			lore = new String[]{ ChatColor.BLUE + "Increased damage", ChatColor.GREEN + "Cost: 1000 coins"};
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
			lore = new String[]{ ChatColor.RED + "Reduced damage taken", ChatColor.GREEN + "Cost: 1000 coins"};
		}
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inventory.addItem(item);

		player.openInventory(inventory);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();

		try{
			PreparedStatement check = conn.prepareStatement("SELECT * FROM SWPerks WHERE player = ?;");
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

				if(set.getInt("flak") == 1){
					flak.put(player.getName(), true);
				}else if(set.getInt("flak") == 2){
					juggernaut.put(player.getName(), false);
				}

				if(set.getInt("commando") == 1){
					commando.put(player.getName(), true);
				}else if(set.getInt("commando") == 2){
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
				insert = conn.prepareStatement("INSERT INTO SWPerks (player, tnt, tacticalmask, martyrdom, scavenger, flak, commando, punch, juggernaut)" +
						" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
				insert.setString(1, player.getName());
				insert.setInt(2, 0);
				insert.setInt(3, 0);
				insert.setInt(4, 0);
				insert.setInt(5, 0);
				insert.setInt(6, 0);
				insert.setInt(7, 0);
				insert.setInt(8, 0);
				insert.setInt(9, 0);
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
				}else if(econ.getBalance(player.getName()) >= 250){
					updatePerk(player.getName(), "tnt", 1);
					econ.withdrawPlayer(player.getName(), 250);
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
				}else if(econ.getBalance(player.getName()) >= 250){
					updatePerk(player.getName(), "tacticalmask", 1);
					econ.withdrawPlayer(player.getName(), 250);
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
				}else if(econ.getBalance(player.getName()) >= 500){
					updatePerk(player.getName(), "martyrdom", 1);
					econ.withdrawPlayer(player.getName(), 500);
					martyrdom.put(player.getName(), true);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Martyrdom" + ChatColor.AQUA + " has been successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough money to purchase that perk. Play more to earn money!");
				}
			}else if(event.getCurrentItem().getType() == Material.ARROW){
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
				}else if(econ.getBalance(player.getName()) >= 500){
					updatePerk(player.getName(), "scavenger", 1);
					econ.withdrawPlayer(player.getName(), 500);
					scavenger.put(player.getName(), true);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Scavenger" + ChatColor.AQUA + " has been successfully purchased!");
				}else{
					player.sendMessage(Tag() + ChatColor.RED + " You don't have enough money to purchase that perk. Play more to earn money!");
				}
			}else if(event.getCurrentItem().getType() == Material.CHAINMAIL_CHESTPLATE){
				if(flak.containsKey(player.getName())){
					if(flak.get(player.getName())){
						player.sendMessage(Tag() + ChatColor.RED + " Perk Flak Jacket has been disabled!");
						flak.put(player.getName(), false);
						updatePerk(player.getName(), "flak", 2);
					}else{
						player.sendMessage(Tag() + ChatColor.GREEN + " Perk Flak Jacket has been enabled!");
						flak.put(player.getName(), true);
						updatePerk(player.getName(), "flak", 1);
					}
				}else if(econ.getBalance(player.getName()) >= 500){
					updatePerk(player.getName(), "flak", 1);
					econ.withdrawPlayer(player.getName(), 500);
					flak.put(player.getName(), true);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "flak" + ChatColor.AQUA + " has been successfully purchased!");
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
				}else if(econ.getBalance(player.getName()) >= 500){
					updatePerk(player.getName(), "commando", 1);
					econ.withdrawPlayer(player.getName(), 500);
					commando.put(player.getName(), true);
					player.sendMessage(Tag() + ChatColor.AQUA + " Perk " + ChatColor.GREEN + "Commando" + ChatColor.AQUA + " has been successfully purchased!");
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
				}else if(econ.getBalance(player.getName()) >= 1000){
					updatePerk(player.getName(), "punch", 1);
					econ.withdrawPlayer(player.getName(), 1000);
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
				}else if(econ.getBalance(player.getName()) >= 1000){
					updatePerk(player.getName(), "juggernaut", 1);
					econ.withdrawPlayer(player.getName(), 1000);
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
			PreparedStatement check = conn.prepareStatement("SELECT * FROM SWPerks WHERE player = ?;");
			check.setString(1, playerName);
			ResultSet set = check.executeQuery();

			if (set.next()){
				PreparedStatement update = conn.prepareStatement("UPDATE SWPerks" +
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

