package me.PocketIsland.Core;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	private static ItemStack item;
	
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
		
		item = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta meta = (BookMeta) item.getItemMeta();
		
		meta.setTitle(ChatColor.GREEN + "Hunger Games");
		meta.setAuthor("PocketIsland");
		
		String[] book = {
				ChatColor.GREEN + "Welcome to"
						+ ChatColor.GOLD + ChatColor.BOLD +  " Hunger Games" + "\n" 
						+ ChatColor.BLACK + ChatColor.STRIKETHROUGH + "-------------------"
						+ ChatColor.RED
						+ "Get started by joining an arena!"
						+ "\n\n" + ChatColor.GREEN + "Use " + ChatColor.BLUE + "/hub " + ChatColor.GREEN + "to return to the hub at anytime!"
						+ "\n\n\n" + ChatColor.BLACK + "How to Play =>",
				ChatColor.GREEN + " " + ChatColor.BOLD + "How to Play" + "\n"
						+ ChatColor.BLUE + "1. Eliminate all players" + "\n"
						+ "2. Retrieve supplies from chests" + "\n" + "3. Chests restock at 5 min" + "\n"
						+ "4. Deathmatch begins at 4 players" + "\n" + "5. Earn $100 per win"
						+ "\n" + "6. Spend cash to sponsor players."
						+ "\n" + ChatColor.BLACK + "Rules =>",
				ChatColor.GREEN + " " + ChatColor.BOLD + "Rules" + "\n"
						+ ChatColor.BLUE + "1. Be Respectful" + "\n"
						+ "2. Don't Spam" + "\n" + "3. Be Ethical" + "\n"
						+ "4. Don't Grief" + "\n" + "5. Have Fun!",
				ChatColor.GREEN + "" + ChatColor.BOLD + "Extra Info" + "\n"
						+ ChatColor.GREEN + "Donate for kits and in-game perks!"
						+ "\n" + ChatColor.RED + "Visit www.store.ipocketisland.com"
						+ "\n\n" + ChatColor.BLUE + "Vote for in-game cash!"
						+ "\n" + ChatColor.BLUE + "Use " + ChatColor.DARK_GREEN + "/vote" + ChatColor.BLUE + " to vote!"
				};

		meta.setPages(book);
		item.setItemMeta(meta);;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
	
		if(cmd.getName().equalsIgnoreCase("help") && (sender instanceof Player))
		{
			Player player = (Player)sender;
			
			player.sendMessage(ChatColor.RED + "I've placed a guide in your inventory!");
			player.getInventory().addItem(item);
			return true;
		}
			
		return false;
	}
	
	@EventHandler
	public void PlayerJoinServerEvent(PlayerJoinEvent event){
		Location spawn;
		Player player; 
		
		spawn = new Location(Bukkit.getWorld("HG1"), 0, 9, 0);
		player = event.getPlayer();
		
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		
		player.teleport(spawn);
		
		player.getInventory().addItem(item);
	}
}
