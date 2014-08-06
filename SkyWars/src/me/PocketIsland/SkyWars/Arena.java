package me.PocketIsland.SkyWars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.mcsg.double0negative.tabapi.TabAPI;

public class Arena extends JavaPlugin implements Listener{
	public final Core _p;

	int _ID;
	int _radius;
	Location[] _spawnLocations;
	List<String> _players;
	Status _status;
	World _world;
	Location _center;
	int _countdown;
	int _timer;
	List<Block> _brokenBlocks;
	List<Block> _placedBlocks;
	Scoreboard _sb;
	List<Location> _chestLocationsT1;
	List<Location> _chestLocationsT2;
	HashMap<String, Core.Kit> _playerKits;

	public Arena(Core p){
		_p = p;
		_ID = 0;
		_spawnLocations = new Location[8];
		_players = new ArrayList<String>();
		_status = Status.Disabled;
		_radius = 0;
		_countdown = 0;
		_timer = 0;
		_world = null;
		_center = null;
		_brokenBlocks = new ArrayList<Block>();
		_placedBlocks = new ArrayList<Block>();
		_chestLocationsT1 = new ArrayList<Location>();
		_chestLocationsT2 = new ArrayList<Location>();
		_playerKits = new HashMap<String, Core.Kit>();
		SetupScoreboard();
	}

	public Arena(Core p, int ID, Location[] spawnLocations, int radius, World world, Location center, List<Location> T1, List<Location> T2){
		_p = p;
		_ID = ID;
		_spawnLocations = spawnLocations;
		_players = new ArrayList<String>();
		_status = Status.Waiting;
		_radius = radius;
		_countdown = 0;
		_timer = 0;
		_world = world;
		_center = center;
		_brokenBlocks = new ArrayList<Block>();
		_placedBlocks = new ArrayList<Block>();
		_chestLocationsT1 = T1;
		_chestLocationsT2 = T2;
		_playerKits = new HashMap<String, Core.Kit>();
		SetupScoreboard();
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		
		if(!cmd.getName().equalsIgnoreCase("kit") || !_players.contains(sender.getName()) || !(sender instanceof Player))
			return false;
		
		Player player = (Player)sender;
		
		if(_status == Status.Waiting || _status == Status.Starting){
			
		}else{
			player.sendMessage(Core.Tag() + ChatColor.RED + " You may not select a kit now!");
		}
		
		return true;
	}
	
	public void Rollback(){
		Core.coreProtect.performRollback(null, 600 - _timer, _radius, _center, null, null);
	}
	
	public void oldRollback(){
		for(Block b : _placedBlocks){
			b.getLocation().getBlock().setType(Material.AIR);
		}

		for(Block b : _brokenBlocks){
			b.getLocation().getBlock().setType(b.getType());
		}

		_placedBlocks = new ArrayList<Block>();
		_brokenBlocks = new ArrayList<Block>();
	}

	public void AddPlayer(Player player){
		_players.add(player.getName());
		player.teleport(_spawnLocations[_players.size() - 1]);
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);

		TabAPI.setPriority(this, player, 2);
		player.setScoreboard(_sb);
	}

	public void RemovePlayer(Player player){
		_players.remove(player.getName());
		_playerKits.remove(player.getName());
		Core.SetupScoreboard(player);
		Core.RefreshScoreboard(player);
		TabAPI.setPriority(this, player, -2);
	}

	public void UpdateArena(){
		if(_status == Status.Waiting){
			if(_players.size() >= Core.minStart){
				StartCountdown();
				_status = Status.Starting;
			}else if(_timer++ >= 5){
				for(int x = 0; x < _players.size(); x++){
					Player player = Bukkit.getPlayer(_players.get(x));
					player.sendMessage(Core.Tag() + ChatColor.RED + " Waiting for " + ChatColor.AQUA + (Core.minStart -_players.size()) + ChatColor.RED + " more players to start");
				}

				_timer = 0;
			}

			RefreshScoreboard();
			RefreshTab();
			TeleportPlayers();
		}else if(_status == Status.Starting){
			if(_countdown <= 0){
				for(int x = 0; x < _players.size(); x++){
					Player player = Bukkit.getPlayer(_players.get(x));
					player.sendMessage(Core.Tag() + ChatColor.RED + " The game has begun!");
					player.setExp(0);
					Core.incrementGames(_players.get(x), 1);
				}

				_timer = 600;
				_status = Status.InGame;
			}else{
				for(int x = 0; x < _players.size(); x++){
					Player player = Bukkit.getPlayer(_players.get(x));
					player.sendMessage(Core.Tag() + ChatColor.RED + " Game starting in " + ChatColor.AQUA + _countdown--);
					player.setExp(17 * _countdown);
				}
			}

			RefreshScoreboard();
			RefreshTab();
			TeleportPlayers();
		}else if(_status == Status.InGame){
			if(_players.size() == 1){
				Player player = Bukkit.getPlayer(_players.get(0));
				Core.econ.bankDeposit(player.getName(), Core.rewardAmount);
				player.sendMessage(Core.Tag() + ChatColor.RED + " Congratulations! You won " + ChatColor.AQUA + "$" + Core.rewardAmount);
				Core.incrementWins(_players.get(0), 1);
				Reset();
				_status = Status.Waiting;
			}else if(_timer <= 0){
				double reward = Core.rewardAmount / _players.size();

				for(int x = 0; x < _players.size(); x++){
					Player player = Bukkit.getPlayer(_players.get(x));
					Core.econ.bankDeposit(player.getName(), reward);
					player.sendMessage(Core.Tag() + ChatColor.RED + " Congratulations! You won " + ChatColor.AQUA + "$" + reward);
					Core.incrementWins(_players.get(x), 1);
				}

				Reset();
				_status = Status.Waiting;
			}else if(_players.size() <= 0){
				Reset();
				_status = Status.Waiting;
			}else{
				RefreshScoreboard();
				RefreshTab();
				_timer--;
			}
		}
	}

	public void Reset(){
		for(int x = 0; x < _players.size(); x++){
			Player player = Bukkit.getPlayer(_players.get(x));
			player.teleport(Core.lobby);
		}

		_players.clear();
		_playerKits.clear();
		
		Rollback();
		
		_timer = 0;
	}

	public void SetupScoreboard(){
		_sb = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective skywars =_sb.registerNewObjective("skywars", "dummy");

		skywars.setDisplayName(ChatColor.RED + "Sky Wars");
		skywars.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	public void RefreshScoreboard(){
		Objective obj;

		if(_status == Status.Waiting){
			obj = _sb.getObjective("skywars");

			obj.getScore(Bukkit.getOfflinePlayer(ChatColor.BLUE + "Waiting:")).setScore(Core.minStart - _players.size());
			obj.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Players:")).setScore(_players.size());
		}else if(_status == Status.Starting){
			obj = _sb.getObjective("skywars");

			obj.getScore(Bukkit.getOfflinePlayer(ChatColor.BLUE + "Starting in:")).setScore(_countdown);
			obj.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Players:")).setScore(_players.size());
		}else if(_status == Status.InGame){
			obj = _sb.getObjective("skywars");

			obj.getScore(Bukkit.getOfflinePlayer(ChatColor.BLUE + "Time Left:")).setScore(_timer);
			obj.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Players:")).setScore(_players.size());
		}
	}

	public void RefreshTab(){
		Player player;

		for(int x = 0; x < _players.size(); x++){
			player = Bukkit.getPlayer(_players.get(x));

			TabAPI.setTabString(this, player, 0, 0, ChatColor.DARK_GREEN + "----------" + TabAPI.nextNull());
			TabAPI.setTabString(this, player, 0, 1, ChatColor.GREEN + "----------" + TabAPI.nextNull());
			TabAPI.setTabString(this, player, 0, 2, ChatColor.DARK_GREEN + "----------" + TabAPI.nextNull());

			TabAPI.setTabString(this, player, 1, 0, ChatColor.DARK_RED + "Sky Wars on" + TabAPI.nextNull());
			TabAPI.setTabString(this, player, 1, 1, ChatColor.DARK_RED + "iPocketIsland" + TabAPI.nextNull());
			TabAPI.setTabString(this, player, 1, 2, ChatColor.DARK_RED + ".com" + TabAPI.nextNull());

			TabAPI.setTabString(this, player, 2, 0, ChatColor.GREEN + "----------" + TabAPI.nextNull());
			TabAPI.setTabString(this, player, 2, 1, ChatColor.DARK_GREEN + "----------" + TabAPI.nextNull());
			TabAPI.setTabString(this, player, 2, 2, ChatColor.GREEN + "----------" + TabAPI.nextNull());

			TabAPI.setTabString(this, player, 4, 0, ChatColor.BLUE + "Players:");

			for(int y = 0, v = 1, h = 4; y < _players.size(); y++){
				if(v >= 3){
					v = 0;
					h++;
				}

				TabAPI.setTabString(this, player, h, v++, ChatColor.WHITE + _players.get(y) + TabAPI.nextNull());
			}

			TabAPI.updatePlayer(player);
		}
	}

	@SuppressWarnings("deprecation")
	public void giveKits(){
		Core.Kit kit;
		ItemStack item;
		Potion potion;
		
		for(String player : _players){
			if(_playerKits.containsKey(player)){
				kit = _playerKits.get(player);
				Player target = Bukkit.getPlayer(player);
				
				if(kit == Core.Kit.creeper){
					item = new ItemStack(Material.MONSTER_EGG, 5);
					item.setDurability((short)50);
					target.getInventory().addItem(item);
				}else if(kit == Core.Kit.tnt){
					item = new ItemStack(Material.TNT, 10);
					target.getInventory().addItem(item);
					item = new ItemStack(Material.FLINT_AND_STEEL, 1);
					target.getInventory().addItem(item);			
				}else if(kit == Core.Kit.troll){
					item = new ItemStack(Material.EGG, 8);
					target.getInventory().addItem(item);
					item = new ItemStack(Material.SNOW_BALL, 8);
					target.getInventory().addItem(item);					
				}else if(kit == Core.Kit.scout){
					target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
					target.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 60, 1));
					target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, 1));
				}else if(kit == Core.Kit.swordsman){
					item = new ItemStack(Material.STONE_SWORD, 1);
					target.getInventory().addItem(item);
				}else if(kit == Core.Kit.knight){
					target.getInventory().setArmorContents(new ItemStack[]{null, new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1), null, null});
				}else if(kit == Core.Kit.witch){
					potion = new Potion(PotionType.STRENGTH, 2, true, false);
					item = potion.toItemStack(1);
					target.getInventory().addItem(item);
								
					potion = new Potion(PotionType.POISON, 2, true, false);
					item = potion.toItemStack(1);
					target.getInventory().addItem(item);
					
					potion = new Potion(PotionType.INSTANT_DAMAGE, 2, true, false);
					item = potion.toItemStack(1);
					target.getInventory().addItem(item);
					
					potion = new Potion(PotionType.REGEN, 2, true, false);
					item = potion.toItemStack(1);
					target.getInventory().addItem(item);
					
					potion = new Potion(PotionType.INSTANT_HEAL, 2, true, false);
					item = potion.toItemStack(1);
					target.getInventory().addItem(item);
				}else if(kit == Core.Kit.archer){
					target.getInventory().addItem(new ItemStack(Material.BOW, 1));
					target.getInventory().addItem(new ItemStack(Material.ARROW, 10));
				}
			}
		}
	}
	
	public void RestockChests(){
		for(int x = 0; x < _chestLocationsT1.size(); x++){
			Chest chest = (Chest)_chestLocationsT1.get(x).getBlock().getState();
			chest.getInventory().setContents(Core.T1Loot);
		}

		for(int x = 0; x < _chestLocationsT2.size(); x++){
			Chest chest = (Chest)_chestLocationsT2.get(x).getBlock().getState();
			chest.getInventory().setContents(Core.T2Loot);
		}
	}

	public void TeleportPlayers(){
		Player player;
		Location loc;

		for(int x = 0; x < _players.size(); x++){
			player = Bukkit.getPlayer(_players.get(x));

			if((int)player.getLocation().getX() != (int)_spawnLocations[x].getX() && (int)player.getLocation().getZ() != (int)_spawnLocations[x].getZ()){
				loc = _spawnLocations[x];
				loc.setPitch(player.getLocation().getPitch());
				loc.setYaw(player.getLocation().getYaw());

				player.teleport(loc);
			}
		}
	}

	public void StartCountdown(){
		_countdown = 15;
	}

	public void StartGame(){

	}

	public void SetSpawn(int x, Location y){
		_spawnLocations[x - 1] = y;
	}

	public Location GetSpawn(int x){
		return _spawnLocations[x - 1];
	}

	public Scoreboard getScoreboard(){
		return _sb;
	}

	public void SetCenter(Location x){
		_center = x;
	}

	public Location GetCenter(){
		return _center;
	}

	public List<Location> GetChestLocationsT1(){
		return _chestLocationsT1;
	}

	public void SetChestLocationsT1(List<Location> x){
		_chestLocationsT1 = x;
	}

	public List<Location> GetChestLocationsT2(){
		return _chestLocationsT2;
	}

	public void SetChestLocationsT2(List<Location> x){
		_chestLocationsT2 = x;
	}

	public int GetID(){
		return _ID;
	}

	public void SetID(int x){
		_ID = x;
	}

	public int GetRadius(){
		return _radius;
	}

	public void SetRadius(int x){
		_radius = x;
	}

	public Status GetStatus(){
		return _status;
	}

	public World GetWorld(){
		return _world;
	}

	public void SetWorld(World x){
		_world = x;
	}

	public void setStatus(Status newStatus){
		_status = newStatus;
	}

	public List<String> GetPlayers(){
		return _players;
	}

	enum Status{
		Waiting,
		Starting,
		InGame,
		Finished,
		Reset,
		Disabled,
	}


	@EventHandler
	public void playerMove(PlayerMoveEvent event){

	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event){
		if(event.isCancelled())
			return;

		Player player = event.getPlayer();

		if(_players.contains(player.getName())){
			if(_status == Status.Waiting || _status == Status.Starting){
				event.setCancelled(true);
			}else if(_status == Status.InGame){
				_brokenBlocks.add(event.getBlock());
			}
		}
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event){
		if(event.isCancelled())
			return;

		Player player = event.getPlayer();

		if(_players.contains(player.getName())){
			if(_status == Status.Waiting || _status == Status.Starting){
				event.setCancelled(true);
			}else if(_status == Status.InGame){
				_placedBlocks.add(event.getBlockPlaced());
			}
		}
	}

	@EventHandler
	public void playerDeath(EntityDeathEvent event){
		Entity entity = event.getEntity();

		if(!(entity instanceof Player))
			return;

		Player player = (Player)entity;

		if(_players.contains(player.getName())){
			RemovePlayer(player);

			player.teleport(Core.lobby);
			Core.GiveLobbyItems(player);
		}
	}

	@EventHandler
	public void playerDamage(EntityDamageEvent event){
		Entity sender = event.getEntity();

		if(!(sender instanceof Player) || event.isCancelled())
			return;

		Player player = (Player)sender;

		if(_players.contains(player.getName())){
			if(_status == Status.Waiting || _status == Status.Starting)
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void playerLeave(PlayerQuitEvent event){
		Player player = event.getPlayer();

		if(_players.contains(player.getName())){
			RemovePlayer(player);
		}
	}

	@EventHandler
	public void onPlayerDeath(EntityDeathEvent event){
		if(event.getEntity() instanceof Player && event.getEntity().getKiller() instanceof Player){
			Player player = (Player)event.getEntity().getKiller();
			Player deadPlayer = (Player)event.getEntity();
			
			if(_players.contains(player.getName())){
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1));
				Core.econ.depositPlayer(player.getName(), 10);
				Core.incrementScore(player.getName(), 10);
				Core.incrementKills(player.getName(), 1);
				Core.incrementDeaths(deadPlayer.getName(), 1);
			}
		}
	}
	
	@EventHandler
	public void onInventoryInteract(InventoryClickEvent event){
		if(event.isCancelled())
			return;

		if(event.getInventory().getName().equalsIgnoreCase("Purchase a perk!") && event.getWhoClicked() instanceof Player){
			Player player = (Player)event.getWhoClicked();
			
			if(event.getCurrentItem().getType() == Material.MONSTER_EGG){
				_playerKits.put(player.getName(), Core.Kit.creeper);
				player.sendMessage(ChatColor.GREEN + "You selected " + ChatColor.DARK_GREEN + "Kit Creeper");
			}else if(event.getCurrentItem().getType() == Material.TNT){
				_playerKits.put(player.getName(), Core.Kit.tnt);
				player.sendMessage(ChatColor.GREEN + "You selected " + ChatColor.DARK_GREEN + "Kit TNT");
			}else if(event.getCurrentItem().getType() == Material.EGG){
				_playerKits.put(player.getName(), Core.Kit.troll);
				player.sendMessage(ChatColor.GREEN + "You selected " + ChatColor.DARK_GREEN + "Kit Troll");
			}else if(event.getCurrentItem().getType() == Material.CARROT){
				_playerKits.put(player.getName(), Core.Kit.scout);
				player.sendMessage(ChatColor.GREEN + "You selected " + ChatColor.DARK_GREEN + "Kit Scout");
			}else if(event.getCurrentItem().getType() == Material.STONE_SWORD){
				_playerKits.put(player.getName(), Core.Kit.swordsman);
				player.sendMessage(ChatColor.GREEN + "You selected " + ChatColor.DARK_GREEN + "Kit Swordsman");
			}else if(event.getCurrentItem().getType() == Material.CHAINMAIL_CHESTPLATE){
				_playerKits.put(player.getName(), Core.Kit.knight);
				player.sendMessage(ChatColor.GREEN + "You selected " + ChatColor.DARK_GREEN + "Kit Knight");
			}else if(event.getCurrentItem().getType() == Material.POTION){
				_playerKits.put(player.getName(), Core.Kit.witch);
				player.sendMessage(ChatColor.GREEN + "You selected " + ChatColor.DARK_GREEN + "Kit Witch");
			}else if(event.getCurrentItem().getType() == Material.BOW){
				_playerKits.put(player.getName(), Core.Kit.archer);
				player.sendMessage(ChatColor.GREEN + "You selected " + ChatColor.DARK_GREEN + "Kit Archer");
			}
			
			event.setCancelled(true);
			player.closeInventory();
		}
	}
}
