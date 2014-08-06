package me.PocketIsland.PocketHubWarp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class PocketHubWarp extends JavaPlugin{
	public final Logger logger = Logger.getLogger("Minecraft");

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled!");
		
		for (Player player : Bukkit.getServer().getOnlinePlayers()){
			player.sendMessage(ChatColor.GREEN + "Connecting you back to the " + ChatColor.AQUA + "hub");
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			try {
				out.writeUTF("Connect");
				out.writeUTF("hub");
			} catch (IOException e) {
			}
			
			player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		}
		
	}

	@Override
	public void onEnable() {	
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().equalsIgnoreCase("hub")){
			if (sender instanceof Player){
				Player player = (Player)sender;
				player.sendMessage(ChatColor.GREEN + "Connecting you back to the " + ChatColor.AQUA + "hub");

				ByteArrayOutputStream b = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(b);

				try {
					out.writeUTF("Connect");
					out.writeUTF("hub");
				} catch (IOException e) {
				}
				
				player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
			}else{
				return false;
			}
		}
		return false;
	}
}
