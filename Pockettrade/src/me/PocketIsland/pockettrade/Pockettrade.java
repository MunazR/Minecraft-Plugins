package me.PocketIsland.pockettrade;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Pockettrade extends JavaPlugin
 implements Listener
{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Pockettrade plugin;

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled!");
	}

	@Override
	public void onEnable() {	
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		Player player = (Player) sender;
		if(cmd.getName().equalsIgnoreCase("trade")){
			if (args.length == 0){
				player.sendMessage(ChatColor.GREEN + "Pocket Trade - Trade Items with other Players");
				player.sendMessage(ChatColor.GOLD + "To trade with other players use " + ChatColor.BLUE + "/trade invite [player]");
				player.sendMessage(ChatColor.GOLD + "To accept a trade invite use " + ChatColor.BLUE + "/trade accept");
				player.sendMessage(ChatColor.GOLD + "To deny a trade invite use " + ChatColor.BLUE + "/trade deny");
				player.sendMessage(ChatColor.GOLD + "For more information use " + ChatColor.BLUE + "/trade help");
			}
			return true;
		}else{
			return false;
		}
	}
}
