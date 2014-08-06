package me.PocketIsland.SurvivalGames;

import java.util.ArrayList;

public class SQLConfig {

	public ArrayList<String> parameters;
	public ArrayList<Object> vars;

	public SQLConfig(ArrayList<String> parameters, ArrayList<Object> vars)
	{
		this.parameters = parameters;
		this.vars = vars;
	}
	public String getString(String param)
	{
		int i = parameters.indexOf(param);
		if(i == -1)return "";
		if(vars.get(i) instanceof String)
			return (String)vars.get(i);
		return "";
	}
	public int getInt(String param)
	{
		int i = parameters.indexOf(param);
		if(i == -1)return 0;
		if(vars.get(i) instanceof Integer)
			return (Integer)(vars.get(i));
		return 0;
	}
	public void set(String param, Object obj)
	{
		int i = parameters.indexOf(param);
		if(i == -1){
			System.out.println("[CONFIG ERROR] : Tried to set a non-existing colum! Param="+param+", obj=" +obj);
			return;
		}
		vars.set(i, obj);
	}

	public boolean isSet(String param)
	{
		int i = parameters.indexOf(param);
		if(i == -1)return false;
		return true;
	}
	public boolean isInt(String param)
	{
		int i = parameters.indexOf(param);
		if(i == -1)return false;
		if(vars.get(i) instanceof Integer)
			return true;
		return false;
	}
	public boolean isString(String param)
	{
		int i = parameters.indexOf(param);
		if(i == -1)return false;
		if(vars.get(i) instanceof String)
			return true;
		return false;
	}
}
