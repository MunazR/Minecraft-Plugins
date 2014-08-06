package me.PocketIsland.SurvivalGames;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public Connection conn;
	
	public void onEnable() {	
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);

		if (!(new File(getDataFolder(), "config.yml")).exists()){
			saveDefaultConfig();
		}
		
		if(!setupSQL()){
			logger.severe(String.format("[%s] - Disabled due to MySQL setup failure!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
	}
	
	enum GameState{
		Idle,
		Starting,
		InGame,
	}
	
	private boolean setupSQL(){

		try {
			Class.forName("com.mysql.jdbc.Driver");

			conn = DriverManager.getConnection("jdbc:mysql://" + getConfig().getString("MySQL.host") + ":" + getConfig().getString("MySQL.port") + "/" + getConfig().getString("MySQL.database"), getConfig().getString("MySQL.user"), getConfig().getString("MySQL.password"));

			if (!conn.isClosed()){

				print(Level.INFO, "Connected!");

				PreparedStatement stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " +
						"SGSTATS (player VARCHAR(255), games INT, kills INT, deaths INT, wins INT);");

				stmt.execute();
				return true;
			}
			else{
				print(Level.SEVERE, "Connection isn't open!");
			}

		} catch (ClassNotFoundException e) {
			print(Level.SEVERE, "Where is your MySQL driver???");
		} catch (SQLException e) {
			print(Level.SEVERE, "An SQL error occurred! " + e.getMessage());
		}

		return false;
	}
	
	private void print(Level level, String msg){
		logger.log(level, String.format("[%s]" + msg, getDescription().getName()));
	}
	
	
}