package com.phdincomputing.SusMeter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin implements Listener {
	public Material[] oreStatsCat = {Material.COAL_ORE,Material.DIAMOND_ORE,Material.EMERALD_ORE,Material.EMERALD_ORE,Material.IRON_ORE,Material.LAPIS_ORE,Material.NETHER_QUARTZ_ORE,Material.REDSTONE_ORE};
	public Material[] caveStatsCat = {Material.STONE,Material.DIRT,Material.ANDESITE,Material.GRANITE,Material.DIORITE};
	public Statistic[] moveStatsCat = {Statistic.SWIM_ONE_CM,Statistic.SPRINT_ONE_CM,Statistic.WALK_ONE_CM};
	public Material[] toolStatsCat = {Material.DIAMOND_PICKAXE,Material.GOLDEN_PICKAXE,Material.IRON_PICKAXE,Material.STONE_PICKAXE,Material.WOODEN_PICKAXE};
	public EntityType[] mobStatsCat = {EntityType.CAVE_SPIDER,EntityType.SKELETON,EntityType.SPIDER,EntityType.ZOMBIE};
	
	public String[] oreNames = {"coal","diamond","emerald","iron","lapis","quartz","redstone"};
	
	@Override
	public void onEnable() 
	{
		getLogger().info("Sus0Meter Started");
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Sus0Meter Stopped");
	}
	
	public HashMap<Material, double[]> getStats(Player p)
	{
		HashMap<Material, double[]> allOreStats = new HashMap<Material, double[]>();
		
		double move = 0;
		for (int i = 0; i < moveStatsCat.length; i++)
			move += p.getStatistic(moveStatsCat[i]);
		double jump = p.getStatistic(Statistic.JUMP);
		double pickaxe = 0;
		for (int i = 0; i < toolStatsCat.length; i++)
			pickaxe += p.getStatistic(Statistic.BREAK_ITEM, toolStatsCat[i]);
		double cave = 0;
		for (int i = 0; i < caveStatsCat.length; i++)
			cave += p.getStatistic(Statistic.MINE_BLOCK, caveStatsCat[i]);
		double mobKill = 0;
		for (int i = 0; i < mobStatsCat.length; i++)
			mobKill += p.getStatistic(Statistic.KILL_ENTITY, mobStatsCat[i]);
		double totalOre = 0;
		for (int i = 0; i < oreStatsCat.length; i++)
			totalOre += p.getStatistic(Statistic.MINE_BLOCK, oreStatsCat[i]);
		
		for (int i = 0; i < oreStatsCat.length; i++)
		{
			double ore = p.getStatistic(Statistic.MINE_BLOCK, oreStatsCat[i]);
			double other = totalOre - ore;
			double[] oreStats = {
					ore/other,
					ore/move,
					ore/cave,
					ore/mobKill,
					ore/jump,
					ore/pickaxe,
			};
			//String oreName = oreStatsCat[i].toString();
			allOreStats.put(oreStatsCat[i], oreStats);
		}
		return allOreStats;
	}
	
	// https://www.baeldung.com/java-round-decimal-number
	public static double roundAvoid(double value, int places) {
	    double scale = Math.pow(10, places);
	    return Math.round(value * scale) / scale;
	}
	
	public void sendFeedback(Player p, List<Player> affected, String arg)
	{
		ArrayList<Material> mats = new ArrayList<Material>();
		String tArg = arg.toLowerCase();
		
		if (tArg.equals("all"))
		{
			mats = new ArrayList<Material>(Arrays.asList(oreStatsCat));
		}
		else if (tArg.contains(","))
		{
			String[] matNames = tArg.split(",");
			for (int i = 0; i < matNames.length; i++)
			{
				//mats = new ArrayList<Material>();
				int indx = Arrays.binarySearch(oreNames, tArg);
				if (indx == -1) 
				{
					p.sendMessage("Unknown Material: " + tArg);
					return;
				}
				mats.add(oreStatsCat[indx]);
			}
		}
		else
		{
			//mats = new ArrayList<Material>();
			int indx = Arrays.binarySearch(oreNames, tArg);
			if (indx == -1) 
			{
				p.sendMessage("Unknown Material: " + tArg);
				return;
			}
			mats.add(oreStatsCat[indx]);
		}
		
		for (int i = 0; i < affected.size(); i++)
		{
			Player t = affected.get(i);
			p.sendMessage("Player: " + t.getName());
			HashMap<Material, double[]> allStat = getStats(t);
			for (int j = 0; j < mats.size(); j++)
			{
				p.sendMessage("Ore: " + mats.get(j).toString());
				double[] stat = allStat.get(mats.get(j));
				String message = "";
				for(double m : stat){
				    message += roundAvoid(m, 2) + ",";
				}
				message = message.substring(0, message.length()-1);
				p.sendMessage(message);
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		//getLogger().info(Integer.toString(((Player)sender).getStatistic(Statistic.KILL_ENTITY, Material.GRASS_BLOCK)));
		getLogger().info(args[0]);
		if (!sender.isOp()) return true;
		@SuppressWarnings("unchecked")
		List<Player> onlinePlayerList = (List<Player>) getServer().getOnlinePlayers();
		if (args.length < 2)
		{
			sender.sendMessage("Usage: /sus [user1,user2,...|all] [orename]");
			return true;
		}
		else if (args[0].toLowerCase().equals("all"))
		{
			sendFeedback((Player) sender, onlinePlayerList, args[1]);
		}
		else if (args[0].contains(","))
		{
			//LIST
			String[] playerNames = args[0].split(",");
			ArrayList<Player> players = new ArrayList<Player>();
			for (int i = 0; i < playerNames.length; i++)
			{
				boolean found = false;
				for (int j = 0; j < onlinePlayerList.size(); j++)
				{
					Player p = onlinePlayerList.get(j);
					if (playerNames[i].equals(p.getName())) {
						found = true;
						players.add(p);
					}
				}
				if (!found) 
				{
					sender.sendMessage("Couldn't find player: " + playerNames[i]);
					return true;
				}
			}
			
			sendFeedback((Player) sender, players, args[1]);
		}
		else
		{
			for (int j = 0; j < onlinePlayerList.size(); j++)
			{
				Player p = onlinePlayerList.get(j);
				if (args[0].equals(p.getName())) {
					ArrayList<Player> a = new ArrayList<Player>();
					a.add(p);
					sendFeedback((Player) sender, a, args[1]);
					return true;
				}
			}
			sender.sendMessage("Couldn't find player: " + args[0]);
			return true;
		}
		return true;
	}
}
