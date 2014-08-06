package me.PocketIsland.SurvivalGames;

import java.util.ArrayList;

public class CustomTeam {

	String teamName, prefix;
	Game game;
	ArrayList<CustomPlayer> players;
	boolean canSeeInvisibaleTeamMate;

	public CustomTeam(String prefix, String teamName, Game game)
	{
		this(prefix, teamName, true, game);
	}
	public CustomTeam(String prefix, String teamName, boolean canSeeOtherTeamMate, Game game)
	{
		this.teamName = teamName;
		this.prefix = prefix;
		this.canSeeInvisibaleTeamMate = canSeeOtherTeamMate;
		this.game = game;
		this.players = new ArrayList<CustomPlayer>();
	}
	public String getTeamName() {
		return teamName;
	}

	public Game getGame() {
		return game;
	}

	public void removePlayer(CustomPlayer player)
	{
		this.players.remove(player);
	}

	public int getSize()
	{
		return players.size();
	}

	public void addPlayer(final CustomPlayer player)
	{
		players.add(player);
		update();
	}

	public void update()
	{
		for(CustomPlayer player : game.getPlayers())
		{
			this.registerAndUpdateTeamForPlayer(player);
		}
	}

	public void registerAndUpdateTeamForPlayer(CustomPlayer player) {
		game.getPlugin().registerAndUpdateScoreboardTeamForPlayer(player.getUser(), prefix, teamName, canSeeInvisibaleTeamMate, getUsers());
	}

	public User[] getUsers()
	{
		ArrayList<User> users = new ArrayList<User>();
		for(CustomPlayer player : players)
		{
			if(player.getTeam() == this)
			{
				users.add(player.getUser());
			}
		}
		return users.toArray(new User[0]);
	}
}
