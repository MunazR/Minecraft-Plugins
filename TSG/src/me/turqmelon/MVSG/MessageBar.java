package me.turqmelon.MVSG;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pw.ender.messagebar.MessageBarSetEvent;

public class MessageBar
  implements Listener
{
  private final Core p;

  public MessageBar(Core p)
  {
    this.p = p;
  }

  @EventHandler
  public void onMsg(MessageBarSetEvent event) {
    Player player = event.getPlayer();
    World lobby = this.p.getExitPoint().getWorld();
    if (player.getWorld().getName().equalsIgnoreCase(lobby.getName())) {
      String text = this.p.getConfig().getString("boss-health.lobby-text");
      text = parseMessage(text, 0);
      event.setMessage(text);
    }
    else if (this.p.isTributeAtAll(player)) {
      int arena = this.p.getTributeArena(player);
      if (this.p.isSpectator(player)) {
        String text = this.p.getConfig().getString("boss-health.spec-text");
        text = parseMessage(text, arena);
        event.setMessage(text);
      }
      else {
        String text = this.p.getConfig().getString("boss-health.arena-text");
        text = parseMessage(text, arena);
        event.setMessage(text);
      }
    }
  }

  public String parseMessage(String msg, int arena)
  {
    msg = msg.replace("&", "§");
    msg = msg.replace("%n", arena);
    return msg;
  }
}