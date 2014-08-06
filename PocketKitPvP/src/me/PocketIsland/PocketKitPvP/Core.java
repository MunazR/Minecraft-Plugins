package me.PocketIsland.PocketKitPvP;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Core plugin;

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
	}

	@EventHandler
	public void onPlayerDrops(PlayerDropItemEvent event){
		Item item = event.getItemDrop();
		if (item.getItemStack().containsEnchantment(Enchantment.ARROW_DAMAGE) == true || item.getItemStack().hasItemMeta() == true){
			Player player = event.getPlayer();
			player.sendMessage(ChatColor.RED + "You may not drop that item!");
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDeathDrops(PlayerDeathEvent event){
		List<ItemStack> drops = event.getDrops();
		ItemStack drop;

		for(int i=0; i < drops.size(); i++)
		{
			drop = drops.get(i);

			if(drop.hasItemMeta() == true){
				event.getDrops().remove(i);
				i = (i-1);
			}else if(drop.getType() == Material.MUSHROOM_SOUP || drop.getType() == Material.GOLD_CHESTPLATE || drop.getType() == Material.GOLD_LEGGINGS || drop.getType() == Material.GOLD_HELMET || drop.getType() == Material.GOLD_BOOTS || drop.getType() == Material.GOLD_SWORD || drop.getType() == Material.BOW){
				event.getDrops().remove(i);
				i = (i-1);
			}
		}
	}
}
