package me.PocketIsland.SurvivalGames;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CustomPlayer {

	Player player;
	User user;
	Game game;
	public int kills,deaths;
	CustomTeam team;
	boolean isSpectator;
	public CustomPlayer(Player player, Game game)
	{
		this.player = player;
		this.game = game;
		this.user = game.plugin.getUserManager().getUser(player);
		System.out.println("Loaded a new user : " + player.getName() + " - " + user);
	}

	public ScoreboardInstance getScoreboard()
	{
		return user.getScoreboard();
	}

	public void save()
	{

	}

	public CustomTeam getTeam() {
		return team;
	}

	public void teleportTo(final Location loc)
	{
		if(loc == null)return;
		Bukkit.getScheduler().runTask(game.getPlugin(), new Runnable() {

			public void run() {
				getPlayer().teleport(loc);
				final Player p = getPlayer();
				//Update the player... MC glitch : it sometimes doesn't show the player correctly
				for(Player player : Bukkit.getOnlinePlayers())
				{
					player.hidePlayer(p);
					player.showPlayer(p);
				}
			}
		});
	}
	public void setTeam(CustomTeam team) {
		if(this.team != null) this.team.removePlayer(this);
		this.team = team;
		team.addPlayer(this);
	}

	public boolean isSpectator() {
		return isSpectator;
	}

	public void setSpectator(boolean isSpectator) {
		this.isSpectator = isSpectator;
	}

	public int getKills() {
		return kills;
	}

	public void setKills(int kills) {
		this.kills = kills;
	}

	public int getDeaths() {
		return deaths;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	public Player getPlayer() {
		return player;
	}

	public User getUser() {
		return user;
	}

	public Game getGame() {
		return game;
	}



}
