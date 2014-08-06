package me.PocketIsland.pockethello;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public class MyPlayerListener implements Listener{
	
	private final Pockethello p;
	public MyPlayerListener(Pockethello p){
		this.p = p;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChat(AsyncPlayerChatEvent event){
		final Player player = event.getPlayer();
		if(! event.isCancelled()){
			int randInt;
			randInt = 20 + (int)(Math.random()* 60);
			if(event.getMessage().toLowerCase().contains("hi pocket") || event.getMessage().toLowerCase().contains("hello pocket") || event.getMessage().toLowerCase().contains("hey pocket")){
				Bukkit.getServer().getScheduler().runTaskLater(p, new Runnable() {
					
					public void run(){
						int randomInt = 0;
						randomInt = 1 + (int)(Math.random()*5);
						switch (randomInt){
						case 1:
							player.sendMessage(ChatColor.LIGHT_PURPLE + "PM" + ChatColor.GRAY + ": " + ChatColor.BLACK + "[" + ChatColor.GREEN + "O" + ChatColor.AQUA + "W" + ChatColor.RED + "N" + ChatColor.LIGHT_PURPLE + "E" + ChatColor.YELLOW + "R" + ChatColor.BLACK + "]" + ChatColor.DARK_RED + "PocketIsland " + ChatColor.RED + "-" + ChatColor.BOLD + ">" + ChatColor.RESET + ChatColor.WHITE + " You " + ChatColor.DARK_GREEN + ">" + ChatColor.LIGHT_PURPLE + " Hello!");
							break;
						case 2:
							player.sendMessage(ChatColor.LIGHT_PURPLE + "PM" + ChatColor.GRAY + ": " + ChatColor.BLACK + "[" + ChatColor.GREEN + "O" + ChatColor.AQUA + "W" + ChatColor.RED + "N" + ChatColor.LIGHT_PURPLE + "E" + ChatColor.YELLOW + "R" + ChatColor.BLACK + "]" + ChatColor.DARK_RED + "PocketIsland " + ChatColor.RED + "-" + ChatColor.BOLD + ">" + ChatColor.RESET + ChatColor.WHITE + " You " + ChatColor.DARK_GREEN + ">" + ChatColor.LIGHT_PURPLE + " Hi there!");
							break;
						case 3:
							player.sendMessage(ChatColor.LIGHT_PURPLE + "PM" + ChatColor.GRAY + ": " + ChatColor.BLACK + "[" + ChatColor.GREEN + "O" + ChatColor.AQUA + "W" + ChatColor.RED + "N" + ChatColor.LIGHT_PURPLE + "E" + ChatColor.YELLOW + "R" + ChatColor.BLACK + "]" + ChatColor.DARK_RED + "PocketIsland " + ChatColor.RED + "-" + ChatColor.BOLD + ">" + ChatColor.RESET + ChatColor.WHITE + " You " + ChatColor.DARK_GREEN + ">" + ChatColor.LIGHT_PURPLE + " How are you?");
							break;
						case 4:
							player.sendMessage(ChatColor.LIGHT_PURPLE + "PM" + ChatColor.GRAY + ": " + ChatColor.BLACK + "[" + ChatColor.GREEN + "O" + ChatColor.AQUA + "W" + ChatColor.RED + "N" + ChatColor.LIGHT_PURPLE + "E" + ChatColor.YELLOW + "R" + ChatColor.BLACK + "]" + ChatColor.DARK_RED + "PocketIsland " + ChatColor.RED + "-" + ChatColor.BOLD + ">" + ChatColor.RESET + ChatColor.WHITE + " You " + ChatColor.DARK_GREEN + ">" + ChatColor.LIGHT_PURPLE + " What's up!");
							break;
						default:
							player.sendMessage(ChatColor.LIGHT_PURPLE + "PM" + ChatColor.GRAY + ": " + ChatColor.BLACK + "[" + ChatColor.GREEN + "O" + ChatColor.AQUA + "W" + ChatColor.RED + "N" + ChatColor.LIGHT_PURPLE + "E" + ChatColor.YELLOW + "R" + ChatColor.BLACK + "]" + ChatColor.DARK_RED + "PocketIsland " + ChatColor.RED + "-" + ChatColor.BOLD + ">" + ChatColor.RESET + ChatColor.WHITE + " You " + ChatColor.DARK_GREEN + ">" + ChatColor.LIGHT_PURPLE + " Having fun?");
						}	
					}
					
				}, randInt);
			}
		}
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		final Player player = event.getPlayer();
		if (!event.getPlayer().hasPlayedBefore()){
			
			for(int x = 10; x < 15; x = x+1){
			
			Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
            
            //Our random generator
            Random r = new Random();   

            //Create our effect with this
            
            if (x == 10){ 
            	FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(Color.GREEN).withFade(Color.BLUE).with(Type.STAR).trail(r.nextBoolean()).build();
                fwm.addEffect(effect);
            }else if (x == 11){
            	FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(Color.RED).withFade(Color.PURPLE).with(Type.BALL).trail(r.nextBoolean()).build();
                fwm.addEffect(effect);
            }else if (x == 12){
                FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(Color.ORANGE).withFade(Color.GREEN).with(Type.BURST).trail(r.nextBoolean()).build();
                fwm.addEffect(effect);
            }else if (x == 13){
                FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(Color.PURPLE).withFade(Color.BLUE).with(Type.BALL_LARGE).trail(r.nextBoolean()).build();
                fwm.addEffect(effect);
            }else if (x == 14){
                FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(Color.AQUA).withFade(Color.ORANGE).with(Type.STAR).trail(r.nextBoolean()).build();
                fwm.addEffect(effect);
            }else if (x == 15){
                FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(Color.GREEN).withFade(Color.BLUE).with(Type.STAR).trail(r.nextBoolean()).build();
                fwm.addEffect(effect);
            }
            
            //Generate some random power and set it
            fwm.setPower(2);
           
            //Then apply this to our rocket
            fw.setFireworkMeta(fwm);
			}
		}
	}
}