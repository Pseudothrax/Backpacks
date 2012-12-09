package edu.unca.atjones.Backpacks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;

public class BackpacksCommandExecutor implements CommandExecutor{
	
	private final Backpacks plugin;
	
    public BackpacksCommandExecutor(Backpacks plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	String name = cmd.getName();
    	if(sender instanceof Player) {
    		Player p = (Player)sender;
    		//Begin Player Commands
    		
    		/**
    		 * Command bkpcreate gives admin users the ability to create backpacks for any player
    		 * and regular users the ability to use rewards to give themselves a new backpack.
    		 * Usage: /bkpcreate <backpack> [size] [player]
    		 */
        	if(name.equalsIgnoreCase("bkpcreate")) {        		
        		
        		if( p.hasPermission("backpacks.admin.create") ) {
        			try {
        				Player target = Bukkit.getServer().getPlayer(args[0]);
        				if(target == null) throw new BackpacksException("Player not found.");
        				plugin.createBackpack(target,args[1],args[2]);
        				p.sendMessage("Backpack successfully created.");
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkpcreate <player> <backpack> [type]");
        			}
        		}
        		else if( p.hasPermission("backpacks.create")) {
        			try {
        				plugin.useReward(p);
        				plugin.createBackpack(p,args[0],"small");
        				p.sendMessage("Backpack successfully created.");
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkpcreate <backpack>");
        			}
        		}
        		else {
        			p.sendMessage("You cannot execute this command.");
        		}
        		return true;
        	}
        	
        	/**
        	 * Command bkphelp gives users access to the help text. The given 
        	 * text differs if user has admin privileges.
        	 */
    		if(name.equalsIgnoreCase("bkphelp")) {
    			if( p.hasPermission("backpacks.admin.help") ) {
    				return false;
    			} 
    			else if( p.hasPermission("backpacks.help") ) {
    				return false;
    			}
    			else {
        			p.sendMessage("You cannot execute this command.");
    			}
        		return true;
    		}
    		
    		/**
    		 * Command bkpopen gives users the ability to view the contents of backpacks.
    		 * Admin users can view other people's backpacks as well.
    		 */
        	if(name.equalsIgnoreCase("bkpopen")) {
	        	if(p.hasPermission("backpacks.admin.open")) {
        			try{ 
        				Player target = Bukkit.getServer().getPlayer(args[0]);
        				if(target == null) throw new BackpacksException("Player not found.");
        				plugin.showBackpack(target,args[1]); 
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkpopen <player> <backpack>");
        			}
	        	}
	        	else if( p.hasPermission("backpacks.open") ) {
        			try{ 
        				plugin.showBackpack(p,args[0]); 
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkpopen <backpack>");
        			}
	        	}
	        	else p.sendMessage("You cannot execute this command.");
	        	return true;
        	}
        	
        	/**
        	 * Command bkpupgrade allows users to use rewards to increase the size of a backpack
        	 * they already possess. Admin users may increase the size of backpacks without using 
        	 * rewards.
        	 */
        	if(name.equalsIgnoreCase("bkpupgrade")) {
	        	if(p.hasPermission("backpacks.admin.upgrade")) {
        			try{ 
        				Player target = Bukkit.getServer().getPlayer(args[0]);
        				if(target == null) throw new BackpacksException("Player not found.");
        				plugin.upgradeBackpack(target,args[1]); 
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkpupgrade <player> <backpack>");
        			}
	        	}
	        	else if( p.hasPermission("backpacks.upgrade") ) {
        			try{ 
        				plugin.useReward(p);
        				plugin.upgradeBackpack(p,args[0]); 
        				p.sendMessage("Backpack Upgraded");
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkpupgrade <backpack>");
        			}
	        	}
	        	else p.sendMessage("You cannot execute this command.");
	        	return true;
        	}
        	
        	/**
        	 * Command bkpgrant allows admin users to give rewards to non-admin users.
        	 */
        	if(name.equalsIgnoreCase("bkpgrant")) {
	        	if(p.hasPermission("backpacks.admin.grant")) {
        			try{ 
        				Player target = Bukkit.getServer().getPlayer(args[0]);
        				if(target == null) throw new BackpacksException("Player not found.");
        				plugin.grantReward(target,Integer.valueOf(args[1])); 
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkpgrant <player> <number>");
        			}
	        	}
	        	else p.sendMessage("You cannot execute this command.");
	        	return true;
        	}

        	/**
        	 * Command bkpremove allows admin users to destroy backpacks.
        	 */
        	if(name.equalsIgnoreCase("bkpremove")) {
        		if( p.hasPermission("backpacks.admin.remove") ) {
        			try{ 
        				Player target = Bukkit.getServer().getPlayer(args[0]);
        				if(target == null) throw new BackpacksException("Player not found.");
        				plugin.removeBackpack(target,args[1]); 
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkpremove <player> <backpack>");
        			} 
        		}
	        	else p.sendMessage("You cannot execute this command.");
	        	return true;
    		} 
        	
        	/**
        	 * Command bkproute allows the creation of routes. Admin users can create routes for
        	 * other players as well.
        	 */
        	if(name.equalsIgnoreCase("bkproute")) {
    			if( p.hasPermission("backpacks.admin.route") ) {
        			try {
        				Player target = Bukkit.getServer().getPlayer(args[0]);
        				if(target == null) throw new BackpacksException("Player not found.");
        				plugin.addRoute(target,args[1],args[2]);
        				p.sendMessage("Route successfully created.");
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkproute <player> <item> <backpack>|default|ignore");
        			}
    			} 
    			else if( p.hasPermission("backpacks.route") ) {
        			try {
        				plugin.addRoute(p,args[0],args[1]);
        				p.sendMessage("Route successfully created.");
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkproute <item> <backpack>|default|ignore");
        			}
    			}
    			else p.sendMessage("You cannot execute this command.");
        		return true;
	    	}
        	
        	/**
        	 * Command bkplist gives users the ability to see a list of all their 
        	 * backpacks by name. Admin users may see the backpacks of other 
        	 * players.
        	 */
        	if(name.equalsIgnoreCase("bkplist")) {
    			if( p.hasPermission("backpacks.admin.list") ) {
        			try {
        				Player target = Bukkit.getServer().getPlayer(args[0]);
        				if(target == null) throw new BackpacksException("Player not found.");
        				Set<String> names = plugin.listBackpacks(target);
        				Iterator<String> iterator = names.iterator();
        				p.sendMessage(String.format("%s have %d Backpacks:",target.getName(),names.size()));
        				while(iterator.hasNext()) {
        					String str = iterator.next();
        					p.sendMessage(str);
        				}
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkplist <player>");
        			}
    			} 
    			else if( p.hasPermission("backpacks.list") ) {
        			try {
        				Set<String> names = plugin.listBackpacks(p);
        				Iterator<String> iterator = names.iterator();
        				p.sendMessage(String.format("You have %d Backpacks:",names.size()));
        				while(iterator.hasNext()) {
        					String str = iterator.next();
        					p.sendMessage(str);
        				}
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkplist");
        			}
    			}
    			else p.sendMessage("You cannot execute this command.");
        		return true;
	    	}
        	
        	/**
        	 * Command bkprewards allows users to see how many rewards they have to spend.
        	 * Admin users can see the rewards for other players.
        	 */
        	if(name.equalsIgnoreCase("bkprewards")) {
    			if( p.hasPermission("backpacks.admin.rewards") ) {
        			try {
        				Player target = Bukkit.getServer().getPlayer(args[0]);
        				if(target == null) throw new BackpacksException("Player not found.");
        				p.sendMessage(String.format("%s has %d rewards available.",args[0],plugin.numRewards(target)));
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkprewards <player");
        			}
    			} 
    			else if( p.hasPermission("backpacks.rewards") ) {
        			try {
        				p.sendMessage(String.format("You have %d rewards available.",plugin.numRewards(p)));
        			}
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkprewards");
        			}
    			}
    			else p.sendMessage("You cannot execute this command.");
        		return true;
	    	}
        	
        	/**
        	 * Command bkproutes allows users the ability to see all routes they have 
        	 * in place. Admin users may see the list for other users.
        	 */
        	if(name.equalsIgnoreCase("bkproutes")) {
    			if( p.hasPermission("backpacks.admin.routes") ) {
        			try {
        				Player target = Bukkit.getServer().getPlayer(args[0]);
        				if(target == null) throw new BackpacksException("Player not found.");
        				String playerName = target.getName();
        				if(plugin.routes.containsKey(playerName)) {
        					HashMap<Integer,String> routeMap = plugin.routes.get(playerName);
        					p.sendMessage(String.format("%s has %d route(s):",playerName,routeMap.size()));
        					Set<Integer> keys = routeMap.keySet();
        					Iterator<Integer> iterator = keys.iterator();
        					while(iterator.hasNext()) {
        						int id = iterator.next();
        						String backpack = routeMap.get(id);
        						String str = Material.getMaterial(id).name();
        						p.sendMessage(String.format("  %s -> %s",str,backpack));
        					}
        				}
        				else p.sendMessage(String.format("%s has no routes.",playerName));
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkproutes <player>");
        			}
    			} 
    			else if( p.hasPermission("backpacks.routes") ) {
        			try {
        				String playerName = p.getName();
        				if(plugin.routes.containsKey(playerName)) {
        					HashMap<Integer,String> routeMap = plugin.routes.get(playerName);
        					p.sendMessage(String.format("You have %d route(s):",routeMap.size()));
        					Set<Integer> keys = routeMap.keySet();
        					Iterator<Integer> iterator = keys.iterator();
        					while(iterator.hasNext()) {
        						int id = iterator.next();
        						String backpack = routeMap.get(id);
        						String str = Material.getMaterial(id).name();
        						p.sendMessage(String.format("  %s -> %s",str,backpack));
        					}
        				}
        				else p.sendMessage("You have no routes.");
        			}
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkproutes");
        			}
    			}
    			else p.sendMessage("You cannot execute this command.");
        		return true;
	    	}
    		//End Player Commands
    	} else {
    		//Begin Console Commands
    		
    		//...None
    		
    		//End Console Commands
    		sender.sendMessage("Command must be executed by a player!");
    	}
    	return false;
    }
}
