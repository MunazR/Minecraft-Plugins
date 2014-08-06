package me.PocketIsland.SurvivalGames;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardInstance {

	User user;
	Scoreboard scoreboardObject;
	Objective scoreboardObjective;
	ArrayList<ScoreboardLine> lines;
	SecretProjectPlugin plugin;
	boolean canSendScoreboard;
	public ScoreboardInstance(final User user)
	{
		this.user = user;
		this.plugin = user.getPlugin();
		this.canSendScoreboard = false;
		this.lines = new ArrayList<ScoreboardLine>();
		Bukkit.getScheduler().runTask(plugin, new Runnable() {
			public void run() {
				scoreboardObject = plugin.getScoreboardManager().getNewScoreboard();
				scoreboardObjective = scoreboardObject.registerNewObjective(user.getPlugin().getCurrentGame().getName(), "");
				scoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
				canSendScoreboard = true;
				user.player.setScoreboard(scoreboardObject);
			}
		});
	}

	public void sendLines(ArrayList<String> lines)
	{
		ArrayList<Integer> points = new ArrayList<Integer>();
		for(int i =  lines.size(); i >=1; i--)
		{
			points.add(i);
		}
		this.sendLines(lines, points);
	}

	public void sendLines(ArrayList<String> lines, ArrayList<Integer> points)
	{
		if(lines.size() != points.size())
			this.sendLines(lines);
		else
		{
			ArrayList<String> prefixes = new ArrayList<String>();
			ArrayList<String> newLines = new ArrayList<String>();
			ArrayList<Integer> newPoints = new ArrayList<Integer>();
			for(int i = 0; i < lines.size(); i++)
			{
				String line = lines.get(i);
				String prefix = null;
				int point = points.get(i);
				if(line.length() > 16)
				{
					prefix = line.substring(0,16);
					line = line.substring(16, line.length());
				}
				prefixes.add(prefix);
				newLines.add(line);
				newPoints.add(point);
			}
			this.sendLines(prefixes,newLines, newPoints);
		}
	}
	/*
	 * The scheduler prevents async changes
	 */
	public void sendLines(final ArrayList<String> prefixesF, final ArrayList<String> linesF, final ArrayList<Integer> pointsF)
	{
		Bukkit.getScheduler().runTask(user.getPlugin(), new Runnable() {

			@SuppressWarnings("deprecation")
			public void run() {
				if(prefixesF.size() != linesF.size() || prefixesF.size() != pointsF.size() || linesF.size() != pointsF.size())
					return;
				if(!canSendScoreboard)return;
				canSendScoreboard = false;
				ArrayList<ScoreboardLine> sLines = new ArrayList<ScoreboardLine>();
				for(int i = 0; i < linesF.size(); i++)
				{
					String prefix = prefixesF.get(i);
					String line = linesF.get(i);
					int point = pointsF.get(i);
					boolean contains = false;
					for(ScoreboardLine l : lines)
					{
						if(l.prefix.equalsIgnoreCase(prefix) && l.line.equalsIgnoreCase(line))
						{
							if(l.points != point)
							{
								l.points = point;
								scoreboardObjective.getScore(Bukkit.getOfflinePlayer(line)).setScore(point);
							}
							sLines.add(l);
							contains = true;
							break;
						}
					}
					if(!contains)
					{
						if(prefix != null && prefix.length() > 0)
						{
							Team team = (scoreboardObject.getTeam(prefix) != null ? scoreboardObject.getTeam(prefix) : scoreboardObject.registerNewTeam(prefix));
							team.setPrefix(prefix);
							team.addPlayer(Bukkit.getOfflinePlayer(line));
						}
						scoreboardObjective.getScore(Bukkit.getOfflinePlayer(line)).setScore(point);
						sLines.add(new ScoreboardLine(prefix, line, point));
					}
				}
				//Remove unwanted lines
				for(ScoreboardLine l : lines)
				{
					boolean contains = false;
					for(int i = 0; i < linesF.size(); i++)
					{
						String line = linesF.get(i);
						if(line.equalsIgnoreCase(l.line))
						{
							contains = true;
							break;
						}
					}
					if(!contains)
					{
						scoreboardObject.resetScores(Bukkit.getOfflinePlayer(l.getLine()));
					}
				}
				lines = sLines;
				Bukkit.getScheduler().runTaskLater(user.getPlugin(), new Runnable() {

					public void run() {
						canSendScoreboard = true;
					}
				}, 1);
			}
		});
	}

	public void reset()
	{
		this.sendLines(new ArrayList<String>(),new ArrayList<String>(),new ArrayList<Integer>());
		this.setTitle(SecretProjectPlugin.instance.getCurrentGame().getName());
	}

	public void setTitle(String line)
	{
		if(line.length() > 16)
		{
			line = line.substring(0, 16);
		}
		scoreboardObjective.setDisplayName(line);
	}
	public Scoreboard getScoreboardObject() {
		return scoreboardObject;
	}



}