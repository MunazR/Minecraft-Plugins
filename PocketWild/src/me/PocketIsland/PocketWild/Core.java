package me.PocketIsland.PocketWild;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin implements Listener {
	public final Logger logger = Logger.getLogger("Minecraft");
	public static List<String> playerList;

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

		if (!(new File(getDataFolder(), "config.yml")).exists())
			saveDefaultConfig();

		playerList = getConfig().getStringList("players");
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {

		if ((cmd.getName().equalsIgnoreCase("wild") || cmd.getName()
				.equalsIgnoreCase("wilderness")) && (sender instanceof Player)) {
			Player player = (Player) sender;
			Random rand = new Random();
			Location wild;
			Double x;
			Double z;
			Double y = null;
			Biome biome;

			if (playerList.contains(player.getName()) && !player.isOp()) {
				player.sendMessage(ChatColor.RED
						+ "You may only use that once!");
			} else {
				do {
					x = ((rand.nextDouble() * 10000) - (rand.nextDouble() * 10000));
					z = ((rand.nextDouble() * 10000) - (rand.nextDouble() * 10000));
					biome = player.getWorld().getBiome((int) Math.round(x),
							(int) Math.round(z));
				} while (biome == Biome.OCEAN);

				for (int i = 0; i < 255; i++) {
					wild = new Location(player.getWorld(), x, i, z);
					if (wild.getBlock().getType() != Material.AIR)
						y = (double) (i + 1);
				}

				wild = new Location(player.getWorld(), x, y, z);
				player.teleport(wild);
				player.sendMessage(ChatColor.GREEN
						+ "You have been teleported to a random location!");
				player.sendMessage(ChatColor.GREEN
						+ "Save this location using " + ChatColor.RED
						+ "/sethome");
				player.sendMessage(ChatColor.DARK_GREEN
						+ "You may no longer use the /wild command.");

				playerList.add(player.getName());
				getConfig().set("players", playerList);
				saveConfig();
			}

			return true;
		}

		return false;
	}

}
