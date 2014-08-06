package me.turqmelon.MVSG;

public enum GameStatus
{
  IDLE("idle"), STARTING("starting"), INGAME("ingame"), DEATHMATCH("deathmatch"), CLEANUP("cleanup");

  String gamePhase;

  private GameStatus(String name) {
    this.gamePhase = name;
  }

  public String toString()
  {
    return this.gamePhase;
  }
}