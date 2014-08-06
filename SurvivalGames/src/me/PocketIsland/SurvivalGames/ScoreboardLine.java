package me.PocketIsland.SurvivalGames;

public class ScoreboardLine {

	String line;
	int points;
	String prefix;
	public ScoreboardLine(String line, int points)
	{
		this("", line, points);
		if(line.length() >= 17)
		{
			this.prefix = line.substring(0,16);
			this.line = line.substring(16, line.length());
		}

	}
	public ScoreboardLine(String prefix, String line, int points)
	{
		if(prefix == null)
			prefix = "";
		this.prefix = prefix;
		this.line = line;
		this.points = points;
	}

	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}


}