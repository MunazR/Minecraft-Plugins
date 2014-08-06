package me.PocketIsland.pocketpeaceful;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.FFlag;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.Board;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.factions.entity.UPlayer;

public class Pocketpeaceful extends JavaPlugin
	implements Listener
{
	 public Logger asdf = Logger.getLogger("Minecraft");

	  protected Map<String, Integer> peacefulCount = new HashMap();

	  public void log(String m)
	  {
	    this.asdf.info("[PocketPeaceful] " + m);
	  }

	  public boolean isTooSoon(String tag)
	  {
	    if (getConfig().contains(tag + ".time")) {
	      long then = getConfig().getLong(tag + ".time");
	      long now = System.currentTimeMillis();

	      if (now - then < 86400000L) {
	        return true;
	      }

	      return false;
	    }

	    return false;
	  }

	  public boolean inWarZone(Faction fac)
	  {
	    if (fac.getId().equals("-2")) {
	      return true;
	    }

	    return false;
	  }

	  public void loop()
	  {
	    Bukkit.getServer().getScheduler().runTaskTimer(this, new Runnable()
	    {
	      public void run()
	      {
	        Iterator<String> iter = Pocketpeaceful.this.peacefulCount.keySet().iterator();
	        int seconds;
	        Faction faction;
	        int s;
	        while (iter.hasNext()) {
	          String fTag = (String)iter.next();
	          seconds = ((Integer)Pocketpeaceful.this.peacefulCount.get(fTag)).intValue();
	          Pocketpeaceful.this.peacefulCount.put(fTag, Integer.valueOf(seconds - 1));

	          if (seconds - 1 == 0) {
	            Faction faction1 = FactionColls.get().getForUniverse("default").getByName(fTag);
	        	  //Faction faction1 = Factions.i.getByTag(fTag);
	            faction1.setFlag(FFlag.PEACEFUL, true);
	            Bukkit.broadcastMessage("§6" + fTag + "§e is now §e§lPEACEFUL§e!");
	            Pocketpeaceful.this.peacefulCount.remove(fTag);
	            Pocketpeaceful.this.getConfig().set(fTag + ".time", Long.valueOf(System.currentTimeMillis()));
	            Pocketpeaceful.this.saveConfig();
	          }
	          else {
	            faction = FactionColls.get().getForUniverse("default").getByName(fTag);
	            s = seconds - 1;
	            Pocketpeaceful.this.sendToFactionMembers(faction, "§ePeaceful in §e§l" + s + "s§e...");
	          }
	        }

	        for (Player all : Bukkit.getServer().getOnlinePlayers()) {
	          Faction faction1 = (UPlayer.get(all)).getFaction();
	          if (faction1.getFlag(FFlag.PEACEFUL)) {
	        	  UPlayer fAll = UPlayer.get(all);
	            Faction war = FactionColls.get().getForUniverse("default").getWarzone();
	        	  //Faction war = Factions.i.getWarZone();
	            Faction loc = BoardColls.get().getFactionAt(fAll.getLastStoodAt);
	            // Faction loc = Board.getFactionAt(fAll.getLastStoodAt());

	            if (loc.getId().equalsIgnoreCase(war.getId())) {
	              List<Entity> nearby = all.getNearbyEntities(1.5D, 1.5D, 1.5D);
	              int sent = 0;
	              for (Entity ent : nearby)
	                if ((ent.getType().equals(EntityType.DROPPED_ITEM)) && 
	                  (sent == 0)) {
	                  sent++;
	                  all.sendMessage("§eYou cannot pickup items in a §4warzone§e while §6peaceful§e.");
	                }
	            }
	          }
	        }
	      }
	    }
	    , 20L, 20L);
	  }

	  @EventHandler
	  public void onDrop(PlayerDropItemEvent event) {
	    Player player = event.getPlayer();
	    UPlayer fPlayer = UPlayer.get(player);
	    Faction faction = fPlayer.getFaction();
	    if (faction.getFlag(FFlag.PEACEFUL)) {
	      Faction war = FactionColls.get().getForUniverse("default").getWarzone();
	      Faction loc = Board.getFactionAt(fPlayer.getLastStoodAt());

	      if (loc.getId().equalsIgnoreCase(war.getId())) {
	        event.setCancelled(true);
	        player.sendMessage("§eYou cannot drop items in a §4warzone§e while §6peaceful§e.");
	      }
	    }
	  }

	  @EventHandler
	  public void onPickup(PlayerPickupItemEvent event) {
	    Player player = event.getPlayer();
	    UPlayer fPlayer = UPlayer.get(player);
	    Faction faction = fPlayer.getFaction();
	    if (faction.getFlag(FFlag.PEACEFUL)) {
	      Faction war = FactionColls.get().getForUniverse("default").getWarzone();
	      Faction loc = Board.getFactionAt(fPlayer.getLastStoodAt());

	      if (loc.getId().equalsIgnoreCase(war.getId()))
	        event.setCancelled(true);
	    }
	  }

	  @EventHandler
	  public void onDamage(EntityDamageEvent event)
	  {
	    Entity entity = event.getEntity();
	    if ((entity instanceof Player)) {
	      Player player = (Player)entity;
	      if ((event.getCause().equals(EntityDamageEvent.DamageCause.CONTACT)) || (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE))) {
	        String tag = UPlayer.get(player).getFaction().getName();

	        if (this.peacefulCount.containsKey(tag)) {
	          this.peacefulCount.remove(tag);
	          sendToFactionMembers((UPlayer.get(player)).getFaction(), "§c§lProcess Cancelled!\n§4" + player.getName() + " took PVP related damage!");
	        }
	      }
	    }
	  }

	  @EventHandler
	  public void onMove(PlayerMoveEvent event)
	  {
	    Player player = event.getPlayer();

	    String tag = UPlayer.get(player).getFaction().getName();

	    if (this.peacefulCount.containsKey(tag)) {
	      UPlayer fAdmin = UPlayer.get(player).getFaction().getLeader();
	      if (((UPlayer.get(player).equals(fAdmin)) && 
	        (!(UPlayer.get(player)).isInOwnTerritory()))) {
	        this.peacefulCount.remove(tag);
	        sendToFactionMembers((UPlayer.get(player)).getFaction(), "§c§lProcess Cancelled!\n§4The faction leader left faction territory!");
	      }
	    }
	  }

	  public void sendToFactionMembers(Faction faction, String message)
	  {
	    String tag = faction.getName();

	    for (Player a : Bukkit.getServer().getOnlinePlayers()) {
	      String theirTag = UPlayer.get(a).getFaction().getName();

	      if (tag.equalsIgnoreCase(theirTag))
	        a.sendMessage(message);
	    }
	  }

	  @EventHandler(priority=EventPriority.HIGHEST)
	  public void onCommand(PlayerCommandPreprocessEvent event)
	  {
	    if (event.getMessage().startsWith("/f tag")) {
	      event.getPlayer().sendMessage("§4I asked a fairy if you could do that, it was pissed....");
	      event.setCancelled(true);
	    }
	  }

	  public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	  {
	    if (command.getName().equalsIgnoreCase("peacefulme"))
	    {
	      if ((sender instanceof Player))
	      {
	        Player player = (Player)sender;
	        UPlayer fPlayer = UPlayer.get(player);
	        Faction faction = fPlayer.getFaction();

	        if ((faction.getName().equalsIgnoreCase("Wilderness")) || (faction.getName().equalsIgnoreCase("Factionless"))) {
	          player.sendMessage("§cError! §4You are not in a faction.");
	        }
	        else if (player.hasPermission("peacefulme.use")) {
	          if (faction.getFlag(FFlag.PEACEFUL)) {
	            if (fPlayer.isInOwnTerritory()) {
	              UPlayer fAdmin = faction.getLeader();
	              if (fAdmin.equals(fPlayer)) {
	                if (!isTooSoon(faction.getName())) {
	                  Bukkit.broadcastMessage("§6" + faction.getName() + "§e is no longer §e§lPEACEFUL§e!");
	                  faction.setFlag(FFlag.PEACEFUL, false);
	                  getConfig().set(faction.getName() + ".time", Long.valueOf(System.currentTimeMillis()));
	                  saveConfig();
	                }
	                else {
	                  player.sendMessage("§cError!§4 You must wait 24 hours between state changes!");
	                }
	              }
	              else
	                player.sendMessage("§cError!§4 You must be the owner of the faction to undo peaceful!");
	            }
	            else
	            {
	              player.sendMessage("§cError!§4 You must be in your own territory to use that!");
	            }
	          }
	          else {
	            try {
	              if (fPlayer.isInOwnTerritory()) {
	                UPlayer fAdmin = faction.getLeader();
	                if (fAdmin.equals(fPlayer)) {
	                  if (!isTooSoon(faction.getName())) {
	                    sendToFactionMembers(faction, "§6" + player.getName() + "§e is making§6 " + faction.getName() + "§e peaceful!");
	                    sendToFactionMembers(faction, "§eDo not take any PVP damage for §e§l30 Seconds§e!");
	                    this.peacefulCount.put(faction.getName(), Integer.valueOf(30));
	                  }
	                  else {
	                    player.sendMessage("§cError!§4 You must wait 24 hours between state changes!");
	                  }
	                }
	                else
	                  player.sendMessage("§cError!§4 You must be the owner of the faction to go peaceful!");
	              }
	              else
	              {
	                player.sendMessage("§cError!§4 You must be in your own territory to use that!");
	              }
	            } catch (NullPointerException e) {
	              player.sendMessage("§cError! §4You are not in a faction.");
	            }
	          }
	        }
	        else {
	          player.sendMessage("§cError!§4 You do not have permission for that!");
	        }

	      }
	      else
	      {
	        log("Players only!");
	      }

	      return true;
	    }

	    return false;
	  }

	  public void onDisable()
	  {
	    log("Disabled!");
	  }

	  public void onEnable()
	  {
	    if (!new File(getDataFolder(), "config.yml").exists()) {
	      saveDefaultConfig();
	    }
	    loop();
	    getServer().getPluginManager().registerEvents(this, this);
	    log("Enabled!");
	  }
}
