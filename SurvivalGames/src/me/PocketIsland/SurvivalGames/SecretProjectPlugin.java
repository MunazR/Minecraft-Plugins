package me.PocketIsland.SurvivalGames;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public abstract class SecretProjectPlugin extends JavaPlugin implements Listener{

	protected UserManager userManager;
	protected Game currentGame;
	public Random rand;
	ScoreboardManager scoreboardManager;
	public static SecretProjectPlugin instance;
	public boolean doMobSpawning;
	public boolean canChat;

	public void onEnable()
	{
		System.out.println("Loading the main core");
		instance = this;
		this.scoreboardManager = this.getServer().getScoreboardManager();
		this.getServer().getPluginManager().registerEvents(this, this);
		userManager = new UserManager(this);
		this.rand = new Random();
		this.doMobSpawning = false;
		this.onLoadPlugin();
		this.canChat = true;
		currentGame = this.getNewGame();
		if(currentGame == null)
		{
			//Either load an empty game or a lobby : Depends on how we want the system, 
			//	do we want the plugin to read from a file and then load the current game 
			//	or something else
		}
		for(Player p : Bukkit.getOnlinePlayers())
		{
			userManager.addUser(p);
		}
		System.out.println("Registering the commands");
		for(String cmd : defaultCmd)
		{
			this.registerCommand(cmd);
		}
		System.out.println("Registered the commands");
		System.out.println("Loaded the main core");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		//TODO:
		if(sender.isOp())
		{
			if(cmd.getName().equalsIgnoreCase("startgame"))
			{
				if(currentGame.isStarted())
				{
					sender.sendMessage(ChatColor.RED+"Game is already started!");
				}
				else
				{
					currentGame.beforeStartGame();
				}
			}
			else if(cmd.getName().equalsIgnoreCase("stopgame"))
			{
				if(!currentGame.isStarted() || currentGame.isStopped())
				{
					sender.sendMessage(ChatColor.RED+"Game is not started or already stopped!");
				}
				else
				{
					currentGame.beforeStopGame();
				}
			}
			else if(cmd.getName().equalsIgnoreCase("state"))
			{
				sender.sendMessage(currentGame.getState().name());
			}
		}
		return true;
	}
	public abstract void onLoadPlugin();

	public void onDisable()
	{
		System.out.println("Unloading the main core");
		if(currentGame != null)
			currentGame.beforeStopGame();
		this.onUnloadPlugin(); 
		System.out.println("Unloaded the main core");
	}

	public abstract void onUnloadPlugin();


	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		event.setJoinMessage("");
		userManager.addUser(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		event.setQuitMessage("");
		userManager.removeUser(event.getPlayer());
	}

	@EventHandler
	public void onPlayerGetsKicked(PlayerKickEvent event)
	{
		event.setLeaveMessage("");
		userManager.removeUser(event.getPlayer());
	}

	public UserManager getUserManager() {
		return userManager;
	}


	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		if(!canChat)
		{
			event.setCancelled(true);
			return;
		}
		//Chat needs to be filtered
		String message = event.getMessage();
		User user = userManager.getUser(event.getPlayer());
		if(user == null)return;
		String prefix = user.getRank().getRankPrefix();
		String format = prefix + "%1$s " + ChatColor.GRAY + " : %2$s";
		event.setFormat(format);
		event.setMessage(message);
	}

	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	public void registerAndUpdateScoreboardTeamForPlayer(final User user, final String prefix, final String teamName, final boolean canSeeInvisibaleTeamMate, final User[] users)
	{
		//To be sure it is sync
		Bukkit.getScheduler().runTask(this, new Runnable() {

			public void run() {
				ScoreboardInstance scoreboard = user.getScoreboard();
				Scoreboard board = scoreboard.getScoreboardObject();
				Team team = board.getTeam(prefix+teamName);
				//No team found
				if(team == null)
				{
					team = board.registerNewTeam(prefix+teamName);
					team.setPrefix(prefix);
					team.setCanSeeFriendlyInvisibles(canSeeInvisibaleTeamMate);
				}
				else
				{
					if(!team.getPrefix().equalsIgnoreCase(prefix))
						team.setPrefix(prefix);
					if(canSeeInvisibaleTeamMate != team.canSeeFriendlyInvisibles())
						team.setCanSeeFriendlyInvisibles(canSeeInvisibaleTeamMate);
				}
				for(User user : users)
				{
					team.addPlayer(user.getPlayer());
				}
			}
		});
	}

	public String clearColor(String text)
	{
		for(ChatColor col : ChatColor.AQUA.getDeclaringClass().getEnumConstants())
		{
			text = text.replace(col+"", "");
		}
		return text;
	}

	protected String[] defaultCmd = new String[]{"startgame","stopgame","state"};
	public void registerCommand(String cmd)
	{
		try
		{
			SimplePluginManager pm = (SimplePluginManager)getServer().getPluginManager();
			Field f = SimplePluginManager.class.getDeclaredField("commandMap");
			f.setAccessible(true);
			SimpleCommandMap cm = (SimpleCommandMap)f.get(pm);
			f.setAccessible(false);
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
			PluginCommand command = c.newInstance(cmd, (Plugin)this);
			c.setAccessible(false);
			command.setExecutor(this);
			cm.register(cmd, command);
		}
		catch(Exception e) {e.printStackTrace();}
	}

	@EventHandler
	public void onMobSpawn(final CreatureSpawnEvent event)
	{
		if(event.getSpawnReason() == SpawnReason.CUSTOM)
		{
			return;
		}
		event.setCancelled(!doMobSpawning);
	}

	public Game getCurrentGame() {
		return currentGame;
	}

	public void setCurrentGame(Game currentGame) {
		this.currentGame = currentGame;
	}

	public static void updateInventory(final Player player)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SecretProjectPlugin.instance, new Runnable(){

			@SuppressWarnings("deprecation")
			public void run() {
				((CraftPlayer)player).getHandle().inventory.update();
				player.updateInventory();
			}}, 1);
	}

	public abstract void onPlayerJoin(User user);
	public abstract void onPlayerLeave(User user);
	public abstract Game getNewGame();

}