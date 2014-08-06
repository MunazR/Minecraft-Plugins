package me.turqmelon.MVSG;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.scoreboard.Scoreboard;

public class SBManager
{
  protected final Map<Integer, Scoreboard> arenaSBs = new HashMap<Integer, Scoreboard>();

  public SBManager(Core p) {  } 
  public void setLobbyScoreboard(Scoreboard sb) { this.arenaSBs.put(Integer.valueOf(-1), sb); }

  public Scoreboard getLobbyScoreboard()
  {
    return (Scoreboard)this.arenaSBs.get(Integer.valueOf(-1));
  }

  public Scoreboard getArenaScoreboard(int arena) {
    return (Scoreboard)this.arenaSBs.get(Integer.valueOf(arena));
  }

  public void setArenaScoreboard(int arena, Scoreboard sb)
  {
    this.arenaSBs.put(Integer.valueOf(arena), sb);
  }
}