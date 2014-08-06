package me.PocketIsland.pocketheal;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MyPlayerListener implements Listener {
	private final Pocketheal p;
	public MyPlayerListener(Pocketheal p){
		this.p = p;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		Player player = event.getEntity();
		if (player.getKiller() instanceof Player){
			Player killer = event.getEntity().getKiller();

			player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
			player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + killer.getName() + ChatColor.RESET + " " + ChatColor.RED + "kiled you with " + ChatColor.GREEN + killer.getHealth() + ChatColor.RED + " HP remaining!");
			player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
			killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 3));
			killer.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
			killer.sendMessage(ChatColor.RED + "You have slain " + (ChatColor.DARK_RED + "" + ChatColor.BOLD) + player.getName());
			killer.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
			killer.setFoodLevel(20);
		}
	}
}
