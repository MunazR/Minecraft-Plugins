package me.turqmelon.MVSG;

public enum WinResult
{
  HAS_WINNER("haswinner"), NO_WINNER("nowinner"), ADMIN_STOP("adminstop");

  String gamePhase;

  private WinResult(String name) {
    this.gamePhase = name;
  }

  public String toString()
  {
    return this.gamePhase;
  }
}