package com.massivecraft.mcore;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class EngineWorldNameSet implements Listener
{
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //
	
	private static EngineWorldNameSet i = new EngineWorldNameSet();
	public static EngineWorldNameSet get() { return i; }
	
	// -------------------------------------------- //
	// SETUP
	// -------------------------------------------- //
	
	public void setup()
	{
		this.worldNamesInner.clear();
		for (World world : Bukkit.getWorlds())
		{
			this.worldNamesInner.add(world.getName());
		}
		
		Bukkit.getPluginManager().registerEvents(this, MCore.get());
	}
	
	// -------------------------------------------- //
	// FIELDS
	// -------------------------------------------- //
	
	private final TreeSet<String> worldNamesInner = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	private final Set<String> worldNamesOuter = Collections.unmodifiableSet(this.worldNamesInner);
	public Set<String> getWorldNames() { return this.worldNamesOuter; }
	
	// -------------------------------------------- //
	// LISTENER
	// -------------------------------------------- //
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onWorldLoad(WorldLoadEvent event)
	{
		this.worldNamesInner.add(event.getWorld().getName());
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onWorldUnload(WorldUnloadEvent event)
	{
		this.worldNamesInner.remove(event.getWorld().getName());
	}

}
