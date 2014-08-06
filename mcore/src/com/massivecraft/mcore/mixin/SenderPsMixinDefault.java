package com.massivecraft.mcore.mixin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.massivecraft.mcore.ps.PS;

public class SenderPsMixinDefault extends SenderPsMixinAbstract
{
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //
	
	private static SenderPsMixinDefault i = new SenderPsMixinDefault();
	public static SenderPsMixinDefault get() { return i; }
	
	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //
	
	@Override
	public PS getSenderPs(String senderId)
	{
		Player player = Bukkit.getPlayerExact(senderId);
		if (player == null) return null;
		return PS.valueOf(player.getLocation());
	}

	@Override
	public void setSenderPs(String senderId, PS ps)
	{
		// Bukkit does not support setting the physical state for offline players for now.
	}
}
