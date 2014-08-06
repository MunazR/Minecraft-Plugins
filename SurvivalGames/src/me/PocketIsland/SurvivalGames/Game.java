package me.PocketIsland.SurvivalGames;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.v1_7_R3.EnumClientCommand;
import net.minecraft.server.v1_7_R3.PacketPlayInClientCommand;
import net.minecraft.util.org.apache.commons.lang3.mutable.MutableInt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

public abstract class Game implements Listener{

	protected SecretProjectPlugin plugin;
	protected String gameName;
	protected ConcurrentHashMap<Player, CustomPlayer> players;
	protected GameState state;
	protected int minimumPlayers, maxPlayers;
	protected int startingTicks;
	protected ArrayList<Integer> startedThreads;
	protected int ticksToStop;
	protected Location spawnPoint, lobbySpawnPoint;
	protected boolean canBuild;
	protected boolean isPvP, isTeamFireEnabled;
	protected boolean makeSpectatorOnDeath; //TODO: make the spectator mode
	protected ArrayList<CustomTeam> teams;
	protected boolean dropXp;
	protected String gamePrefix;
	protected boolean doPlayerDrops;
	protected boolean canPlayerMove;
	protected ArrayList<String> rules;
	public Game(SecretProjectPlugin plugin, String gameName)
	{
		this.plugin = plugin;
		this.gameName = gameName;
		this.gamePrefix =  ChatColor.RED + "[" + ChatColor.RESET + "%" + ChatColor.RESET + "" +ChatColor.RED + "] " + ChatColor.GOLD + ": ";
		this.players = new ConcurrentHashMap<Player, CustomPlayer>();
		this.state = GameState.WAITING;
		this.minimumPlayers = 4;
		this.startedThreads = new ArrayList<Integer>();
		this.maxPlayers = 16;
		rules = new ArrayList<String>();
		this.dropXp = true;
		this.doPlayerDrops = false;
		this.loadWorld(getWorldName());
		this.spawnPoint = this.getSpawn();
		this.lobbySpawnPoint = this.getLobbySpawnPoint();
		this.canBuild = false;
		this.canPlayerMove = true;
		this.isPvP = false;
		this.teams = new  ArrayList<CustomTeam>();
		this.makeSpectatorOnDeath = false;
		this.startingTicks = 1200;//One minute
		this.ticksToStop = 12000;//10 minite game : <= 20 to remove the timer
		//Registering a listener
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
		final MutableInt threadTimerId = new MutableInt(0);
		BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable(){

			public void run() {
				if(isStarted() || isStopped())
				{
					Bukkit.getScheduler().cancelTask(threadTimerId.getValue());
					return;
				}
				if(players.size() >= minimumPlayers)
				{
					startingTicks --;
					if(startingTicks % 600 == 0 && startingTicks != 0)
					{
						sendChatMessage(ChatColor.AQUA+"Starting in " + (startingTicks/600) + " seconds!");
					}
				}
				if(startingTicks == 0)
				{
					beforeStartGame();
					Bukkit.getScheduler().cancelTask(threadTimerId.intValue());
				}
				else
				{
					ArrayList<String> lines = new ArrayList<String>();
					lines.add(ChatColor.RED+""+ChatColor.BOLD+"Max players");
					lines.add(ChatColor.RED+""+maxPlayers);
					lines.add(" ");
					lines.add(ChatColor.GOLD+""+ChatColor.BOLD+"Current players");
					lines.add(ChatColor.GOLD+""+players.size());
					lines.add("  ");
					lines.add(ChatColor.YELLOW+""+ChatColor.BOLD+"Min players");
					lines.add(ChatColor.YELLOW+""+minimumPlayers);
					lines.add("   ");
					lines.add(ChatColor.GRAY+""+ChatColor.BOLD+"Starting in");
					lines.add(ChatColor.GRAY+""+getTime(startingTicks));
					lines.add("    ");
					lines.add(ChatColor.WHITE+""+ChatColor.BOLD+"Points");
					for(CustomPlayer player : getPlayers())
					{
						ArrayList<String> linesP = new ArrayList<String>(lines);
						linesP.add(ChatColor.WHITE+""+player.getUser().getPoints());
						player.getScoreboard().sendLines(linesP);
					}
				}
			}}, 1, 1);
		threadTimerId.setValue(task.getTaskId());
		this.startedThreads.add(task.getTaskId());
	}

	String getTime(int ticks)
	{
		int seconds = ticks/20;
		int minutes = (int)seconds/60;
		int secs = 0;
		if(seconds%60 == 0);
		else
			secs = (int) (seconds-(minutes*60));
		String msg = "";
		if(minutes != 0)
			msg+= minutes + " minute"+(secs <= 1 ? "" : "s")+" ";
		if(secs != 0)
			msg+=""+secs + " second"+(secs <= 1 ? "" : "s");
		return msg;
	}

	String getWorldName()
	{
		return plugin.clearColor(gameName.replace(" ", "").replace("_", "").replace("-", "")).toUpperCase();
	}


	public CustomPlayer[] getPlayers()
	{
		ArrayList<CustomPlayer> playerArray = new ArrayList<CustomPlayer>();
		Enumeration<CustomPlayer> enu = players.elements();
		while(enu.hasMoreElements())
		{
			playerArray.add(enu.nextElement());
		}
		return playerArray.toArray(new CustomPlayer[0]);
	}

	public void addPlayerToTheGame(Player player)
	{
		final CustomPlayer custom = new CustomPlayer(player, this);
		this.sendChatMessageToPlayer(custom, ChatColor.AQUA+"Welcome to " + gameName + ChatColor.AQUA+" !");
		this.clearPlayer(custom);
		players.put(player, custom);
		if(isStarted())
		{
			Bukkit.getScheduler().runTaskLater(getPlugin(), new Runnable(){

				public void run() {
					loadPlayer(custom);
					loadPlayerInventory(custom);
					custom.teleportTo(spawnPoint);
				}},1);

		}
		else
		{
			custom.teleportTo(lobbySpawnPoint);
		}
		update();
	}

	public void sendChatMessage(String message)
	{
		String msg = ChatColor.RED + "[" + ChatColor.RESET + this.gameName + ChatColor.RESET + "" +ChatColor.RED + "] " + ChatColor.DARK_RED + ": " + ChatColor.RESET + message;
		this.sendRawChatMessage(msg);
	}

	public void sendRawChatMessage(String message)
	{
		Enumeration<CustomPlayer> enu = players.elements();
		while(enu.hasMoreElements())
		{
			CustomPlayer pl = enu.nextElement();
			pl.getPlayer().sendMessage(message);
		}
	}
	public void sendChatMessageToPlayer(Player player, String message)
	{
		String msg = gamePrefix.replace("%", gameName) + ChatColor.RESET + message;
		this.sendRawChatMessageToPlayer(player, msg);
	}

	public void sendChatMessageToPlayer(CustomPlayer player, String message)
	{
		String msg = gamePrefix.replace("%", gameName) + ChatColor.RESET + message;
		this.sendRawChatMessageToPlayer(player.player, msg);
	}
	public void sendRawChatMessageToPlayer(Player player, String message)
	{
		player.sendMessage(message);
	}

	public CustomPlayer getCustomPlayer(Player player)
	{
		if(player == null)return null;
		return players.get(player);
	}

	public void removePlayerToTheGame(Player player)
	{
		CustomPlayer custom = players.get(player);
		if(custom != null)
		{
			if(custom.getTeam() != null)
				custom.getTeam().removePlayer(custom);
			players.remove(player);
		}
		boolean stopGame = players.size() <= 1;
		for(CustomTeam team : this.teams)
		{
			if(team.getSize() < 1)
			{
				stopGame = true;
				break;
			}
		}
		if(stopGame)
			beforeStopGame();
		update();
	}

	public void beforeStartGame()
	{
		state = GameState.STARTED;
		this.canPlayerMove = false;
		if(rules.size() == 0)
		{
			rules.add("No cheating");
			rules.add("No spaming");
		}
		getPlugin().canChat = false;
		//Send rules
		Bukkit.getScheduler().runTask(getPlugin(), new Runnable(){

			public void run() {
				for(int i = 0; i < 10; i++)sendRawChatMessage("");
				sendRawChatMessage(ChatColor.GREEN+"Rules : ");
				for(String rule : rules)
					sendRawChatMessage("     " + ChatColor.GRAY+" - " + ChatColor.DARK_GREEN + rule);
				sendRawChatMessage("");
			}});
		for(int i = 1; i < 10; i++)
		{
			final int seconds = 10-i;
			BukkitTask t = Bukkit.getScheduler().runTaskLater(getPlugin(), new Runnable() {

				public void run() {
					ArrayList<String> lines = new ArrayList<String>();
					lines.add(ChatColor.RED+""+ChatColor.BOLD+"Max players");
					lines.add(ChatColor.RED+""+maxPlayers);
					lines.add(" ");
					lines.add(ChatColor.GOLD+""+ChatColor.BOLD+"Current players");
					lines.add(ChatColor.GOLD+""+players.size());
					lines.add("  ");
					lines.add(ChatColor.YELLOW+""+ChatColor.BOLD+"Min players");
					lines.add(ChatColor.YELLOW+""+minimumPlayers);
					lines.add("   ");
					lines.add(ChatColor.GRAY+""+ChatColor.BOLD+"Starting in");
					lines.add(ChatColor.GRAY+""+getTime(seconds*20));
					lines.add("    ");
					lines.add(ChatColor.WHITE+""+ChatColor.BOLD+"Points");
					for(CustomPlayer player : getPlayers())
					{
						ArrayList<String> linesP = new ArrayList<String>(lines);
						linesP.add(ChatColor.WHITE+""+player.getUser().getPoints());
						player.getScoreboard().sendLines(linesP);
					}
				}
			}, (i-1)*20);
			this.startedThreads.add(t.getTaskId());
		}
		BukkitTask t = Bukkit.getScheduler().runTaskLater(getPlugin(), new Runnable() {

			public void run() {
				ArrayList<String> lines = new ArrayList<String>();
				lines.add(ChatColor.RED+""+ChatColor.BOLD+"Max players");
				lines.add(ChatColor.RED+""+maxPlayers);
				lines.add(" ");
				lines.add(ChatColor.GOLD+""+ChatColor.BOLD+"Current players");
				lines.add(ChatColor.GOLD+""+players.size());
				lines.add("  ");
				lines.add(ChatColor.YELLOW+""+ChatColor.BOLD+"Min players");
				lines.add(ChatColor.YELLOW+""+minimumPlayers);
				lines.add("   ");
				lines.add(ChatColor.GRAY+""+ChatColor.BOLD+"Starting in");
				lines.add(ChatColor.GRAY+""+ChatColor.RED+"NOW!");
				lines.add("    ");
				lines.add(ChatColor.WHITE+""+ChatColor.BOLD+"Points");
				for(CustomPlayer player : getPlayers())
				{
					ArrayList<String> linesP = new ArrayList<String>(lines);
					linesP.add(ChatColor.WHITE+""+player.getUser().getPoints());
					player.getScoreboard().sendLines(linesP);
				}
				for(int i = 0; i < 10; i++)sendRawChatMessage("");
			}
		}, 181);
		this.startedThreads.add(t.getTaskId());
		for(CustomPlayer customPlayer : getPlayers())
		{
			customPlayer.getPlayer().setWalkSpeed(0);
			loadPlayer(customPlayer);
		}
		//Starts he game 10 secs after
		BukkitTask t1 = Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

			public void run() {
				getPlugin().canChat = true;
				canPlayerMove = true;
				if(ticksToStop > 20)
				{
					final MutableInt threadTimerId = new MutableInt(0);
					BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable(){

						public void run() {
							ticksToStop--;
							if(ticksToStop % 1200 == 0 && ticksToStop != 0)
							{
								sendChatMessage(ChatColor.AQUA+"Stopping in " + (ticksToStop/1200) + " minute"+(ticksToStop == 1200 ? "" : "s")+"!");
							}
							if(ticksToStop == 0)
							{
								beforeStopGame();
								Bukkit.getScheduler().cancelTask(threadTimerId.intValue());
							}
						}}, 1, 1);
					threadTimerId.setValue(task.getTaskId());
					startedThreads.add(task.getTaskId());
				}
				for(CustomPlayer customPlayer : getPlayers())
				{
					customPlayer.getPlayer().setWalkSpeed(0.2f);
				}
				startGame();
			}
		},200);
		this.startedThreads.add(t1.getTaskId());
	}
	@EventHandler
	public void onPlayerMoveEventTop(final PlayerMoveEvent event)
	{
		if(getCustomPlayer(event.getPlayer()) == null)return;//Not in game
		if(event.getTo().getBlockX() == event.getPlayer().getLocation().getBlockX() && event.getTo().getBlockZ() == event.getPlayer().getLocation().getBlockZ())
		{
			return;
		}
		if(!this.canPlayerMove)
		{
			Bukkit.getScheduler().runTask(getPlugin(), new Runnable(){

				public void run() {event.getPlayer().teleport(new Location(event.getPlayer().getWorld(),event.getFrom().getBlockX() + 0.5,event.getFrom().getBlockY(),event.getFrom().getBlockZ() + 0.5, event.getPlayer().getLocation().getYaw(),event.getPlayer().getLocation().getPitch()));

				}});
			return;
		}
	}
	public void loadPlayer(final CustomPlayer customPlayer)
	{
		customPlayer.setTeam(this.getNewTeamForPlayer(customPlayer));
		customPlayer.player.teleport(this.getSpawnPointForTeam(customPlayer.getTeam()));
		clearPlayer(customPlayer);
		this.loadPlayerInventory(customPlayer);
		SecretProjectPlugin.updateInventory(customPlayer.getPlayer());
		Bukkit.getScheduler().runTaskLater(getPlugin(), new Runnable() {

			public void run() {
				for(CustomTeam team : teams)
				{
					team.registerAndUpdateTeamForPlayer(customPlayer);
				}
			}
		},1);
	}
	public abstract void startGame();

	public abstract void stopGame();

	public abstract Location getSpawn();

	public abstract CustomTeam getNewTeamForPlayer(CustomPlayer Player); 

	public abstract Location getSpawnPointForTeam(CustomTeam team); 

	public abstract Location getLobbySpawnPoint();

	public void beforeStopGame()
	{
		this.state = GameState.STOPPED;
		this.forceStop();
	}


	public void destroy()
	{
		for(int id : startedThreads)
		{
			Bukkit.getScheduler().cancelTask(id);
		}
		System.out.println("Stopped : " + startedThreads.size() + " threads!");
		//Unregistering the listener
		HandlerList.unregisterAll(this);
	}

	@EventHandler
	public void onInteractTop(PlayerInteractEvent event)
	{
		CustomPlayer player = getCustomPlayer(event.getPlayer());
		if(player == null)return;
		if(player.isSpectator())
		{
			event.setCancelled(true);
			SecretProjectPlugin.updateInventory(player.getPlayer());
			return;
		}
		if(!canPlayerMove)
		{
			event.setCancelled(true);
			SecretProjectPlugin.updateInventory(player.getPlayer());
			return;
		}
	}
	@EventHandler
	public void onPvPTOP(EntityDamageByEntityEvent event)
	{
		if(this.state == GameState.WAITING && event.getEntity() instanceof Player)
		{
			event.setCancelled(true);
			return;
		}
		Entity damager = (Entity) (event.getDamager() instanceof Projectile ?  ((Projectile)event.getDamager()).getShooter() : event.getDamager());
		if(damager instanceof Player && event.getEntity() instanceof Player)
		{
			CustomPlayer playerDamager = getCustomPlayer((Player)damager);
			CustomPlayer playerDamaged = getCustomPlayer((Player)event.getEntity());
			if(playerDamager == null || playerDamaged == null)return;
			if(!this.isPvP || (!this.isTeamFireEnabled && playerDamager.getTeam() == playerDamaged.getTeam()))
			{
				event.setCancelled(true);
				return;
			}
			HitType type = event.getDamager() instanceof Arrow ? HitType.PROJECTILE_ARROW : event.getDamager() instanceof Egg ? HitType.PROJECTILE_EGG : event.getDamager() instanceof Snowball ? HitType.PROJECTILE_SNOWBALL :  HitType.PVP;
			event.setDamage(this.onPlayerHitPlayer(playerDamaged, playerDamager, type, event.getDamage()));
			if(event.getDamage() == 0)
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPvPTOP(EntityDamageEvent event)
	{
		if(this.state == GameState.WAITING && event.getEntity() instanceof Player)
		{
			Player player = (Player)event.getEntity();
			if(event.getCause() == DamageCause.VOID)
			{
				player.damage(10000.0);
			}
			event.setCancelled(true);
			return;
		}
	}

	//Can be overriden
	public double onPlayerHitPlayer(CustomPlayer player, CustomPlayer damager, HitType type, double damage)
	{
		return damage;
	}

	@EventHandler
	public void onBuildPlace(BlockPlaceEvent event)
	{
		CustomPlayer player = this.getCustomPlayer(event.getPlayer());
		if(player == null)return;
		if(!this.canBuild || !this.canPlayerPlaceBlock(player, event.getBlock()))
		{
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onBuildBreak(BlockBreakEvent event)
	{
		CustomPlayer player = this.getCustomPlayer(event.getPlayer());
		if(player == null)return;
		if(!this.canBuild || !this.canPlayerBreakBlock(player, event.getBlock()))
		{
			event.setCancelled(true);
		}
	}

	public boolean canPlayerPlaceBlock(CustomPlayer player, Block block)
	{
		return true;
	}

	public boolean canPlayerBreakBlock(CustomPlayer player, Block block)
	{
		return true;
	}

	public void onPlayerKillPlayer(CustomPlayer player, CustomPlayer killer)
	{
		return;
	}

	public abstract void loadPlayerInventory(CustomPlayer player);

	public void clearPlayer(final CustomPlayer custom)
	{
		final Player player = custom.getPlayer();
		player.setWalkSpeed(0.2f);
		player.getInventory().clear();
		player.getInventory().setBoots(new ItemStack(Material.AIR));
		player.getInventory().setLeggings(new ItemStack(Material.AIR));
		player.getInventory().setChestplate(new ItemStack(Material.AIR));
		player.getInventory().setHelmet(new ItemStack(Material.AIR));
		player.setFoodLevel(20);
		player.setLevel(0);
		player.setExp(0);
	}

	public void resetPlayer(Player player)
	{
		player.getInventory().clear();
		for(PotionEffect potion : player.getActivePotionEffects())
		{
			player.removePotionEffect(potion.getType());
		}
		player.getInventory().setBoots(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setHelmet(null);
		CraftPlayer cp = (CraftPlayer)player;
		cp.setMaxHealth(20);
		cp.setHealth(20);
	}

	public ItemStack setColor(ItemStack item, int color){
		if(item.getItemMeta() != null)
		{
			if(item.getItemMeta() instanceof LeatherArmorMeta)
			{
				LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
				meta.setColor(Color.fromRGB(color));
				item.setItemMeta(meta);
			}
		}
		return item;
	}

	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent event)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
            	((CraftPlayer)event.getEntity()).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
               
            }
        }, 1L);
		event.setDeathMessage("");
		if(!this.dropXp)
			event.setDroppedExp(0);
		final CustomPlayer player = this.getCustomPlayer(event.getEntity());
		if(player == null)return;
		Bukkit.getScheduler().runTaskLater(getPlugin(), new Runnable(){

			public void run() {
				clearPlayer(player);
				loadPlayerInventory(player);
				SecretProjectPlugin.updateInventory(player.getPlayer());
			}},3);
		if(!doPlayerDrops)
			event.getDrops().clear();
		player.deaths++;
		CustomPlayer killer = this.getCustomPlayer(event.getEntity().getKiller());
		if(killer != null)
			killer.kills++;
		update();
		this.onPlayerKillPlayer(player, killer);
	}

	public void update()
	{

	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		CustomPlayer player = this.getCustomPlayer(event.getPlayer());
		if(player == null)return;
		if(state == GameState.WAITING)
		{
			event.setRespawnLocation(spawnPoint);
			return;
		}
		event.setRespawnLocation(this.getSpawnPointForTeam(player.team));
	}

	public SecretProjectPlugin getPlugin() {
		return plugin;
	}

	public World loadWorld(String name)
	{
		World world = Bukkit.getWorld(name);
		//if world not laoded :
		if(world == null)
		{
			world = WorldCreator.name(name).createWorld();
		}
		return world;
	}

	public void forceStop()
	{
		final Game game = plugin.getNewGame();
		plugin.setCurrentGame(game);
		Bukkit.getScheduler().runTaskLater(getPlugin(), new Runnable(){

			public void run() {
				for(Player p : Bukkit.getOnlinePlayers())
				{
					game.addPlayerToTheGame(p);
				}
			}},2);
		for(User u : plugin.getUserManager().getUsers())
		{
			u.getScoreboard().reset();
			u.showRank();
		}
		this.destroy();
	}

	public boolean isStarted()
	{
		return (state == GameState.STARTED);
	}

	public boolean isStopped()
	{
		return (state == GameState.STOPPED);
	}

	public String getName()
	{
		return gameName;
	}

	public GameState getState() {
		return state;
	}


}