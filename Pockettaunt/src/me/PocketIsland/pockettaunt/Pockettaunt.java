package me.PocketIsland.pockettaunt;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;


public class Pockettaunt extends JavaPlugin{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Pockettaunt plugin;

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
		if(cmd.getName().equalsIgnoreCase("taunt")){
			if (args.length == 0){
				ArrayList<String> list = new ArrayList<String>();

				Location source = player.getLocation();


				for (Player pl : getServer().getOnlinePlayers()){
					if (pl.getLocation().distance(source) < 25){
						list.add(pl.getName());
					}
				}

				Player target;
				for (String p : list){
					target = Bukkit.getServer().getPlayer(p);
					if (target.isOnline()){
						target.sendMessage(ChatColor.DARK_GREEN + "");
						target.sendMessage(ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ ");
						target.sendMessage(ChatColor.GREEN + "");
						target.sendMessage(ChatColor.BOLD + player.getName() + ChatColor.GREEN + " TAUNTS YOU!");
						target.sendMessage(ChatColor.DARK_GREEN + "");
						target.sendMessage(ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~  " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ ");
						target.sendMessage(ChatColor.DARK_GREEN + "");
					}
				}
			}else if (args.length == 1){

				player.sendMessage(ChatColor.DARK_GREEN + "");
				player.sendMessage(ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ ");
				player.sendMessage(ChatColor.GREEN + "");
				player.sendMessage(ChatColor.BOLD + player.getName() + ChatColor.GREEN + " TAUNTS YOU!");
				player.sendMessage(ChatColor.DARK_GREEN + "");
				player.sendMessage(ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~  " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ ");
				player.sendMessage(ChatColor.DARK_GREEN + "");

				Player target = Bukkit.getServer().getPlayer(args[0]);
				if (target.isOnline()){
					target.sendMessage(ChatColor.DARK_GREEN + "");
					target.sendMessage(ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ ");
					target.sendMessage(ChatColor.GREEN + "");
					target.sendMessage(ChatColor.BOLD + player.getName() + ChatColor.GREEN + " TAUNTS YOU!");
					target.sendMessage(ChatColor.DARK_GREEN + "");
					target.sendMessage(ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~  " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ " + ChatColor.DARK_GREEN + "~ " + ChatColor.GREEN + "~ ");
					target.sendMessage(ChatColor.DARK_GREEN + "");
				}
			}
			return true;

		}
		return false;
	}
}
