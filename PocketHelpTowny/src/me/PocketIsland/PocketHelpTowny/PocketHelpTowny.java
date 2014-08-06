package me.PocketIsland.PocketHelpTowny;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
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
import org.bukkit.plugin.java.JavaPlugin;

public class PocketHelpTowny extends JavaPlugin
implements Listener
{
	public final Logger logger = Logger.getLogger("Minecraft");
	public ItemStack item2 = new ItemStack(Material.WRITTEN_BOOK, 1);

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled!");
	}

	@Override
	public void onEnable() {	
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");

		//Book
		//ItemStack item2 = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta meta2 = (BookMeta) item2.getItemMeta();
		meta2.setTitle(ChatColor.GREEN + "Server Info");
		meta2.setAuthor("PocketIsland");
		String[] book = {((ChatColor.GREEN + "" + ChatColor.BOLD) + "Welcome to" + "\n" + (ChatColor.GOLD + "" + ChatColor.BOLD) + "PocketIsland's" + "\n" + "Server!" + "\n" + (ChatColor.BLACK + "" + ChatColor.STRIKETHROUGH) + "-------------------" + ChatColor.BLUE + "You are currently in the " + ChatColor.RED + "Towny Survival" + ChatColor.BLUE + " server!"+ "\n" + "" + "\n" + ChatColor.BLUE + "In Towny Survival there's no PvP, only Survival. It's based off the " + ChatColor.RED + "Towny" + ChatColor.BLUE + " plugin. Read on to learn more about Towny." + "\n" + "\n" + ChatColor.RED + "Read more ->"), ((ChatColor.BLUE + "" + ChatColor.BOLD) + "Rules" + "\n" + ChatColor.BLUE + "1. Be Respectful" + "\n" + "2. Don't Spam" +"\n" +  "3. Be Ethical" + "\n" + "4. Don't Grief" + "\n" + "5. No scamming!" + "\n" + "6. No hacking or glitching" + "\n" + "7. No advertising" + "\n" + "8. Have fun!" + "\n" + "\n" + ChatColor.RED + "Read more -->"), (ChatColor.RED + "What is Towny?" + "\n" + (ChatColor.BLACK + "" + ChatColor.STRIKETHROUGH) + "-------------------" + ChatColor.BLUE + "In towny there are various towns and nations owned by players. Players can join towns and become a resident. Multiple towns can form a nation. Towns own a portion of land in the world."), (ChatColor.RED + "Becoming a resident" + "\n" + (ChatColor.BLACK + "" + ChatColor.STRIKETHROUGH) + "-------------------" + ChatColor.BLUE + "Join a town to become a resident. As a resident you can own a plot of land in the town. You can build here and eventually sell it. You also have to pay taxes as a resident to live in the town." + "\n" + ChatColor.RED + "Read more -->"), (ChatColor.RED + "Economy" + "\n" + (ChatColor.BLACK + "" + ChatColor.STRIKETHROUGH) + "-------------------" + ChatColor.BLUE + "Earn money by selling items at the shop. You are required to pay taxes when you live in a town. If you don't you'll be kicked. Gather resources in the wilderness." + ChatColor.RED + "\n" + "Read More -->"), (ChatColor.LIGHT_PURPLE + "Donation Info" + "\n" + (ChatColor.BLACK + "" + ChatColor.STRIKETHROUGH) + "-------------------" + ChatColor.BLUE + "Donate to keep the server " + ChatColor.AQUA + "ALIVE" + ChatColor.BLUE + "!" + "\n" + ChatColor.BLUE + "Visit " + ChatColor.AQUA + "www.store.ipocketisland.com" + ChatColor.BLUE + " to donate to the server and purchase ranks!" + "\n" + "\n" + ChatColor.BLUE + "Have fun playing!")};
		meta2.setPages(book);
		item2.setItemMeta(meta2);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().equalsIgnoreCase("help")){
			if ((sender instanceof Player)){
				final Player player = (Player)sender;
				player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------------");
				player.sendMessage(ChatColor.GREEN + "Welcome to " + ChatColor.AQUA + "PocketIsland's Server");
				player.sendMessage(ChatColor.GOLD + "I've placed a book in your inventory to help you!");
				player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------------");
				player.getInventory().addItem(item2);
			}else{
				return false;
			}
		}
		return false;
	}
	
	@EventHandler
		public void onPlayerFirstJoin(PlayerJoinEvent Event){
		Player player = Event.getPlayer();
		
		if (!Event.getPlayer().hasPlayedBefore()){
			player.getInventory().addItem(item2);
		}
	}

}
