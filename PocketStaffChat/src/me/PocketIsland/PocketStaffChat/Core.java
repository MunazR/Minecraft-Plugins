package me.PocketIsland.PocketStaffChat;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin{
	public final Logger logger = Logger.getLogger("Minecraft");

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled!");
	}

	public void onEnable() {	
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");
	}


	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {

		if(cmd.getName().equalsIgnoreCase("staff") || cmd.getName().equalsIgnoreCase("s")){
			if(sender.hasPermission("chat.staff") || sender.isOp()
					){

				String msg = (ChatColor.BOLD + "" + ChatColor.DARK_GREEN) + "[" + (ChatColor.BOLD + "" + ChatColor.GREEN) + "STAFF" + (ChatColor.BOLD + "" + ChatColor.DARK_GREEN) + "] " + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + "> ";

				for(int x = 0; x < args.length; x++)
					msg += args[x] + " ";

				msg.replaceAll("&", "§");

				for(Player member : Bukkit.getOnlinePlayers())
					if(member.hasPermission("chat.staff"))
						member.sendMessage(msg);

				return true;
			}
		}else{
			sender.sendMessage(ChatColor.DARK_RED + "You must be a staff member to use that!");
			return true;
		}
		return false;
	}
}
