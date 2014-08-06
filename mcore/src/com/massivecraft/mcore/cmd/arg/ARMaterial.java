package com.massivecraft.mcore.cmd.arg;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

public class ARMaterial extends ArgReaderAbstract<Material>
{
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //
	
	private static ARMaterial i = new ARMaterial();
	public static ARMaterial get() { return i; }
	
	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //
	
	@Override
	public ArgResult<Material> read(String arg, CommandSender sender)
	{
		ArgResult<Material> result = new ArgResult<Material>(Material.matchMaterial(arg));
		if (!result.hasResult())
		{
			result.getErrors().add("<b>No material matches <h>"+arg+"<b>.");
			result.getErrors().add("<i>Suggestion: <aqua>http://www.minecraftwiki.net/wiki/Data_values");
		}
		return result;
	}
	
}
