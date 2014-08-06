package me.PocketIsland.Pocketsg;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyPlayerListener implements Listener {
	private final Pocketsg p;
	public static int deathmatchcount[];
	
	public MyPlayerListener(Pocketsg p){
	this.p = p;
	}

	@EventHandler
	public void onPlayerJoin(com.skitscape.survivalgames.Events.JoinEvent event){
		
	}
}
