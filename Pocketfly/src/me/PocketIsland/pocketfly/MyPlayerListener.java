package me.PocketIsland.pocketfly;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;

public class MyPlayerListener implements Listener{
	private final Pocketfly p;
	public MyPlayerListener(Pocketfly p){
		this.p = p;
	}
	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event){
		Entity player1 = event.getEntity();
		Entity player2 = event.getDamager();
		if ((player1 instanceof Player)){
			Player sender1 = (Player)player1;
			if ((player2 instanceof Player)){
				
				Player sender2 = (Player)player2;
				
				if (p.hashmap.containsKey(sender1)){
					p.hashmap.remove(sender1);
					sender1.setAllowFlight(false);
					sender1.setCanPickupItems(true);
					sender1.sendMessage(ChatColor.RED + "You have been attacked by " + ChatColor.BLUE + sender2.getDisplayName() + ChatColor.RED + " and are no longer flying!");

				}else if (p.hashmap.containsKey(sender2)){
					p.hashmap.remove(sender2);
					sender2.setAllowFlight(false);
					sender2.setCanPickupItems(true);
					sender2.sendMessage(ChatColor.RED + "You have been attacked by " + ChatColor.BLUE + sender1.getDisplayName() + ChatColor.RED + " and are no longer flying!");
				
				}
				
			}
		}
	}
	
	@EventHandler
	public void onPlayerBowDamage(EntityShootBowEvent event){
		Entity player1 = event.getEntity();
		if (player1 instanceof Player){
			Player sender1 = (Player)player1;
			
			if (p.hashmap.containsKey(sender1)){
				event.setCancelled(true);
				sender1.sendMessage(ChatColor.RED + "You may not use a bow while flying!");
			}
		}
	}
}
