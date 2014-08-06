package me.PocketIsland.PocketHelpPvP;

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


public class PocketHelpPvP extends JavaPlugin
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
		String[] book = {((ChatColor.GREEN + "" + ChatColor.BOLD) + "Welcome to" + "\n" + (ChatColor.GOLD + "" + ChatColor.BOLD) + "PocketIsland's" + "\n" + "Server!" + "\n" + (ChatColor.BLACK + "" + ChatColor.STRIKETHROUGH) + "-------------------" + ChatColor.BLUE + "You are currently in the " + ChatColor.RED + "Survival PvP" + ChatColor.BLUE + " server!"+ "\n" + "" + "\n" + ChatColor.BLUE + "Survival PvP is based upon " + ChatColor.RED + "Factions, mcMMO & Economy." + "\n" + "\n" + ChatColor.RED + "Read more ->"), ((ChatColor.BLUE + "" + ChatColor.BOLD) + "Rules" + "\n" + ChatColor.BLUE + "1. Be Respectful" + "\n" + "2. Don't Spam" +"\n" +  "3. Be Ethical" + "\n" + "4. Don't Grief" + "\n" + "5. No scamming!" + "\n" + "6. No hacking or glitching" + "\n" + "7. No advertising" + "\n" + "8. Have fun!" + "\n" + "\n" + ChatColor.RED + "Read more -->"), (ChatColor.RED + "FACTIONS" + "\n" + (ChatColor.BLACK + "" + ChatColor.STRIKETHROUGH) + "-------------------" + ChatColor.BLUE + "Create a faction or join one. faction can claim land to protect it. You can invite players to your faction to increase the amount of land you can claim. Gain power by killing enemies. Forge alliances and declare enemies."), (ChatColor.RED + "mcMMO" + "\n" + (ChatColor.BLACK + "" + ChatColor.STRIKETHROUGH) + "-------------------" + ChatColor.BLUE + "mcMMO allows you to level up skills such as mining, swords, axes and more! Leveling up your skills enchances your ability to PvP and PvE. To use mcMMO abilities such as bleeding and berserker, right-click with the weapon or tool in-hand. To level up skills just keep using them!" + "/n" + ChatColor.RED + "Read more -->"), (ChatColor.RED + "Economy" + "\n" + (ChatColor.BLACK + "" + ChatColor.STRIKETHROUGH) + "-------------------" + ChatColor.BLUE + "Grow your balance and become rich! Sell and buy items at the player shop. Earn money by creating farms, mining and more. Use money to purchase armor, echantments, horses and more!" + ChatColor.RED + "\n" + "Read More -->"), (ChatColor.RED + "Important Commands" + "\n" + (ChatColor.BLACK + "" + ChatColor.STRIKETHROUGH) + "-------------------" + ChatColor.BLUE + "/f help - Faction help" + "\n" + "/warp survival - Enter the survival world" + "\n" + "/warp shop - Visit the shop" + "\n" + "/spawn - Return to spawn" + "\n" + "/bal - Check your money" + "\n" + "\n" + ChatColor.RED + "Read more -->"),(ChatColor.LIGHT_PURPLE + "Donation Info" + "\n" + (ChatColor.BLACK + "" + ChatColor.STRIKETHROUGH) + "-------------------" + ChatColor.BLUE + "Donate to keep the server " + ChatColor.AQUA + "ALIVE" + ChatColor.BLUE + "!" + "\n" + ChatColor.BLUE + "Visit " + ChatColor.AQUA + "www.store.ipocketisland.com" + ChatColor.BLUE + " to donate to the server and purchase ranks!" + "\n" + "\n" + ChatColor.BLUE + "Have fun playing!")};
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
