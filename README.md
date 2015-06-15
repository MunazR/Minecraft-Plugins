# Minecraft-Plugins
These plugins use CraftBukkit API to affect the gameplay of Minecraft players connected to a Bukkit server.

The plugins are all developed in Java using CraftBukkit API. Some of them also integrate external APIs such as DisguiseCraft, CombatTag and Essentials. Additionally some of the plugins also use SQL for user data storage.

These plugins were originally created for use on ipocketisland.com, now known as minejam.com

<h2>PocketBank</h2>
<p>
Displays player account balance on signs labeled "[Bank]" on the first line. Each player will view the sign differently
<br/><br/>
Requires Vault, Economy and IndividualSigns
</p>

<h2>PocketCash</h2>
<p>
Awards players with money when they get a kill
<br/><br/>
Requries Vault and an Economy plugin
</p>

<h2>PocketPerks</h2>
<p>
Gives players the ability to purchase perks using their in-game balance.
<br/>
Players are awarded will bonuses for continous kills.
Perks include
</p>
<ul>
<li>TNT: TNT ignites instantly when placed</li>
<li>Tactical Mask: Immune to flash grenades (eggs)</li>
<li>Martyrdom: Drop a TNT when you die</li>
<li>Rush: Increased speed when respawning</li>
<li>Commando: Receive no fall damage</li>
<li>Hardline: Kill streaks require 1 less kill</li>
<li>Punch: Deal 20% aditional damage</li>
<li>Juggernaught: Take 20% less damage</li>
</ul>
<p>Killstreaks include</p>
<ul>
<li>5 Kills: Speed boost</li>
<li>10 Kills: Strength boost</li>
<li>15 Kills: Dogs</li>
<li>25 Kills: Super strength boost</li>
<li>50 Kills: Tactical nuke</li>
</ul>
<p>
Player data is stored in an SQL database. A configuration file will be generated on the first run. Please fill in the fields with the credentials and server information for your SQL database.
<br/><br/>
Requires JDBC and Vault with an Economy plugin.
</p>

<h2>PocketRankUp</h2>
<p>
Tracks player statistics including kills, deaths, kill death ratio, killstreak, level and balance and displays it on the player's scoreboard.
<br/><br/>
All data is stored in an SQL database. Run the plugin at least once and fill in the SQL credentials and information in the configuration file. Uses a leveling algorithm to allow players to level up by getting kills with increasing difficulty as players level up. Features global messages to announce level ups. 
<br/><br/>
Requires JDBC and Vault with an Economy plugin.
</p>
<h2>PocketFly</h2>
<p>
Gives donors the ability to fly.
<br/><br/>
Flying is disabled when the player enters combat and can not be enabled while in combat
<br/><br/>
Requires CombatTag and CombatTagAPI
</p>
<h2>PocketWild</h2>
<p>
Teleports a player to a random location when they use command "/wild", can only be used once by the player.
</p>
