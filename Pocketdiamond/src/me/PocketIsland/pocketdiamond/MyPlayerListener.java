package me.PocketIsland.pocketdiamond;


import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class MyPlayerListener implements Listener{
	
	private final Pocketdiamond p;
	public MyPlayerListener(Pocketdiamond p){
		this.p = p;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		//boolean found;
		//found = p.getConfig().contains(player.getName()));
		
		if (p.getConfig().contains(player.getName())) {
			player.getInventory().addItem(new ItemStack(Material.DIAMOND, 64));
			p.saveConfig();
		}
	}
}
