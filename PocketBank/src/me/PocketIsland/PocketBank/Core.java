package me.PocketIsland.PocketBank;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import de.blablubbabc.insigns.SignSendEvent;

public class Core extends JavaPlugin implements Listener {
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Economy econ = null;

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion()
				+ " has been disabled!");
	}

	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion()
				+ " has been enabled!");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);

		if (!setupEconomy()) {
			logger.severe(String.format(
					"[%s] - Disabled due to no Vault dependency found!",
					getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer()
				.getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	@EventHandler
	public void signCreate(SignChangeEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();

		if (block.getState() instanceof Sign) {
			if ((player.hasPermission("pocketbank.create") || player.isOp())) {
				Sign sign = (Sign) block;
				String[] lines = sign.getLines();
				if (lines[0] == "[Bank]") {
					sign.setLine(1, ChatColor.DARK_GREEN + "[Bank]");
					sign.setLine(2, ChatColor.RED + "Your balance will");
					sign.setLine(3, ChatColor.GREEN + "be displayed here");
					sign.update();
				}
			}
		}
	}
	
	@EventHandler
	public void signSend(SignSendEvent event){
		if(event.getLine(0).equalsIgnoreCase("[Bank]"))
		{
			event.setLine(0, ChatColor.GREEN + "[Bank]");
			event.setLine(1, ChatColor.RED + event.getPlayer().getName());
			event.setLine(2, ChatColor.RED + "Your balance is ");
			event.setLine(3, ChatColor.DARK_GREEN + "$" + econ.getBalance(event.getPlayer().getName()));
		}
	}
}
