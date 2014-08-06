package me.PocketIsland.PocketCash;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class PocketCash extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static PocketCash plugin;
	public static Economy econ = null;
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled!");
	}

	public void onEnable() {	
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		
        if (!setupEconomy() ) {
            logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
	}
	
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event){
    	if ((event.getEntity().getKiller() instanceof Player)){
    		Player killer = event.getEntity().getKiller();
    		Player player = event.getEntity().getPlayer();
    		
    		int money;
    		if(killer.hasPermission("pocketpvp.donor")){
    			money = 20;
    		}else{
    			money = 10;
    		}
    		
    		econ.depositPlayer(killer.getName(), money);
    		
    		killer.sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "PocketCash" + ChatColor.AQUA + "]" + ChatColor.RED + " You received " + ChatColor.BLUE + "$" + money + ChatColor.RED + " for killing " + ChatColor.GOLD + player.getName());
    		killer.sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "PocketCash" + ChatColor.AQUA + "]" + ChatColor.GREEN + " New Balance: " + ChatColor.BLUE + econ.getBalance(killer.getName()));
    	}
    }
    
}
