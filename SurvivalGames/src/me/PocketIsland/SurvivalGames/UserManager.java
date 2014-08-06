package me.PocketIsland.SurvivalGames;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

public class UserManager {

	ConcurrentHashMap<Player,User> users;
	SecretProjectPlugin plugin;

	public UserManager(SecretProjectPlugin plugin)
	{
		users = new ConcurrentHashMap<Player,User>();
		this.plugin = plugin;
	}

	//Shouldn't need to verify if user is already in the database
	public void addUser(Player player)
	{
		final User user = new User(plugin, player);
		users.put(player, user);
		//We load the information in an async task
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			public void run() {
				user.loadUser();
			}});
		plugin.onPlayerJoin(user);
	}

	public void removeUser(Player player)
	{
		final User user = users.get(player);
		if(user == null)return;
		users.remove(player);
		//We unload the information in an async task
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

					public void run() {
						user.unloadUser();
					}});
		plugin.onPlayerLeave(user);
	}

	public User getUser(Player player)
	{
		return users.get(player);
	}

	public User[] getUsers()
	{
		ArrayList<User> userArray = new ArrayList<User>();
		Enumeration<User> userEnum = users.elements();
		while(userEnum.hasMoreElements())
		{
			User user = userEnum.nextElement();
			userArray.add(user);
		}
		return userArray.toArray(new User[0]);
	}

}
