package me.PocketIsland.SurvivalGames;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class User {

	Player player;
	SecretProjectPlugin plugin;
	UserRank rank;
	SQLConfig config;
	ScoreboardInstance scoreboard;
	int points;

	public User(SecretProjectPlugin plugin, Player player)
	{
		this.player = player;
		this.plugin = plugin;
		this.rank = UserRank.NORMAL;
		this.points = 0;
		scoreboard= new ScoreboardInstance(this);
		//TODO: load config from external database
		config = new SQLConfig(new ArrayList<String>(), new ArrayList<Object>());
	}

	public void loadUser()
	{
		this.rank = UserRank.VIP;
		this.points = 10;
		//Load player scoreboard rank
		showRank();
	}

	public void showRank()
	{
		final User THIS = this;
		Bukkit.getScheduler().runTask(plugin, new Runnable() {

			public void run() {
				plugin.registerAndUpdateScoreboardTeamForPlayer(THIS, rank.rankPrefix, rank.rankName, true, plugin.getUserManager().getUsers());
			}
		});
	}

	public void unloadUser()
	{

	}

	public UserRank getRank() {
		return rank;
	}

	public void setRank(UserRank rank) {
		this.rank = rank;
	}

	public Player getPlayer() {
		return player;
	}

	public SecretProjectPlugin getPlugin() {
		return plugin;
	}

	public SQLConfig getConfig() {
		return config;
	}

	public ScoreboardInstance getScoreboard() {
		return scoreboard;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}




}
