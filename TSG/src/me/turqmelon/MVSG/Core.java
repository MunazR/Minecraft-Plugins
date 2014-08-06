package me.turqmelon.MVSG;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.milkbowl.vault.economy.Economy;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.mcsg.double0negative.tabapi.TabAPI;

public class Core extends JavaPlugin
  implements Listener
{
  protected Map<Integer, GameStatus> gameStatus = new HashMap();
  protected Map<String, Material> brokenBlocks = new HashMap();
  protected Map<String, Material> placedBlocks = new HashMap();

  protected Map<String, String> chosenKit = new HashMap();

  protected Map<String, Integer> gamers = new HashMap();
  protected Map<String, Integer> gamerOfArena = new HashMap();

  public String tag = "§8[§6MVSG§8]§2 ";
  public String name = "§aMelon Survival Games";
  private int graceSeconds = 0;

  private boolean disabled = false;

  public static Economy econ = null;

  protected Map<Integer, Integer> gameTicks = new HashMap();

  protected Map<String, Location> wandP1 = new HashMap();
  protected Map<String, Location> wandP2 = new HashMap();

  public String loadType = "none";

  public int branded = 0;

  public SBManager sb = new SBManager(this);

  public void log(String m)
  {
    getLogger().info(m);
  }

  public boolean isActualArena(int arena)
  {
    int max = getMaxArenas();

    if ((arena > 0) && (arena <= max)) {
      return true;
    }

    return false;
  }

  public Location getWandLocation(Player player, int point)
  {
    if (point == 1) {
      if (this.wandP1.containsKey(player.getName())) {
        return (Location)this.wandP1.get(player.getName());
      }
    }
    else if ((point == 2) && 
      (this.wandP2.containsKey(player.getName()))) {
      return (Location)this.wandP2.get(player.getName());
    }

    return null;
  }

  public void giveKitStuff(Player player)
  {
    if (this.chosenKit.containsKey(player.getName())) {
      String kit = (String)this.chosenKit.get(player.getName());

      player.sendMessage(this.tag + "Enjoy your §a" + WordUtils.capitalizeFully(kit) + "§2 kit!");

      player.getInventory().setHelmet(getKitHelm(kit));
      player.getInventory().setChestplate(getKitChest(kit));
      player.getInventory().setLeggings(getKitLegs(kit));
      player.getInventory().setBoots(getKitFeet(kit));

      List<PotionEffect> effects = getKitPotionEffects(kit);
      if (effects.size() > 0) {
    	  for (PotionEffect effect : effects) {
    		  player.addPotionEffect(effect);
        }
      }

      List<ItemStack> items = getKitItems(kit);
      if (items.size() > 0) {
        for (ItemStack item : items) {
          player.getInventory().addItem(new ItemStack[] { item });
        }
      }

      this.chosenKit.remove(player.getName());
    }
  }

  public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
  {
    if ((command.getName().equalsIgnoreCase("sg")) || (command.getName().equalsIgnoreCase("hg")) || (command.getName().equalsIgnoreCase("survivalgames")) || (command.getName().equalsIgnoreCase("hungergames"))) {
      String typed = command.getName();

      if ((this.disabled) && (!sender.getName().equalsIgnoreCase("turqmelon"))) {
        sender.sendMessage(this.tag + "§cSurvivalGames has been disabled for this server.");
        sender.sendMessage(this.tag + "§cUsage has been suspended until further notice.");
        return true;
      }

      if (args.length == 0) {
        sender.sendMessage(this.tag + "§2§lTurq's Survival Games§2 version §8[§a" + getDescription().getVersion() + "§8]§2!");
        sender.sendMessage(this.tag + "Developed by §aturqmelon§2, §3§ntwitter.com/turqmelon§2.");
        sender.sendMessage(this.tag + "Type §8[§a/" + command.getName() + " help§8]§2 for commands.");
        if (this.disabled) {
          sender.sendMessage(this.tag + "§cThis copy of TSG has been disabled!");
        }

      }
      else if (args.length == 2) {
        String cmd = args[0].toLowerCase();
        String var = args[1];
        int id;
        if ((cmd.equalsIgnoreCase("list")) || (cmd.equalsIgnoreCase("l"))) {
          if ((sender instanceof Player))
          {
            Player player = (Player)sender;
            String perm = "sg.list";
            if (player.hasPermission(perm)) {
              try {
                int arena = Integer.parseInt(var);

                if ((isActualArena(arena)) && (isArenaEnabled(arena)))
                {
                  StringBuilder list = new StringBuilder();
                  StringBuilder spec = new StringBuilder();
                  for (Player pl : getTributesOfArena(arena)) {
                    id = ((Integer)this.gamers.get(pl.getName())).intValue();
                    if (id != 0) {
                      list.append(pl.getDisplayName() + "§8, §f");
                    }
                    else {
                      spec.append(pl.getDisplayName() + "§8, §f");
                    }
                  }

                  int alive = getTributesOfArena(arena).size();

                  player.sendMessage(this.tag + "There are §8[§a" + alive + "§8]§2 players in §aArena " + arena + "§2!");
                  player.sendMessage(this.tag + "Tributes: §f" + list);
                  player.sendMessage(this.tag + "Spectators: §f" + spec);
                }
                else {
                  player.sendMessage(this.tag + "§cArena §8[§4" + arena + "§8]§c is not available.");
                }
              } catch (NumberFormatException e) {
                player.sendMessage(this.tag + "§cExpected number, received string \"§f" + var + "§c\".");
              }
            }
            else
            {
              player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
            }

          }
          else
          {
            sender.sendMessage(this.tag + "§5You need to be a player to use that!");
          }
        }
        else if ((cmd.equalsIgnoreCase("getconfig")) || (cmd.equalsIgnoreCase("gc"))) {
          if ((sender instanceof Player))
          {
            Player player = (Player)sender;
            String perm = "sg.getconfig";
            if (player.hasPermission(perm))
            {
              String key = var;

              Object value = getConfig().get(key);

              player.sendMessage(this.tag + "The value of §a" + key + "§2 is §a" + value + "§2.");
            }
            else
            {
              player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
            }
          }
          else
          {
            sender.sendMessage(this.tag + "§5You need to be a player to use that!");
          }
        }
        else if ((cmd.equalsIgnoreCase("map")) || (cmd.equalsIgnoreCase("m"))) {
          if ((sender instanceof Player))
          {
            Player player = (Player)sender;
            String perm = "sg.map";
            if (player.hasPermission(perm)) {
              try {
                int arena = Integer.parseInt(var);

                if ((isActualArena(arena)) && (isArenaEnabled(arena)))
                {
                  player.sendMessage(this.tag + "Map info for §aArena " + arena + "§2...");
                  player.sendMessage(this.tag + "§eMAP | §aName: §b" + getMapName(arena));
                  player.sendMessage(this.tag + "§eMAP | §aCreator: §b" + getMapAuthor(arena));
                  player.sendMessage(this.tag + "§eMAP | §aLink: §b" + getMapLink(arena));
                }
                else
                {
                  player.sendMessage(this.tag + "§cArena §8[§4" + arena + "§8]§c is not available.");
                }
              } catch (NumberFormatException e) {
                player.sendMessage(this.tag + "§cExpected number, received string \"§f" + var + "§c\".");
              }
            }
            else
            {
              player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
            }
          }
          else
          {
            sender.sendMessage(this.tag + "§5You need to be a player to use that!");
          }
        }
        else if (cmd.equalsIgnoreCase("stats")) {
          if ((sender instanceof Player))
          {
            Player player = (Player)sender;
            String perm = "sg.stats";
            if (player.hasPermission(perm))
            {
              Player target = Bukkit.getServer().getPlayer(var);
              if (target != null)
              {
                if (isUsingStats())
                {
                  player.sendMessage(this.tag + "§2§l" + WordUtils.capitalizeFully(target.getName()) + "'s Personal Stats");
                  player.sendMessage(this.tag + "Survival Games Wins: §a§l" + getWinsForPlayer(target));
                  player.sendMessage(this.tag + "Total Tribute Kills: §a§l" + getKillsForPlayer(target));
                  player.sendMessage(this.tag + "Total Chests Found: §a§l" + getOpenedChestsForPlayer(target));
                }
                else
                {
                  player.sendMessage(this.tag + "§cThis server does not have stats enabled.");
                }
              }
              else
              {
                player.sendMessage(this.tag + "§cPlayer \"§f" + var + "§c\" not found or not online.");
              }
            }
            else
            {
              player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
            }
          }
          else
          {
            sender.sendMessage(this.tag + "§5You need to be a player to use that!");
          }
        }
        else
        {
          int chests;
          int chests1;
          Object loc;
          if (cmd.equalsIgnoreCase("enable")) {
            if ((sender instanceof Player))
            {
              Player player = (Player)sender;
              String perm = "sg.enable";
              if (player.hasPermission(perm)) {
                try
                {
                  int arena = Integer.parseInt(var);

                  if (isActualArena(arena))
                  {
                    if (!isArenaEnabled(arena))
                    {
                      chests1 = (loc = Bukkit.getServer().getOnlinePlayers()).length;
                      for (chests = 0; chests < chests1; chests++) { 
                    	layer all = loc[chests];
                        all.sendMessage(this.tag + player.getDisplayName() + "§2 has §aENABLED§a Arena " + arena + "§2!");
                      }

                      getConfig().set("arenas.arena" + arena + ".status", Integer.valueOf(1));
                      saveConfig();

                      setGameStatus(GameStatus.IDLE, arena);
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cArena " + arena + "§4 is already enabled.");
                    }
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§cArena §8[§4" + arena + "§8]§c doesn't exist!");
                  }
                }
                catch (NumberFormatException e)
                {
                  player.sendMessage(this.tag + "§cExpected number, received string \"§f" + var + "§c\".");
                }
              }
              else {
                player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
              }
            }
            else
            {
              sender.sendMessage(this.tag + "§5You need to be a player to use that!");
            }
          }
          else if (cmd.equalsIgnoreCase("disable")) {
            if ((sender instanceof Player))
            {
              Player player = (Player)sender;
              String perm = "sg.disable";
              if (player.hasPermission(perm))
              {
                try
                {
                  int arena = Integer.parseInt(var);

                  if (isActualArena(arena))
                  {
                    if (isArenaEnabled(arena))
                    {
                      chests1 = (loc = Bukkit.getServer().getOnlinePlayers()).length; for (chests = 0; chests < chests1; chests++) { Player all = loc[chests];
                        all.sendMessage(this.tag + player.getDisplayName() + "§2 has §cDISABLED§a Arena " + arena + "§2!");
                      }

                      getConfig().set("arenas.arena" + arena + ".status", Integer.valueOf(0));
                      saveConfig();

                      if (getTributesOfArena(arena).size() > 0) {
                        for (Player pl : getTributesOfArena(arena)) {
                          leaveGame(pl, arena);
                        }
                      }

                      endGame(WinResult.ADMIN_STOP, arena);

                      updateArenaStatusSign(arena);
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cArena " + arena + "§4 is already disabled.");
                    }
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§cArena §8[§4" + arena + "§8]§c doesn't exist!");
                  }
                }
                catch (NumberFormatException e)
                {
                  player.sendMessage(this.tag + "§cExpected number, received string \"§f" + var + "§c\".");
                }
              }
              else
              {
                player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
              }
            }
            else
            {
              sender.sendMessage(this.tag + "§5You need to be a player to use that!");
            }
          }
          else if (cmd.equalsIgnoreCase("restock")) {
            if ((sender instanceof Player))
            {
              Player player = (Player)sender;
              String perm = "sg.restock";
              if (player.hasPermission(perm)) {
                try
                {
                  int arena = Integer.parseInt(var);

                  if (isActualArena(arena))
                  {
                    refillChests(1, arena);
                    refillChests(2, arena);

                    player.sendMessage(this.tag + "Restocked all chests in §aArena " + arena + "§2!");
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§cArena §8[§4" + arena + "§8]§c doesn't exist!");
                  }
                } catch (NumberFormatException e) {
                  player.sendMessage(this.tag + "§cExpected number, received string \"§f" + var + "§c\".");
                }
              }
              else
              {
                player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
              }
            }
            else
            {
              sender.sendMessage(this.tag + "§5You need to be a player to use that!");
            }
          }
          else if (cmd.equalsIgnoreCase("kick")) {
            if ((sender instanceof Player))
            {
              Player player = (Player)sender;
              String perm = "sg.kick";
              if (player.hasPermission(perm))
              {
                Player target = Bukkit.getServer().getPlayer(var);
                if (target != null)
                {
                  if (isTributeAtAll(target))
                  {
                    int arena = getTributeArena(target);
                    player.sendMessage(this.tag + "Kicked " + target.getName() + "§2 from the survival games.");
                    target.sendMessage(this.tag + "Kicked by " + player.getName() + "§2!");

                    leaveGame(target, arena);
                  }
                  else
                  {
                    player.sendMessage(this.tag + target.getDisplayName() + "§c is not playing survival games.");
                  }
                }
                else
                {
                  player.sendMessage(this.tag + "§cPlayer \"§f" + var + "§c\" not found or not online.");
                }
              }
              else
              {
                player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
              }
            }
            else
            {
              sender.sendMessage(this.tag + "§5You need to be a player to use that!");
            }
          }
          else if ((cmd.equalsIgnoreCase("setheadwall")) || (cmd.equalsIgnoreCase("shw"))) {
            if ((sender instanceof Player))
            {
              Player player = (Player)sender;
              String perm = "sg.setheadwall";
              if (player.hasPermission(perm))
              {
                try
                {
                  int arena = Integer.parseInt(var);

                  if (isActualArena(arena))
                  {
                    if ((this.wandP1.containsKey(player.getName())) && (this.wandP2.containsKey(player.getName())))
                    {
                      Location p1 = getWandLocation(player, 1);
                      Location p2 = getWandLocation(player, 2);

                      getConfig().set("arenas.arena" + arena + ".use-head-wall", Boolean.valueOf(true));
                      saveConfig();

                      setHeadWall(arena, p1, p2);

                      player.sendMessage(this.tag + "Head wall for §aArena " + arena + "§2 created!");
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cMake a region selection first! Get a wand with §8[§4/sg wand§8]§c!");
                    }
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§cArena §8[§4" + arena + "§8]§c does not exist!");
                  }
                }
                catch (NumberFormatException e)
                {
                  player.sendMessage(this.tag + "§cExpected number, received string \"§f" + var + "§c\".");
                }
              }
              else
              {
                player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
              }
            }
            else
            {
              sender.sendMessage(this.tag + "§5You need to be a player to use that!");
            }
          }
          else if (cmd.equals("fstart")) {
            if ((sender instanceof Player))
            {
              Player player = (Player)sender;

              String perm = "sg.fstart";
              if (player.hasPermission(perm))
              {
                try
                {
                  int arena = Integer.parseInt(var);

                  if ((isActualArena(arena)) && (isArenaEnabled(arena)))
                  {
                    if (canJoinGame(getGameStatus(arena)))
                    {
                      startGame(arena);
                      sendTributeMessage(player.getDisplayName() + "§2 force started the game!", true, arena);
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cThe game must be accepting players.");
                    }
                  }
                  else {
                    player.sendMessage(this.tag + "§cArena §8[§4" + arena + "§8]§c is not available!");
                  }
                }
                catch (NumberFormatException e)
                {
                  player.sendMessage(this.tag + "§cExpected number, received string \"§f" + var + "§c\".");
                }
              }
              else
              {
                player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
              }
            }
            else
            {
              sender.sendMessage(this.tag + "§5You need to be a player to use that!");
            }
          }
          else if (cmd.equals("fend")) {
            if ((sender instanceof Player))
            {
              Player player = (Player)sender;

              String perm = "sg.fend";
              if (player.hasPermission(perm))
              {
                try
                {
                  int arena = Integer.parseInt(var);

                  if ((isActualArena(arena)) && (isArenaEnabled(arena)))
                  {
                    if (!canJoinGame(getGameStatus(arena)))
                    {
                      endGame(WinResult.ADMIN_STOP, arena);
                      sendTributeMessage(player.getDisplayName() + "§2 force stopped the game!", true, arena);
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cThe game must be started.");
                    }
                  }
                  else {
                    player.sendMessage(this.tag + "§cArena §8[§4" + arena + "§8]§c is not available!");
                  }
                }
                catch (NumberFormatException e)
                {
                  player.sendMessage(this.tag + "§cExpected number, received string \"§f" + var + "§c\".");
                }
              }
              else
              {
                player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
              }
            }
            else
            {
              sender.sendMessage(this.tag + "§5You need to be a player to use that!");
            }
          }
          else if (cmd.equals("fdm")) {
            if ((sender instanceof Player))
            {
              Player player = (Player)sender;

              String perm = "sg.fdm";
              if (player.hasPermission(perm))
              {
                try
                {
                  int arena = Integer.parseInt(var);

                  if ((isActualArena(arena)) && (isArenaEnabled(arena)))
                  {
                    if (getGameStatus(arena) == GameStatus.INGAME)
                    {
                      setTicks(61, arena);
                      sendTributeMessage(player.getDisplayName() + "§2 forced the deathmatch countdown!", true, arena);
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cThe game must be in progress.");
                    }
                  }
                  else {
                    player.sendMessage(this.tag + "§cArena §8[§4" + arena + "§8]§c is not available!");
                  }
                }
                catch (NumberFormatException e)
                {
                  player.sendMessage(this.tag + "§cExpected number, received string \"§f" + var + "§c\".");
                }
              }
              else
              {
                player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
              }
            }
            else
            {
              sender.sendMessage(this.tag + "§5You need to be a player to use that!");
            }
          }
          else if ((cmd.equalsIgnoreCase("spectate")) || (cmd.equalsIgnoreCase("s"))) {
            if ((sender instanceof Player))
            {
              Player player = (Player)sender;
              String perm = "sg.spectate";
              if (player.hasPermission(perm)) {
                try
                {
                  int target = Integer.parseInt(var);

                  if (isActualArena(target))
                  {
                    if (isArenaEnabled(target))
                    {
                      if ((player.hasPermission("sg.spectate." + target)) || (player.hasPermission("sg.spectate.all")))
                      {
                        int i = 0;
                        List localList1 = (id = player.getInventory().getContents()).length; for (chests1 = 0; chests1 < localList1; chests1++) { ItemStack stack = id[chests1];
                          if (stack != null)
                          {
                            if (stack.getType() != Material.AIR) {
                              i++;
                            }
                          }
                        }

                        List localList2 = (id = player.getInventory().getArmorContents()).length; for (chests1 = 0; chests1 < localList2; chests1++){
                        ItemStack stack = id[chests1];
                          if (stack != null)
                          {
                            if (stack.getType() != Material.AIR) {
                              i++;
                            }
                          }

                        }

                        boolean check = getConfig().getBoolean("starting.make-sure-inv-is-empty");
                        if (!check) {
                          i = 0;
                        }

                        if (i == 0)
                        {
                          if (!canJoinGame(getGameStatus(target)))
                          {
                            spectateSG(player, target, false, 0);
                          }
                          else
                          {
                            player.sendMessage(this.tag + "§cArena §8[§4" + target + "§8]§c hasn't yet started!");
                          }
                        }
                        else
                        {
                          player.sendMessage(this.tag + "§cYou must have an empty inventory to spectate!");
                        }
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§cYou do not have access to Arena §8[§4" + target + "§8]§c!");
                      }
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cArena §8[§4" + target + "§8]§c is not enabled.");
                    }
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§cSpecified arena §8[§4" + target + "§8]§c doesn't exist.");
                  }
                }
                catch (NumberFormatException e)
                {
                  player.sendMessage(this.tag + "§cExpected number, received string \"§f" + var + "§c\".");
                }
              }
              else
              {
                player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
              }
            }
            else
            {
              sender.sendMessage(this.tag + "§5You need to be a player to use that!");
            }
          }
          else if ((cmd.equalsIgnoreCase("join")) || (cmd.equalsIgnoreCase("j"))) {
            if ((sender instanceof Player))
            {
              Player player = (Player)sender;
              String perm = "sg.join";
              if (player.hasPermission(perm)) {
                try
                {
                  int target = Integer.parseInt(var);

                  if (isActualArena(target))
                  {
                    if (isArenaEnabled(target))
                    {
                      if ((player.hasPermission("sg.join." + target)) || (player.hasPermission("sg.join.all")))
                      {
                        int i = 0;
                        List localList3 = (id = player.getInventory().getContents()).length; for (chests1 = 0; chests1 < localList3; chests1++) { ItemStack stack = id[chests1];
                          if (stack != null)
                          {
                            if (stack.getType() != Material.AIR) {
                              i++;
                            }
                          }
                        }

                        List localList4 = (id = player.getInventory().getArmorContents()).length; for (chests1 = 0; chests1 < localList4; chests1++) { ItemStack stack = id[chests1];
                          if (stack != null)
                          {
                            if (stack.getType() != Material.AIR) {
                              i++;
                            }
                          }

                        }

                        boolean check = getConfig().getBoolean("starting.make-sure-inv-is-empty");
                        if (!check) {
                          i = 0;
                        }

                        if (i == 0)
                        {
                          if (canJoinGame(getGameStatus(target)))
                          {
                            int tribs = getTributesOfArena(target).size();
                            if (tribs < 24)
                            {
                              joinSG(player, target);
                            }
                            else if (player.hasPermission("sg.vip"))
                            {
                              int id = 1;
                              boolean kicked = false;
                              do
                              {
                                Player trib = getTributeFromID(target, id);

                                if (!trib.hasPermission("sg.vip")) {
                                  kicked = true;
                                  leaveGame(trib, target);
                                  trib.sendMessage(this.tag + "§cYou were kicked to make room for a VIP!");
                                  joinSG(player, target);
                                  player.sendMessage(this.tag + "Kicked §a" + trib.getDisplayName() + "§2 to make room for you!");
                                  break;
                                }

                                id++;
                              }
                              while (
                                id <= 24);

                              if (!kicked) {
                                player.sendMessage(this.tag + "§cArena §8[§4" + target + "§8]§c is full, and there were no players available to kick!");
                              }
                            }
                            else
                            {
                              player.sendMessage(this.tag + "§cArena §8[§4" + target + "§8]§c is full!");
                            }

                          }
                          else
                          {
                            player.sendMessage(this.tag + "§cArena §8[§4" + target + "§8]§c has already started!");
                          }
                        }
                        else
                        {
                          player.sendMessage(this.tag + "§cYou must have an empty inventory to join!");
                        }
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§cYou do not have access to Arena §8[§4" + target + "§8]§c!");
                      }
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cArena §8[§4" + target + "§8]§c is not enabled.");
                    }
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§cSpecified arena §8[§4" + target + "§8]§c doesn't exist.");
                  }
                }
                catch (NumberFormatException e)
                {
                  player.sendMessage(this.tag + "§cExpected number, received string \"§f" + var + "§c\".");
                }
              }
              else
              {
                player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
              }
            }
            else
            {
              sender.sendMessage(this.tag + "§5You need to be a player to use that!");
            }
          }
          else
            sender.sendMessage(this.tag + "§cUnrecognized command. Type §8[§4/sg help§8]§c for commands.");
        }
      }
      else
      {
        Location p2;
        if (args.length == 4) {
          String cmd = args[0].toLowerCase();
          String var = args[1];
          String var2 = args[2];
          String var3 = args[3];
          if ((cmd.equalsIgnoreCase("setchestradius")) || (cmd.equalsIgnoreCase("scr"))) {
            if ((sender instanceof Player))
            {
              Player player = (Player)sender;
              String perm = "sg.setchestradius";
              if (player.hasPermission(perm)) {
                try
                {
                  int arena = Integer.parseInt(var);
                  int tier = Integer.parseInt(var3);
                  try
                  {
                    int radius = Integer.parseInt(var2);
                    if (isActualArena(arena))
                    {
                      if (!isArenaEnabled(arena))
                      {
                        Location p1 = player.getLocation().subtract(radius, radius, radius);
                        Location p2 = player.getLocation().add(radius, radius, radius);

                        int i = 0;

                        List found = blocksFromTwoPoints(p1, p2);

                        for (Block block : found) {
                          if (block.getType() == Material.CHEST) {
                            List chests = new ArrayList();
                            for (Location loc : getChests(tier, arena)) {
                              chests.add(loc.getBlockX() + ">" + loc.getBlockY() + ">" + loc.getBlockZ());
                            }

                            chests.add(block.getLocation().getBlockX() + ">" + block.getLocation().getBlockY() + ">" + block.getLocation().getBlockZ());

                            getConfig().set("chests.arena" + arena + ".tier" + tier, chests);

                            block.getWorld().strikeLightningEffect(block.getLocation());

                            i++;
                          }
                        }
                        saveConfig();

                        player.sendMessage(this.tag + "Registered §8[§a" + i + "§8]§2 chests to §aArena " + arena + "§2!");
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§cPlease disable the arena before editing chests.");
                      }
                    }
                    else
                      player.sendMessage(this.tag + "§cArena ID is out of bounds!");
                  }
                  catch (NumberFormatException e)
                  {
                    if (var2.equalsIgnoreCase("w"))
                    {
                      if ((this.wandP1.containsKey(player.getName())) && (this.wandP2.containsKey(player.getName())))
                      {
                        if (isActualArena(arena))
                        {
                          if (!isArenaEnabled(arena))
                          {
                            Location p1 = getWandLocation(player, 1);
                            p2 = getWandLocation(player, 2);

                            int i = 0;

                            List found = blocksFromTwoPoints(p1, p2);

                            for (??? = found.iterator(); ???.hasNext(); ) { Block block = (Block)???.next();
                              if (block.getType() == Material.CHEST) {
                                List chests = new ArrayList();
                                for (??? = getChests(tier, arena).iterator(); ???.hasNext(); ) { Location loc = (Location)???.next();
                                  chests.add(loc.getBlockX() + ">" + loc.getBlockY() + ">" + loc.getBlockZ());
                                }

                                chests.add(block.getLocation().getBlockX() + ">" + block.getLocation().getBlockY() + ">" + block.getLocation().getBlockZ());

                                getConfig().set("chests.arena" + arena + ".tier" + tier, chests);

                                block.getWorld().strikeLightningEffect(block.getLocation());

                                i++;
                              }
                            }
                            saveConfig();

                            player.sendMessage(this.tag + "Registered §8[§a" + i + "§8]§2 chests to §aArena " + arena + "§2!");
                          }
                          else
                          {
                            player.sendMessage(this.tag + "§cPlease disable the arena before editing chests.");
                          }
                        }
                        else {
                          player.sendMessage(this.tag + "§cArena ID is out of bounds!");
                        }
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§cYou need to make a selection first! Type §8[§4/sg wand§8]§c to spawn a wand.");
                      }
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cPlease enter a radius or \"W\" for a wand selection.");
                    }
                  }
                }
                catch (NumberFormatException e)
                {
                  player.sendMessage(this.tag + "§cBoth the tier and arena and radius must be numbers.");
                }
              }
              else
              {
                player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
              }
            }
            else
            {
              sender.sendMessage(this.tag + "§5You need to be a player to use that!");
            }
          }
          else
            sender.sendMessage(this.tag + "§cUnrecognized command. Type §8[§4/sg help§8]§c for commands.");
        }
        else
        {
          Player player;
          String perm;
          Location loc;
          if (args.length == 3) {
            String cmd = args[0].toLowerCase();
            String var = args[1];
            String var2 = args[2];

            if ((cmd.equalsIgnoreCase("setarena")) || (cmd.equalsIgnoreCase("sa"))) {
              if ((sender instanceof Player))
              {
                Player player = (Player)sender;
                String perm = "sg.setarena";
                if (player.hasPermission(perm)) {
                  try
                  {
                    int arena = Integer.parseInt(var);
                    int radius = Integer.parseInt(var2);

                    if (isActualArena(arena))
                    {
                      setArenaCenter(player.getLocation(), arena);
                      Location loc = player.getLocation();

                      getConfig().set("arenas.arena" + arena + ".radius", Integer.valueOf(radius));
                      getConfig().set("arenas.arena" + arena + ".status", Integer.valueOf(0));
                      getConfig().set("arenas.arena" + arena + ".world", player.getWorld().getName());
                      getConfig().set("arenas.arena" + arena + ".dmradius", Integer.valueOf(40));
                      getConfig().set("arenas.arena" + arena + ".available-kits", new ArrayList());

                      saveConfig();

                      player.sendMessage(this.tag + "§2§lArena " + arena + " Created!");
                      player.sendMessage(this.tag + "World: §a" + loc.getWorld().getName());
                      player.sendMessage(this.tag + "X:§a " + loc.getBlockX() + "§2 Y: §a" + loc.getBlockY() + "§2 Z: §a" + loc.getBlockZ());
                      player.sendMessage(this.tag + "§2§oDetails can be edited in your config file.");
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cArena ID is out of bounds!");
                    }
                  }
                  catch (NumberFormatException e)
                  {
                    player.sendMessage(this.tag + "§cBoth the radius and arena must be numbers.");
                  }
                }
                else
                {
                  player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                }
              }
              else
              {
                sender.sendMessage(this.tag + "§5You need to be a player to use that!");
              }
            }
            else if ((cmd.equalsIgnoreCase("setmaplink")) || (cmd.equalsIgnoreCase("sml"))) {
              if ((sender instanceof Player))
              {
                Player player = (Player)sender;
                try
                {
                  int arena = Integer.parseInt(var);

                  if (isActualArena(arena))
                  {
                    String title = var2;

                    getConfig().set("mapinfo.arena" + arena + ".link", title);
                    saveConfig();

                    player.sendMessage(this.tag + "§2§lMap Information Updated!");
                    player.sendMessage(this.tag + "A§a" + arena + "§8: §2Name: §a" + getMapName(arena));
                    player.sendMessage(this.tag + "A§a" + arena + "§8: §2Author: §a" + getMapAuthor(arena));
                    player.sendMessage(this.tag + "A§a" + arena + "§8: §2Link: §a" + getMapLink(arena));
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§cArena ID is out of bounds!");
                  }
                }
                catch (NumberFormatException e)
                {
                  player.sendMessage(this.tag + "§cBoth the radius and arena must be numbers.");
                }
              }
              else
              {
                sender.sendMessage(this.tag + "§5You need to be a player to use that!");
              }
            }
            else if ((cmd.equalsIgnoreCase("setmapauthor")) || (cmd.equalsIgnoreCase("sma"))) {
              if ((sender instanceof Player))
              {
                Player player = (Player)sender;
                try
                {
                  int arena = Integer.parseInt(var);

                  if (isActualArena(arena))
                  {
                    String title = var2;
                    title = title.replace("_", " ");

                    getConfig().set("mapinfo.arena" + arena + ".author", title);
                    saveConfig();

                    player.sendMessage(this.tag + "§2§lMap Information Updated!");
                    player.sendMessage(this.tag + "A§a" + arena + "§8: §2Name: §a" + getMapName(arena));
                    player.sendMessage(this.tag + "A§a" + arena + "§8: §2Author: §a" + getMapAuthor(arena));
                    player.sendMessage(this.tag + "A§a" + arena + "§8: §2Link: §a" + getMapLink(arena));
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§cArena ID is out of bounds!");
                  }
                }
                catch (NumberFormatException e)
                {
                  player.sendMessage(this.tag + "§cBoth the radius and arena must be numbers.");
                }
              }
              else
              {
                sender.sendMessage(this.tag + "§5You need to be a player to use that!");
              }
            }
            else if ((cmd.equalsIgnoreCase("setmapname")) || (cmd.equalsIgnoreCase("smn"))) {
              if ((sender instanceof Player))
              {
                Player player = (Player)sender;
                try
                {
                  int arena = Integer.parseInt(var);

                  if (isActualArena(arena))
                  {
                    String title = var2;
                    title = title.replace("_", " ");

                    getConfig().set("mapinfo.arena" + arena + ".name", title);
                    saveConfig();

                    player.sendMessage(this.tag + "§2§lMap Information Updated!");
                    player.sendMessage(this.tag + "A§a" + arena + "§8: §2Name: §a" + getMapName(arena));
                    player.sendMessage(this.tag + "A§a" + arena + "§8: §2Author: §a" + getMapAuthor(arena));
                    player.sendMessage(this.tag + "A§a" + arena + "§8: §2Link: §a" + getMapLink(arena));
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§cArena ID is out of bounds!");
                  }
                }
                catch (NumberFormatException e)
                {
                  player.sendMessage(this.tag + "§cBoth the radius and arena must be numbers.");
                }
              }
              else
              {
                sender.sendMessage(this.tag + "§5You need to be a player to use that!");
              }
            }
            else if (cmd.equalsIgnoreCase("removetributesign")) {
              if ((sender instanceof Player))
              {
                Player player = (Player)sender;
                String perm = "sg.removetributesign";
                if (player.hasPermission(perm))
                {
                  try
                  {
                    int arena = Integer.parseInt(var);
                    int trib = Integer.parseInt(var2);

                    if (isActualArena(arena))
                    {
                      if ((trib >= 1) && (trib <= 24))
                      {
                        player.sendMessage(this.tag + "§2§lStat sign for Tribute " + trib + " in Arena " + arena + ", removed!");

                        setTributeSign(arena, trib, new Location(player.getWorld(), 0.0D, 0.0D, 0.0D));
                      }
                      else {
                        player.sendMessage(this.tag + "Non-existant tribute!");
                      }
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cArena ID is out of bounds!");
                    }
                  }
                  catch (NumberFormatException e)
                  {
                    player.sendMessage(this.tag + "§cThe arena and tribute need to be numbers!");
                  }
                }
                else
                {
                  player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                }
              }
              else
              {
                sender.sendMessage(this.tag + "§5You need to be a player to use that!");
              }
            }
            else if (cmd.equalsIgnoreCase("settributesign")) {
              if ((sender instanceof Player))
              {
                Player player = (Player)sender;
                String perm = "sg.settributesign";
                if (player.hasPermission(perm))
                {
                  try
                  {
                    int arena = Integer.parseInt(var);
                    int trib = Integer.parseInt(var2);

                    if (isActualArena(arena))
                    {
                      if ((trib >= 1) && (trib <= 24))
                      {
                        Block block = player.getTargetBlock(null, 30);

                        if ((block.getType() == Material.WALL_SIGN) || (block.getType() == Material.SIGN_POST))
                        {
                          Location loc = block.getLocation();
                          player.sendMessage(this.tag + "§2§lStat sign for Tribute " + trib + " in Arena " + arena + ", set!");
                          player.sendMessage(this.tag + "X:§a " + loc.getBlockX() + "§2 Y: §a" + loc.getBlockY() + "§2 Z: §a" + loc.getBlockZ());

                          setTributeSign(arena, trib, loc);
                        }
                        else
                        {
                          player.sendMessage(this.tag + "§cThat's not a sign!");
                        }
                      }
                      else
                      {
                        player.sendMessage(this.tag + "Non-existant tribute!");
                      }
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cArena ID is out of bounds!");
                    }
                  }
                  catch (NumberFormatException e)
                  {
                    player.sendMessage(this.tag + "§cThe arena and tribute need to be numbers!");
                  }
                }
                else
                {
                  player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                }
              }
              else
              {
                sender.sendMessage(this.tag + "§5You need to be a player to use that!");
              }
            }
            else if ((cmd.equalsIgnoreCase("setchest")) || (cmd.equalsIgnoreCase("sc"))) {
              if ((sender instanceof Player))
              {
                Player player = (Player)sender;
                String perm = "sg.setarena";
                if (player.hasPermission(perm)) {
                  try
                  {
                    int arena = Integer.parseInt(var);
                    int tier = Integer.parseInt(var2);

                    if (isActualArena(arena))
                    {
                      if (!isArenaEnabled(arena))
                      {
                        Block block = player.getTargetBlock(null, 20);

                        if (block.getType() == Material.CHEST) {
                          Object chests = new ArrayList();
                          for (Location loc : getChests(tier, arena)) {
                            ((List)chests).add(loc.getBlockX() + ">" + loc.getBlockY() + ">" + loc.getBlockZ());
                          }

                          ((List)chests).add(block.getLocation().getBlockX() + ">" + block.getLocation().getBlockY() + ">" + block.getLocation().getBlockZ());

                          getConfig().set("chests.arena" + arena + ".tier" + tier, chests);
                          saveConfig();

                          player.sendMessage(this.tag + "Chest registered for §aArena " + arena + "§2! Tier: §a" + tier);
                        }
                        else
                        {
                          player.sendMessage(this.tag + "§cTarget block is not a chest.");
                        }
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§cPlease disable the arena before editing chests.");
                      }
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cArena ID is out of bounds!");
                    }
                  }
                  catch (NumberFormatException e)
                  {
                    player.sendMessage(this.tag + "§cBoth the tier and arena must be numbers.");
                  }
                }
                else
                {
                  player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                }
              }
              else
              {
                sender.sendMessage(this.tag + "§5You need to be a player to use that!");
              }
            }
            else if ((cmd.equalsIgnoreCase("setconfig")) || (cmd.equalsIgnoreCase("sc"))) {
              if ((sender instanceof Player))
              {
                Player player = (Player)sender;
                String perm = "sg.getconfig";
                if (player.hasPermission(perm))
                {
                  String key = var;
                  Object setting = var2;

                  getConfig().set(key, setting);
                  saveConfig();

                  player.sendMessage(this.tag + "§2§lConfiguration Updated!");
                  player.sendMessage(this.tag + "Setting: §a" + key);
                  player.sendMessage(this.tag + "Value: §a" + setting);
                }
                else
                {
                  player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                }
              }
              else
              {
                sender.sendMessage(this.tag + "§5You need to be a player to use that!");
              }
            }
            else if ((cmd.equalsIgnoreCase("sts")) || (cmd.equalsIgnoreCase("settributespawn"))) {
              if ((sender instanceof Player))
              {
                player = (Player)sender;
                perm = "sg.settributespawn";
                if (player.hasPermission(perm)) {
                  try
                  {
                    int arena = Integer.parseInt(var);
                    int tribute = Integer.parseInt(var2);

                    if (isActualArena(arena))
                    {
                      if (!isArenaEnabled(arena))
                      {
                        if ((tribute > 0) && (tribute < 25))
                        {
                          loc = player.getLocation();

                          setTributeSpawn(tribute, loc, arena);

                          player.sendMessage(this.tag + "§2§lTribute " + tribute + " in arena " + arena + ", updated!");
                          player.sendMessage(this.tag + "X:§a " + loc.getBlockX() + "§2 Y: §a" + loc.getBlockY() + "§2 Z: §a" + loc.getBlockZ());
                        }
                        else
                        {
                          player.sendMessage(this.tag + "§cBad tribute ID! Must be 1 - 24!");
                        }
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§cPlease disable the arena before editing spawn points.");
                      }
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cArena ID is out of bounds!");
                    }
                  }
                  catch (NumberFormatException e)
                  {
                    player.sendMessage(this.tag + "§cBoth the tribute # and arena must be numbers.");
                  }
                }
                else
                {
                  player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                }
              }
              else
              {
                sender.sendMessage(this.tag + "§5You need to be a player to use that!");
              }
            }
            else {
              sender.sendMessage(this.tag + "§cUnrecognized command. Type §8[§4/sg help§8]§c for commands.");
            }

          }
          else if (args.length == 1) {
            String cmd = args[0].toLowerCase();
            if ((cmd.equals("help")) || (cmd.equals("?"))) {
              sender.sendMessage(this.tag + "§2§lCommands List§8 : §2< > Required || [ ] Optional");

              if ((sender.hasPermission("sg.join")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " join§9 <Arena>§7 - §3Join an arena");
              }
              if ((sender.hasPermission("sg.leave")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " leave§7 - §3Leave an arena");
              }
              if ((sender.hasPermission("sg.lobby")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " lobby§7 - §3Teleport to the SG lobby");
              }
              if ((sender.hasPermission("sg.list")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " list§9 [Arena]§7 - §3Shows tributes");
              }
              if ((sender.hasPermission("sg.map")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " map§9 [Arena]§7 - §3Shows map info for an arena");
              }
              if ((sender.hasPermission("sg.stats")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " stats§9 [Player]§7 - §3Shows stats for a player");
              }
              if ((sender.hasPermission("sg.spectate")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " spectate§9 <Arena>§7 - §3Spectates an arena");
              }
              if (sender.hasPermission("sg.arenas")) {
                sender.sendMessage(this.tag + "§a/" + typed + " arenas§7 - §3Lists off arenas and info");
              }
              if ((sender.hasPermission("sg.setlobby")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " setlobby§7 - §3Sets the lobby point to you");
              }
              if ((sender.hasPermission("sg.setarena")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " setarena§9 <Arena> <Radius>§7 - §3Sets an arena center and radius");
              }
              if ((sender.hasPermission("sg.settributespawn")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " sts§9 <Arena> <1-24>§7 - §3Sets a tribute spawn point");
              }
              if ((sender.hasPermission("sg.setchest")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " setchest§9 <Arena> <Tier>§7 - §3Registers a target chest");
              }
              if ((sender.hasPermission("sg.setchestradius")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " setchestradius§9 <Arena> <Radius>§1|§9<W> <Tier>§7 - §3Registers all chests in a radius");
              }
              if ((sender.hasPermission("sg.removechest")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " removechest§9 <Arena>§7 - §3Unregisters a target chest");
              }
              if (sender.hasPermission("sg.enable")) {
                sender.sendMessage(this.tag + "§a/" + typed + " enable§9 [Arena]§7 - §3Enable a disabled arena");
              }
              if (sender.hasPermission("sg.disable")) {
                sender.sendMessage(this.tag + "§a/" + typed + " disable§9 [Arena]§7 - §3Disable an enabled arena");
              }
              if (sender.hasPermission("sg.restock")) {
                sender.sendMessage(this.tag + "§a/" + typed + " restock§9 [Arena]§7 - §3Force a chest restock");
              }
              if (sender.hasPermission("sg.fstart")) {
                sender.sendMessage(this.tag + "§a/" + typed + " fstart§9 [Arena]§7 - §3Force starts a game");
              }
              if (sender.hasPermission("sg.fend")) {
                sender.sendMessage(this.tag + "§a/" + typed + " fend§9 [Arena]§7 - §3Force ends a game");
              }
              if (sender.hasPermission("sg.fdm")) {
                sender.sendMessage(this.tag + "§a/" + typed + " fdm§9 [Arena]§7 - §3Force a deathmatch on a game");
              }
              if ((sender.hasPermission("sg.kick")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " kick§9 <Player>§7 - §3Kick a player out of a game");
              }
              if ((sender.hasPermission("sg.wand")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " wand§7 - §3Gives you the SG wand");
              }
              if ((sender.hasPermission("sg.setheadwall")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " setheadwall §9<Arena>§7 - §3Sets a tribute display wall");
              }
              if ((sender.hasPermission("sg.settributesign")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " settributesign §9<Arena> <Tribute>§7 - §3Creates a tribute stat sign");
              }
              if ((sender.hasPermission("sg.removetributesign")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " removetributesign §9<Arena> <Tribute>§7 - §3Removes a tribute stat sign");
              }
              if ((sender.hasPermission("sg.setmapname")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " setmapname §9<Arena> <Name>§7 - §3Changes a map name for an arena (Use _ as a space)");
              }
              if ((sender.hasPermission("sg.setmapauthor")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " setmapauthor §9<Arena> <Author>§7 - §3Changes a map author for an arena (Use _ as a space)");
              }
              if ((sender.hasPermission("sg.setmaplink")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " setmaplink §9<Arena> <Link>§7 - §3Changes a map link for an arena");
              }
              if ((sender.hasPermission("sg.getconfig")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " getconfig §9<ConfigKey>§7 - §3Returns the value of a config setting");
              }
              if ((sender.hasPermission("sg.setconfig")) && ((sender instanceof Player))) {
                sender.sendMessage(this.tag + "§a/" + typed + " setconfig §9<ConfigKey> <Value>§7 - §3Changes a configuration setting");
              }
              if (sender.getName().equals("turqmelon")) {
                sender.sendMessage(this.tag + "§c/" + typed + " suspend§7 - §4Suspends SG access to this server.");
              }
              if (sender.getName().equals("turqmelon"))
                sender.sendMessage(this.tag + "§c/" + typed + " unsuspend§7 - §4Allows SG access for this server.");
            }
            else
            {
              String perm;
              if ((cmd.equalsIgnoreCase("setconfig")) || (cmd.equalsIgnoreCase("sc"))) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  perm = "sg.getconfig";
                  if (player.hasPermission(perm))
                  {
                    player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                    player.sendMessage(this.tag + "§3Changes a configuration setting");
                    player.sendMessage(this.tag + "§a/" + typed + " setconfig §9<ConfigKey> <Value>");
                    player.sendMessage(this.tag + "§7§oNote: §eA configkey is the headings + settings seperated by periods. To use the killperk effect for example, you'd use §ckillperks.effect§e.");
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else if (cmd.equalsIgnoreCase("suspend")) {
                if (sender.getName().equalsIgnoreCase("turqmelon")) {
                  getConfig().set("sg-healthy", Boolean.valueOf(true));
                  saveConfig();
                  player = (perm = Bukkit.getServer().getOnlinePlayers()).length; for (perm = 0; perm < player; perm++) { Player all = perm[perm];
                    all.kickPlayer(this.tag + "§cPolcity infraction. SG liscense has been suspended.");
                  }
                  Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "stop");
                }
              }
              else if (cmd.equalsIgnoreCase("unsuspend")) {
                if (sender.getName().equalsIgnoreCase("turqmelon")) {
                  getConfig().set("sg-healthy", Boolean.valueOf(false));
                  saveConfig();
                  player = (perm = Bukkit.getServer().getOnlinePlayers()).length; for (perm = 0; perm < player; perm++) { Player all = perm[perm];
                    all.kickPlayer(this.tag + "§cLiscense has been unsuspended. Required restart.");
                  }
                  Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "stop");
                }
              }
              else if ((cmd.equalsIgnoreCase("getconfig")) || (cmd.equalsIgnoreCase("gc"))) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  String perm = "sg.getconfig";
                  if (player.hasPermission(perm))
                  {
                    player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                    player.sendMessage(this.tag + "§3Returns the value of a config setting");
                    player.sendMessage(this.tag + "§a/" + typed + " gc §9<Arena>");
                    player.sendMessage(this.tag + "§7§oNote: §eA configkey is the headings + settings seperated by periods. To use the killperk effect for example, you'd use §ckillperks.effect§e.");
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else if ((cmd.equalsIgnoreCase("setmaplink")) || (cmd.equalsIgnoreCase("sml"))) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  String perm = "sg.setmaplink";
                  if (player.hasPermission(perm))
                  {
                    player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                    player.sendMessage(this.tag + "§3Changes a map link for an arena");
                    player.sendMessage(this.tag + "§a/" + typed + " sml §9<Arena> <Link>");
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else if ((cmd.equalsIgnoreCase("setmapauthor")) || (cmd.equalsIgnoreCase("sma"))) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  String perm = "sg.setmapauthor";
                  if (player.hasPermission(perm))
                  {
                    player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                    player.sendMessage(this.tag + "§3Changes a map author for an arena (Use _ as a space)");
                    player.sendMessage(this.tag + "§a/" + typed + " sma §9<Arena> <Author>");
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else if ((cmd.equalsIgnoreCase("setmapname")) || (cmd.equalsIgnoreCase("smn"))) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  String perm = "sg.setmapname";
                  if (player.hasPermission(perm))
                  {
                    player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                    player.sendMessage(this.tag + "§3Changes a map name for an arena (Use _ as a space)");
                    player.sendMessage(this.tag + "§a/" + typed + " smn §9<Arena> <Name>");
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else if ((cmd.equalsIgnoreCase("join")) || (cmd.equalsIgnoreCase("j"))) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  String perm = "sg.join";
                  if (player.hasPermission(perm))
                  {
                    player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                    player.sendMessage(this.tag + "§3Joins the specified arena.");
                    player.sendMessage(this.tag + "§a/" + typed + " j §9<Arena>");
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else if ((cmd.equalsIgnoreCase("leave")) || (cmd.equalsIgnoreCase("quit"))) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  String perm = "sg.leave";
                  if (player.hasPermission(perm))
                  {
                    if (isTributeAtAll(player))
                    {
                      int arena = getTributeArena(player);
                      player.sendMessage(this.tag + "Leaving §aArena " + arena + "§2...");
                      leaveGame(player, arena);
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cYou are not a participant of any arenas.");
                    }
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else if (cmd.equalsIgnoreCase("lobby")) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  String perm = "sg.lobby";
                  if (player.hasPermission(perm))
                  {
                    if (!isTributeAtAll(player))
                    {
                      player.teleport(getExitPoint());
                      player.sendMessage(this.tag + "Welcome to the Survival Games lobby!");
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cUse §8[§4/sg leave§8]§c to leave the arena.");
                    }
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else if ((cmd.equalsIgnoreCase("list")) || (cmd.equalsIgnoreCase("l"))) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  String perm = "sg.list";
                  if (player.hasPermission(perm))
                  {
                    if (isTributeAtAll(player))
                    {
                      int arena = getTributeArena(player);

                      StringBuilder list = new StringBuilder();
                      StringBuilder spec = new StringBuilder();
                      for (Player pl : getTributesOfArena(arena)) {
                        int id = ((Integer)this.gamers.get(pl.getName())).intValue();
                        if (id != 0) {
                          list.append(pl.getDisplayName() + "§8, §f");
                        }
                        else {
                          spec.append(pl.getDisplayName() + "§8, §f");
                        }
                      }

                      int alive = getTributesOfArena(arena).size();

                      player.sendMessage(this.tag + "There are §8[§a" + alive + "§8]§2 players in §aArena " + arena + "§2!");
                      player.sendMessage(this.tag + "Tributes: §f" + list);
                      player.sendMessage(this.tag + "Spectators: §f" + spec);
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cUnknown arena!§4 Please join an arena, or specify.");
                      player.sendMessage(this.tag + "§3Shows tributes.");
                      player.sendMessage(this.tag + "§a/" + typed + " l §9[Arena]");
                    }

                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else if ((cmd.equalsIgnoreCase("map")) || (cmd.equalsIgnoreCase("m"))) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  String perm = "sg.map";
                  if (player.hasPermission(perm))
                  {
                    if (isTributeAtAll(player))
                    {
                      int arena = getTributeArena(player);

                      player.sendMessage(this.tag + "Map info for §aArena " + arena + "§2...");
                      player.sendMessage(this.tag + "§eMAP | §aName: §b" + getMapName(arena));
                      player.sendMessage(this.tag + "§eMAP | §aCreator: §b" + getMapAuthor(arena));
                      player.sendMessage(this.tag + "§eMAP | §aLink: §b" + getMapLink(arena));
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cUnknown arena!§4 Please join an arena, or specify.");
                      player.sendMessage(this.tag + "§3Shows map info for an arena");
                      player.sendMessage(this.tag + "§a/" + typed + " m §9[Arena]");
                    }

                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else if (cmd.equalsIgnoreCase("stats")) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  String perm = "sg.stats";
                  if (player.hasPermission(perm))
                  {
                    if (isUsingStats())
                    {
                      player.sendMessage(this.tag + "§2§lYour Personal Stats");
                      player.sendMessage(this.tag + "Survival Games Wins: §a§l" + getWinsForPlayer(player));
                      player.sendMessage(this.tag + "Total Tribute Kills: §a§l" + getKillsForPlayer(player));
                      player.sendMessage(this.tag + "Total Chests Found: §a§l" + getOpenedChestsForPlayer(player));
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cThis server does not have stats enabled.");
                    }
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else if ((cmd.equalsIgnoreCase("spectate")) || (cmd.equalsIgnoreCase("s"))) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  String perm = "sg.spectate";
                  if (player.hasPermission(perm))
                  {
                    player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                    player.sendMessage(this.tag + "§3Spectates an arena");
                    player.sendMessage(this.tag + "§a/" + typed + " s §9<Arena>");
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else if (cmd.equalsIgnoreCase("removetributesign")) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  String perm = "sg.removetributesign";
                  if (player.hasPermission(perm))
                  {
                    player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                    player.sendMessage(this.tag + "§3Removes a tribute stat sign");
                    player.sendMessage(this.tag + "§a/" + typed + " removetributesign §9<Arena> <Tribute>");
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else if (cmd.equalsIgnoreCase("settributesign")) {
                if ((sender instanceof Player))
                {
                  Player player = (Player)sender;
                  String perm = "sg.settributesign";
                  if (player.hasPermission(perm))
                  {
                    player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                    player.sendMessage(this.tag + "§3Creates a tribute stat sign");
                    player.sendMessage(this.tag + "§a/" + typed + " settributesign §9<Arena> <Tribute>");
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else
                {
                  sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                }
              }
              else
              {
                int x;
                int y;
                if (cmd.equalsIgnoreCase("arenas")) {
                  String perm = "sg.arenas";
                  if (sender.hasPermission(perm))
                  {
                    sender.sendMessage(this.tag + "There are §8[§a" + getMaxArenas() + "§8]§2 allocated arenas.");
                    for (int i = 1; i <= getMaxArenas(); i++) {
                      String msg = "";
                      try
                      {
                        msg = msg + "§a§l " + i + " §8| §e";
                        String gameState = getGameStatus(i).toString();
                        int ticks = getTicks(i);
                        x = getArenaCenter(i).getBlockX();
                        y = getArenaCenter(i).getBlockY();
                        int z = getArenaCenter(i).getBlockZ();

                        msg = msg + x + ", " + y + ", " + z + "§8 | §2";
                        if (isArenaEnabled(i)) {
                          msg = msg + gameState + "§8 | §2";
                          msg = msg + ticks;
                        }
                        else {
                          msg = msg + "§c§lDISABLED";
                        }
                      }
                      catch (Exception e) {
                        msg = msg + "§eNot Created";
                      }

                      sender.sendMessage(msg);
                    }

                    sender.sendMessage(this.tag + "§2§oMore arenas can be allocated in the config.");
                  }
                  else
                  {
                    sender.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                  }
                }
                else if ((cmd.equalsIgnoreCase("setlobby")) || (cmd.equalsIgnoreCase("sl"))) {
                  if ((sender instanceof Player))
                  {
                    Player player = (Player)sender;
                    String perm = "sg.setlobby";
                    if (player.hasPermission(perm))
                    {
                      Location loc = player.getLocation();

                      getConfig().set("exit-location.x", Integer.valueOf(player.getLocation().getBlockX()));
                      getConfig().set("exit-location.y", Integer.valueOf(player.getLocation().getBlockY()));
                      getConfig().set("exit-location.z", Integer.valueOf(player.getLocation().getBlockZ()));
                      getConfig().set("exit-location.world", player.getLocation().getWorld().getName());

                      saveConfig();

                      player.sendMessage(this.tag + "§2§lLobby/Exit TP Location Updated!");
                      player.sendMessage(this.tag + "World: §a" + loc.getWorld().getName());
                      player.sendMessage(this.tag + "X:§a " + loc.getBlockX() + "§2 Y: §a" + loc.getBlockY() + "§2 Z: §a" + loc.getBlockZ());
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                    }
                  }
                  else
                  {
                    sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                  }

                }
                else if ((cmd.equalsIgnoreCase("setheadwall")) || (cmd.equalsIgnoreCase("shw"))) {
                  if ((sender instanceof Player))
                  {
                    Player player = (Player)sender;
                    String perm = "sg.setheadwall";
                    if (player.hasPermission(perm))
                    {
                      player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                      player.sendMessage(this.tag + "§3Sets a tribute display wall");
                      player.sendMessage(this.tag + "§a/" + typed + " shw §9<Arena>");
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                    }
                  }
                  else
                  {
                    sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                  }
                }
                else if ((cmd.equalsIgnoreCase("setarena")) || (cmd.equalsIgnoreCase("sa"))) {
                  if ((sender instanceof Player))
                  {
                    Player player = (Player)sender;
                    String perm = "sg.setarena";
                    if (player.hasPermission(perm))
                    {
                      player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                      player.sendMessage(this.tag + "§3Sets an arena center and radius");
                      player.sendMessage(this.tag + "§a/" + typed + " sa §9<Arena> <Radius>");
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                    }
                  }
                  else
                  {
                    sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                  }
                }
                else
                {
                  String[] lore;
                  if ((cmd.equalsIgnoreCase("wand")) || (cmd.equalsIgnoreCase("w"))) {
                    if ((sender instanceof Player))
                    {
                      Player player = (Player)sender;
                      String perm = "sg.wand";
                      if (player.hasPermission(perm))
                      {
                        ItemStack stack = new ItemStack(Material.STICK, 1);
                        ItemMeta meta = stack.getItemMeta();
                        meta.setDisplayName(this.tag + "Wand");
                        lore = new String[] { "§4§oLeft click for P1.", "§4§oRight click for P2." };

                        meta.setLore(Arrays.asList(lore));

                        stack.setItemMeta(meta);

                        player.getInventory().addItem(new ItemStack[] { stack });
                        player.sendMessage(this.tag + "There you go!");
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                      }
                    }
                    else
                    {
                      sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                    }
                  }
                  else if ((cmd.equalsIgnoreCase("settributespawn")) || (cmd.equalsIgnoreCase("sts"))) {
                    if ((sender instanceof Player))
                    {
                      Player player = (Player)sender;
                      String perm = "sg.settributespawn";
                      if (player.hasPermission(perm))
                      {
                        player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                        player.sendMessage(this.tag + "§3Sets a tribute spawn point");
                        player.sendMessage(this.tag + "§a/" + typed + " sts §9<Arena> <1-24>");
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                      }
                    }
                    else
                    {
                      sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                    }
                  }
                  else if ((cmd.equalsIgnoreCase("setchest")) || (cmd.equalsIgnoreCase("sc"))) {
                    if ((sender instanceof Player))
                    {
                      Player player = (Player)sender;
                      String perm = "sg.setchest";
                      if (player.hasPermission(perm))
                      {
                        player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                        player.sendMessage(this.tag + "§3Registers a target chest");
                        player.sendMessage(this.tag + "§a/" + typed + " sc §9<Arena> <Tier>");
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                      }
                    }
                    else
                    {
                      sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                    }
                  }
                  else if ((cmd.equalsIgnoreCase("setchestradius")) || (cmd.equalsIgnoreCase("scr"))) {
                    if ((sender instanceof Player))
                    {
                      Player player = (Player)sender;
                      String perm = "sg.setchestradius";
                      if (player.hasPermission(perm))
                      {
                        player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                        player.sendMessage(this.tag + "§3Registers all chests in a radius");
                        player.sendMessage(this.tag + "§a/" + typed + " scr §9<Arena> <Radius>§1|§9<W> <Tier>");
                        player.sendMessage(this.tag + "§2§oIf you have a wand selection made, you can enter \"W\" to use your selection.");
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                      }
                    }
                    else
                    {
                      sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                    }
                  }
                  else if ((cmd.equalsIgnoreCase("removechest")) || (cmd.equalsIgnoreCase("rc"))) {
                    if ((sender instanceof Player))
                    {
                      Player player = (Player)sender;
                      String perm = "sg.removechest";
                      if (player.hasPermission(perm))
                      {
                        player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                        player.sendMessage(this.tag + "§3Unregisters a target chest");
                        player.sendMessage(this.tag + "§a/" + typed + " rc §9<Arena>");
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                      }
                    }
                    else
                    {
                      sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                    }
                  }
                  else if (cmd.equalsIgnoreCase("enable")) {
                    if ((sender instanceof Player))
                    {
                      Player player = (Player)sender;
                      String perm = "sg.enable";
                      if (player.hasPermission(perm)) {
                        player.sendMessage(this.tag + "§cUnknown arena!§4 Please join an arena, or specify.");
                        player.sendMessage(this.tag + "§3Enable a disabled arena");
                        player.sendMessage(this.tag + "§a/" + typed + " enable §9[Arena]");
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                      }
                    }
                    else
                    {
                      sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                    }
                  }
                  else if (cmd.equalsIgnoreCase("disable")) {
                    if ((sender instanceof Player))
                    {
                      Player player = (Player)sender;
                      String perm = "sg.disable";
                      if (player.hasPermission(perm))
                      {
                        if (isTributeAtAll(player))
                        {
                          int arena = getTributeArena(player);

                          if (isArenaEnabled(arena))
                          {
                            x = (y = Bukkit.getServer().getOnlinePlayers()).length; for (lore = 0; lore < x; lore++) { Player all = y[lore];
                              all.sendMessage(this.tag + player.getDisplayName() + "§2 has §cDISABLED§a Arena " + arena + "§2!");
                            }

                            getConfig().set("arenas.arena" + arena + ".status", Integer.valueOf(0));
                            saveConfig();

                            if (getTributesOfArena(arena).size() > 0) {
                              for (Player pl : getTributesOfArena(arena)) {
                                leaveGame(pl, arena);
                              }
                            }

                            endGame(WinResult.ADMIN_STOP, arena);
                          }
                          else
                          {
                            player.sendMessage(this.tag + "§cArena " + arena + "§4 is already disabled.");
                          }

                        }
                        else
                        {
                          player.sendMessage(this.tag + "§cUnknown arena!§4 Please join an arena, or specify.");
                          player.sendMessage(this.tag + "§3Disable an enabled arena");
                          player.sendMessage(this.tag + "§a/" + typed + " disable §9[Arena]");
                        }
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                      }
                    }
                    else
                    {
                      sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                    }
                  }
                  else if (cmd.equalsIgnoreCase("restock")) {
                    if ((sender instanceof Player))
                    {
                      Player player = (Player)sender;
                      String perm = "sg.restock";
                      if (player.hasPermission(perm))
                      {
                        if (isTributeAtAll(player))
                        {
                          int arena = getTributeArena(player);

                          refillChests(1, arena);
                          refillChests(2, arena);

                          player.sendMessage(this.tag + "Restocked all chests in §aArena " + arena + "§2!");
                        }
                        else
                        {
                          player.sendMessage(this.tag + "§cUnknown arena!§4 Please join an arena, or specify.");
                          player.sendMessage(this.tag + "§3Force a chest restock");
                          player.sendMessage(this.tag + "§a/" + typed + " restock §9[Arena]");
                        }
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                      }
                    }
                    else
                    {
                      sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                    }
                  }
                  else if (cmd.equals("fstart")) {
                    if ((sender instanceof Player))
                    {
                      Player player = (Player)sender;

                      String perm = "sg.fstart";
                      if (player.hasPermission(perm))
                      {
                        if (isTributeAtAll(player)) {
                          int arena = getTributeArena(player);
                          if (canJoinGame(getGameStatus(arena)))
                          {
                            startGame(arena);
                            sendTributeMessage(player.getDisplayName() + "§2 force started your game!", true, arena);
                          }
                          else
                          {
                            player.sendMessage(this.tag + "§cGame already in progress.");
                          }
                        }
                        else
                        {
                          player.sendMessage(this.tag + "§cUnknown arena!§4 Please join an arena, or specify.");
                          player.sendMessage(this.tag + "§3Force starts a game");
                          player.sendMessage(this.tag + "§a/" + typed + " fstart §9[Arena]");
                        }
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                      }
                    }
                    else
                    {
                      sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                    }
                  }
                  else if (cmd.equals("fend")) {
                    if ((sender instanceof Player))
                    {
                      Player player = (Player)sender;

                      String perm = "sg.fend";
                      if (player.hasPermission(perm))
                      {
                        if (isTributeAtAll(player))
                        {
                          int arena = getTributeArena(player);

                          if (!canJoinGame(getGameStatus(arena)))
                          {
                            endGame(WinResult.ADMIN_STOP, arena);
                            sendTributeMessage(player.getDisplayName() + "§2 force ended your game!", true, arena);
                          }
                          else
                          {
                            player.sendMessage(this.tag + "§cThere is no game running.");
                          }

                        }
                        else
                        {
                          player.sendMessage(this.tag + "§cUnknown arena!§4 Please join an arena, or specify.");
                          player.sendMessage(this.tag + "§3Force ends a game");
                          player.sendMessage(this.tag + "§a/" + typed + " fend §9[Arena]");
                        }
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                      }
                    }
                    else
                    {
                      sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                    }
                  }
                  else if (cmd.equals("fdm")) {
                    if ((sender instanceof Player))
                    {
                      Player player = (Player)sender;

                      String perm = "sg.fdm";
                      if (player.hasPermission(perm))
                      {
                        if (isTributeAtAll(player))
                        {
                          int arena = getTributeArena(player);

                          if (getGameStatus(arena) == GameStatus.INGAME)
                          {
                            setTicks(61, arena);
                            sendTributeMessage(player.getDisplayName() + "§2 forced the deathmatch countdown!", true, arena);
                          }
                          else
                          {
                            player.sendMessage(this.tag + "§cThe game must be in progress.");
                          }

                        }
                        else
                        {
                          player.sendMessage(this.tag + "§cUnknown arena!§4 Please join an arena, or specify.");
                          player.sendMessage(this.tag + "§3Force starts deathmatch");
                          player.sendMessage(this.tag + "§a/" + typed + " fdm §9[Arena]");
                        }
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                      }
                    }
                    else
                    {
                      sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                    }
                  }
                  else if (cmd.equalsIgnoreCase("kick")) {
                    if ((sender instanceof Player))
                    {
                      Player player = (Player)sender;
                      String perm = "sg.kick";
                      if (player.hasPermission(perm))
                      {
                        player.sendMessage(this.tag + "§cSyntax Error!§4 Correct usage below.");
                        player.sendMessage(this.tag + "§3Kicks a player from the games.");
                        player.sendMessage(this.tag + "§a/" + typed + " kick §9<Player>");
                      }
                      else
                      {
                        player.sendMessage(this.tag + "§4You need §c" + perm + "§4 to do that.");
                      }
                    }
                    else
                    {
                      sender.sendMessage(this.tag + "§5You need to be a player to use that!");
                    }
                  }
                  else
                    sender.sendMessage(this.tag + "§cUnrecognized command. Type §8[§4/sg help§8]§c for commands."); 
                }
              }
            }
          }
        }
      }
      return true;
    }

    return false;
  }

  public boolean isArenaEnabled(int arena) {
    int status = getConfig().getInt("arenas.arena" + arena + ".status");
    if (status == 0) {
      return false;
    }
    if (status == 1) {
      return true;
    }

    return false;
  }

  public World getSurvivalGamesWorld(int arena)
  {
    try {
      World world = Bukkit.getWorld(getConfig().getString("arenas.arena" + arena + ".world"));

      if (world != null) {
        return world;
      }

      log("SEVERE! CANNOT LOAD SURVIVAL GAMES WORLD FOR ARENA " + arena + "!!!");
      return (World)Bukkit.getWorlds().get(0);
    } catch (Exception e) {
    }
    return (World)Bukkit.getWorlds().get(0);
  }

  public boolean isTributeAtAll(Player player)
  {
    if (this.gamers.containsKey(player.getName())) {
      return true;
    }

    return false;
  }

  public boolean isTributeOfArena(Player player, int arena)
  {
    if (this.gamers.containsKey(player.getName())) {
      int a = ((Integer)this.gamerOfArena.get(player.getName())).intValue();
      if (arena == a) {
        return true;
      }

      return false;
    }

    return false;
  }

  public void sendNonTributeMessage(String msg, boolean showTag)
  {
    if (showTag) {
      msg = this.tag + msg;
    }

    for (Player all : Bukkit.getServer().getOnlinePlayers())
      if (!isTributeAtAll(all))
        all.sendMessage(msg);
  }

  public void setTicks(int ticks, int arena)
  {
    this.gameTicks.put(Integer.valueOf(arena), Integer.valueOf(ticks));
  }

  public int getTicks(int arena) {
    if (this.gameTicks.containsKey(Integer.valueOf(arena))) {
      return ((Integer)this.gameTicks.get(Integer.valueOf(arena))).intValue();
    }

    return 0;
  }

  public void enterWarmup(int arena)
  {
    setTicks(getStartingCountdown(), arena);

    setGameStatus(GameStatus.STARTING, arena);
    sendNonTributeMessage("§aArena " + arena + " §astarting in §8[§2" + getStartingCountdown() + "§8]§a seconds!", true);
  }

  public String getMapLink(int arena)
  {
    return getConfig().getString("mapinfo.arena" + arena + ".link");
  }

  public String getMapAuthor(int arena) {
    return getConfig().getString("mapinfo.arena" + arena + ".author");
  }

  public String getMapName(int arena) {
    return getConfig().getString("mapinfo.arena" + arena + ".name");
  }

  public Player getTributeFromID(int arena, int id) {
    String result = "";

    for (Player pl : getTributesOfArena(arena)) {
      int spawn = ((Integer)this.gamers.get(pl.getName())).intValue();
      if (spawn == id) {
        result = pl.getName();
        break;
      }
    }

    return Bukkit.getServer().getPlayer(result);
  }

  public void spectateSG(Player player, int arena, boolean goRightToTrib, int targetTrib)
  {
    if (this.gamers.containsKey(player.getName())) {
      return;
    }

    if (isArenaEnabled(arena))
    {
      log("Spectator " + player.getName() + " joined arena " + arena + "!");

      for (PotionEffect effect : player.getActivePotionEffects()) {
        if (player.hasPotionEffect(effect.getType())) {
          player.removePotionEffect(effect.getType());
        }
      }

      this.gamers.put(player.getName(), Integer.valueOf(0));
      this.gamerOfArena.put(player.getName(), Integer.valueOf(arena));
      Location loc = getArenaCenter(arena);
      int highestBlock = getSurvivalGamesWorld(arena).getHighestBlockYAt(loc);
      loc.setY(highestBlock);
      player.teleport(loc);

      player.getInventory().clear();
      player.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
      player.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
      player.getInventory().setLeggings(new ItemStack(Material.AIR, 1));
      player.getInventory().setBoots(new ItemStack(Material.AIR, 1));
      player.setHealth(20.0D);
      player.setFoodLevel(20);
      player.setSaturation(8.0F);
      String gm = getConfig().getString("starting.force-gamemode").toUpperCase();

      player.setGameMode(GameMode.valueOf(gm));
      player.sendMessage(this.tag + "Welcome to " + this.name + "§2!");
      player.sendMessage(this.tag + "You are spectating§a Arena " + arena + "§2!");
      if (!goRightToTrib)
      {
        player.sendMessage(this.tag + "Use your watch to teleport to different tributes.");
        player.sendMessage(this.tag + "Type §8[§a/sg leave§8]§2 to leave.");
      }
      else {
        Player target = getTributeFromID(arena, targetTrib);
        player.sendMessage(this.tag + "Teleporting you to " + target.getDisplayName() + "§2.");
        player.teleport(target.getLocation());
      }

      if (isSponsoringEnabled()) {
        player.sendMessage(this.tag + "§3Right click tributes to sponsor them!");
      }

      refreshSpectator(player);
      player.updateInventory();
    }
    else
    {
      log("Failed to send " + player.getName() + " to arena " + arena + " because it's disabled!");
    }
  }

  public void joinSG(final Player player, final int arena)
  {
    if (this.gamers.containsKey(player.getName())) {
      return;
    }

    if (isArenaEnabled(arena))
    {
      log("Tribute " + player.getName() + " joined arena " + arena + "!");

      int next = getNextTributeSlot(arena);

      for (PotionEffect effect : player.getActivePotionEffects()) {
        if (player.hasPotionEffect(effect.getType())) {
          player.removePotionEffect(effect.getType());
        }
      }

      player.setFlying(false);
      player.setAllowFlight(false);

      int val = getRemainingTributes(arena) + 1;

      sendTributeMessage(player.getDisplayName() + "§2 joined! §8[§a" + val + "§8] §2Tributes", true, arena);
      this.gamers.put(player.getName(), Integer.valueOf(next));
      this.gamerOfArena.put(player.getName(), Integer.valueOf(arena));
      player.setFallDistance(0.0F);
      player.teleport(getTributeSpawn(next, arena).add(0.0D, 2.0D, 0.0D));

      player.getInventory().clear();
      player.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
      player.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
      player.getInventory().setLeggings(new ItemStack(Material.AIR, 1));
      player.getInventory().setBoots(new ItemStack(Material.AIR, 1));
      player.setHealth(20.0D);
      player.setFallDistance(0.0F);
      player.setFoodLevel(20);
      player.setSaturation(8.0F);
      String gm = getConfig().getString("starting.force-gamemode").toUpperCase();

      player.setGameMode(GameMode.valueOf(gm));

      player.sendMessage(this.tag + "Welcome to " + this.name + "§2!");
      player.sendMessage(this.tag + "There are §8[§a" + getTributesOfArena(arena).size() + "§8]§2 of §8[§a24§8]§2 tributes!");
      int needed = getWhenGameShouldStartPlayers();
      if (getTributesOfArena(arena).size() < needed) {
        player.sendMessage(this.tag + "Games begin when we have §8[§a" + needed + "§8]§2 tributes!");
      }
      else {
        player.sendMessage(this.tag + "Games starting in " + formatTime(getTicks(arena)) + "!");
      }
      player.sendMessage(this.tag + "Type §8[§a/sg leave§8]§2 to leave.");

      for (Player pl : getTributesOfArena(arena)) {
        player.playSound(pl.getLocation(), Sound.ORB_PICKUP, 1.0F, -1.0F);
      }

      if ((getTributesOfArena(arena).size() >= needed) && (getGameStatus(arena) == GameStatus.IDLE)) {
        enterWarmup(arena);
        sendTributeMessage("§3Games starting in §8[§b" + getStartingCountdown() + "§8] §3seconds!", true, arena);
      }

      if (player.hasPermission("sg.canusekits")) {
        Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable()
        {
          public void run() {
            List allowedKits = Core.this.getConfig().getStringList("arenas.arena" + arena + ".available-kits");
            if (allowedKits.size() > 0) {
              player.sendMessage("§8§m-----§8" + Core.this.tag + "§8§m------------------------------");
              player.sendMessage("§2§lSELECT A KIT");
              for (String entry : allowedKits) {
                if (player.hasPermission("sg.kit." + entry.toLowerCase())) {
                  String description = Core.this.getConfig().getString("kits." + entry.toLowerCase() + ".description");
                  player.sendMessage("§a§l/" + WordUtils.capitalizeFully(entry) + "§8 - §7" + description);
                }
                else {
                  player.sendMessage("§c§l/" + WordUtils.capitalizeFully(entry) + "§8 - §4" + "You do not have this kit!");
                }
              }
              player.sendMessage("§8§m------------------------------§8" + Core.this.tag + "§8§m-----");
              player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0F, 2.0F);
            }
          }
        }
        , 40L);
      }
    }
    else
    {
      log("Failed to send " + player.getName() + " to arena " + arena + " because it's disabled!");
    }
  }

  public boolean isSameWorld(World world1, World world2) {
    if (world1.getName().equals(world2.getName())) {
      return true;
    }

    return false;
  }

  public int getClosestArena(Player player)
  {
    World world = player.getWorld();

    int nearest = 0;
    double nearestValue = 1.7976931348623157E+308D;

    for (int i = 1; i <= getMaxArenas(); i++) {
      World sgWorld = getSurvivalGamesWorld(i);

      if (isSameWorld(world, sgWorld))
      {
        double distance = player.getLocation().distance(getArenaCenter(i));
        if (distance < nearestValue) {
          nearest = i;
          nearestValue = distance;
        }

      }

    }

    return nearest;
  }

  @EventHandler
  public void onBreak(BlockBreakEvent event)
  {
    Player player = event.getPlayer();
    Block block = event.getBlock();

    if (isTributeAtAll(player))
    {
      int arena = getTributeArena(player);
      int id = ((Integer)this.gamers.get(player.getName())).intValue();

      if (id == 0) {
        event.setCancelled(true);
        return;
      }

      if (!getAllowedBlocksToBreak().contains(block.getType())) {
        player.sendMessage(this.tag + "§cYou can't break " + block.getType().toString().toLowerCase().replace("_", " ") + "!");
        event.setCancelled(true);
      }
      else {
        String key = block.getLocation().getWorld().getName() + ">" + block.getLocation().getBlockX() + ">" + block.getLocation().getBlockY() + ">" + 
          block.getLocation().getBlockZ() + ">" + arena;
        this.brokenBlocks.put(key, block.getType());
      }
    }
  }

  @EventHandler
  public void onHit(ProjectileHitEvent event)
  {
    Entity entity = event.getEntity();

    if ((entity instanceof Projectile)) {
      Projectile proj = (Projectile)entity;

      LivingEntity shooter = proj.getShooter();

      if ((shooter instanceof Player))
      {
        Player player = (Player)shooter;

        if ((proj instanceof Snowball))
        {
          Snowball snow = (Snowball)proj;

          if ((useSlowingSnowballs()) && (isTributeAtAll(player)))
          {
            List near = snow.getNearbyEntities(3.0D, 3.0D, 3.0D);
            for (Entity entry : near) {
              if ((entry instanceof LivingEntity)) {
                LivingEntity victim = (LivingEntity)entry;

                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0));
              }
            }

            snow.getWorld().createExplosion(snow.getLocation(), 0.0F);
          }

        }
        else if ((proj instanceof Egg))
        {
          Egg egg = (Egg)proj;

          if ((useFlashBangEggs()) && (isTributeAtAll(player)))
          {
            List near = egg.getNearbyEntities(5.0D, 5.0D, 5.0D);
            for (Entity entry : near) {
              if ((entry instanceof LivingEntity)) {
                LivingEntity victim = (LivingEntity)entry;

                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 0));
              }
            }

            egg.getWorld().createExplosion(egg.getLocation(), 0.0F);
          }
        }
      }
    }
  }

  public void clearGroundItems(int arena)
  {
    World world = getSurvivalGamesWorld(arena);
    for (Entity entity : world.getEntities())
      if ((entity instanceof Item))
      {
        Location center = getArenaCenter(arena);
        Location item = entity.getLocation();

        double distance = item.distance(center);

        int radius = getArenaRadius(arena);

        if (distance <= radius)
          entity.remove();
      }
  }

  @EventHandler
  public void onPlace(BlockPlaceEvent event)
  {
    Player player = event.getPlayer();
    Block block = event.getBlock();

    if (isTributeAtAll(player))
    {
      int arena = getTributeArena(player);

      int id = ((Integer)this.gamers.get(player.getName())).intValue();

      if (id == 0) {
        event.setCancelled(true);
        return;
      }

      if ((useInstaTNT()) && (block.getType() == Material.TNT))
      {
        block.setType(Material.AIR);
        TNTPrimed tnt = (TNTPrimed)block.getWorld().spawn(block.getLocation(), TNTPrimed.class);

        tnt.setFuseTicks(40);
      }
      else if (!getAllowsBlocksToPlace().contains(block.getType())) {
        player.sendMessage(this.tag + "§cYou can't place " + block.getType().toString().toLowerCase().replace("_", " ") + "!");
        event.setCancelled(true);
      }
      else {
        String key = block.getLocation().getWorld().getName() + ">" + block.getLocation().getBlockX() + ">" + block.getLocation().getBlockY() + ">" + 
          block.getLocation().getBlockZ() + ">" + arena;
        this.placedBlocks.put(key, block.getType());
      }
    }
  }

  public void refillChestsForNight(int arena)
  {
    for (Location loc : getChests(1, arena)) {
      Block block = loc.getBlock();

      loc.getBlock().getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 1);
      try {
        if (block.getType() == Material.CHEST) {
          Chest chest = (Chest)block.getState();
          Inventory inv = chest.getInventory();

          inv.clear();

          chest.update();
          chest.update(true);

          int slotsToFill = new Random().nextInt(27);

          int minSlots = getConfig().getInt("chestloot.minimum-items-per-chest");
          int maxSlots = getConfig().getInt("chestloot.maximum-items-per-chest");

          if (slotsToFill < minSlots) {
            slotsToFill = minSlots;
          }
          else if (slotsToFill > maxSlots) {
            slotsToFill = maxSlots;
          }

          for (int i = 0; i < slotsToFill; i++) {
            int slot = new Random().nextInt(inv.getSize() - 1);

            List possible = getPossibleLoot(2);
            Collections.shuffle(possible);

            inv.setItem(slot, (ItemStack)possible.get(0));
          }

          chest.getInventory().setContents(inv.getContents());

          chest.update();
          chest.update(true);
        }
        else
        {
          log("ERROR! Block at " + loc + " is NOT a chest! Please correct this!");
        }
      }
      catch (Exception localException) {
      }
    }
    for (Location loc : getChests(2, arena)) {
      Block block = loc.getBlock();

      loc.getBlock().getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 1);
      try {
        if (block.getType() == Material.CHEST) {
          Chest chest = (Chest)block.getState();
          Inventory inv = chest.getInventory();

          inv.clear();

          chest.update();
          chest.update(true);

          int slotsToFill = new Random().nextInt(5);
          if (slotsToFill <= 0) {
            slotsToFill = 1;
          }

          for (int i = 0; i < slotsToFill; i++) {
            int slot = new Random().nextInt(inv.getSize() - 1);

            List possible = getPossibleLoot(2);
            Collections.shuffle(possible);

            inv.setItem(slot, (ItemStack)possible.get(0));
          }

          chest.getInventory().setContents(inv.getContents());

          chest.update();
          chest.update(true);
        }
        else
        {
          log("ERROR! Block at " + loc + " is NOT a chest! Please correct this!");
        }
      }
      catch (Exception localException1) {
      }
    }
  }

  public void refillChests(int tier, int arena) {
    for (Location loc : getChests(tier, arena)) {
      Block block = loc.getBlock();

      loc.getBlock().getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 1);
      try
      {
        if (block.getType() == Material.CHEST) {
          Chest chest = (Chest)block.getState();
          Inventory inv = chest.getInventory();

          inv.clear();

          chest.update();
          chest.update(true);

          int slotsToFill = new Random().nextInt(27);

          int minSlots = getConfig().getInt("chestloot.minimum-items-per-chest");
          int maxSlots = getConfig().getInt("chestloot.maximum-items-per-chest");

          if (slotsToFill < minSlots) {
            slotsToFill = minSlots;
          }
          else if (slotsToFill > maxSlots) {
            slotsToFill = maxSlots;
          }

          for (int i = 0; i < slotsToFill; i++) {
            int slot = new Random().nextInt(inv.getSize() - 1);

            List possible = getPossibleLoot(tier);
            Collections.shuffle(possible);

            inv.setItem(slot, (ItemStack)possible.get(0));
          }

          chest.getInventory().setContents(inv.getContents());

          chest.update();
          chest.update(true);
        }
        else
        {
          log("ERROR! Block at " + loc + " is NOT a chest! Please correct this!");
        }
      }
      catch (Exception localException)
      {
      }
    }
  }

  public List<Location> getChests(int tier, int arena)
  {
    List vals = new ArrayList();
    for (String entry : getConfig().getStringList("chests.arena" + arena + ".tier" + tier))
    {
      String[] raw = entry.split(">");

      int x = Integer.parseInt(raw[0]);
      int y = Integer.parseInt(raw[1]);
      int z = Integer.parseInt(raw[2]);

      vals.add(new Location(getSurvivalGamesWorld(arena), x, y, z));
    }

    return vals;
  }

  public List<ItemStack> getPossibleLoot(int tier) {
    List vals = new ArrayList();
    for (String entry : getConfig().getStringList("chestloot.tier" + tier)) {
      String[] raw = entry.split(">");

      Material mat = Material.getMaterial(Integer.parseInt(raw[0]));
      int amt = new Random().nextInt(Integer.parseInt(raw[1]) + 1);

      if (amt <= 0) {
        amt = 1;
      }

      vals.add(new ItemStack(mat, amt));
    }

    return vals;
  }

  public Inventory generateSpectatorMenu(int arena)
  {
    Inventory inv = Bukkit.createInventory(null, 27, "§2§lArena " + arena);

    int slot = 0;
    for (Player trib : getTributesOfArena(arena)) {
      int id = ((Integer)this.gamers.get(trib.getName())).intValue();
      if (id != 0)
      {
        if (slot > 24)
          break;
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        ItemMeta meta = head.getItemMeta();

        meta.setDisplayName(trib.getName());
        int xp = (int)trib.getExp();
        String[] desc = { "§8§m--------------------", "§4Health:§c " + trib.getHealth() + "§f/§c" + trib.getMaxHealth(), "§6Hunger:§e " + trib.getFoodLevel() + "§f/§e" + 20, "§2Level: §a" + trib.getLevel(), "§3EXP: §b" + xp + "§f/§b" + trib.getExpToLevel(), "§5§oClick to TP!", this.tag };

        meta.setLore(Arrays.asList(desc));

        head.setItemMeta(meta);

        inv.setItem(slot, head);
        slot++;
      }

    }

    ItemStack leave = new ItemStack(Material.ARROW, 1);
    ItemMeta meta = leave.getItemMeta();

    meta.setDisplayName("Click to leave the game!");
    leave.setItemMeta(meta);

    inv.setItem(26, leave);

    return inv;
  }

  public void refreshSpectator(Player player) {
    if (isTributeAtAll(player)) {
      int id = ((Integer)this.gamers.get(player.getName())).intValue();
      if (id == 0) {
        player.getInventory().clear();
        giveSpectatorWatch(player);
        player.setAllowFlight(true);
        player.setFlying(true);
        for (Player all : player.getWorld().getPlayers())
          if (isTributeAtAll(all)) {
            all.hidePlayer(player);
            int id2 = ((Integer)this.gamers.get(all.getName())).intValue();
            if (id2 != 0) {
              player.showPlayer(all);
            }
            else
              player.hidePlayer(all);
          }
      }
    }
  }

  @EventHandler
  public void onTeleport(PlayerTeleportEvent event)
  {
    Player player = event.getPlayer();

    refreshSpectator(player);
  }

  public boolean isGracePeriod(int arena)
  {
    if (this.graceSeconds > 0) {
      int totalTime = getDeathmatchTimeTrigger() * 60;
      int ticks = getTicks(arena);
      int diff = totalTime - ticks;
      if (diff <= this.graceSeconds) {
        return true;
      }
    }
    return false;
  }

  @EventHandler
  public void onDamage(EntityDamageByEntityEvent event) {
    Entity entity2 = event.getDamager();

    if ((entity2 instanceof Player)) {
      Player player = (Player)entity2;

      if (isTributeAtAll(player)) {
        int id = ((Integer)this.gamers.get(player.getName())).intValue();
        if ((id == 0) || (canJoinGame(getGameStatus(getTributeArena(player))))) {
          event.setCancelled(true);
        }
        else if (isGracePeriod(getTributeArena(player))) {
          player.sendMessage(this.tag + "§cCan't attack players during grace period!");
          event.setCancelled(true);
        }
      }
    }
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent event)
  {
    Player player = event.getEntity();
    Entity killer = player.getKiller();

    if (isTributeAtAll(player)) {
      if ((killer instanceof Player)) {
        Player k = (Player)killer;
        if (isTributeAtAll(k)) {
          setKilsForPlayer(k, getKillsForPlayer(k) + 1);
          givePotionEffectForKill(k);
        }
      }

      int arena = getTributeArena(player);

      int id = ((Integer)this.gamers.get(player.getName())).intValue();

      if (id != 0) {
        shootFirework(player.getLocation());
        eliminateTribute(player, event.getDeathMessage(), arena);
      }
      else {
        leaveGame(player, arena);
      }
      event.setDeathMessage(null);
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event)
  {
    Player player = event.getPlayer();

    if (isTributeAtAll(player))
    {
      int arena = getTributeArena(player);

      leaveGame(player, arena);
    }
  }

  @EventHandler
  public void OnDrop(PlayerDropItemEvent event)
  {
    Player player = event.getPlayer();

    if (isTributeAtAll(player)) {
      int id = ((Integer)this.gamers.get(player.getName())).intValue();
      if (id == 0) {
        event.setCancelled(true);
      }
      else if (canJoinGame(getGameStatus(getTributeArena(player))))
        event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPickup(PlayerPickupItemEvent event)
  {
    Player player = event.getPlayer();

    if (isTributeAtAll(player)) {
      int id = ((Integer)this.gamers.get(player.getName())).intValue();
      if (id == 0)
        event.setCancelled(true);
    }
  }

  @EventHandler
  public void onSignChange(SignChangeEvent event)
  {
    Player player = event.getPlayer();
    String coreLine = event.getLine(0);

    if ((coreLine.equalsIgnoreCase("[SG]")) || (coreLine.equalsIgnoreCase("[SurvivalGames]")) || (coreLine.equalsIgnoreCase("[MelonSG]")))
    {
      String coreTag = getConfig().getString("branding.sign-tag").replace("&", "§");

      event.setLine(0, coreTag);

      String cmd = event.getLine(1);

      if (cmd.equalsIgnoreCase("join"))
      {
        if (player.hasPermission("sg.makesign.join"))
        {
          try
          {
            int arena = Integer.parseInt(event.getLine(2));

            if (isActualArena(arena))
            {
              event.setLine(1, "§2§lJoin Game");
              event.setLine(2, "§nArena " + arena);
            }
            else
            {
              event.setLine(1, "§f" + arena);
              event.setLine(2, "§4is not an");
              event.setLine(3, "§4arena!");
            }
          }
          catch (NumberFormatException e) {
            event.setLine(1, "§f" + event.getLine(2));
            event.setLine(2, "§4is not a");
            event.setLine(3, "§4valid number!");
          }
        }
        else
        {
          event.setLine(1, "§4You do not");
          event.setLine(2, "§4have");
          event.setLine(3, "§4permission!");
        }

      }
      else if (cmd.equalsIgnoreCase("spectate"))
      {
        if (player.hasPermission("sg.makesign.spectate"))
        {
          try
          {
            int arena = Integer.parseInt(event.getLine(2));

            if (isActualArena(arena))
            {
              event.setLine(1, "§2§lWatch Game");
              event.setLine(2, "§nArena " + arena);
            }
            else
            {
              event.setLine(1, "§f" + arena);
              event.setLine(2, "§4is not an");
              event.setLine(3, "§4arena!");
            }
          }
          catch (NumberFormatException e) {
            event.setLine(1, "§f" + event.getLine(2));
            event.setLine(2, "§4is not a");
            event.setLine(3, "§4valid number!");
          }
        }
        else
        {
          event.setLine(1, "§4You do not");
          event.setLine(2, "§4have");
          event.setLine(3, "§4permission!");
        }

      }
      else if (cmd.equalsIgnoreCase("list"))
      {
        if (player.hasPermission("sg.makesign.list"))
        {
          try
          {
            int arena = Integer.parseInt(event.getLine(2));

            if (isActualArena(arena))
            {
              event.setLine(1, "§2§lWho's Left?");
              event.setLine(2, "§nArena " + arena);
            }
            else
            {
              event.setLine(1, "§f" + arena);
              event.setLine(2, "§4is not an");
              event.setLine(3, "§4arena!");
            }
          }
          catch (NumberFormatException e) {
            event.setLine(1, "§f" + event.getLine(2));
            event.setLine(2, "§4is not a");
            event.setLine(3, "§4valid number!");
          }
        }
        else
        {
          event.setLine(1, "§4You do not");
          event.setLine(2, "§4have");
          event.setLine(3, "§4permission!");
        }

      }
      else if (cmd.equalsIgnoreCase("map"))
      {
        if (player.hasPermission("sg.makesign.map"))
        {
          try
          {
            int arena = Integer.parseInt(event.getLine(2));

            if (isActualArena(arena))
            {
              event.setLine(1, "§2§lMap Info");
              event.setLine(2, "§nArena " + arena);
            }
            else
            {
              event.setLine(1, "§f" + arena);
              event.setLine(2, "§4is not an");
              event.setLine(3, "§4arena!");
            }
          }
          catch (NumberFormatException e) {
            event.setLine(1, "§f" + event.getLine(2));
            event.setLine(2, "§4is not a");
            event.setLine(3, "§4valid number!");
          }
        }
        else
        {
          event.setLine(1, "§4You do not");
          event.setLine(2, "§4have");
          event.setLine(3, "§4permission!");
        }

      }
      else if (cmd.equalsIgnoreCase("status"))
      {
        if (player.hasPermission("sg.makesign.status"))
        {
          try
          {
            int arena = Integer.parseInt(event.getLine(2));

            if (isActualArena(arena))
            {
              event.setLine(1, "§2§lStatus");
              event.setLine(2, "§nArena " + arena);

              setArenaSign(arena, event.getBlock().getLocation());
            }
            else
            {
              event.setLine(1, "§f" + arena);
              event.setLine(2, "§4is not an");
              event.setLine(3, "§4arena!");
            }
          }
          catch (NumberFormatException e) {
            event.setLine(1, "§f" + event.getLine(2));
            event.setLine(2, "§4is not a");
            event.setLine(3, "§4valid number!");
          }
        }
        else
        {
          event.setLine(1, "§4You do not");
          event.setLine(2, "§4have");
          event.setLine(3, "§4permission!");
        }

      }
      else if (cmd.equalsIgnoreCase("auto"))
      {
        if (player.hasPermission("sg.makesign.auto"))
        {
          event.setLine(1, "§2§lFind me");
          event.setLine(2, "§2§la game!");
        }
        else
        {
          event.setLine(1, "§4You do not");
          event.setLine(2, "§4have");
          event.setLine(3, "§4permission!");
        }
      }
      else
      {
        event.setLine(1, "§4Unknown sign!");
        event.setLine(2, "§4Auto List Map");
        event.setLine(3, "§4Spectate Join");
      }
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event)
  {
    Player player = event.getPlayer();

    ItemStack inHand = player.getItemInHand();
    if (inHand.getType() == Material.WATCH)
    {
      if (isTributeAtAll(player)) {
        int id = ((Integer)this.gamers.get(player.getName())).intValue();
        if (id == 0)
        {
          event.setCancelled(true);
          player.openInventory(generateSpectatorMenu(getTributeArena(player)));
          player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1.0F, -1.0F);
          return;
        }
      }

    }

    if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
    {
      if (isTributeAtAll(player)) {
        int id = ((Integer)this.gamers.get(player.getName())).intValue();
        if (id == 0) {
          event.setCancelled(true);
        }
      }

      Block block = event.getClickedBlock();

      if (block.getType() == Material.CHEST) {
        Chest chest = (Chest)block.getState();

        if (isTributeAtAll(player)) {
          int id = ((Integer)this.gamers.get(player.getName())).intValue();
          if (id != 0) {
            int inInv = 0;
            for (ItemStack stack : chest.getInventory().getContents()) {
              if (stack != null) {
                inInv++;
              }
            }

            if (inInv > 0)
              setOpenedChestsForPlayer(player, getOpenedChestsForPlayer(player) + 1);
          }
          else
          {
            event.setCancelled(true);
          }
        }

      }
      else if ((block.getType() == Material.WALL_SIGN) || (block.getType() == Material.SIGN_POST))
      {
        Sign sign = (Sign)block.getState();
        String t = getConfig().getString("branding.sign-tag").replace("&", "§");

        if ((sign.getLine(0).equalsIgnoreCase(t)) || (sign.getLine(0).equalsIgnoreCase("§5[MelonSG]")))
        {
          String cmd = sign.getLine(1);

          if (cmd.equalsIgnoreCase("§2§lJoin Game")) {
            int arena = Integer.parseInt(sign.getLine(2).replace("§nArena ", ""));

            player.chat("/survivalgames join " + arena);
          }
          else if (cmd.equalsIgnoreCase("§2§lWatch Game")) {
            int arena = Integer.parseInt(sign.getLine(2).replace("§nArena ", ""));

            player.chat("/survivalgames spectate " + arena);
          }
          else if (cmd.equalsIgnoreCase("§2§lWho's Left?")) {
            int arena = Integer.parseInt(sign.getLine(2).replace("§nArena ", ""));

            player.chat("/survivalgames list " + arena);
          }
          else if (cmd.equalsIgnoreCase("§2§lMap Info")) {
            int arena = Integer.parseInt(sign.getLine(2).replace("§nArena ", ""));

            player.chat("/survivalgames map " + arena);
          }
          else if (cmd.equalsIgnoreCase("§2§lFind me"))
          {
            if (player.hasPermission("sg.autofinder"))
            {
              int found = 0;
              for (int i = 1; i <= getMaxArenas(); i++) {
                GameStatus s = getGameStatus(i);
                int id;
                String name;
                Player target;
                int arena;
                int i;
                ItemStack stack;
                ItemStack stack;
                boolean check;
                Location loc;
                World world;
                int id;
                Location loc;
                World world;
                if ((s == GameStatus.IDLE) || (s == GameStatus.STARTING)) {
                  int tribs = getTributesOfArena(i).size();
                  if ((tribs < 24) && (isArenaEnabled(i))) {
                    found = 1;
                    player.chat("/survivalgames join " + i);
                    break;
                  }
                }

              }

              if (found == 0) {
                player.sendMessage(this.tag + "§cNo open games found at this time.");
              }
            }
            else
            {
              player.sendMessage(this.tag + "§4You don't have permission to use the auto game finder.");
            }

          }

        }
        else if (sign.getLine(0).startsWith("§nTribute"))
        {
          try {
            id = Integer.parseInt(sign.getLine(0).replace("§nTribute ", ""));
            name = sign.getLine(1);

            target = Bukkit.getServer().getPlayer(name);
            if (target == null) return;
            if (isTributeAtAll(target)) {
              arena = getTributeArena(target);

              if (player.hasPermission("sg.spectate"))
              {
                if ((player.hasPermission("sg.spectate." + target)) || (player.hasPermission("sg.spectate.all")))
                {
                  i = 0;
                  for (stack : player.getInventory().getContents()) {
                    if (stack != null)
                    {
                      if (stack.getType() != Material.AIR) {
                        i++;
                      }
                    }
                  }

                  for (stack : player.getInventory().getArmorContents()) {
                    if (stack != null)
                    {
                      if (stack.getType() != Material.AIR) {
                        i++;
                      }
                    }

                  }

                  check = getConfig().getBoolean("starting.make-sure-inv-is-empty");
                  if (!check) {
                    i = 0;
                  }

                  if (i == 0)
                  {
                    if (!canJoinGame(getGameStatus(arena)))
                    {
                      spectateSG(player, arena, true, id);
                    }
                    else
                    {
                      player.sendMessage(this.tag + "§cArena §8[§4" + target + "§8]§c hasn't yet started!");
                    }
                  }
                  else
                  {
                    player.sendMessage(this.tag + "§cYou must have an empty inventory to spectate!");
                  }
                }
                else
                {
                  player.sendMessage(this.tag + "§cYou do not have access to Arena §8[§4" + target + "§8]§c!");
                }
              }
              else
              {
                player.sendMessage(this.tag + "§cYou don't have permission to watch games.");
              }
            }
            else
            {
              player.sendMessage(this.tag + "§cTribute not found.");
            }

          }
          catch (NumberFormatException e)
          {
            player.sendMessage(this.tag + "§cFailed to get tribute ID.");
          }

        }

      }
      else if ((inHand.getType() == Material.STICK) && 
        (inHand.getItemMeta() != null) && 
        (inHand.getItemMeta().getDisplayName().equalsIgnoreCase(this.tag + "Wand"))) {
        event.setCancelled(true);
        this.wandP2.put(player.getName(), event.getClickedBlock().getLocation());
        player.sendMessage(this.tag + "§2§lPosition 2 set!");
        loc = event.getClickedBlock().getLocation();
        player.sendMessage(this.tag + "World: §a" + loc.getWorld().getName());
        player.sendMessage(this.tag + "X:§a " + loc.getBlockX() + "§2 Y: §a" + loc.getBlockY() + "§2 Z: §a" + loc.getBlockZ());

        if (this.wandP1.containsKey(player.getName())) {
          world = ((Location)this.wandP1.get(player.getName())).getWorld();

          if (!isSameWorld(world, player.getWorld())) {
            this.wandP1.remove(player.getName());
          }

        }

      }

    }
    else if (event.getAction() == Action.PHYSICAL) {
      if (isTributeAtAll(player)) {
        id = ((Integer)this.gamers.get(player.getName())).intValue();
        if (id == 0) {
          event.setCancelled(true);
        }
      }
    }
    else if ((event.getAction() == Action.LEFT_CLICK_BLOCK) && 
      (inHand.getType() == Material.STICK) && 
      (inHand.getItemMeta().getDisplayName().equalsIgnoreCase(this.tag + "Wand"))) {
      event.setCancelled(true);
      this.wandP1.put(player.getName(), event.getClickedBlock().getLocation());
      player.sendMessage(this.tag + "§2§lPosition 1 set!");
      loc = event.getClickedBlock().getLocation();
      player.sendMessage(this.tag + "World: §a" + loc.getWorld().getName());
      player.sendMessage(this.tag + "X:§a " + loc.getBlockX() + "§2 Y: §a" + loc.getBlockY() + "§2 Z: §a" + loc.getBlockZ());
      if (this.wandP2.containsKey(player.getName())) {
        world = ((Location)this.wandP2.get(player.getName())).getWorld();

        if (!isSameWorld(world, player.getWorld()))
          this.wandP2.remove(player.getName());
      }
    }
  }

  public Location getExitPoint()
  {
    String world = getConfig().getString("exit-location.world");
    int x = getConfig().getInt("exit-location.x");
    int y = getConfig().getInt("exit-location.y");
    int z = getConfig().getInt("exit-location.z");

    return new Location(Bukkit.getWorld(world), x, y, z);
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event)
  {
    Player player = event.getPlayer();
    Location loc = event.getTo();

    if (isTributeAtAll(player))
    {
      int arena = getTributeArena(player);

      if (canJoinGame(getGameStatus(arena)))
      {
        int id = ((Integer)this.gamers.get(player.getName())).intValue();

        if (id != 0)
        {
          Location assigned = getTributeSpawn(id, arena);

          if ((loc.getBlockX() != assigned.getBlockX()) || (loc.getBlockZ() != assigned.getBlockZ())) {
            assigned.setPitch(player.getLocation().getPitch());
            assigned.setYaw(player.getLocation().getYaw());
            double x = assigned.getBlockX();
            double z = assigned.getBlockZ();
            assigned.setX(x);
            assigned.setZ(z);

            player.teleport(assigned);
          }
        }
      }
    }
  }

  @EventHandler
  public void onDamage(EntityDamageEvent event)
  {
    Entity entity = event.getEntity();
    if ((entity instanceof Player)) {
      Player player = (Player)entity;

      if (isTributeAtAll(player)) {
        int id = ((Integer)this.gamers.get(player.getName())).intValue();
        if (id == 0)
          event.setCancelled(true);
      }
    }
  }

  public void leaveGame(Player player, int arena)
  {
    if (isTributeAtAll(player)) {
      int id = ((Integer)this.gamers.get(player.getName())).intValue();
      if ((canJoinGame(getGameStatus(arena))) || (id == 0)) {
        if (player != null)
        {
          player.getInventory().clear();
          player.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
          player.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
          player.getInventory().setLeggings(new ItemStack(Material.AIR, 1));
          player.getInventory().setBoots(new ItemStack(Material.AIR, 1));
          player.teleport(getExitPoint());
          player.sendMessage(this.tag + "You've been teleported out of the arena.");
          player.getInventory().clear();
          player.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
          player.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
          player.getInventory().setLeggings(new ItemStack(Material.AIR, 1));
          player.getInventory().setBoots(new ItemStack(Material.AIR, 1));
        }

        this.gamers.remove(player.getName());
        this.gamerOfArena.remove(player.getName());

        if (id != 0) {
          sendTributeMessage(player.getDisplayName() + "§2 left the arena.", true, arena);
        }
        else {
          player.setFlying(false);
          player.setAllowFlight(false);
          for (Player all : Bukkit.getServer().getOnlinePlayers()) {
            all.showPlayer(player);
          }
        }
        if (this.chosenKit.containsKey(player.getName()))
          this.chosenKit.remove(player.getName());
      }
      else
      {
        player.damage(1000.0D);
      }
    }
  }

  public void giveSpectatorWatch(Player player)
  {
    if (isTributeAtAll(player)) {
      int id = ((Integer)this.gamers.get(player.getName())).intValue();
      if (id == 0)
      {
        ItemStack watch = new ItemStack(Material.WATCH, 1);
        ItemMeta meta = watch.getItemMeta();

        meta.setDisplayName(this.tag + "Spectator Translocator");

        watch.setItemMeta(meta);

        player.getInventory().addItem(new ItemStack[] { watch });
      }
    }
  }

  public boolean canKitBeUsedEverywhere(String kit)
  {
    kit = kit.toLowerCase();

    return getConfig().getBoolean("kits." + kit + ".usable-on-all-arenas");
  }

  public void eliminateTribute(Player player, String reason, int arena)
  {
    int left = getRemainingTributes(arena) - 1;
    sendTributeMessage("§cTribute §4" + player.getDisplayName() + "§c has fallen.", true, arena);
    sendTributeMessage("§8[§4" + left + "§8] §ctributes remain.", true, arena);

    boolean useLightning = getConfig().getBoolean("lightning.enable-lightning");
    if (useLightning) {
      int count = getConfig().getInt("lightning.start-at-tributes");
      if (left == count) {
        sendTributeMessage("§6Lightning strikes will now show your positions.", true, arena);
        sendTributeMessage("§6No more hiding, deathmatch is around the corner!", true, arena);
      }
    }

    if ((left == getDeathmatchRequiredPlayers()) && (getGameStatus(arena) == GameStatus.INGAME)) {
      sendTributeMessage("§3Only §8[§b" + left + "§8] §3tributes remain!", true, arena);
      sendTributeMessage("§3Deathmatch will begin in §8[§b60§8] §3seconds!", true, arena);
      setTicks(60, arena);
    }
    else if (left == 1) {
      endGame(WinResult.HAS_WINNER, arena);
    }
    else if (left == 0) {
      endGame(WinResult.NO_WINNER, arena);
    }

    if (player != null) {
      player.sendMessage(this.tag + "Your Death Cause: §a" + reason.replace(player.getName(), "You"));
      player.getWorld().strikeLightningEffect(player.getLocation());
      player.teleport(getExitPoint());
      player.sendMessage(this.tag + "You've been teleported out of the arena.");
    }

    this.gamers.remove(player.getName());
    this.gamerOfArena.remove(player.getName());
    for (Player all : getTributesOfArena(arena))
      all.playSound(all.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, -1.0F);
  }

  @EventHandler(priority=EventPriority.HIGH)
  public void onBlockBurn(BlockBurnEvent event)
  {
    Block block = event.getBlock();

    World world = block.getWorld();
    for (int i = 1; i <= getMaxArenas(); i++)
    {
      World sWorld = getSurvivalGamesWorld(i);

      if (isSameWorld(world, sWorld))
      {
        Location l1 = block.getLocation();
        Location l2 = getArenaCenter(i);
        int radius = getArenaRadius(i);

        if (l1.distance(l2) <= radius)
          event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority=EventPriority.HIGH)
  public void onBlockSpread(BlockSpreadEvent event)
  {
    Block block = event.getBlock();

    World world = block.getWorld();
    for (int i = 1; i <= getMaxArenas(); i++)
      if (isArenaEnabled(i)) {
        World sWorld = getSurvivalGamesWorld(i);

        if (isSameWorld(world, sWorld))
        {
          Location l1 = block.getLocation();
          Location l2 = getArenaCenter(i);
          int radius = getArenaRadius(i);

          if (l1.distance(l2) <= radius)
            event.setCancelled(true);
        }
      }
  }

  @EventHandler
  public void onDecay(LeavesDecayEvent event)
  {
    Block block = event.getBlock();

    World world = block.getWorld();
    for (int i = 1; i <= getMaxArenas(); i++)
      if (isArenaEnabled(i)) {
        World sWorld = getSurvivalGamesWorld(i);

        if (isSameWorld(world, sWorld))
        {
          Location l1 = block.getLocation();
          Location l2 = getArenaCenter(i);
          int radius = getArenaRadius(i);

          if (l1.distance(l2) <= radius)
            event.setCancelled(true);
        }
      }
  }

  public boolean isSpectator(Player player)
  {
    if (isTributeAtAll(player)) {
      int id = ((Integer)this.gamers.get(player.getName())).intValue();
      if (id == 0) {
        return true;
      }
    }

    return false;
  }

  @EventHandler
  public void onOpen(InventoryOpenEvent event) {
    HumanEntity ent = event.getPlayer();

    if ((ent instanceof Player)) {
      Player player = (Player)ent;

      if (isSpectator(player))
      {
        InventoryType inv = event.getInventory().getType();

        if ((inv == InventoryType.ANVIL) || 
          (inv == InventoryType.BEACON) || 
          (inv == InventoryType.BREWING) || 
          (inv == InventoryType.CRAFTING) || 
          (inv == InventoryType.DISPENSER) || 
          (inv == InventoryType.DROPPER) || 
          (inv == InventoryType.ENCHANTING) || 
          (inv == InventoryType.ENDER_CHEST) || 
          (inv == InventoryType.FURNACE) || 
          (inv == InventoryType.HOPPER) || 
          (inv == InventoryType.MERCHANT) || 
          (inv == InventoryType.WORKBENCH))
          event.setCancelled(true);
      }
    }
  }

  public void loop()
  {
    Bukkit.getServer().getScheduler().runTaskTimer(this, new Runnable()
    {
      public void run()
      {
        if (Core.this.useScoreboard()) {
          Core.this.updateScoreboardViewers();
        }

        if (Core.this.useLobbyScoreboard()) {
          Core.this.refreshLobbyScoreboard();
        }

        for (Player all : Bukkit.getServer().getOnlinePlayers()) {
          Core.this.refreshTabList(all);
        }

        for (int arena = 1; arena <= Core.this.getMaxArenas(); arena++)
        {
          if ((!Core.this.sb.arenaSBs.containsKey(Integer.valueOf(arena))) && (Core.this.useScoreboard())) {
            Core.this.registerArenaScoreboard(arena);
          }

          if ((Core.this.useScoreboard()) && (Core.this.sb.arenaSBs.containsKey(Integer.valueOf(arena)))) {
            Core.this.refreshScoreboard(arena);
          }

          if (Core.this.isArenaEnabled(arena)) {
            Core.this.updateArenaStatusSign(arena);
            Core.this.refreshHeadWall(arena);
            int i = 1;
            do
            {
              Core.this.updateTributeSign(arena, i);
              i++;
            }
            while (
              i <= 24);
            for (??? = Core.this.getSurvivalGamesWorld(arena).getPlayers().iterator(); ((Iterator)???).hasNext(); ) { Player pl = (Player)((Iterator)???).next();
              if (!Core.this.isTributeAtAll(pl)) {
                double distance = pl.getLocation().distance(Core.this.getArenaCenter(arena));
                if ((distance < Core.this.getArenaRadius(arena)) && 
                  (!pl.hasPermission("sg.admin"))) {
                  pl.getInventory().clear();
                  pl.setLevel(0);
                  pl.setExp(0.0F);
                  pl.teleport(Core.this.getExitPoint());
                }

              }

            }

            if (Core.this.getGameStatus(arena) == GameStatus.IDLE) {
              Core.this.setTicks(0, arena);
              if (Core.this.getTributesOfArena(arena).size() > 0) {
                for (??? = Core.this.getTributesOfArena(arena).iterator(); ((Iterator)???).hasNext(); ) { Player pl = (Player)((Iterator)???).next();
                  pl.setHealth(20.0D);
                  pl.setFoodLevel(20);
                  pl.setSaturation(8.0F);
                }
              }
            }
            else
            {
              Core.this.setTicks(Core.this.getTicks(arena) - 1, arena);
            }
            boolean showChestCount;
            if (Core.this.getGameStatus(arena) == GameStatus.STARTING)
            {
              if (Core.this.getTributesOfArena(arena).size() < Core.this.getWhenGameShouldStartPlayers()) {
                Core.this.sendTributeMessage("§cStart aborted, not enough tributes!", true, arena);
                Core.this.setGameStatus(GameStatus.IDLE, arena);
              }

              for (??? = Core.this.getTributesOfArena(arena).iterator(); ((Iterator)???).hasNext(); ) { Player pl = (Player)((Iterator)???).next();
                pl.setLevel(Core.this.getTicks(arena));
                pl.setHealth(20.0D);
                pl.setFoodLevel(20);
                pl.setSaturation(8.0F);
              }

              if (Core.this.getTicks(arena) == 45) {
                Core.this.sendTributeMessage("§3Games begin in §8[§b" + Core.this.getTicks(arena) + "§8]§3 seconds!", true, arena);
              }
              else if (Core.this.getTicks(arena) == 30) {
                Core.this.sendTributeMessage("§3Games begin in §8[§b" + Core.this.getTicks(arena) + "§8]§3 seconds!", true, arena);
              }
              else if (Core.this.getTicks(arena) == 20) {
                boolean showMapInfo = Core.this.getConfig().getBoolean("starting.display-map-info");
                showChestCount = Core.this.getConfig().getBoolean("starting.display-chest-count");

                if (showMapInfo) {
                  Core.this.sendTributeMessage("§eMAP | §aName: §b" + Core.this.getMapName(arena), true, arena);
                  Core.this.sendTributeMessage("§eMAP | §aCreator: §b" + Core.this.getMapAuthor(arena), true, arena);
                  Core.this.sendTributeMessage("§eMAP | §aLink: §b" + Core.this.getMapLink(arena), true, arena);
                }
                if (showChestCount) {
                  int chests = Core.this.getChests(1, arena).size() + Core.this.getChests(2, arena).size();
                  Core.this.sendTributeMessage("There are §8[§a" + chests + "§8]§2 chests in this map!", true, arena);
                }

              }
              else if (Core.this.getTicks(arena) == 15) {
                Core.this.sendTributeMessage("§3Games begin in §8[§b" + Core.this.getTicks(arena) + "§8]§3 seconds!", true, arena);
              }
              else if ((Core.this.getTicks(arena) > 0) && (Core.this.getTicks(arena) <= 10)) {
                Core.this.sendTributeMessage("§3Games begin in §8[§b" + Core.this.getTicks(arena) + "§8]§3 seconds!", true, arena);
                for (Player pl : Core.this.getTributesOfArena(arena)) {
                  pl.playSound(pl.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
                }
              }
              else if (Core.this.getTicks(arena) == 0) {
                for (Player pl : Core.this.getTributesOfArena(arena)) {
                  pl.playSound(pl.getLocation(), Sound.ORB_PICKUP, 1.0F, 3.0F);
                }
                Core.this.startGame(arena);
              }
            }
            else
            {
              Object pl;
              if (Core.this.getGameStatus(arena) == GameStatus.DEATHMATCH)
              {
                boolean killIfLeft;
                for (Player pl : Core.this.getTributesOfArena(arena)) {
                  if (pl.getWorld().getName().equalsIgnoreCase(Core.this.getSurvivalGamesWorld(arena).getName())) {
                    killIfLeft = Core.this.getConfig().getBoolean("game.kill-tributes-who-leave-radius");
                    if (killIfLeft) {
                      int id = ((Integer)Core.this.gamers.get(pl.getName())).intValue();
                      double distance = pl.getLocation().distance(Core.this.getArenaCenter(arena));
                      if ((distance >= Core.this.getArenaRadius(arena)) && (id != 0))
                        Core.this.eliminateTribute(pl, "Leaving the arena!", arena);
                    }
                  }
                  else
                  {
                    Core.this.eliminateTribute(pl, "Leaving the arena!", arena);
                  }
                }

                int dmradius = Core.this.getDeathmatchRadius(arena);
                for (Player pl : Core.this.getTributesOfArena(arena)) {
                  double distance = pl.getLocation().distance(Core.this.getArenaCenter(arena));

                  if (distance >= dmradius) {
                    int id = ((Integer)Core.this.gamers.get(pl.getName())).intValue();
                    if (id != 0) {
                      pl.getWorld().strikeLightning(pl.getLocation());
                      pl.damage(4.0D);
                      pl.sendMessage(Core.this.tag + "§cReturn to the deathmatch arena!");
                    }
                  }

                }

                if (Core.this.getTicks(arena) == 300) {
                  Core.this.sendTributeMessage("The game ends in " + Core.this.formatTime(Core.this.getTicks(arena)) + "!", true, arena);
                }
                else if (Core.this.getTicks(arena) == 240) {
                  Core.this.sendTributeMessage("The game ends in " + Core.this.formatTime(Core.this.getTicks(arena)) + "!", true, arena);
                }
                else if (Core.this.getTicks(arena) == 180) {
                  Core.this.sendTributeMessage("The game ends in " + Core.this.formatTime(Core.this.getTicks(arena)) + "!", true, arena);
                }
                else if (Core.this.getTicks(arena) == 120) {
                  Core.this.sendTributeMessage("The game ends in " + Core.this.formatTime(Core.this.getTicks(arena)) + "!", true, arena);
                }
                else if (Core.this.getTicks(arena) == 90) {
                  Core.this.sendTributeMessage("The game ends in " + Core.this.formatTime(Core.this.getTicks(arena)) + "!", true, arena);
                }
                else if (Core.this.getTicks(arena) == 60) {
                  Core.this.sendTributeMessage("The game ends in " + Core.this.formatTime(Core.this.getTicks(arena)) + "!", true, arena);
                }
                else if (Core.this.getTicks(arena) == 45) {
                  Core.this.sendTributeMessage("The game ends in " + Core.this.formatTime(Core.this.getTicks(arena)) + "!", true, arena);
                }
                else if (Core.this.getTicks(arena) == 30) {
                  Core.this.sendTributeMessage("The game ends in " + Core.this.formatTime(Core.this.getTicks(arena)) + "!", true, arena);
                }
                else if (Core.this.getTicks(arena) == 15) {
                  Core.this.sendTributeMessage("The game ends in " + Core.this.formatTime(Core.this.getTicks(arena)) + "!", true, arena);
                }
                else if ((Core.this.getTicks(arena) > 0) && (Core.this.getTicks(arena) <= 10)) {
                  Core.this.sendTributeMessage("The game ends in " + Core.this.formatTime(Core.this.getTicks(arena)) + "!", true, arena);
                  for (killIfLeft = Core.this.getTributesOfArena(arena).iterator(); killIfLeft.hasNext(); ) { pl = (Player)killIfLeft.next();
                    ((Player)pl).playSound(((Player)pl).getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
                  }
                }
                else if (Core.this.getTicks(arena) == 0) {
                  Core.this.endGame(WinResult.NO_WINNER, arena);
                }

              }
              else if (Core.this.getGameStatus(arena) == GameStatus.INGAME)
              {
                for (pl = Core.this.getTributesOfArena(arena).iterator(); ((Iterator)pl).hasNext(); ) { Player pl = (Player)((Iterator)pl).next();
                  if (pl.getWorld().getName().equalsIgnoreCase(Core.this.getSurvivalGamesWorld(arena).getName())) {
                    boolean killIfLeft = Core.this.getConfig().getBoolean("game.kill-tributes-who-leave-radius");
                    if (killIfLeft) {
                      int id = ((Integer)Core.this.gamers.get(pl.getName())).intValue();
                      double distance = pl.getLocation().distance(Core.this.getArenaCenter(arena));
                      if ((distance >= Core.this.getArenaRadius(arena)) && (id != 0))
                        Core.this.eliminateTribute(pl, "Leaving the arena!", arena);
                    }
                  }
                  else
                  {
                    Core.this.eliminateTribute(pl, "Leaving the arena!", arena);
                  }
                }

                int restock3 = Core.this.getConfig().getInt("chestloot.restock-chests-after-seconds") + 3;
                int restock2 = Core.this.getConfig().getInt("chestloot.restock-chests-after-seconds") + 2;
                int restock1 = Core.this.getConfig().getInt("chestloot.restock-chests-after-seconds") + 1;

                int restock = Core.this.getConfig().getInt("chestloot.restock-chests-after-seconds");

                boolean allTier2 = Core.this.getConfig().getBoolean("chestloot.all-chests-restock-as-tier2");

                if (Core.this.getTicks(arena) == restock3) {
                  Core.this.sendTributeMessage("Restocking chests... §a3§2...", true, arena);
                  for (Player all : Core.this.getTributesOfArena(arena)) {
                    all.playSound(all.getLocation(), Sound.ORB_PICKUP, 1.0F, -2.0F);
                  }
                }
                else if (Core.this.getTicks(arena) == restock2) {
                  Core.this.sendTributeMessage("Restocking chests... §a2§2...", true, arena);
                  for (Player all : Core.this.getTributesOfArena(arena)) {
                    all.playSound(all.getLocation(), Sound.ORB_PICKUP, 1.0F, -2.0F);
                  }
                }
                else if (Core.this.getTicks(arena) == restock1) {
                  Core.this.sendTributeMessage("Restocking chests... §a1§2...", true, arena);
                  for (Player all : Core.this.getTributesOfArena(arena)) {
                    all.playSound(all.getLocation(), Sound.ORB_PICKUP, 1.0F, -2.0F);
                  }
                }
                else if (Core.this.getTicks(arena) == restock) {
                  if (allTier2) {
                    Core.this.refillChestsForNight(arena);
                  }
                  else {
                    Core.this.refillChests(1, arena);
                    Core.this.refillChests(2, arena);
                  }
                  Core.this.sendTributeMessage("All chests have been §arestocked§2!", true, arena);
                  for (Player all : Core.this.getTributesOfArena(arena)) {
                    all.playSound(all.getLocation(), Sound.LEVEL_UP, 1.0F, -2.0F);
                  }
                }

                int spectators = 0;
                for (Player pl : Core.this.getTributesOfArena(arena)) {
                  int id = ((Integer)Core.this.gamers.get(pl.getName())).intValue();
                  if (id == 0) {
                    spectators++;
                  }
                }

                if (Core.this.getTicks(arena) == 1800) {
                  Core.this.sendTributeMessage("Arena deathmatch in " + Core.this.formatTime(Core.this.getTicks(arena)) + "§2!", true, arena);
                  Core.this.sendTributeMessage("§8[§a" + Core.this.getRemainingTributes(arena) + "§8] §2tributes remain.", true, arena);
                  Core.this.sendTributeMessage("There are §8[§a" + spectators + "§8]§2 spectators watching.", true, arena);
                }
                else if (Core.this.getTicks(arena) == 1500) {
                  Core.this.sendTributeMessage("Arena deathmatch in " + Core.this.formatTime(Core.this.getTicks(arena)) + "§2!", true, arena);
                  Core.this.sendTributeMessage("§8[§a" + Core.this.getRemainingTributes(arena) + "§8] §2tributes remain.", true, arena);
                  Core.this.sendTributeMessage("There are §8[§a" + spectators + "§8]§2 spectators watching.", true, arena);
                }
                else if (Core.this.getTicks(arena) == 1200) {
                  Core.this.sendTributeMessage("Arena deathmatch in " + Core.this.formatTime(Core.this.getTicks(arena)) + "§2!", true, arena);
                  Core.this.sendTributeMessage("§8[§a" + Core.this.getRemainingTributes(arena) + "§8] §2tributes remain.", true, arena);
                  Core.this.sendTributeMessage("There are §8[§a" + spectators + "§8]§2 spectators watching.", true, arena);
                }
                else if (Core.this.getTicks(arena) == 900) {
                  Core.this.sendTributeMessage("Arena deathmatch in " + Core.this.formatTime(Core.this.getTicks(arena)) + "§2!", true, arena);
                  Core.this.sendTributeMessage("§8[§a" + Core.this.getRemainingTributes(arena) + "§8] §2tributes remain.", true, arena);
                  Core.this.sendTributeMessage("There are §8[§a" + spectators + "§8]§2 spectators watching.", true, arena);
                }
                else if (Core.this.getTicks(arena) == 600) {
                  Core.this.sendTributeMessage("Arena deathmatch in " + Core.this.formatTime(Core.this.getTicks(arena)) + "§2!", true, arena);
                  Core.this.sendTributeMessage("§8[§a" + Core.this.getRemainingTributes(arena) + "§8] §2tributes remain.", true, arena);
                  Core.this.sendTributeMessage("There are §8[§a" + spectators + "§8]§2 spectators watching.", true, arena);
                }
                else if (Core.this.getTicks(arena) == 300) {
                  Core.this.sendTributeMessage("Arena deathmatch in " + Core.this.formatTime(Core.this.getTicks(arena)) + "§2!", true, arena);
                  Core.this.sendTributeMessage("§8[§a" + Core.this.getRemainingTributes(arena) + "§8] §2tributes remain.", true, arena);
                  Core.this.sendTributeMessage("There are §8[§a" + spectators + "§8]§2 spectators watching.", true, arena);
                }
                else if (Core.this.getTicks(arena) == 180) {
                  Core.this.sendTributeMessage("Arena deathmatch in " + Core.this.formatTime(Core.this.getTicks(arena)) + "§2!", true, arena);
                  Core.this.sendTributeMessage("§8[§a" + Core.this.getRemainingTributes(arena) + "§8] §2tributes remain.", true, arena);
                  Core.this.sendTributeMessage("There are §8[§a" + spectators + "§8]§2 spectators watching.", true, arena);
                }
                else if (Core.this.getTicks(arena) == 120) {
                  Core.this.sendTributeMessage("Arena deathmatch in " + Core.this.formatTime(Core.this.getTicks(arena)) + "§2!", true, arena);
                  Core.this.sendTributeMessage("§8[§a" + Core.this.getRemainingTributes(arena) + "§8] §2tributes remain.", true, arena);
                  Core.this.sendTributeMessage("There are §8[§a" + spectators + "§8]§2 spectators watching.", true, arena);
                }
                else if (Core.this.getTicks(arena) == 60) {
                  Core.this.sendTributeMessage("§3Deathmatch in §8[§b" + Core.this.getTicks(arena) + "§8] §3seconds!", true, arena);
                }
                else if (Core.this.getTicks(arena) == 45) {
                  Core.this.sendTributeMessage("§3Deathmatch in §8[§b" + Core.this.getTicks(arena) + "§8] §3seconds!", true, arena);
                }
                else if (Core.this.getTicks(arena) == 30) {
                  Core.this.sendTributeMessage("§3Deathmatch in §8[§b" + Core.this.getTicks(arena) + "§8] §3seconds!", true, arena);
                }
                else if (Core.this.getTicks(arena) == 15) {
                  Core.this.sendTributeMessage("§3Deathmatch in §8[§b" + Core.this.getTicks(arena) + "§8] §3seconds!", true, arena);
                }
                else if ((Core.this.getTicks(arena) > 0) && (Core.this.getTicks(arena) <= 10)) {
                  for (Player pl : Core.this.getTributesOfArena(arena)) {
                    pl.playSound(pl.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
                  }
                  Core.this.sendTributeMessage("§3Deathmatch in §8[§b" + Core.this.getTicks(arena) + "§8] §3seconds!", true, arena);
                }
                else if (Core.this.getTicks(arena) == 0) {
                  Core.this.startDM(arena);
                }
              }
            }
          }
        }
      }
    }
    , 20L, 20L);
  }

  public Player getWinner(int arena) {
    if (getRemainingTributes(arena) == 1)
    {
      int id;
      for (Player pl : getTributesOfArena(arena)) {
        id = ((Integer)this.gamers.get(pl.getName())).intValue();
        if (id == 0) {
          leaveGame(pl, arena);
        }
      }

      List possible = new ArrayList();
      for (Player pl : getAllTributes()) {
        int ar = getTributeArena(pl);
        if (ar == arena) {
          possible.add(pl);
        }
      }

      return (Player)possible.get(0);
    }

    log("ERROR! Failed to find a winner in arena " + arena + "!");
    return null;
  }

  public void giveReward(final Player player)
  {
    Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable()
    {
      public void run()
      {
        if (player != null) {
          ItemStack stack = Core.this.getReward();

          if (stack.getType() != Material.ACTIVATOR_RAIL) {
            int amount = stack.getAmount();

            Core.econ.depositPlayer(player.getName(), amount);
            player.sendMessage(Core.this.tag + "§bYou won §f" + amount + " " + Core.econ.currencyNamePlural() + "§b!");
          }
          else
          {
            String friendlyname = WordUtils.capitalizeFully(stack.getType().toString().replace("_", " "));
            int amount = stack.getAmount();

            player.sendMessage(Core.this.tag + "§bYou won §8[§3" + amount + "§8]§b " + friendlyname + "§b!");
          }
        }
      }
    }
    , 200L);
  }

  public void shootFirework(Location loc)
  {
    int power = (int)(Math.random() * 3.0D) + 1;
    Firework fireworks = (Firework)loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
    FireworkMeta fireworkmeta = fireworks.getFireworkMeta();
    List c = new ArrayList();
    c.add(Color.BLUE);
    c.add(Color.GREEN);
    FireworkEffect e = FireworkEffect.builder().flicker(true).withColor(c).withFade(c).with(FireworkEffect.Type.BALL_LARGE).trail(true).build();
    fireworkmeta.addEffect(e);
    fireworkmeta.setPower(power);
    fireworks.setFireworkMeta(fireworkmeta);
  }

  public void endGame(final WinResult result, final int arena)
  {
    setTicks(9000, arena);
    sendTributeMessage("§aThe Survival Games have ended!", true, arena);
    Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable()
    {
      public void run()
      {
        if (result == WinResult.ADMIN_STOP) {
          Core.this.sendTributeMessage("§cThe match was force ended.", true, arena);
        }
        else if (result == WinResult.NO_WINNER) {
          Core.this.sendTributeMessage("§cWe have no winner!", true, arena);
        }
        else if (result == WinResult.HAS_WINNER) {
          Player winner = Core.this.getWinner(arena);
          Core.this.sendTributeMessage(winner.getDisplayName() + "§2 won the games!", true, arena);
          Core.this.giveReward(winner);
          int wins = Core.this.getWinsForPlayer(winner);
          Core.this.setWinsForPlayer(winner, wins + 1);
          Core.this.sendNonTributeMessage(winner.getDisplayName() + "§e won The Survival Games on arena " + arena + "!", true);
        }

        int i = 1;
        do
        {
          Location loc = Core.this.getTributeSpawn(i, arena);
          Core.this.shootFirework(loc);

          i++;
        }
        while (
          i <= 24);
      }
    }
    , 10L);
    Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable()
    {
      public void run()
      {
        List remove = new ArrayList();
        int id;
        if (Core.this.getTributesOfArena(arena).size() > 0)
        {
          for (Player player : Core.this.getTributesOfArena(arena)) {
            if (player != null) {
              player.getInventory().clear();
              player.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
              player.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
              player.getInventory().setLeggings(new ItemStack(Material.AIR, 1));
              player.getInventory().setBoots(new ItemStack(Material.AIR, 1));
              player.teleport(Core.this.getExitPoint());
              player.sendMessage(Core.this.tag + "You've been teleported out of the arena.");
            }

            id = ((Integer)Core.this.gamers.get(player.getName())).intValue();
            if (id == 0)
            {
              player.setFlying(false);
              player.setAllowFlight(false);
              player.getInventory().clear();
              for (Player all : Bukkit.getServer().getOnlinePlayers()) {
                all.showPlayer(player);
              }
            }
            if (Core.this.chosenKit.containsKey(player.getName())) {
              Core.this.chosenKit.remove(player.getName());
            }
            remove.add(player.getName());
          }
        }
        int i = 1;
        do
        {
          Location loc = Core.this.getTributeSpawn(i, arena);
          Core.this.shootFirework(loc);

          i++;
        }
        while (
          i <= 24);
        Core.this.setGameStatus(GameStatus.CLEANUP, arena);

        for (String entry : remove) {
          Core.this.gamers.remove(entry);
          Core.this.gamerOfArena.remove(entry);
        }

        Core.this.clearGroundItems(arena);

        Core.this.refillChests(1, arena);
        Core.this.refillChests(2, arena);
        Core.this.sendTributeMessage("All chests have been §arestocked§2!", true, arena);
        for (Player all : Core.this.getTributesOfArena(arena)) {
          all.playSound(all.getLocation(), Sound.LEVEL_UP, 1.0F, -2.0F);
        }

        Core.this.rollbackBlocks(arena);

        Core.this.setGameStatus(GameStatus.IDLE, arena);
      }
    }
    , 100L);
  }

  public boolean doesSignExistForArena(int arena) {
    int x = getConfig().getInt("arenas.arena" + arena + ".sign.x");
    int y = getConfig().getInt("arenas.arena" + arena + ".sign.y");
    int z = getConfig().getInt("arenas.arena" + arena + ".sign.z");

    if ((x != 0) || (y != 0) || (z != 0)) {
      return true;
    }

    return false;
  }

  public boolean doesSignExistForTribute(int tribute, int arena)
  {
    int x = getConfig().getInt("arenas.arena" + arena + ".tribute" + tribute + ".sign.x");
    int y = getConfig().getInt("arenas.arena" + arena + ".tribute" + tribute + ".sign.y");
    int z = getConfig().getInt("arenas.arena" + arena + ".tribute" + tribute + ".sign.z");

    if ((x != 0) || (y != 0) || (z != 0)) {
      return true;
    }

    return false;
  }

  public void updateArenaStatusSign(int arena)
  {
    if (doesSignExistForArena(arena)) {
      String w = getConfig().getString("arenas.arena" + arena + ".sign.world");
      int x = getConfig().getInt("arenas.arena" + arena + ".sign.x");
      int y = getConfig().getInt("arenas.arena" + arena + ".sign.y");
      int z = getConfig().getInt("arenas.arena" + arena + ".sign.z");

      Location loc = new Location(Bukkit.getWorld(w), x, y, z);

      Block block = loc.getBlock();

      if ((block.getType() == Material.SIGN_POST) || (block.getType() == Material.WALL_SIGN))
      {
        Sign sign = (Sign)block.getState();

        sign.setLine(0, "§nArena " + arena);
        if (isArenaEnabled(arena)) {
          if (getGameStatus(arena) == GameStatus.IDLE) {
            int needed = getWhenGameShouldStartPlayers();
            int inArena = getTributesOfArena(arena).size();

            sign.setLine(1, "§1WAITING");
            sign.setLine(2, "Need §4" + needed);
            sign.setLine(3, inArena + "§5/§0" + 24);
          }
          else if (getGameStatus(arena) == GameStatus.STARTING) {
            int inArena = getTributesOfArena(arena).size();

            sign.setLine(1, "§aSTARTING");
            sign.setLine(2, "§n" + parseSeconds(getTicks(arena)));
            if (inArena == 24) {
              sign.setLine(3, "§4§lFull!");
            }
            else {
              sign.setLine(3, inArena + "§5/§0" + 24);
            }

          }
          else if (getGameStatus(arena) == GameStatus.INGAME)
          {
            sign.setLine(1, "§5IN GAME");
            int spec = 0;
            int trib = 0;
            for (Player pl : getTributesOfArena(arena)) {
              int id = ((Integer)this.gamers.get(pl.getName())).intValue();
              if (id == 0) {
                spec++;
              }
              else {
                trib++;
              }
            }
            sign.setLine(2, "§2" + trib + "§0 Tributes");
            sign.setLine(3, "§4" + spec + "§0 Watching");
          }
          else if (getGameStatus(arena) == GameStatus.DEATHMATCH)
          {
            sign.setLine(1, "§cDEATHMATCH");
            int spec = 0;
            int trib = 0;
            for (Player pl : getTributesOfArena(arena)) {
              int id = ((Integer)this.gamers.get(pl.getName())).intValue();
              if (id == 0) {
                spec++;
              }
              else {
                trib++;
              }
            }
            sign.setLine(2, "§2" + trib + "§0 Tributes");
            sign.setLine(3, "§4" + spec + "§0 Watching");
          }
          else if (getGameStatus(arena) == GameStatus.CLEANUP)
          {
            sign.setLine(1, "§lCleaning Up!");
          }
        }
        else
        {
          sign.setLine(1, "§4§lDISABLED");
          sign.setLine(2, "");
          sign.setLine(3, "");
        }
        sign.update();
      }
    }
  }

  public void updateTributeSign(int arena, int tribute)
  {
    if (doesSignExistForTribute(tribute, arena))
    {
      String w = getConfig().getString("arenas.arena" + arena + ".tribute" + tribute + ".sign.world");
      int x = getConfig().getInt("arenas.arena" + arena + ".tribute" + tribute + ".sign.x");
      int y = getConfig().getInt("arenas.arena" + arena + ".tribute" + tribute + ".sign.y");
      int z = getConfig().getInt("arenas.arena" + arena + ".tribute" + tribute + ".sign.z");

      String target = "nobody!";
      for (Player pl : getTributesOfArena(arena)) {
        int id = ((Integer)this.gamers.get(pl.getName())).intValue();
        if (id == tribute) {
          target = pl.getName();
        }
      }

      Player t = Bukkit.getServer().getPlayer(target);
      if (t != null)
      {
        Location loc = new Location(Bukkit.getWorld(w), x, y, z);
        Block block = loc.getBlock();

        if ((block.getType() == Material.SIGN_POST) || (block.getType() == Material.WALL_SIGN)) {
          Sign sign = (Sign)block.getState();

          sign.setLine(0, "§nTribute " + tribute);
          sign.setLine(1, t.getName());
          sign.setLine(2, "HP: §4" + t.getHealth() + "/20");

          String hand = WordUtils.capitalizeFully(t.getItemInHand().getType().toString().replace("_", " "));

          if (hand.equalsIgnoreCase("AIR")) {
            hand = "Nothing";
          }

          sign.setLine(3, hand);

          sign.update();
        }

      }

      if (target.equalsIgnoreCase("nobody!")) {
        Location loc = new Location(Bukkit.getWorld(w), x, y, z);
        Block block = loc.getBlock();

        if ((block.getType() == Material.SIGN_POST) || (block.getType() == Material.WALL_SIGN)) {
          Sign sign = (Sign)block.getState();

          sign.setLine(0, "§nTribute " + tribute);
          sign.setLine(1, "");
          sign.setLine(2, "");
          sign.setLine(3, "");

          sign.update();
        }
      }
    }
  }

  public void setArenaSign(int arena, Location loc1)
  {
    getConfig().set("arenas.arena" + arena + ".sign.world", loc1.getWorld().getName());
    getConfig().set("arenas.arena" + arena + ".sign.x", Integer.valueOf(loc1.getBlockX()));
    getConfig().set("arenas.arena" + arena + ".sign.y", Integer.valueOf(loc1.getBlockY()));
    getConfig().set("arenas.arena" + arena + ".sign.z", Integer.valueOf(loc1.getBlockZ()));

    saveConfig();
  }

  public void setTributeSign(int arena, int tribute, Location loc1)
  {
    getConfig().set("arenas.arena" + arena + ".tribute" + tribute + ".sign.world", loc1.getWorld().getName());
    getConfig().set("arenas.arena" + arena + ".tribute" + tribute + ".sign.x", Integer.valueOf(loc1.getBlockX()));
    getConfig().set("arenas.arena" + arena + ".tribute" + tribute + ".sign.y", Integer.valueOf(loc1.getBlockY()));
    getConfig().set("arenas.arena" + arena + ".tribute" + tribute + ".sign.z", Integer.valueOf(loc1.getBlockZ()));

    saveConfig();
  }

  public void setHeadWall(int arena, Location loc1, Location loc2)
  {
    getConfig().set("arenas.arena" + arena + ".head-wall.world", loc1.getWorld().getName());
    getConfig().set("arenas.arena" + arena + ".head-wall.p1.x", Integer.valueOf(loc1.getBlockX()));
    getConfig().set("arenas.arena" + arena + ".head-wall.p1.y", Integer.valueOf(loc1.getBlockY()));
    getConfig().set("arenas.arena" + arena + ".head-wall.p1.z", Integer.valueOf(loc1.getBlockZ()));
    getConfig().set("arenas.arena" + arena + ".head-wall.p2.x", Integer.valueOf(loc2.getBlockX()));
    getConfig().set("arenas.arena" + arena + ".head-wall.p2.y", Integer.valueOf(loc2.getBlockY()));
    getConfig().set("arenas.arena" + arena + ".head-wall.p2.z", Integer.valueOf(loc2.getBlockZ()));

    saveConfig();
  }

  public void refreshHeadWall(int arena)
  {
    if (useHeadWall(arena))
    {
      World world = Bukkit.getWorld(getConfig().getString("arenas.arena" + arena + ".head-wall.world"));

      int x1 = getConfig().getInt("arenas.arena" + arena + ".head-wall.p1.x");
      int y1 = getConfig().getInt("arenas.arena" + arena + ".head-wall.p1.y");
      int z1 = getConfig().getInt("arenas.arena" + arena + ".head-wall.p1.z");

      int x2 = getConfig().getInt("arenas.arena" + arena + ".head-wall.p2.x");
      int y2 = getConfig().getInt("arenas.arena" + arena + ".head-wall.p2.y");
      int z2 = getConfig().getInt("arenas.arena" + arena + ".head-wall.p2.z");

      Location loc1 = new Location(world, x1, y1, z1);
      Location loc2 = new Location(world, x2, y2, z2);

      List blocks = blocksFromTwoPoints(loc1, loc2);

      List tribs = new ArrayList();
      int id;
      for (Player pl : getTributesOfArena(arena)) {
        id = ((Integer)this.gamers.get(pl.getName())).intValue();
        if (id != 0) {
          tribs.add(pl.getName());
        }
      }

      int i = 0;
      for (Block block : blocks)
        if (block.getType() == Material.SKULL)
        {
          try {
            Skull skull = (Skull)block.getState();
            skull.setOwner((String)tribs.get(i));
            skull.update();
          }
          catch (Exception e) {
            Skull skull = (Skull)block.getState();
            skull.setOwner("Steve");
            skull.update();
          }

          i++;
        }
    }
  }

  public int getGamesInState(GameStatus status)
  {
    int count = 0;

    for (int i = 1; i <= getMaxArenas(); i++)
    {
      GameStatus state = getGameStatus(i);

      if ((state == status) && (isArenaEnabled(i))) {
        count++;
      }

    }

    return count;
  }

  public void refreshLobbyScoreboard()
  {
    Scoreboard board = this.sb.getLobbyScoreboard();
    try {
      board.registerNewObjective("display", "dummy");
    }
    catch (Exception localException)
    {
    }
    Objective obj = board.getObjective("display");

    obj.setDisplayName(this.tag + "Lobby");

    Score waiting = obj.getScore(Bukkit.getOfflinePlayer("§9Waiting:"));
    waiting.setScore(getGamesInState(GameStatus.IDLE));

    Score starting = obj.getScore(Bukkit.getOfflinePlayer("§aStarting:"));
    starting.setScore(getGamesInState(GameStatus.STARTING));

    Score ingame = obj.getScore(Bukkit.getOfflinePlayer("§dIn Game:"));
    ingame.setScore(getGamesInState(GameStatus.INGAME));

    Score dm = obj.getScore(Bukkit.getOfflinePlayer("§4Deathmatch:"));
    dm.setScore(getGamesInState(GameStatus.DEATHMATCH));

    Score total = obj.getScore(Bukkit.getOfflinePlayer("§eTotal Arenas:"));
    total.setScore(getMaxArenas());

    obj.setDisplaySlot(DisplaySlot.SIDEBAR);
  }

  public void refreshScoreboard(int arena)
  {
    Scoreboard board = this.sb.getArenaScoreboard(arena);
    GameStatus state = getGameStatus(arena);
    try {
      board.registerNewObjective("display", "dummy");
    }
    catch (Exception localException)
    {
    }
    Objective obj = board.getObjective("display");

    if (state == GameStatus.IDLE) {
      board.resetScores(Bukkit.getOfflinePlayer("§9Time Left:"));
      board.resetScores(Bukkit.getOfflinePlayer("§9Tributes:"));
      board.resetScores(Bukkit.getOfflinePlayer("§9Spectators:"));
      obj.setDisplayName("§2Arena " + arena + "§8 : §eIdle");
      Score min = obj.getScore(Bukkit.getOfflinePlayer("§9Minimum:"));
      min.setScore(getWhenGameShouldStartPlayers());

      Score have = obj.getScore(Bukkit.getOfflinePlayer("§9Waiting:"));
      have.setScore(getRemainingTributes(arena));

      obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
    else if (state == GameStatus.STARTING) {
      board.resetScores(Bukkit.getOfflinePlayer("§9Minimum:"));
      board.resetScores(Bukkit.getOfflinePlayer("§9Waiting:"));
      board.resetScores(Bukkit.getOfflinePlayer("§9Spectators:"));

      obj.setDisplayName("§2Arena " + arena + "§8 : §aStarting");
      Score time = obj.getScore(Bukkit.getOfflinePlayer("§9Time Left:"));
      time.setScore(getTicks(arena) - 1);

      Score have = obj.getScore(Bukkit.getOfflinePlayer("§9Tributes:"));
      have.setScore(getRemainingTributes(arena));

      obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
    else if (state == GameStatus.INGAME) {
      board.resetScores(Bukkit.getOfflinePlayer("§9Minimum:"));
      board.resetScores(Bukkit.getOfflinePlayer("§9Time Left:"));
      board.resetScores(Bukkit.getOfflinePlayer("§9Waiting:"));
      if (isGracePeriod(arena))
        obj.setDisplayName("§6Grace Period§8 : §e" + parseSeconds(getTicks(arena) - 1));
      else {
        obj.setDisplayName("§dIn Game§8 : §e" + parseSeconds(getTicks(arena) - 1));
      }

      Score have = obj.getScore(Bukkit.getOfflinePlayer("§9Tributes:"));
      have.setScore(getRemainingTributes(arena));
      Score specs = obj.getScore(Bukkit.getOfflinePlayer("§9Spectators:"));
      specs.setScore(getArenaSpecs(arena));

      obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
    else if (state == GameStatus.DEATHMATCH) {
      board.resetScores(Bukkit.getOfflinePlayer("§9Minimum:"));
      board.resetScores(Bukkit.getOfflinePlayer("§9Time Left:"));
      board.resetScores(Bukkit.getOfflinePlayer("§9Waiting:"));
      obj.setDisplayName("§4Deathmatch§8 : §e" + parseSeconds(getTicks(arena) - 1));

      Score have = obj.getScore(Bukkit.getOfflinePlayer("§9Tributes:"));
      have.setScore(getRemainingTributes(arena));
      Score specs = obj.getScore(Bukkit.getOfflinePlayer("§9Spectators:"));
      specs.setScore(getArenaSpecs(arena));

      obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
  }

  public boolean useLobbyScoreboard()
  {
    return getConfig().getBoolean("scoreboard.use-lobby-scoreboard");
  }

  public boolean useScoreboard() {
    return getConfig().getBoolean("scoreboard.use-scoreboard");
  }

  public int getArenaSpecs(int arena) {
    int spectators = 0;
    for (Player pl : getTributesOfArena(arena)) {
      int id = ((Integer)this.gamers.get(pl.getName())).intValue();
      if (id == 0) {
        spectators++;
      }
    }
    return spectators;
  }

  public void registerLobbyScoreboard()
  {
    ScoreboardManager man = Bukkit.getScoreboardManager();

    Scoreboard s = man.getNewScoreboard();

    this.sb.setLobbyScoreboard(s);
  }

  public void updateScoreboardViewers()
  {
    for (Player all : Bukkit.getServer().getOnlinePlayers())
    {
      if (isTributeAtAll(all)) {
        int inArena = getTributeArena(all);

        all.setScoreboard(this.sb.getArenaScoreboard(inArena));
      }
      else
      {
        World pWorld = all.getWorld();
        World lWorld = getExitPoint().getWorld();

        if ((isSameWorld(pWorld, lWorld)) && (useLobbyScoreboard())) {
          all.setScoreboard(this.sb.getLobbyScoreboard());
        }
        else
          all.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
      }
    }
  }

  public void registerArenaScoreboard(int arena)
  {
    ScoreboardManager manager = Bukkit.getScoreboardManager();

    Scoreboard board = manager.getNewScoreboard();

    this.sb.setArenaScoreboard(arena, board);
  }

  public boolean useHeadWall(int arena)
  {
    return getConfig().getBoolean("arenas.arena" + arena + ".use-head-wall");
  }

  public void startDM(int arena) {
    setTicks(getGameMaxTime() * 60, arena);
    sendTributeMessage("§cThe deathmatch has begun!", true, arena);
    sendTributeMessage(getConfig().getString("branding.dm-message").replace("&", "§"), true, arena);

    for (Player pl : getTributesOfArena(arena)) {
      int id = ((Integer)this.gamers.get(pl.getName())).intValue();
      if (id != 0) {
        Location loc = getTributeSpawn(((Integer)this.gamers.get(pl.getName())).intValue(), arena);
        pl.teleport(loc.add(0.0D, 2.0D, 0.0D));
        pl.playSound(pl.getLocation(), Sound.ZOMBIE_PIG_DEATH, 1.0F, -2.0F);
      }
      else {
        Location loc = getArenaCenter(arena);
        pl.teleport(loc.add(0.45D, 2.0D, 0.45D));
        pl.playSound(pl.getLocation(), Sound.ZOMBIE_PIG_DEATH, 1.0F, -2.0F);
      }
    }

    setGameStatus(GameStatus.DEATHMATCH, arena);
  }

  public String parseChatFormat(int arena, Player player, int pid, String msg)
  {
    String format = "";

    String playerName = "";
    boolean useDispNames = getConfig().getBoolean("chat.use-display-names");
    if (useDispNames) {
      playerName = player.getDisplayName();
    }
    else {
      playerName = player.getName();
    }

    if (pid == 0) {
      String sFormat = getConfig().getString("chat.spec-chat.format");

      sFormat = sFormat.replace("&", "§");

      sFormat = sFormat.replace("%n", playerName);
      sFormat = sFormat.replace("%m", msg);
      sFormat = sFormat.replace("%a", arena);

      int tributeID = pid;
      if (pid > 11) {
        pid /= 2;
      }

      sFormat = sFormat.replace("%d", tributeID);

      format = sFormat;
    }
    else {
      String sFormat = getConfig().getString("chat.tribute-chat.format");

      sFormat = sFormat.replace("&", "§");

      sFormat = sFormat.replace("%n", playerName);
      sFormat = sFormat.replace("%m", msg);
      sFormat = sFormat.replace("%a", arena);

      int tributeID = pid;
      if (pid > 11) {
        pid /= 2;
      }

      sFormat = sFormat.replace("%d", tributeID);

      format = sFormat;
    }

    return format;
  }

  @EventHandler(priority=EventPriority.HIGHEST)
  public void onChat(AsyncPlayerChatEvent event) {
    Player player = event.getPlayer();
    String message = event.getMessage();

    if (isSGOnlyChat()) {
      boolean hideNonSGChat = getConfig().getBoolean("chat.hide-non-sg-chat");
      if (hideNonSGChat) {
        event.getRecipients().clear();
        for (Player all : Bukkit.getServer().getOnlinePlayers()) {
          if (!isTributeAtAll(all)) {
            event.getRecipients().add(all);
          }
        }
      }
      if (isTributeAtAll(player))
      {
        int arena = getTributeArena(player);

        int id = ((Integer)this.gamers.get(player.getName())).intValue();

        event.setCancelled(true);
        String format = parseChatFormat(arena, player, id, message);
        Player pl;
        if (id != 0) {
          int range = getConfig().getInt("chat.tribute-chat.range");
          if (range == -1) {
            for (Player pl : getTributesOfArena(arena)) {
              pl.sendMessage(format);
            }
          }
          else {
            for (??? = getTributesOfArena(arena).iterator(); ???.hasNext(); ) { pl = (Player)???.next();
              double distance = pl.getLocation().distance(player.getLocation());
              if (distance <= range)
                pl.sendMessage(format);
            }
          }
        }
        else
        {
          for (Player pl : getTributesOfArena(arena)) {
            int id2 = ((Integer)this.gamers.get(pl.getName())).intValue();
            if (id2 == 0)
              pl.sendMessage(format);
          }
        }
      }
    }
  }

  @EventHandler(priority=EventPriority.HIGHEST)
  public void onCmd(PlayerCommandPreprocessEvent event)
  {
    Player player = event.getPlayer();
    String cmd = event.getMessage();
    List allowedKits;
    if (isTributeAtAll(player)) {
      int arena = getTributeArena(player);
      int id = ((Integer)this.gamers.get(player.getName())).intValue();

      if ((cmd.equalsIgnoreCase("/leave")) && (getConfig().getBoolean("game.enable-leave-alias"))) {
        player.chat("/sg leave");
        event.setCancelled(true);
        return;
      }

      if ((canJoinGame(getGameStatus(arena))) && (id != 0))
      {
        allowedKits = getConfig().getStringList("arenas.arena" + arena + ".available-kits");
        for (String entry : allowedKits) {
          cmd = cmd.toLowerCase();
          if (cmd.equalsIgnoreCase("/" + entry.toLowerCase())) {
            if ((player.hasPermission("sg.canusekits")) && (player.hasPermission("sg.kit." + entry.toLowerCase()))) {
              this.chosenKit.put(player.getName(), entry);
              player.sendMessage(this.tag + "You will get your kit items for §a" + WordUtils.capitalizeFully(entry) + "§2 when the game begins.");
            }
            else {
              player.sendMessage(this.tag + "§cYou don't have access to that kit!");
            }
            event.setCancelled(true);
            return;
          }
        }

      }

      cmd = cmd.toLowerCase();
      if (cmd.equalsIgnoreCase("/list")) {
        event.setCancelled(true);
        player.chat("/survivalgames list");
        return;
      }

    }

    if ((isPreventingNonSGCommands()) && 
      (isTributeAtAll(player)) && (!player.hasPermission("sg.admin"))) {
      List allowedCmds = getConfig().getStringList("game.command-whitelist");
      if (allowedCmds.size() > 0) {
        for (String entry : allowedCmds) {
          if (cmd.startsWith(entry)) {
            return;
          }
        }
      }
      if ((!cmd.startsWith("/sg")) && (!cmd.startsWith("/survivalgames")) && (!cmd.startsWith("/hg")) && (!cmd.startsWith("/hungergames"))) {
        player.sendMessage(this.tag + "§cOnly SG commands are allowed in Survival Games.");
        event.setCancelled(true);
      }
    }
  }

  public void startGame(int arena)
  {
    setGameStatus(GameStatus.INGAME, arena);

    boolean changeTime = getConfig().getBoolean("game.set-to-day-on-start");

    if (changeTime) {
      getSurvivalGamesWorld(arena).setTime(0L);
    }

    for (Player pl : getTributesOfArena(arena)) {
      pl.getInventory().clear();
      pl.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
      pl.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
      pl.getInventory().setLeggings(new ItemStack(Material.AIR, 1));
      pl.getInventory().setBoots(new ItemStack(Material.AIR, 1));

      giveKitStuff(pl);
    }

    setTicks(getDeathmatchTimeTrigger() * 60, arena);

    sendTributeMessage("§2The games have begun!", true, arena);
    sendTributeMessage("§2Deathmatch begins in " + formatTime(getTicks(arena)) + "§2!", true, arena);
    if (this.graceSeconds > 0)
      sendTributeMessage("§2You have " + formatTime(this.graceSeconds) + " of a §6grace period§2!", true, arena);
  }

  public int getTributeArena(Player player)
  {
    if (this.gamerOfArena.containsKey(player.getName())) {
      return ((Integer)this.gamerOfArena.get(player.getName())).intValue();
    }

    return 0;
  }

  public void sendTributeMessage(String msg, boolean showTag, int arena)
  {
    if (showTag) {
      msg = this.tag + msg;
    }

    for (Player pl : getTributesOfArena(arena)) {
      int theirArena = getTributeArena(pl);
      if (theirArena == arena)
        pl.sendMessage(msg);
    }
  }

  public int getNextTributeSlot(int arena)
  {
    int one = 0;
    int two = 0;
    int three = 0;
    int four = 0;
    int five = 0;
    int six = 0;
    int seven = 0;
    int eight = 0;
    int nine = 0;
    int ten = 0;
    int eleven = 0;
    int twelve = 0;
    int thirteen = 0;
    int fourteen = 0;
    int fifteen = 0;
    int sixteen = 0;
    int seventeen = 0;
    int eightteen = 0;
    int nineteen = 0;
    int twenty = 0;
    int twentyone = 0;
    int twentytwo = 0;
    int twentythree = 0;
    int twentyfour = 0;

    for (Player entry : getTributesOfArena(arena))
    {
      int id = ((Integer)this.gamers.get(entry.getName())).intValue();

      if (id == 1) {
        one = 1;
      }
      else if (id == 2) {
        two = 1;
      }
      else if (id == 3) {
        three = 1;
      }
      else if (id == 4) {
        four = 1;
      }
      else if (id == 5) {
        five = 1;
      }
      else if (id == 6) {
        six = 1;
      }
      else if (id == 7) {
        seven = 1;
      }
      else if (id == 8) {
        eight = 1;
      }
      else if (id == 9) {
        nine = 1;
      }
      else if (id == 10) {
        ten = 1;
      }
      else if (id == 11) {
        eleven = 1;
      }
      else if (id == 12) {
        twelve = 1;
      }
      else if (id == 13) {
        thirteen = 1;
      }
      else if (id == 14) {
        fourteen = 1;
      }
      else if (id == 15) {
        fifteen = 1;
      }
      else if (id == 16) {
        sixteen = 1;
      }
      else if (id == 17) {
        seventeen = 1;
      }
      else if (id == 18) {
        eightteen = 1;
      }
      else if (id == 19) {
        nineteen = 1;
      }
      else if (id == 20) {
        twenty = 1;
      }
      else if (id == 21) {
        twentyone = 1;
      }
      else if (id == 22) {
        twentytwo = 1;
      }
      else if (id == 23) {
        twentythree = 1;
      }
      else if (id == 24) {
        twentyfour = 1;
      }
    }

    if (one == 0) {
      return 1;
    }
    if (two == 0) {
      return 2;
    }
    if (three == 0) {
      return 3;
    }
    if (four == 0) {
      return 4;
    }
    if (five == 0) {
      return 5;
    }
    if (six == 0) {
      return 6;
    }
    if (seven == 0) {
      return 7;
    }
    if (eight == 0) {
      return 8;
    }
    if (nine == 0) {
      return 9;
    }
    if (ten == 0) {
      return 10;
    }
    if (eleven == 0) {
      return 11;
    }
    if (twelve == 0) {
      return 12;
    }
    if (thirteen == 0) {
      return 13;
    }
    if (fourteen == 0) {
      return 14;
    }
    if (fifteen == 0) {
      return 15;
    }
    if (sixteen == 0) {
      return 16;
    }
    if (seventeen == 0) {
      return 17;
    }
    if (eightteen == 0) {
      return 18;
    }
    if (nineteen == 0) {
      return 19;
    }
    if (twenty == 0) {
      return 20;
    }
    if (twentyone == 0) {
      return 21;
    }
    if (twentytwo == 0) {
      return 22;
    }
    if (twentythree == 0) {
      return 23;
    }
    if (twentyfour == 0) {
      return 24;
    }

    return 0;
  }

  public static List<Block> blocksFromTwoPoints(Location loc1, Location loc2)
  {
    List blocks = new ArrayList();

    int topBlockX = loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX();
    int bottomBlockX = loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX();

    int topBlockY = loc1.getBlockY() < loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY();
    int bottomBlockY = loc1.getBlockY() > loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY();

    int topBlockZ = loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ();
    int bottomBlockZ = loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ();

    for (int x = bottomBlockX; x <= topBlockX; x++)
    {
      for (int z = bottomBlockZ; z <= topBlockZ; z++)
      {
        for (int y = bottomBlockY; y <= topBlockY; y++)
        {
          Block block = loc1.getWorld().getBlockAt(x, y, z);

          blocks.add(block);
        }
      }
    }

    return blocks;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();

    File dataFile = new File(getDataFolder(), getFileName());
    FileConfiguration stats = getStatsFile();

    if (!stats.contains("stats." + player.getName().toLowerCase())) {
      stats.set("stats." + player.getName().toLowerCase() + ".wins", Integer.valueOf(0));
      stats.set("stats." + player.getName().toLowerCase() + ".games", Integer.valueOf(0));
      stats.set("stats." + player.getName().toLowerCase() + ".kills", Integer.valueOf(0));
      stats.set("stats." + player.getName().toLowerCase() + ".chests", Integer.valueOf(0));
      try {
        stats.save(dataFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
      log("Stats file generated for " + player.getName() + "!");
    }

    if (this.branded == 1)
      player.sendMessage(this.tag + "This server uses §2§lTurq's Survival Games§2, by §aturqmelon§2!");
  }

  public ItemStack getReward()
  {
    try
    {
      int id = getConfig().getInt("winreward.item");

      int amt = getConfig().getInt("winreward.amount");

      Material mat = getItemById(id);

      return new ItemStack(mat, amt);
    }
    catch (NumberFormatException e)
    {
      try
      {
        Material mat = Material.getMaterial(getConfig().getString("winreward.item").toUpperCase());

        int amt = getConfig().getInt("winreward.amount");

        return new ItemStack(mat, amt);
      }
      catch (Exception ex)
      {
        String val = getConfig().getString("winreward.item");

        if (val.equalsIgnoreCase("MONEY"))
        {
          int amt = getConfig().getInt("winreward.amount");

          return new ItemStack(Material.ACTIVATOR_RAIL, amt);
        }

        log("ERROR! Unknown reward! Returning null.");
      }

    }

    return null;
  }

  public String parseSeconds(int s)
  {
    long minute = TimeUnit.SECONDS.toMinutes(s) - TimeUnit.SECONDS.toHours(s) * 60L;
    long second = TimeUnit.SECONDS.toSeconds(s) - TimeUnit.SECONDS.toMinutes(s) * 60L;

    String seconds = second;
    String minutes = minute;

    return minutes + "m" + seconds + "s";
  }

  public String formatTime(int s)
  {
    long minute = TimeUnit.SECONDS.toMinutes(s) - TimeUnit.SECONDS.toHours(s) * 60L;
    long second = TimeUnit.SECONDS.toSeconds(s) - TimeUnit.SECONDS.toMinutes(s) * 60L;

    if (minute > 0L)
    {
      if (second > 0L) {
        return "§8[§a" + minute + "§8]§2 minutes §8[§a" + second + "§8]§2 seconds";
      }

      return "§8[§a" + minute + "§8]§2 minutes";
    }

    return "§8[§a" + second + "§8]§2 seconds";
  }

  public void setTributeSpawn(int tribute, Location loc, int arena)
  {
    if ((tribute >= 1) && (tribute <= 24))
    {
      getConfig().set("spawns.arena" + arena + "." + tribute + ".x", Integer.valueOf(loc.getBlockX()));
      getConfig().set("spawns.arena" + arena + "." + tribute + ".y", Integer.valueOf(loc.getBlockY()));
      getConfig().set("spawns.arena" + arena + "." + tribute + ".z", Integer.valueOf(loc.getBlockZ()));

      saveConfig();
    }
    else
    {
      log("ERROR! Cannot set tribute spawn for anything larger than 24 and smaller than 1.");
    }
  }

  public void setArenaCenter(Location loc, int arena)
  {
    getConfig().set("arenas.arena" + arena + ".center.x", Integer.valueOf(loc.getBlockX()));
    getConfig().set("arenas.arena" + arena + ".center.y", Integer.valueOf(loc.getBlockY()));
    getConfig().set("arenas.arena" + arena + ".center.z", Integer.valueOf(loc.getBlockZ()));

    saveConfig();
  }

  public void setWinsForPlayer(Player player, int num)
  {
    if (isUsingStats()) {
      String user = player.getName().toLowerCase();

      FileConfiguration stats = getStatsFile();

      stats.set("stats." + user + ".wins", Integer.valueOf(num));
      File dataFile = new File(getDataFolder(), getFileName());
      try
      {
        stats.save(dataFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void setGamesForPlayer(Player player, int num) {
    if (isUsingStats()) {
      String user = player.getName().toLowerCase();

      FileConfiguration stats = getStatsFile();

      stats.set("stats." + user + ".games", Integer.valueOf(num));
      File dataFile = new File(getDataFolder(), getFileName());
      try
      {
        stats.save(dataFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void setKilsForPlayer(Player player, int num) {
    if (isUsingStats()) {
      String user = player.getName().toLowerCase();

      FileConfiguration stats = getStatsFile();

      stats.set("stats." + user + ".kills", Integer.valueOf(num));
      File dataFile = new File(getDataFolder(), getFileName());
      try
      {
        stats.save(dataFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void setOpenedChestsForPlayer(Player player, int num) {
    if (isUsingStats()) {
      String user = player.getName().toLowerCase();

      FileConfiguration stats = getStatsFile();

      stats.set("stats." + user + ".chests", Integer.valueOf(num));

      File dataFile = new File(getDataFolder(), getFileName());
      try
      {
        stats.save(dataFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public int getOpenedChestsForPlayer(Player player)
  {
    String user = player.getName().toLowerCase();

    FileConfiguration stats = getStatsFile();

    return stats.getInt("stats." + user + ".chests");
  }

  public int getPlayedGamesForPlayer(Player player) {
    String user = player.getName().toLowerCase();

    FileConfiguration stats = getStatsFile();

    return stats.getInt("stats." + user + ".games");
  }

  public int getKillsForPlayer(Player player) {
    String user = player.getName().toLowerCase();

    FileConfiguration stats = getStatsFile();

    return stats.getInt("stats." + user + ".kills");
  }

  public int getWinsForPlayer(Player player) {
    String user = player.getName().toLowerCase();

    FileConfiguration stats = getStatsFile();

    return stats.getInt("stats." + user + ".wins");
  }

  public Location getTributeSpawn(int id, int arena) {
    int x = getConfig().getInt("spawns.arena" + arena + "." + id + ".x");
    int y = getConfig().getInt("spawns.arena" + arena + "." + id + ".y");
    int z = getConfig().getInt("spawns.arena" + arena + "." + id + ".z");

    return new Location(getSurvivalGamesWorld(arena), x + 0.5D, y, z + 0.5D, 0.0F, 64.0F);
  }

  public Location getArenaCenter(int arena)
  {
    int x = getConfig().getInt("arenas.arena" + arena + ".center.x");
    int y = getConfig().getInt("arenas.arena" + arena + ".center.y");
    int z = getConfig().getInt("arenas.arena" + arena + ".center.z");

    return new Location(getSurvivalGamesWorld(arena), x, y, z);
  }

  public int getDeathmatchRequiredPlayers()
  {
    return getConfig().getInt("deathmatch.start-at-players");
  }

  public int getGameMaxTime() {
    return getConfig().getInt("deathmatch.endgame-after-minutes");
  }

  public int getDeathmatchTimeTrigger() {
    return getConfig().getInt("deathmatch.start-after-time-minutes");
  }

  public int getDeathmatchRadius(int arena) {
    return getConfig().getInt("arenas.arena" + arena + ".dmradius");
  }

  public List<Material> getAllowsBlocksToPlace() {
    List blocks = getConfig().getStringList("blocks.allow-place");
    List list = new ArrayList();

    for (String val : blocks) {
      int id = Integer.parseInt(val);
      list.add(Material.getMaterial(id));
    }

    return list;
  }

  public List<Material> getAllowedBlocksToBreak() {
    List blocks = getConfig().getStringList("blocks.allow-break");
    List list = new ArrayList();

    for (String val : blocks) {
      int id = Integer.parseInt(val);
      list.add(Material.getMaterial(id));
    }

    return list;
  }

  public boolean doesJoinCommandRequireEmptyInventory() {
    return getConfig().getBoolean("joining.require-empty-inventory");
  }

  public boolean doesJoinCommandRequireSameWorld() {
    return getConfig().getBoolean("joining.require-same-world");
  }

  public boolean doesJoinCommandRequireFullHP() {
    return getConfig().getBoolean("joining.require-full-health");
  }

  public int getArenaRadius(int arena) {
    return getConfig().getInt("arenas.arena" + arena + ".radius");
  }

  public boolean isSGOnlyChat() {
    return getConfig().getBoolean("chat.arena-specific-chat");
  }

  public boolean isPreventingNonSGCommands() {
    return getConfig().getBoolean("game.prevent-non-sg-commands");
  }

  public int getStartingCountdown() {
    return getConfig().getInt("starting.game-starting-countdown");
  }

  public int getWhenGameShouldStartPlayers() {
    return getConfig().getInt("starting.start-countdown-at-players");
  }

  public void rollbackBlocks(int arena)
  {
    int rb1 = 0;
    int rb2 = 0;

    List keysToRemove = new ArrayList();

    for (int i = 0; i < this.brokenBlocks.size(); i++)
    {
      String key = (String)this.brokenBlocks.keySet().toArray()[i];

      Material block = (Material)this.brokenBlocks.values().toArray()[i];

      String[] analyze = key.split(">");

      World world = Bukkit.getWorld(analyze[0]);
      int x = Integer.parseInt(analyze[1]);
      int y = Integer.parseInt(analyze[2]);
      int z = Integer.parseInt(analyze[3]);

      int a = Integer.parseInt(analyze[4]);

      if (a == arena) {
        rb1++;

        Location loc = new Location(world, x, y, z);
        loc.getBlock().setType(block);

        keysToRemove.add(key);
      }
    }
    String key;
    for (int i = 0; i < this.placedBlocks.size(); i++)
    {
      key = (String)this.placedBlocks.keySet().toArray()[i];

      String[] analyze = key.split(">");

      World world = Bukkit.getWorld(analyze[0]);
      int x = Integer.parseInt(analyze[1]);
      int y = Integer.parseInt(analyze[2]);
      int z = Integer.parseInt(analyze[3]);

      int a = Integer.parseInt(analyze[4]);

      if (a == arena) {
        rb2++;

        Location loc = new Location(world, x, y, z);
        loc.getBlock().setType(Material.AIR);

        keysToRemove.add(key);
      }

    }

    for (String entry : keysToRemove) {
      if (this.placedBlocks.containsKey(entry)) {
        this.placedBlocks.remove(entry);
      }
      if (this.brokenBlocks.containsKey(entry)) {
        this.brokenBlocks.remove(entry);
      }
    }

    log("Rolled back " + rb1 + " broken blocks in arena " + arena + "!");
    log("Rolled back " + rb2 + " placed blocks in arena " + arena + "!");
  }

  public List<ItemStack> getKitItems(String kit)
  {
    List active = new ArrayList();
    if (isKitEnabled(kit))
    {
      List values = getConfig().getStringList("kits." + kit.toLowerCase() + ".items");
      for (String entry : values)
      {
        String[] v = entry.split(">");

        int id = Integer.parseInt(v[0]);
        int amt = Integer.parseInt(v[1]);

        Material mat = Material.getMaterial(id);
        ItemStack stack = new ItemStack(mat, amt);

        active.add(stack);
      }

    }

    return active;
  }

  public void givePotionEffectForKill(Player player) {
    boolean giveEffect = getConfig().getBoolean("killperks.give-potions-on-kill");

    if (giveEffect)
    {
      int effect = getConfig().getInt("killperks.effect");
      int time = getConfig().getInt("killperks.duration") * 20;
      int amp = getConfig().getInt("killperks.amplifier");

      PotionEffect finalEffect = new PotionEffect(PotionEffectType.getById(effect), time, amp);

      player.addPotionEffect(finalEffect);
    }
  }

  public List<PotionEffect> getKitPotionEffects(String kit)
  {
    List active = new ArrayList();
    if (isKitEnabled(kit))
    {
      List values = getConfig().getStringList("kits." + kit.toLowerCase() + ".potion-effects");
      for (String entry : values)
      {
        String[] v = entry.split(">");

        int potionID = Integer.parseInt(v[0]);
        int time = Integer.parseInt(v[1]) * 20;
        int amp = Integer.parseInt(v[2]);

        PotionEffectType type = PotionEffectType.getById(potionID);

        PotionEffect effect = new PotionEffect(type, time, amp);

        active.add(effect);
      }

    }

    return active;
  }

  public ItemStack getKitFeet(String kit) {
    if (isKitEnabled(kit)) {
      String item = getConfig().getString("kits." + kit.toLowerCase() + ".feet");
      try
      {
        int id = Integer.parseInt(item);

        return new ItemStack(Material.getMaterial(id), 1);
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    }

    return new ItemStack(Material.AIR, 1);
  }

  public ItemStack getKitLegs(String kit) {
    if (isKitEnabled(kit)) {
      String item = getConfig().getString("kits." + kit.toLowerCase() + ".legs");
      try
      {
        int id = Integer.parseInt(item);

        return new ItemStack(Material.getMaterial(id), 1);
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    }

    return new ItemStack(Material.AIR, 1);
  }

  public ItemStack getKitChest(String kit) {
    if (isKitEnabled(kit)) {
      String item = getConfig().getString("kits." + kit.toLowerCase() + ".chest");
      try
      {
        int id = Integer.parseInt(item);

        return new ItemStack(Material.getMaterial(id), 1);
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    }

    return new ItemStack(Material.AIR, 1);
  }

  public ItemStack getKitHelm(String kit) {
    if (isKitEnabled(kit)) {
      String item = getConfig().getString("kits." + kit.toLowerCase() + ".helmet");
      try
      {
        int id = Integer.parseInt(item);

        return new ItemStack(Material.getMaterial(id), 1);
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    }

    return new ItemStack(Material.AIR, 1);
  }

  public boolean isKitEnabled(String kit) {
    boolean useKits = getConfig().getBoolean("kits.use-kits");
    if (useKits) {
      return getConfig().getBoolean("kits." + kit.toLowerCase() + ".enabled");
    }
    return false;
  }

  public int getMaxArenas() {
    return getConfig().getInt("arenas.arena-amount");
  }

  public void setGlobalGameStatus(GameStatus s) {
    for (int i = 1; i <= getMaxArenas(); i++)
    {
      setGameStatus(s, i);
    }
  }

  public void setGameStatus(GameStatus s, int arena)
  {
    this.gameStatus.put(Integer.valueOf(arena), s);
  }

  public GameStatus getGameStatus(int arena)
  {
    if (this.gameStatus.containsKey(Integer.valueOf(arena))) {
      return (GameStatus)this.gameStatus.get(Integer.valueOf(arena));
    }

    return GameStatus.IDLE;
  }

  public int getRemainingTributes(int arena)
  {
    int i = 0;
    for (Player trib : getTributesOfArena(arena))
    {
      int id = ((Integer)this.gamers.get(trib.getName())).intValue();
      if (id != 0) {
        i++;
      }

    }

    return i;
  }

  public boolean isSponsoringEnabled()
  {
    return getConfig().getBoolean("sponsoring.enable-sponsoring");
  }

  @EventHandler
  public void onClick(PlayerInteractEntityEvent event) {
    Entity entity = event.getRightClicked();
    Player player = event.getPlayer();

    if (isTributeAtAll(player)) {
      int id = ((Integer)this.gamers.get(player.getName())).intValue();
      if (id == 0)
      {
        if ((entity instanceof Player)) {
          Player target = (Player)entity;

          if (isTributeAtAll(target)) {
            int id2 = ((Integer)this.gamers.get(target.getName())).intValue();
            if (id2 != 0)
            {
              if (isSponsoringEnabled())
              {
                player.sendMessage(this.tag + "Opening sponsoring window for " + target.getDisplayName() + "§2...");
                player.openInventory(generateSponsorWindow(player, target.getName()));
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onInvClick(InventoryClickEvent event)
  {
    HumanEntity entity = event.getWhoClicked();
    if ((entity instanceof Player)) {
      Player player = (Player)entity;

      ItemStack clicked = event.getCurrentItem();

      if (clicked != null)
      {
        if (clicked.getType() == Material.SKULL_ITEM)
        {
          List lore = clicked.getItemMeta().getLore();

          if (lore.contains(this.tag))
          {
            event.setCancelled(true);

            if (isTributeAtAll(player)) {
              int id = ((Integer)this.gamers.get(player.getName())).intValue();

              if (id == 0) {
                int arena = getTributeArena(player);

                Player target = Bukkit.getServer().getPlayer(clicked.getItemMeta().getDisplayName());
                if (target != null)
                {
                  if (isTributeAtAll(target)) {
                    int ar = getTributeArena(target);

                    if (arena == ar) {
                      player.teleport(target);
                      refreshSpectator(player);

                      player.closeInventory();
                      player.sendMessage(this.tag + "Teleported to " + target.getDisplayName() + "§2.");
                    }

                  }

                }

              }

            }

          }

        }
        else if ((clicked.getType() == Material.ARROW) && 
          (clicked.getItemMeta().getDisplayName().equalsIgnoreCase("Click to leave the game!"))) {
          player.closeInventory();
          player.chat("/sg leave");
        }

        try
        {
          List lore = clicked.getItemMeta().getLore();
          if (lore.contains(this.tag)) {
            event.setCancelled(true);

            if ((!lore.contains("§cYou don't have permission")) && (!lore.contains("[Can't Afford]")))
            {
              String metaName = clicked.getItemMeta().getDisplayName();

              String[] val = metaName.split(":");

              int i = Integer.parseInt(val[0].replace("§e", ""));

              String name = getConfig().getString("sponsoring.item" + i + ".name").replace("&", "§");
              String type = getConfig().getString("sponsoring.item" + i + ".type");
              int cost = getConfig().getInt("sponsoring.item" + i + ".cost");

              Player target = Bukkit.getServer().getPlayer(event.getInventory().getTitle().replace("§1", ""));

              if (target != null)
              {
                if (isTributeAtAll(target))
                {
                  econ.withdrawPlayer(player.getName(), cost);

                  player.closeInventory();
                  player.sendMessage(this.tag + "Sponsored " + target.getDisplayName() + "§2 a(n) §a" + name + "§2!");
                  target.sendMessage(this.tag + "You were sponsored a(n) §a" + name + "§2!");
                  player.sendMessage(this.tag + "§cCharged §f" + cost + " " + econ.currencyNamePlural() + "§c!");

                  target.playSound(target.getLocation(), Sound.SPLASH2, 1.0F, -1.0F);

                  if (type.equalsIgnoreCase("ITEM")) {
                    int id = getConfig().getInt("sponsoring.item" + i + ".item");
                    int amount = getConfig().getInt("sponsoring.item" + i + ".amount");
                    int data = getConfig().getInt("sponsoring.item" + i + ".data");

                    Material mat = getItemById(id);

                    if (data != -1) {
                      target.getInventory().addItem(new ItemStack[] { new ItemStack(mat, amount, (byte)data) });
                    }
                    else {
                      target.getInventory().addItem(new ItemStack[] { new ItemStack(mat, amount) });
                    }

                  }
                  else if (type.equalsIgnoreCase("XP")) {
                    int xp = getConfig().getInt("sponsoring.item" + i + ".exp");
                    int lvls = getConfig().getInt("sponsoring.item" + i + ".levels");

                    target.setLevel(target.getLevel() + lvls);
                    target.setExp(target.getExp() + xp);
                  }
                  else if (type.equalsIgnoreCase("POTIONEFFECT")) {
                    int potionID = getConfig().getInt("sponsoring.item" + i + ".potion");
                    int dur = getConfig().getInt("sponsoring.item" + i + ".duration") * 20;
                    int amp = getConfig().getInt("sponsoring.item" + i + ".amplifier");

                    PotionEffect effect = new PotionEffect(PotionEffectType.getById(potionID), dur, amp);

                    target.addPotionEffect(effect);
                  }

                }
                else
                {
                  player.closeInventory();
                }
              }
              else
              {
                player.closeInventory();
                player.sendMessage(this.tag + "§cTribute \"§f" + event.getInventory().getTitle() + "§c\" not found!");
              }
            }
          }
        }
        catch (NullPointerException localNullPointerException)
        {
        }
      }
    }
  }

  public Inventory generateSponsorWindow(Player target, String targetPlayer)
  {
    double balance = econ.getBalance(target.getName());

    int itemCount = getConfig().getInt("sponsoring.number-of-items");
    int size = 9;

    if (itemCount <= 9) {
      size = 9;
    }
    else if ((itemCount > 9) && (itemCount <= 18)) {
      size = 18;
    }
    else if ((itemCount > 9) && (itemCount <= 27)) {
      size = 27;
    }
    else if ((itemCount > 9) && (itemCount <= 36)) {
      size = 36;
    }
    else if ((itemCount > 9) && (itemCount <= 48)) {
      size = 48;
    }
    else if ((itemCount > 9) && (itemCount <= 54)) {
      size = 54;
    }

    Inventory inv = Bukkit.createInventory(null, size, "§1" + targetPlayer);

    for (int i = 1; i <= itemCount; i++)
    {
      String name = getConfig().getString("sponsoring.item" + i + ".name").replace("&", "§");
      String type = getConfig().getString("sponsoring.item" + i + ".type");
      int cost = getConfig().getInt("sponsoring.item" + i + ".cost");

      Material icon = getItemById(getConfig().getInt("sponsoring.item" + i + ".icon"));
      ItemStack stack = new ItemStack(icon, 1);
      ItemMeta meta = stack.getItemMeta();

      meta.setDisplayName("§e" + i + ": §f" + name);

      List lore = new ArrayList();
      if (target.hasPermission("sg.sponsor.item" + i)) {
        if (type.equalsIgnoreCase("ITEM"))
        {
          int id = getConfig().getInt("sponsoring.item" + i + ".item");
          int amount = getConfig().getInt("sponsoring.item" + i + ".amount");

          Material mat = getItemById(id);

          lore.add("§8Item:§b " + amount + "x " + WordUtils.capitalizeFully(mat.toString().replace("_", " ")));
        }
        else if (type.equalsIgnoreCase("XP")) {
          int xp = getConfig().getInt("sponsoring.item" + i + ".exp");
          int lvls = getConfig().getInt("sponsoring.item" + i + ".levels");

          lore.add("§8XP:§b " + lvls + " Level(s), " + xp + " EXP");
        }
        else if (type.equalsIgnoreCase("POTIONEFFECT")) {
          int potionID = getConfig().getInt("sponsoring.item" + i + ".potion");
          int dur = getConfig().getInt("sponsoring.item" + i + ".duration");
          int amp = getConfig().getInt("sponsoring.item" + i + ".amplifier");

          int l = amp + 1;

          String t = WordUtils.capitalizeFully(PotionEffectType.getById(potionID).getName().replace("_", " "));

          lore.add("§8Potion:§b " + t + " " + l + "for, " + dur + "s");
        }
        String currency = "";
        if ((cost > 1) && (cost != 0)) {
          currency = econ.currencyNameSingular();
        }
        else {
          currency = econ.currencyNamePlural();
        }

        if (balance >= cost) {
          lore.add("§8Cost:§b " + cost + " " + currency);
        }
        else
          lore.add("§8Cost:§c " + cost + " " + currency + "§4 [Can't Afford]");
      }
      else
      {
        lore.add("§cYou don't have permission");
        lore.add("§cto sponsor this item!");
      }

      lore.add(this.tag);

      meta.setLore(lore);

      stack.setItemMeta(meta);

      if ((target.hasPermission("sg.sponsor.item" + i)) && (balance >= cost)) {
        int slot = i - 1;
        inv.setItem(slot, stack);
      }

    }

    return inv;
  }

  public Material getItemById(int id)
  {
    return Material.getMaterial(id);
  }

  public List<Player> getTributesOfArena(int arena) {
    List list = new ArrayList();
    for (Player all : Bukkit.getServer().getOnlinePlayers()) {
      if (this.gamers.containsKey(all.getName())) {
        int theirArena = getTributeArena(all);
        if (theirArena == arena) {
          list.add(all);
        }
      }
    }

    return list;
  }

  public List<Player> getAllTributes() {
    List list = new ArrayList();
    for (Player all : Bukkit.getServer().getOnlinePlayers()) {
      if (this.gamers.containsKey(all.getName())) {
        list.add(all);
      }
    }

    return list;
  }

  public boolean useFlashBangEggs() {
    return getConfig().getBoolean("specialitems.flashbangeggs");
  }

  public boolean useSlowingSnowballs() {
    return getConfig().getBoolean("specialitems.slowingsnowballs");
  }

  public boolean useInstaTNT() {
    return getConfig().getBoolean("specialitems.instatnt");
  }

  @EventHandler
  public void onTNT(EntityExplodeEvent event) {
    Entity entity = event.getEntity();

    for (int i = 1; i <= getMaxArenas(); i++)
      if (isArenaEnabled(i))
      {
        World world = getSurvivalGamesWorld(i);
        if (entity != null) {
          World entityWorld = entity.getWorld();

          if (isSameWorld(world, entityWorld)) {
            double distance = entity.getLocation().distance(getArenaCenter(i));

            int radius = getArenaRadius(i);
            radius += 30;

            if (distance <= radius)
            {
              List near = entity.getNearbyEntities(5.0D, 5.0D, 5.0D);

              for (Entity entry : near) {
                if ((entry instanceof LivingEntity))
                {
                  LivingEntity ent = (LivingEntity)entry;
                  ent.damage(6.0D);
                }

              }

              event.blockList().clear();
              break;
            }
          }
        }
      }
  }

  public void refreshTabList(Player player)
  {
    if (shouldIUseTabListIAmIndecisiveAndCantThinkForMyself())
    {
      if (isTributeAtAll(player))
      {
        int arena = getTributeArena(player);

        int priority = getConfig().getInt("specialstuff.packetapi-priority");

        if (priority > 2) {
          priority = 2;
        }
        else if (priority < 1) {
          priority = 1;
        }

        TabAPI.setPriority(this, player, priority);

        TabAPI.setTabString(this, player, 0, 0, "§2============§a");
        TabAPI.setTabString(this, player, 0, 1, "§2============§b");
        TabAPI.setTabString(this, player, 0, 2, "§2============§c");

        TabAPI.setTabString(this, player, 1, 0, "§6§lThe");
        TabAPI.setTabString(this, player, 1, 1, "§6§lSurvival");
        TabAPI.setTabString(this, player, 1, 2, "§6§lGames");

        TabAPI.setTabString(this, player, 2, 0, "§2============§d");
        TabAPI.setTabString(this, player, 2, 1, "§2============§e");
        TabAPI.setTabString(this, player, 2, 2, "§2============§f");

        TabAPI.setTabString(this, player, 3, 0, "§6§nArena " + arena);
        TabAPI.setTabString(this, player, 3, 1, "§6§n" + WordUtils.capitalizeFully(getGameStatus(arena).toString()));
        TabAPI.setTabString(this, player, 3, 2, "§6§n" + getRemainingTributes(arena) + " Tributes");
        TabAPI.setTabString(this, player, 4, 0, "§a§d");
        TabAPI.setTabString(this, player, 4, 1, "§e§l" + parseSeconds(getTicks(arena) - 1));
        TabAPI.setTabString(this, player, 4, 2, "§a§e");
        TabAPI.setTabString(this, player, 5, 0, "§a§f");
        TabAPI.setTabString(this, player, 5, 1, "§a§1");
        TabAPI.setTabString(this, player, 5, 2, "§a§2");
        TabAPI.setTabString(this, player, 6, 0, "§2§nTributes");
        TabAPI.setTabString(this, player, 6, 1, "§a§3");
        TabAPI.setTabString(this, player, 6, 2, "§a§4");
        List tribs = new ArrayList();
        List specs = new ArrayList();

        for (Player pl : getTributesOfArena(arena)) {
          int id = ((Integer)this.gamers.get(pl.getName())).intValue();
          if (id != 0) {
            StringBuilder n = new StringBuilder();
            n.append(pl.getName());
            if (n.length() > 16) {
              n.setLength(16);
            }

            tribs.add(n);
          }
          else
          {
            StringBuilder n = new StringBuilder();
            n.append(pl.getName());
            if (n.length() > 16) {
              n.setLength(16);
            }

            specs.add(n);
          }
        }

        int x = 7;
        int y = 0;

        int i = 0;
        do
        {
          try
          {
            TabAPI.setTabString(this, player, x, y, (String)tribs.get(i));
          }
          catch (Exception e) {
            TabAPI.setTabString(this, player, x, y, TabAPI.nextNull() + TabAPI.nextNull() + TabAPI.nextNull());
          }

          i++;
          y++;

          if (y > 2) {
            y = 0;
            x++;
          }
        }
        while (
          x <= 11);

        TabAPI.setTabString(this, player, 12, 0, "§2§nSpectators");
        TabAPI.setTabString(this, player, 12, 1, "§c§3");
        TabAPI.setTabString(this, player, 12, 2, "§c§4");

        int x1 = 13;
        int y1 = 0;

        int i1 = 0;
        do
        {
          try
          {
            TabAPI.setTabString(this, player, x1, y1, (String)specs.get(i1));
          }
          catch (Exception e) {
            TabAPI.setTabString(this, player, x1, y1, TabAPI.nextNull() + TabAPI.nextNull() + TabAPI.nextNull());
          }

          i1++;
          y1++;

          if (y1 > 2) {
            y1 = 0;
            x1++;
          }
        }
        while (
          x1 <= 16);
        TabAPI.updatePlayer(player);
      }
      else
      {
        TabAPI.clearTab(player);
        TabAPI.setPriority(this, player, -2);
        TabAPI.updatePlayer(player);
      }
    }
  }

  public boolean shouldIUseTabListIAmIndecisiveAndCantThinkForMyself()
  {
    return getConfig().getBoolean("specialstuff.use-packetapi");
  }

  public boolean canJoinGame(GameStatus status)
  {
    if ((status != GameStatus.IDLE) && (status != GameStatus.STARTING)) {
      return false;
    }

    return true;
  }

  public void onDisable()
  {
    setGlobalGameStatus(GameStatus.IDLE);

    for (Player pl : getAllTributes()) {
      int arena = getTributeArena(pl);
      leaveGame(pl, arena);
    }

    setGlobalGameStatus(GameStatus.CLEANUP);

    for (int i = 1; i <= getMaxArenas(); i++) {
      rollbackBlocks(i);
    }

    log("Disabled!");
  }

  public static String MD5(String md5) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] array = md.digest(md5.getBytes());
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < array.length; i++) {
        sb.append(Integer.toHexString(array[i] & 0xFF | 0x100).substring(1, 3));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
    }
    return null;
  }

  public void onEnable()
  {
    if (!new File(getDataFolder(), "config.yml").exists()) {
      saveDefaultConfig();
    }

    getServer().getPluginManager().registerEvents(this, this);

    String key = getConfig().getString("branding.custom-branding-key");
    String asMD5 = MD5(key);

    log("Checking custom branding key...");
    if (asMD5.equals("6da0ffe44f84fdca3624bfd60aefc776")) {
      log("Key is [VALID]! Using specified branding settings.");
      this.tag = getConfig().getString("branding.tag").replace("&", "§");
      this.name = getConfig().getString("branding.name").replace("&", "§");
    }
    else {
      log("Key is [INVALID] (or not entered), using plugin defaults.");
      this.tag = "§8[§6MelonSG§8]§2 ";
      this.name = "§aTurq's Survival Games§2";
      getConfig().set("branding.dm-message", "&4Now now, President Snow does not like when tributes run!");
      getConfig().set("branding.sign-tag", "&5[MelonSG]");
      saveConfig();
      this.branded = 1;
    }

    int configVersion = getConfig().getInt("config-version");
    if (configVersion < 3) {
      log("Configuration outdated! Settings brought up to date.");

      getConfig().set("config-version", Integer.valueOf(3));
      getConfig().set("starting.make-sure-inv-is-empty", Boolean.valueOf(true));
      log("Update complete: 1 new option(s) generated.");
      saveConfig();
    }

    if (configVersion < 4) {
      log("Configuration outdated! Settings brought up to date.");

      getConfig().set("config-version", Integer.valueOf(4));
      getConfig().set("specialstuff.use-packetapi", Boolean.valueOf(false));
      log("Update complete: 1 new option(s) generated.");
      saveConfig();
    }

    if (configVersion < 5) {
      log("Configuration outdated! Settings brought up to date.");

      getConfig().set("config-version", Integer.valueOf(5));
      getConfig().set("sponsoring.enable-sponsoring", Boolean.valueOf(false));
      getConfig().set("sponsoring.number-of-items", Integer.valueOf(3));
      log("Update complete: 2 new option(s) generated.");
      saveConfig();
    }

    if (configVersion < 6) {
      log("Configuration outdated! Settings brought up to date.");

      getConfig().set("config-version", Integer.valueOf(6));
      getConfig().set("specialitems.instatnt", Boolean.valueOf(true));
      getConfig().set("specialitems.slowingsnowballs", Boolean.valueOf(true));
      getConfig().set("specialitems.flashbangeggs", Boolean.valueOf(true));
      getConfig().set("specialitems.trackingcompass", Boolean.valueOf(true));
      getConfig().set("specialitems.revealingdiamond", Boolean.valueOf(true));
      log("Update complete: 5 new option(s) generated.");
      saveConfig();
    }

    if (configVersion < 7) {
      log("Configuration outdated! Settings brought up to date.");
      getConfig().set("config-version", Integer.valueOf(7));
      getConfig().set("chestloot.all-chests-restock-as-tier2", Boolean.valueOf(true));
      getConfig().set("chestloot.restock-chests-after-seconds", Integer.valueOf(440));
      log("Update complete: 2 new option(s) generated.");
      saveConfig();
    }
    if (configVersion < 8) {
      log("Configuration outdated! Settings brought up to date.");
      getConfig().set("config-version", Integer.valueOf(8));
      getConfig().set("scoreboard.use-scoreboard", Boolean.valueOf(true));
      getConfig().set("scoreboard.use-lobby-scoreboard", Boolean.valueOf(true));
      log("Update complete: 2 new option(s) generated.");
      saveConfig();
    }
    if (configVersion < 9) {
      log("Configuration outdated! Settings brought up to date.");
      getConfig().set("config-version", Integer.valueOf(9));
      getConfig().set("stats-options.enable-stats-system", Boolean.valueOf(true));
      getConfig().set("stats-options.method", "ymlFile");
      getConfig().set("stats-options.ymlfile.file-name", "stats.yml");
      log("Update complete: 3 new option(s) generated.");
      saveConfig();
    }
    if (configVersion < 10) {
      log("Configuration outdated! Settings brought up to date.");
      getConfig().set("config-version", Integer.valueOf(10));
      getConfig().set("chat.arena-specific-chat", Boolean.valueOf(isSGOnlyChat()));
      getConfig().set("chat.hide-non-sg-chat", Boolean.valueOf(true));
      getConfig().set("chat.use-display-names", Boolean.valueOf(true));
      getConfig().set("chat.tribute-chat.format", "&8[&bA%a&8] &4%d&8|&2%n&8: &7%m");
      getConfig().set("chat.tribute-chat.range", Integer.valueOf(-1));
      getConfig().set("chat.spec-chat.format", "&8[&bA%a&8|&3Spec&8] &9%n&8: &f%m");
      getConfig().set("lightning.enable-lightning", Boolean.valueOf(false));
      getConfig().set("lightning.start-at-tributes", Integer.valueOf(6));
      getConfig().set("lightning.interval-in-seconds", Integer.valueOf(30));
      getConfig().set("game.command-whitelist", new ArrayList());
      getConfig().set("starting.force-gamemode", GameMode.SURVIVAL.toString());
      getConfig().set("chestloot.maximum-items-per-chest", Integer.valueOf(10));
      getConfig().set("chestloot.minimum-items-per-chest", Integer.valueOf(3));
      log("Update complete: 13 new option(s) generated.");
      saveConfig();
    }
    if (configVersion < 11) {
      getConfig().set("config-version", Integer.valueOf(11));
      getConfig().set("grace-period.enable-grace-period", Boolean.valueOf(false));
      getConfig().set("grace-period.grace-period-seconds", Integer.valueOf(15));
      log("Update complete: 2 new option(s) generated.");
      saveConfig();
    }
    if (configVersion < 12) {
      getConfig().set("config-version", Integer.valueOf(12));
      getConfig().set("boss-health.enable-messagebari-api", Boolean.valueOf(false));
      getConfig().set("boss-health.lobby-text", "&aWelcome to the Survival Games lobby!");
      getConfig().set("boss-health.arena-text", "&2Survival Games: &aArena %n");
      getConfig().set("boss-health.spec-text", "&3Spectating: &bArena %n");
      getConfig().set("game.enable-leave-alias", Boolean.valueOf(true));
      log("Update complete: 6 new option(s) generated.");
      saveConfig();
    }

    if (shouldIUseTabListIAmIndecisiveAndCantThinkForMyself()) {
      Plugin tabPlugin = Bukkit.getPluginManager().getPlugin("TabAPI");
      if (tabPlugin == null) {
        log("ERROR! I was told to send data to the tablist, but TabAPI is not installed! NOOOO! :(");
        getConfig().set("specialstuff.use-packetapi", Boolean.valueOf(false));
        getConfig().set("specialstuff.packetapi-priority", Integer.valueOf(2));
        saveConfig();
      }
    }

    if (getConfig().getBoolean("grace-period.enable-grace-period")) {
      this.graceSeconds = getConfig().getInt("grace-period.grace-period-seconds");
    }

    if (getConfig().getBoolean("sg-healthy")) {
      this.disabled = true;
    }

    if (useLobbyScoreboard()) {
      registerLobbyScoreboard();
      log("Lobby scoreboard registered.");
    }

    if (isUsingStats()) {
      log("Stats enabled; using specified method.");

      String process = getStatsMethod();
      if (process.equalsIgnoreCase("ymlfile")) {
        log("Using YMLFile for stats storage.");
        process = "ymlfile";
        String fileName = getFileName();
        if (!new File(getDataFolder(), fileName).exists()) {
          log("Specified stats file " + fileName + " not found, creating!");
          File dataFile = new File(getDataFolder(), fileName);
          try {
            dataFile.createNewFile();
          } catch (IOException e1) {
            e1.printStackTrace();
          }
          FileConfiguration statsFile = new YamlConfiguration();
          try {
            statsFile.load(dataFile);
            statsFile.save(dataFile);
          }
          catch (FileNotFoundException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          } catch (InvalidConfigurationException e) {
            e.printStackTrace();
          }
        }
      }
      else
      {
        log("Unknown file method! Stats will not be saved for this session.");
      }
    }
    else
    {
      log("Stats disabled; skipping load process.");
    }

    if (getConfig().getBoolean("boss-health.enable-messagebar-api")) {
      getServer().getPluginManager().registerEvents(new MessageBar(this), this);
    }

    loop();

    int seconds = getConfig().getInt("lightning.interval-in-seconds");
    lightningLoop(seconds);

    setupEconomy();

    log("I am now enabled! Version " + getDescription().getVersion() + " by turqmelon");
  }

  public void lightningLoop(int seconds)
  {
    seconds *= 20;

    boolean useLightning = getConfig().getBoolean("lightning.enable-lightning");
    if (useLightning)
      Bukkit.getServer().getScheduler().runTaskTimer(this, new Runnable()
      {
        public void run()
        {
          for (int i = 1; i <= Core.this.getMaxArenas(); i++)
          {
            if ((Core.this.isActualArena(i)) && (Core.this.isArenaEnabled(i)))
            {
              if (Core.this.getGameStatus(i) == GameStatus.INGAME)
              {
                int tribCount = Core.this.getRemainingTributes(i);
                int minimum = Core.this.getConfig().getInt("lightning.start-at-tributes");

                if (tribCount <= minimum)
                {
                  for (Player pl : Core.this.getTributesOfArena(i))
                  {
                    int id = ((Integer)Core.this.gamers.get(pl.getName())).intValue();
                    if (id > 0)
                      pl.getWorld().strikeLightningEffect(pl.getLocation());
                  }
                }
              }
            }
          }
        }
      }
      , 20L, seconds);
  }

  public FileConfiguration getStatsFile()
  {
    File dataFile = new File(getDataFolder(), getFileName());
    FileConfiguration data = new YamlConfiguration();
    try
    {
      data.load(dataFile);
    }
    catch (IOException|InvalidConfigurationException e) {
      e.printStackTrace();
    }

    return data;
  }

  public String getFileName() {
    return getConfig().getString("stats-options.ymlfile.file-name");
  }

  public String getStatsMethod() {
    return getConfig().getString("stats-options.method");
  }

  public boolean isUsingStats() {
    return getConfig().getBoolean("stats-options.enable-stats-system");
  }

  private boolean setupEconomy() {
    if (getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }
    RegisteredServiceProvider rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return false;
    }
    econ = (Economy)rsp.getProvider();
    return econ != null;
  }
}