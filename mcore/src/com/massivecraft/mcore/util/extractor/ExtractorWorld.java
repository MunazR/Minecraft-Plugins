package com.massivecraft.mcore.util.extractor;

public class ExtractorWorld implements Extractor
{
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //
	
	private static ExtractorWorld i = new ExtractorWorld();
	public static ExtractorWorld get() { return i; }
	
	// -------------------------------------------- //
	// OVERRIDE: EXTRACTOR
	// -------------------------------------------- //
	
	@Override
	public Object extract(Object o)
	{
		return ExtractorLogic.worldFromObject(o);
	}
	
}
