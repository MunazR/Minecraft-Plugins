package me.PocketIsland.pockettab;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcsg.double0negative.tabapi.TabAPI;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UPlayer;

public class Pockettab extends JavaPlugin
	implements Listener				
{

	  public Logger asdf = Logger.getLogger("Minecraft");
	  public static Economy econ = null;

	  public void log(String m) {
	    this.asdf.info("[PocketTab] " + m);
	  }

	  public void loop() {
	    Bukkit.getServer().getScheduler().runTaskTimer(this, new Runnable()
	    {
	      public void run()
	      {
	        for (Player p : Bukkit.getServer().getOnlinePlayers())
	          Pockettab.this.buildTabList(p);
	      }
	    }
	    , 20L, 100L);
	  }

	  public void buildTabList(Player p) {
	    List<String> factionPeople = new ArrayList<String>();
	    List<String> staffPeople = new ArrayList<String>();

	    TabAPI.clearTab(p);
	    TabAPI.updatePlayer(p);

	    UPlayer fPlayer = UPlayer.get(p);;
	    Faction faction = fPlayer.getFaction();

	    String tag = faction.getName();
	    Faction f;
	    for (Player all : Bukkit.getServer().getOnlinePlayers()) {
	      if (all.hasPermission("pockettab.admin")) {
	        staffPeople.add("§" + getConfig().getString("admin-color") + all.getName());
	      }
	      else if (all.hasPermission("pockettab.mod")) {
	        staffPeople.add("§" + getConfig().getString("mod-color") + all.getName());
	      }
	      else if (all.getName().equalsIgnoreCase("turqmelon")) {
	        staffPeople.add("§a" + all.getName());
	      }

	      UPlayer fP = UPlayer.get(all);
	      f = fP.getFaction();

	      String otherTag = f.getName();

	      if (otherTag.equalsIgnoreCase(tag)) {
	        factionPeople.add("§e" + all.getName());
	      }
	    }

	    String onlineString = "";
	    int online = Bukkit.getServer().getOnlinePlayers().length;
	    int max = Bukkit.getServer().getMaxPlayers();

	    onlineString = online + "§2/§f" + max;

	    TabAPI.setTabString(this, p, 0, 0, "§b==========" + TabAPI.nextNull());
	    TabAPI.setTabString(this, p, 0, 1, "§3==========" + TabAPI.nextNull());
	    TabAPI.setTabString(this, p, 0, 2, "§b==========" + TabAPI.nextNull());
	    TabAPI.setTabString(this, p, 1, 0, "§a§lPOCKETISLAND" + TabAPI.nextNull());
	    TabAPI.setTabString(this, p, 1, 1, "§2Online Now:" + TabAPI.nextNull());
	    TabAPI.setTabString(this, p, 1, 2, onlineString);
	    TabAPI.setTabString(this, p, 2, 0, "§3==========" + TabAPI.nextNull());
	    TabAPI.setTabString(this, p, 2, 1, "§b==========" + TabAPI.nextNull());
	    TabAPI.setTabString(this, p, 2, 2, "§3==========" + TabAPI.nextNull());
	    TabAPI.setTabString(this, p, 3, 0, "§9" + TabAPI.nextNull());
	    TabAPI.setTabString(this, p, 3, 1, "§0" + TabAPI.nextNull());
	    TabAPI.setTabString(this, p, 3, 2, "§n" + TabAPI.nextNull());
	    TabAPI.setTabString(this, p, 4, 0, "§4§lYour Info");
	    TabAPI.setTabString(this, p, 4, 1, "§4§lStaff On");
	    TabAPI.setTabString(this, p, 4, 2, "§4§lFaction On");
	    TabAPI.setTabString(this, p, 5, 0, "§e§nName");
	    TabAPI.setTabString(this, p, 6, 0, p.getName());
	    TabAPI.setTabString(this, p, 8, 0, "§e§nKills");
	    TabAPI.setTabString(this, p, 9, 0, getConfig().getInt(new StringBuilder("stats.").append(p.getName().toLowerCase()).append(".kills").toString()) + TabAPI.nextNull());
	    TabAPI.setTabString(this, p, 11, 0, "§e§nDeaths");
	    TabAPI.setTabString(this, p, 12, 0, getConfig().getInt(new StringBuilder("stats.").append(p.getName().toLowerCase()).append(".deaths").toString()) + TabAPI.nextNull());

	    if (tag.equalsIgnoreCase("Wilderness")) {
	      tag = "Factionless";
	    }
	    TabAPI.setTabString(this, p, 14, 0, "§e§nFaction");
	    TabAPI.setTabString(this, p, 15, 0, tag);
	    TabAPI.setTabString(this, p, 17, 0, "§e§nBalance");
	    TabAPI.setTabString(this, p, 18, 0, econ.getBalance(p.getName()) + TabAPI.nextNull());

	    int i = 5;
	    StringBuilder build;
	    for (String entry : staffPeople) {
	      build = new StringBuilder();
	      build.append(entry);
	      if (build.length() > 16) {
	        build.setLength(16);
	      }
	      TabAPI.setTabString(this, p, i, 1, build + TabAPI.nextNull());
	      if (i == 18)
	      {
	        break;
	      }
	      i++;
	    }

	    if (i == 5) {
	      TabAPI.setTabString(this, p, 5, 1, "§a " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 6, 1, "§b " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 7, 1, "§c " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 8, 1, "§d " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 9, 1, "§e " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 10, 1, "§f " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 11, 1, "§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 1, "§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 1, "§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 1, "§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 1, "§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 1, "§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 1, "§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 1, "§8 " + TabAPI.nextNull());
	    }
	    else if (i == 6) {
	      TabAPI.setTabString(this, p, 6, 1, "§b " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 7, 1, "§c " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 8, 1, "§d " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 9, 1, "§e " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 10, 1, "§f " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 11, 1, "§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 1, "§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 1, "§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 1, "§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 1, "§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 1, "§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 1, "§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 1, "§8 " + TabAPI.nextNull());
	    }
	    else if (i == 7) {
	      TabAPI.setTabString(this, p, 7, 1, "§c " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 8, 1, "§d " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 9, 1, "§e " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 10, 1, "§f " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 11, 1, "§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 1, "§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 1, "§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 1, "§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 1, "§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 1, "§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 1, "§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 1, "§8 " + TabAPI.nextNull());
	    }
	    else if (i == 8) {
	      TabAPI.setTabString(this, p, 8, 1, "§d " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 9, 1, "§e " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 10, 1, "§f " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 11, 1, "§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 1, "§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 1, "§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 1, "§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 1, "§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 1, "§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 1, "§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 1, "§8 " + TabAPI.nextNull());
	    }
	    else if (i == 9) {
	      TabAPI.setTabString(this, p, 9, 1, "§e " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 10, 1, "§f " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 11, 1, "§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 1, "§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 1, "§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 1, "§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 1, "§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 1, "§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 1, "§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 1, "§8 " + TabAPI.nextNull());
	    }
	    else if (i == 10) {
	      TabAPI.setTabString(this, p, 10, 1, "§f " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 11, 1, "§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 1, "§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 1, "§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 1, "§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 1, "§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 1, "§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 1, "§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 1, "§8 " + TabAPI.nextNull());
	    }
	    else if (i == 11) {
	      TabAPI.setTabString(this, p, 11, 1, "§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 1, "§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 1, "§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 1, "§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 1, "§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 1, "§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 1, "§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 1, "§8 " + TabAPI.nextNull());
	    }
	    else if (i == 12) {
	      TabAPI.setTabString(this, p, 12, 1, "§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 1, "§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 1, "§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 1, "§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 1, "§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 1, "§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 1, "§8 " + TabAPI.nextNull());
	    }
	    else if (i == 13) {
	      TabAPI.setTabString(this, p, 13, 1, "§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 1, "§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 1, "§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 1, "§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 1, "§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 1, "§8 " + TabAPI.nextNull());
	    }
	    else if (i == 14) {
	      TabAPI.setTabString(this, p, 14, 1, "§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 1, "§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 1, "§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 1, "§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 1, "§8 " + TabAPI.nextNull());
	    }
	    else if (i == 15) {
	      TabAPI.setTabString(this, p, 15, 1, "§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 1, "§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 1, "§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 1, "§8 " + TabAPI.nextNull());
	    }
	    else if (i == 16) {
	      TabAPI.setTabString(this, p, 16, 1, "§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 1, "§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 1, "§8 " + TabAPI.nextNull());
	    }
	    else if (i == 17) {
	      TabAPI.setTabString(this, p, 17, 1, " " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 1, " " + TabAPI.nextNull());
	    }
	    else if (i == 18) {
	      TabAPI.setTabString(this, p, 18, 1, " " + TabAPI.nextNull());
	    }

	    int i2 = 5;
	    for (String entry : factionPeople) {
	      StringBuilder build1 = new StringBuilder();
	      build1.append(entry);
	      if (build1.length() > 16) {
	        build1.setLength(16);
	      }
	      TabAPI.setTabString(this, p, i2, 2, build1 + TabAPI.nextNull());
	      if (i2 == 18)
	      {
	        break;
	      }
	      i2++;
	    }

	    if (i2 == 5) {
	      TabAPI.setTabString(this, p, 5, 2, "§m§a " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 6, 2, "§m§b " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 7, 2, "§m§c " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 8, 2, "§m§d " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 9, 2, "§m§e " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 10, 2, "§m§f " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 11, 2, "§m§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 2, "§m§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 2, "§m§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 2, "§m§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 2, "§m§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 2, "§m§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 2, "§m§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 2, "§m§8 " + TabAPI.nextNull());
	    }
	    else if (i2 == 6) {
	      TabAPI.setTabString(this, p, 6, 2, "§m§b " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 7, 2, "§m§c " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 8, 2, "§m§d " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 9, 2, "§m§e " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 10, 2, "§m§f " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 11, 2, "§m§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 2, "§m§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 2, "§m§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 2, "§m§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 2, "§m§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 2, "§m§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 2, "§m§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 2, "§m§8 " + TabAPI.nextNull());
	    }
	    else if (i2 == 7) {
	      TabAPI.setTabString(this, p, 7, 2, "§m§c " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 8, 2, "§m§d " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 9, 2, "§m§e " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 10, 2, "§m§f " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 11, 2, "§m§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 2, "§m§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 2, "§m§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 2, "§m§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 2, "§m§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 2, "§m§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 2, "§m§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 2, "§m§8 " + TabAPI.nextNull());
	    }
	    else if (i2 == 8) {
	      TabAPI.setTabString(this, p, 8, 2, "§m§d " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 9, 2, "§m§e " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 10, 2, "§m§f " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 11, 2, "§m§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 2, "§m§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 2, "§m§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 2, "§m§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 2, "§m§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 2, "§m§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 2, "§m§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 2, "§m§8 " + TabAPI.nextNull());
	    }
	    else if (i2 == 9) {
	      TabAPI.setTabString(this, p, 9, 2, "§m§e " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 10, 2, "§m§f " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 11, 2, "§m§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 2, "§m§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 2, "§m§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 2, "§m§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 2, "§m§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 2, "§m§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 2, "§m§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 2, "§m§8 " + TabAPI.nextNull());
	    }
	    else if (i2 == 10) {
	      TabAPI.setTabString(this, p, 10, 2, "§m§f " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 11, 2, "§m§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 2, "§m§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 2, "§m§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 2, "§m§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 2, "§m§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 2, "§m§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 2, "§m§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 2, "§m§8 " + TabAPI.nextNull());
	    }
	    else if (i2 == 11) {
	      TabAPI.setTabString(this, p, 11, 2, "§m§1 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 12, 2, "§m§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 2, "§m§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 2, "§m§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 2, "§m§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 2, "§m§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 2, "§m§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 2, "§m§8 " + TabAPI.nextNull());
	    }
	    else if (i2 == 12) {
	      TabAPI.setTabString(this, p, 12, 2, "§m§2 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 13, 2, "§m§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 2, "§m§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 2, "§m§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 2, "§m§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 2, "§m§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 2, "§m§8 " + TabAPI.nextNull());
	    }
	    else if (i2 == 13) {
	      TabAPI.setTabString(this, p, 13, 2, "§m§3 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 14, 2, "§m§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 2, "§m§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 2, "§m§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 2, "§m§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 2, "§m§8 " + TabAPI.nextNull());
	    }
	    else if (i2 == 14) {
	      TabAPI.setTabString(this, p, 14, 2, "§m§4 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 15, 2, "§m§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 2, "§m§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 2, "§m§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 2, "§m§8 " + TabAPI.nextNull());
	    }
	    else if (i2 == 15) {
	      TabAPI.setTabString(this, p, 15, 2, "§m§5 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 16, 2, "§m§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 2, "§m§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 2, "§m§8 " + TabAPI.nextNull());
	    }
	    else if (i2 == 16) {
	      TabAPI.setTabString(this, p, 16, 2, "§m§6 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 17, 2, "§m§7 " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 2, "§m§8 " + TabAPI.nextNull());
	    }
	    else if (i2 == 17) {
	      TabAPI.setTabString(this, p, 17, 2, " " + TabAPI.nextNull());
	      TabAPI.setTabString(this, p, 18, 2, " " + TabAPI.nextNull());
	    }
	    else if (i2 == 18) {
	      TabAPI.setTabString(this, p, 18, 2, " " + TabAPI.nextNull());
	    }

	    TabAPI.setTabString(this, p, 19, 0, "§b§lWebsite:");
	    TabAPI.setTabString(this, p, 19, 1, "§9" + getConfig().getString("server-site"));
	    TabAPI.setTabString(this, p, 19, 2, "§9" + getConfig().getString("server-site-ending"));

	    TabAPI.updatePlayer(p);

	    factionPeople.clear();
	    staffPeople.clear();
	  }

	  @EventHandler
	  public void onDie(EntityDeathEvent event)
	  {
	    Entity entity = event.getEntity();

	    if ((entity instanceof Player)) {
	      Player player = (Player)entity;

	      getConfig().set("stats." + player.getName().toLowerCase() + ".deaths", Integer.valueOf(getConfig().getInt("stats." + player.getName().toLowerCase() + ".deaths") + 1));
	      saveConfig();

	      Entity killer = player.getKiller();
	      if ((killer instanceof Player)) {
	        Player k = (Player)killer;

	        getConfig().set("stats." + k.getName().toLowerCase() + ".kills", Integer.valueOf(getConfig().getInt("stats." + k.getName().toLowerCase() + ".kills") + 1));
	        saveConfig();
	      }
	    }
	  }

	  @EventHandler
	  public void onJoin(PlayerJoinEvent event)
	  {
	    final Player player = event.getPlayer();
	    TabAPI.setPriority(this, player, 2);

	    if (!getConfig().contains("stats." + player.getName().toLowerCase() + ".deaths")) {
	      String n = player.getName().toLowerCase();
	      getConfig().set("stats." + n + ".kills", Integer.valueOf(0));
	      getConfig().set("stats." + n + ".deaths", Integer.valueOf(0));
	      saveConfig();
	    }

	    Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable()
	    {
	      public void run() {
	        TabAPI.updatePlayer(player);
	      }
	    }
	    , 3L);
	  }

	  public void onEnable()
	  {
	    if (!new File(getDataFolder(), "config.yml").exists()) {
	      saveDefaultConfig();
	    }
	    loop();
	    getServer().getPluginManager().registerEvents(this, this);
	    log("Enabled!");
	    setupEconomy();
	  }

	  public void onDisable()
	  {
	    log("Disabled!");
	  }

	  private boolean setupEconomy() {
	    if (getServer().getPluginManager().getPlugin("Vault") == null) {
	      return false;
	    }
	    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
	    if (rsp == null) {
	      return false;
	    }
	    econ = (Economy)rsp.getProvider();
	    return econ != null;
	  }
	
}
