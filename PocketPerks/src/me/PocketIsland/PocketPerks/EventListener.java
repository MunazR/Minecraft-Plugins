package me.PocketIsland.PocketPerks;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EventListener implements Listener{
	private final Core p;

	public EventListener(Core plugin) {
		this.p = plugin;
	}

	@EventHandler (priority = EventPriority.LOW)
	public void blockPlace(BlockPlaceEvent event){
		if(event.isCancelled())
			return;

		Player player = event.getPlayer();

		if(event.getBlockPlaced().getType() == Material.TNT && p.tnt.containsKey(player.getName()) && p.tnt.get(player.getName())){
			player.getWorld().spawn(event.getBlock().getLocation(), TNTPrimed.class);
			event.getBlock().setType(Material.AIR);
		}
	}

	@EventHandler
	public void onTNTExplosion(EntityExplodeEvent event) {
		if(event.getEntityType() == EntityType.PRIMED_TNT) {
			event.blockList().clear();
		}
	}

	@EventHandler
	public void onPlayerEggThrow(PlayerEggThrowEvent event){
		Egg egg = event.getEgg();

		event.setHatching(false);

		List<Entity> near = egg.getNearbyEntities(3.0D, 3.0D, 3.0D);
		for (Entity entry : near) {
			if ((entry instanceof Player)) {
				Player victim = (Player)entry;

				if(p.tacticalMask.containsKey(victim.getName()) && p.tacticalMask.get(victim.getName())){
					victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 0));
					victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 50, 0));
				}else{
					victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
					victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 0));
				}
			}
		}

		egg.getWorld().createExplosion(egg.getLocation(), 0.0F);
	}

	@EventHandler
	public void onPlayerDeath(EntityDeathEvent event){
		if(event.getEntity() instanceof Player && event.getEntity().getKiller() instanceof Player){
			Player killer = (Player)event.getEntity().getKiller();
			Player player = (Player)event.getEntity();

			if(p.scavenger.containsKey(killer.getName()) && p.scavenger.get(killer.getName())){
				killer.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP, 3));
			}

			if(p.martyrdom.containsKey(player.getName()) && p.martyrdom.get(player.getName())){
				player.getWorld().spawn(player.getLocation(), TNTPrimed.class);
			}

			p.killstreak.put(player.getName(), 0);
			p.killstreak.put(killer.getName(), p.killstreak.get(killer.getName()) + 1);
			p.checkForKillstreak(killer);
		}
	}



	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event){
		final Player player = (Player)event.getPlayer();

		if(p.rush.containsKey(player.getName()) && p.rush.get(player.getName())){
			p.getServer().getScheduler().scheduleSyncDelayedTask(p, new Runnable() {
				public void run() {
					PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 300, 2);
					PotionEffect health = new PotionEffect(PotionEffectType.HEALTH_BOOST, 300, 1);
					PotionEffect regen = new PotionEffect(PotionEffectType.REGENERATION, 300, 1);
					
					player.addPotionEffect(health);
					player.addPotionEffect(speed);
					player.addPotionEffect(regen);
				}
			}, 60L);
		}
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event){
		if(event.isCancelled())
			return;

		if(event.getEntity() instanceof Player){
			Player player = (Player)event.getEntity();

			if(p.juggernaut.containsKey(player.getName()) && p.juggernaut.get(player.getName())){
				event.setDamage(event.getDamage() * 0.8);
			}
		}

		if(event.getDamager() instanceof Player){
			Player killer = (Player)event.getDamager();

			if(p.punch.containsKey(killer.getName()) && p.punch.get(killer.getName())){
				event.setDamage(event.getDamage() * 1.2);
			}
		}
	}

	@EventHandler
	public void onPlayerFall(EntityDamageEvent event){
		if(event.isCancelled())
			return;

		if(event.getEntity() instanceof Player){
			Player player = (Player)event.getEntity();

			if(event.getCause() == DamageCause.FALL){
				if(p.commando.containsKey(player.getName()) && p.commando.get(player.getName())){
					event.setDamage(0);
				}
			}
		}
	}

}
