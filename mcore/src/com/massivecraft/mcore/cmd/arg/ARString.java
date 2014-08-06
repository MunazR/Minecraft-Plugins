package com.massivecraft.mcore.cmd.arg;

public class ARString extends ARAbstractPrimitive<String>
{
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //
	
	private static ARString i = new ARString();
	public static ARString get() { return i; }
	
	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //
	
	@Override
	public String typename()
	{
		return "string";
	}

	@Override
	public String convert(String arg) throws Exception
	{
		return arg;
	}
	
}
