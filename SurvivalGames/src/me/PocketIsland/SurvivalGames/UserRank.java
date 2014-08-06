package me.PocketIsland.SurvivalGames;

import org.bukkit.ChatColor;

public enum UserRank {

	//TODO: NEEDS MORE INFO

	NORMAL(0, "", ""),
	VIP(1, ChatColor.GREEN+"["+ChatColor.DARK_GREEN+"VIP"+ChatColor.GREEN+"] ","VIP");

	int permissionLevel;
	String rankPrefix, rankName;

	UserRank(int permissionLevel, String rankPrefix, String rankName)
	{
		this.permissionLevel = permissionLevel;
		this.rankName = rankName;
		this.rankPrefix = rankPrefix;
	}

	public int getPermissionLevel() {
		return permissionLevel;
	}

	public String getRankPrefix() {
		return rankPrefix;
	}

	public String getRankName() {
		return rankName;
	}

}