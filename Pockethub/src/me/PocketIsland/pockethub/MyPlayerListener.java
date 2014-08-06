package me.PocketIsland.pockethub;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import me.confuser.barapi.BarAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MyPlayerListener implements Listener {

	private final Pockethub p;

	public MyPlayerListener(Pockethub p) {
		this.p = p;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) throws IOException {

		Player player = event.getPlayer();
		player.setAllowFlight(true);
		player.setHealth(20);
		player.setFoodLevel(20);
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 156740,
				2));
		player.getInventory().clear();
		Location spawn = new Location(player.getWorld(), 3.577, 6, 251.7, 0, 0);
		player.getLocation().setPitch(0);
		player.getLocation().setYaw(180);
		player.teleport(spawn);

		ItemStack item = new ItemStack(Material.COMPASS, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.LIGHT_PURPLE + " " + ChatColor.MAGIC
				+ "---" + ChatColor.RESET + " " + ChatColor.GREEN
				+ ChatColor.BOLD + "POCKET" + ChatColor.RED + ChatColor.BOLD
				+ "WARP " + ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "---");
		String[] lore = { ChatColor.GREEN
				+ "Right click to quickly connect to where you want to go!" };
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);

		ItemStack item2 = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta meta2 = (BookMeta) item2.getItemMeta();
		meta2.setTitle(ChatColor.GREEN + "Server Info");
		meta2.setAuthor("PocketIsland");
		String[] book = {
				ChatColor.GREEN + " " + ChatColor.BOLD + "Welcome to" + "\n"
						+ ChatColor.GOLD + ChatColor.BOLD + "MineJam"
						+ "\n" + "Server!" + "\n" + ChatColor.BLACK
						+ ChatColor.STRIKETHROUGH + "-------------------"
						+ ChatColor.RED
						+ "Join a server by using your handy dandy compass!"
						+ "\n" + "\n" + ChatColor.DARK_GREEN + "Survival PvP"
						+ "\n" + ChatColor.GOLD + "Towny Survival" + "\n"
						+ ChatColor.DARK_RED + "Kit PvP" + "\n"
						+ ChatColor.DARK_RED + "Hunger Games" + "\n"
						+ ChatColor.DARK_PURPLE + "Creative" + "\n"
						+ ChatColor.AQUA + "Sky Block" + "\n" + ChatColor.RED
						+ "TF2",
				ChatColor.GREEN + " " + ChatColor.BOLD + "Rules" + "\n"
						+ ChatColor.BLUE + "1. Be Respectful" + "\n"
						+ "2. Don't Spam" + "\n" + "3. Be Ethical" + "\n"
						+ "4. Don't Grief" + "\n" + "5. Have Fun!",
				ChatColor.GOLD + "Keep the server " + ChatColor.GREEN
						+ "ALIVE!" + "\n" + ChatColor.RED
						+ "Donate by visiting " + "\n" + ChatColor.BLUE
						+ "store.minejam.com" };
		meta2.setPages(book);
		item2.setItemMeta(meta2);

		ItemStack item3 = new ItemStack(Material.GOLD_INGOT, 1);
		ItemMeta meta3 = item3.getItemMeta();
		meta3.setDisplayName(ChatColor.LIGHT_PURPLE + " " + ChatColor.MAGIC
				+ "---" + ChatColor.RESET + " " + ChatColor.AQUA
				+ ChatColor.BOLD + "Donate " + ChatColor.LIGHT_PURPLE
				+ ChatColor.MAGIC + "---");
		String[] lore1 = { ChatColor.GREEN + "Right click to donate!" };
		meta3.setLore(Arrays.asList(lore1));
		item3.setItemMeta(meta3);

		ItemStack item4 = new ItemStack(Material.PAPER, 1);
		ItemMeta meta4 = item4.getItemMeta();
		meta4.setDisplayName(ChatColor.LIGHT_PURPLE + " " + ChatColor.MAGIC
				+ "---" + ChatColor.RESET + " " + ChatColor.BLUE
				+ ChatColor.BOLD + "Vote " + ChatColor.LIGHT_PURPLE
				+ ChatColor.MAGIC + "---");
		String[] lore2 = { ChatColor.GREEN + "Right click to vote!" };
		meta4.setLore(Arrays.asList(lore2));
		item4.setItemMeta(meta4);

		// Compass
		player.getInventory().addItem(item);
		// Book
		player.getInventory().addItem(item2);
		// Gold Ingot
		player.getInventory().addItem(item3);
		// Vote - Paper
		player.getInventory().addItem(item4);
		
		BarAPI.setMessage(player, ChatColor.GREEN + "You are playing on " + ChatColor.BOLD + "" + ChatColor.RED + "MineJam" + ChatColor.DARK_RED + ".com", 100);
		player.sendMessage(ChatColor.GREEN + "Don't forget to vote daily using " + ChatColor.BLUE + "/vote");
	}

	@EventHandler
	public void onPlayerCompassInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (player.getItemInHand().getType() == Material.COMPASS) {
			Inventory inventory = Bukkit.createInventory(player, 9,
					"Pocket Warp - Pick One!");

			// Diamond Sword - Survival PvP
			ItemStack item = new ItemStack(Material.DIAMOND_SWORD, 1);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName((ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC)
					+ "---" + ChatColor.RESET + " " + ChatColor.GREEN + ""
					+ ChatColor.BOLD + "Survival" + ChatColor.RED + " "
					+ ChatColor.BOLD + "PvP "
					+ (ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC) + "---");
			String[] lore0 = { "Spend hours playing Factions" + ChatColor.GREEN
					+ " PvP" };
			meta.setLore(Arrays.asList(lore0));
			item.setItemMeta(meta);
			inventory.addItem(item);

			// Diamond Axe - Hardcore Factions
			ItemStack item7 = new ItemStack(Material.DIAMOND_AXE, 1);
			ItemMeta meta7 = item7.getItemMeta();
			meta7.setDisplayName((ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC)
					+ "---"
					+ ChatColor.RESET
					+ " "
					+ ChatColor.GREEN
					+ ""
					+ ChatColor.BOLD
					+ "Hardcore"
					+ ChatColor.RED
					+ " "
					+ ChatColor.BOLD
					+ "Factions "
					+ (ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC) + "---");
			String[] lore7 = { "Raid and conquer in" + ChatColor.GREEN
					+ " Hardcore Factions" };
			meta7.setLore(Arrays.asList(lore7));
			item7.setItemMeta(meta7);
			inventory.addItem(item7);

			// Gold Sword - Kit PvP
			item = new ItemStack(Material.GOLD_SWORD, 1);
			meta.setDisplayName((ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC)
					+ "---" + ChatColor.RESET + " " + ChatColor.BLUE + ""
					+ ChatColor.BOLD + "Kit " + ChatColor.RED + ""
					+ ChatColor.BOLD + "PvP "
					+ (ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC) + "---");
			String[] lore6 = { "Become a trained killer in" + ChatColor.GREEN
					+ " Kit PvP" };
			meta.setLore(Arrays.asList(lore6));
			item.setItemMeta(meta);
			inventory.addItem(item);
			
			// Bow - Hunger Games
			item = new ItemStack(Material.BOW, 1);
			meta.setDisplayName((ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC)
					+ "---" + ChatColor.RESET + " " + ChatColor.GREEN + ""
					+ ChatColor.BOLD + "Hunger" + ChatColor.RED + " "
					+ ChatColor.BOLD + "Games "
					+ (ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC) + "---");
			String[] lore1 = { "Get addicted to Minecraft " + ChatColor.GREEN
					+ "Hunger Games" };
			meta.setLore(Arrays.asList(lore1));
			item.setItemMeta(meta);
			inventory.addItem(item);
			
			// Emerald - Sky Wars
			item = new ItemStack(Material.EMERALD, 1);
			meta.setDisplayName((ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC)
					+ "---" + ChatColor.RESET + " " + ChatColor.GREEN + ""
					+ ChatColor.BOLD + "Sky" + ChatColor.RED + " "
					+ ChatColor.BOLD + "Wars "
					+ (ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC) + "---");
			String[] lore8= { "Become a SkyWars " + ChatColor.GREEN
					+ "Master" };
			meta.setLore(Arrays.asList(lore8));
			item.setItemMeta(meta);
			inventory.addItem(item);

			// Grass Block - Sky Block
			item = new ItemStack(Material.GRASS, 1);
			meta.setDisplayName((ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC)
					+ "---" + ChatColor.RESET + " " + ChatColor.BLUE + ""
					+ ChatColor.BOLD + "Sky " + ChatColor.RED + ""
					+ ChatColor.BOLD + "Block "
					+ (ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC) + "---");
			String[] lore4 = { "Build a home in" + ChatColor.GREEN
					+ " Sky Block" };
			meta.setLore(Arrays.asList(lore4));
			item.setItemMeta(meta);
			inventory.addItem(item);

			// Sapling - Creative
			item = new ItemStack(Material.SAPLING, 1);
			meta.setDisplayName((ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC)
					+ "---" + ChatColor.RESET + " " + ChatColor.GREEN + ""
					+ ChatColor.BOLD + "Creative "
					+ (ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC) + "---");
			String[] lore2 = { "Build your dreams in" + ChatColor.GREEN
					+ " Creative" };
			meta.setLore(Arrays.asList(lore2));
			item.setItemMeta(meta);
			inventory.addItem(item);

			// Pickaxe -Towny
			item = new ItemStack(Material.DIAMOND_PICKAXE, 1);
			meta.setDisplayName((ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC)
					+ "---" + ChatColor.RESET + " " + ChatColor.BLUE + ""
					+ ChatColor.BOLD + "Towny " + ChatColor.RED + ""
					+ ChatColor.BOLD + "Survival "
					+ (ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC) + "---");
			String[] lore5 = { "Become a mayor" + ChatColor.GREEN
					+ " Towny Survival" };
			meta.setLore(Arrays.asList(lore5));
			item.setItemMeta(meta);
			inventory.addItem(item);

			player.openInventory(inventory);
		} else if (player.getItemInHand().getType() == Material.GOLD_INGOT) {
			player.sendMessage(ChatColor.DARK_AQUA + ""
					+ ChatColor.STRIKETHROUGH
					+ "----------------------------------------");
			player.sendMessage(ChatColor.GOLD + "Click the link: "
					+ ChatColor.GREEN + "http://store.minejam.com/");
			player.sendMessage(ChatColor.DARK_AQUA + ""
					+ ChatColor.STRIKETHROUGH
					+ "----------------------------------------");
		} else if (player.getItemInHand().getType() == Material.PAPER) {
			player.sendMessage(ChatColor.DARK_AQUA + ""
					+ ChatColor.STRIKETHROUGH
					+ "----------------------------------------");
			player.sendMessage(ChatColor.GOLD + "Click the link: "
					+ ChatColor.BLUE + "http://bit.ly/YMpnW2");
			player.sendMessage(ChatColor.DARK_AQUA + ""
					+ ChatColor.STRIKETHROUGH
					+ "----------------------------------------");
		}
	}

	@EventHandler
	public void onChestClick(InventoryClickEvent event) throws IOException {
		HumanEntity player = event.getWhoClicked();
		if (player instanceof Player) {
			Player sender = (Player) player;

			if (event.getCurrentItem().getType() == Material.DIAMOND_SWORD) {
				sender.sendMessage(ChatColor.GREEN + "Transferring you to "
						+ ChatColor.RED + "Survival PvP");
				sender.sendMessage(ChatColor.GREEN + "Use " + ChatColor.BLUE
						+ "/hub" + ChatColor.GREEN + " to return to the hub");

				ByteArrayOutputStream b = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(b);

				out.writeUTF("Connect");
				out.writeUTF("pvp");

				sender.sendPluginMessage(p, "BungeeCord", b.toByteArray());

				sender.closeInventory();
			} else if (event.getCurrentItem().getType() == Material.BOW) {
				sender.sendMessage(ChatColor.GREEN + "Transferring you to "
						+ ChatColor.RED + "Hunger Games");
				sender.sendMessage(ChatColor.GREEN + "Use " + ChatColor.BLUE
						+ "/hub" + ChatColor.GREEN + " to return to the hub");

				ByteArrayOutputStream b = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(b);

				out.writeUTF("Connect");
				out.writeUTF("hg");

				sender.sendPluginMessage(p, "BungeeCord", b.toByteArray());

				sender.closeInventory();
			} else if (event.getCurrentItem().getType() == Material.SAPLING) {
				sender.sendMessage(ChatColor.GREEN + "Transferring you to "
						+ ChatColor.RED + "Creative");
				sender.sendMessage(ChatColor.GREEN + "Use " + ChatColor.BLUE
						+ "/hub" + ChatColor.GREEN + " to return to the hub");

				ByteArrayOutputStream b = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(b);

				out.writeUTF("Connect");
				out.writeUTF("creative");

				sender.sendPluginMessage(p, "BungeeCord", b.toByteArray());

				sender.closeInventory();
			} else if (event.getCurrentItem().getType() == Material.GRASS) {
				sender.sendMessage(ChatColor.GREEN + "Transferring you to "
						+ ChatColor.RED + "Sky Block");
				sender.sendMessage(ChatColor.GREEN + "Use " + ChatColor.BLUE
						+ "/hub" + ChatColor.GREEN + " to return to the hub");

				ByteArrayOutputStream b = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(b);

				out.writeUTF("Connect");
				out.writeUTF("sb");

				sender.sendPluginMessage(p, "BungeeCord", b.toByteArray());

				sender.closeInventory();
			} else if (event.getCurrentItem().getType() == Material.DIAMOND_PICKAXE) {
				sender.sendMessage(ChatColor.GREEN + "Transferring you to "
						+ ChatColor.RED + "Towny Survival");
				sender.sendMessage(ChatColor.GREEN + "Use " + ChatColor.BLUE
						+ "/hub" + ChatColor.GREEN + " to return to the hub");

				ByteArrayOutputStream b = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(b);

				out.writeUTF("Connect");
				out.writeUTF("towny");

				sender.sendPluginMessage(p, "BungeeCord", b.toByteArray());
			} else if (event.getCurrentItem().getType() == Material.GOLD_SWORD) {
				sender.sendMessage(ChatColor.GREEN + "Transferring you to "
						+ ChatColor.RED + "Kit PvP");
				sender.sendMessage(ChatColor.GREEN + "Use " + ChatColor.BLUE
						+ "/hub" + ChatColor.GREEN + " to return to the hub");

				ByteArrayOutputStream b = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(b);

				out.writeUTF("Connect");
				out.writeUTF("kitpvp");

				sender.sendPluginMessage(p, "BungeeCord", b.toByteArray());

				sender.closeInventory();
			} else if (event.getCurrentItem().getType() == Material.DIAMOND_AXE) {
				sender.sendMessage(ChatColor.GREEN + "Transferring you to "
						+ ChatColor.RED + "Hardcore Factions");
				sender.sendMessage(ChatColor.GREEN + "Use " + ChatColor.BLUE
						+ "/hub" + ChatColor.GREEN + " to return to the hub");

				ByteArrayOutputStream b = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(b);

				out.writeUTF("Connect");
				out.writeUTF("hcf");

				sender.sendPluginMessage(p, "BungeeCord", b.toByteArray());

				sender.closeInventory();
			}else if (event.getCurrentItem().getType() == Material.EMERALD) {
				sender.sendMessage(ChatColor.GREEN + "Transferring you to "
						+ ChatColor.RED + "Sky Wars");
				sender.sendMessage(ChatColor.GREEN + "Use " + ChatColor.BLUE
						+ "/hub" + ChatColor.GREEN + " to return to the hub");

				ByteArrayOutputStream b = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(b);

				out.writeUTF("Connect");
				out.writeUTF("sw");

				sender.sendPluginMessage(p, "BungeeCord", b.toByteArray());

				sender.closeInventory();
			}
		}
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event){
		if(event.isCancelled())
			return;
		
		Player player = event.getPlayer();
		
		if(player.isOp() || player.hasPermission("chat.mod")){
			event.setMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "Staff" + ChatColor.GRAY + "] " + ChatColor.WHITE + event.getMessage());
		}else{
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "Chat is disabled in the hub!");
		}
	}
}
