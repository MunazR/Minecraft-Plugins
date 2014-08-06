package me.PocketIsland.pocketjoin;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UPlayer;

public class Pocketjoin extends JavaPlugin
 implements Listener
{
	 public Logger asdf = Logger.getLogger("Minecraft");

	  public void log(String m) {
	    this.asdf.info("[PocketJoin] " + m);
	  }

	  @EventHandler
	  public void onKick(PlayerKickEvent event) {
	    event.setLeaveMessage(null);
	  }

	  @EventHandler
	  public void onQuit(PlayerQuitEvent event) {
	    Player player = event.getPlayer();
	    UPlayer factionPlayer = UPlayer.get(player);

	    Faction faction = factionPlayer.getFaction();

	    String fName = faction.getName();

	    event.setQuitMessage(null);
	    for (Player all : Bukkit.getServer().getOnlinePlayers()) {
	      UPlayer aPlayer = UPlayer.get(all);
	      Faction aFaction = aPlayer.getFaction();

	      String afName = aFaction.getName();

	      if ((afName.equals(fName)) && (!fName.equalsIgnoreCase("§2Wilderness"))) {
	        all.sendMessage("§6Faction member §e" + player.getName() + "§6 quit the game!");
	      }
	      else {
	        List<String> quits = getConfig().getStringList("quit.list");

	        for (String entry : quits) {
	          String e = entry.toLowerCase();

	          if (player.hasPermission("pocketjoin.quit." + e)) {
	            String join = getConfig().getString("quit." + e);
	            String j1 = join.replace("&", "§");
	            String j2 = j1.replace("%name", player.getName());
	            all.sendMessage(j2);
	            break;
	          }
	        }
	      }
	    }
	  }

	  @EventHandler
	  public void onJoin(PlayerJoinEvent event)
	  {
	    Player player = event.getPlayer();
	    UPlayer factionPlayer = UPlayer.get(player);

	    Faction faction = factionPlayer.getFaction();

	    String fName = faction.getName();

	    event.setJoinMessage(null);
	    for (Player all : Bukkit.getServer().getOnlinePlayers()) {
	      UPlayer aPlayer = UPlayer.get(all);

	      Faction aFaction = aPlayer.getFaction();

	      String afName = aFaction.getName();

	      if ((afName.equals(fName)) && (!fName.equalsIgnoreCase("§2Wilderness"))) {
	        all.sendMessage("§6Faction member §e" + player.getName() + "§6 joined the game!");
	      }
	      else
	      {
	        List<String> joins = getConfig().getStringList("join.list");
	        for (String entry : joins) {
	          String e = entry.toLowerCase();

	          if (player.hasPermission("pocketjoin.join." + e)) {
	            String join = getConfig().getString("join." + e);
	            String j1 = join.replace("&", "§");
	            String j2 = j1.replace("%name", player.getName());
	            all.sendMessage(j2);
	            break;
	          }
	        }
	      }
	    }
	  }

	  public void onEnable()
	  {
	    if (!new File(getDataFolder(), "config.yml").exists()) {
	      saveDefaultConfig();
	    }

	    getServer().getPluginManager().registerEvents(this, this);

	    log("Enabled!");
	  }

	  public void onDisable()
	  {
	    log("Disabled!");
	  }
}
