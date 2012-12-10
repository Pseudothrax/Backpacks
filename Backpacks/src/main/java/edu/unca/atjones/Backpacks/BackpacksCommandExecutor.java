package edu.unca.atjones.Backpacks;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        		if( p.hasPermission("backpacks.create") && args.length > 0 ) {
        			try {
	        			if( args.length == 1 ) {
	        				if( !p.hasPermission("backpacks.admin.create") ) plugin.useReward(p);
	        				plugin.createBackpack(p,args[0],"small");
	        				p.sendMessage("Backpack successfully created.");
	        			}
	        			else if( p.hasPermission("backpacks.admin.create") ) {
	        				
	        				if( args.length == 2 ) {
	        					plugin.createBackpack(p,args[0],args[1]);
	        				}
	        				else {
	        					Player target = Bukkit.getServer().getPlayer(args[2]);
	        					if(target == null) throw new BackpacksException("Player not found.");
	        					plugin.createBackpack(target,args[0],args[1]);
	        				}
	        				p.sendMessage("Backpack successfully created.");
	        			}
	        			else p.sendMessage("Must be an Admin");
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				return false;
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
    			if( p.hasPermission("backpacks.help") ) {
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
    		 * Usage /bkpopen <backpack> [player]
    		 */
        	if(name.equalsIgnoreCase("bkpopen")) {
	        	if(p.hasPermission("backpacks.open") && args.length > 0 ) {
        			try{ 
        				if(args.length == 1) {
        					plugin.openBackpack(p,p,args[0]); 
        				}
        				else if( p.hasPermission("backpacks.admin.open") && args.length == 2 ) {
        					Player target = Bukkit.getServer().getPlayer(args[1]);
        					if(target == null) throw new BackpacksException("Player not found.");
        					plugin.openBackpack(p,target,args[1]); 
        				}
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				return false;
        			}
	        	}
	        	else p.sendMessage("You cannot execute this command.");
	        	return true;
        	}
        	
        	/**
        	 * Command bkpupgrade allows users to use rewards to increase the size of a backpack
        	 * they already possess. Admin users may increase the size of backpacks without using 
        	 * rewards.
        	 * Usage /bkpupgrade <backpack> [player]
        	 */
        	if(name.equalsIgnoreCase("bkpupgrade")) {
	        	if( p.hasPermission("backpacks.upgrade") && args.length > 0) {
        			try{ 
        				
        				if( args.length == 1 ) {
        					plugin.useReward(p);
        					plugin.upgradeBackpack(p,args[0]); 
        					p.sendMessage("Backpack Upgraded");
        				}
        				else if( p.hasPermission("backpacks.admin.upgrade") && args.length == 2 ) {
        					Player target = Bukkit.getServer().getPlayer(args[1]);
        					if(target == null) throw new BackpacksException("Player not found.");
        					plugin.upgradeBackpack(target,args[0]); 
        					p.sendMessage("Backpack Upgraded");
        				}
        				
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				return false;
        			}
	        	}
	        	else p.sendMessage("You cannot execute this command.");
	        	return true;
        	}
        	
        	/**
        	 * Usage /bkpname <old> <new> [player]
        	 */
        	if(name.equalsIgnoreCase("bkprename")) {
        		if(p.hasPermission("backpacks.rename") && args.length > 0 ) {
        			try {
        				if( args.length == 2 ) {
        					plugin.renameBackpack(p, args[0], args[1]);
        					p.sendMessage("Backpack renamed.");
        				}
        				else if(p.hasPermission("backpacks.admin.rename") && args.length == 3 ) {
        					Player target = Bukkit.getServer().getPlayer(args[0]);
            				if(target == null) throw new BackpacksException("Player not found.");
            				plugin.renameBackpack(target, args[0], args[1]);
            				p.sendMessage("Backpack renamed.");
        				}
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(NumberFormatException e) { p.sendMessage("Argument 2 must be an integer"); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				return false;
        			}
        		}
        		else p.sendMessage("You cannot execute this command.");
	        	return true;
        	}
        	
        	/**
        	 * Command bkpgrant allows admin users to give rewards to non-admin users.
        	 */
        	if(name.equalsIgnoreCase("bkpgrant")) {
	        	if(p.hasPermission("backpacks.grant") && args.length > 0 ) {
        			try{ 
        				Player target = Bukkit.getServer().getPlayer(args[0]);
        				if(target == null) throw new BackpacksException("Player not found.");
        				int count = 1;
        				if(args.length == 1 ) {
        					plugin.grantReward(target,count); 
        				}
        				else if( args.length == 2 ) {
        					count = Integer.valueOf(args[1]);
        					plugin.grantReward(target,count); 
        				}
        				else throw new BackpacksException("Too many arguments");
        				p.sendMessage(String.format("%n rewards granted to %s",count,target.getName()));
        				
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(NumberFormatException e) { p.sendMessage("Argument 2 must be an integer"); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				return false;
        			}
	        	}
	        	else p.sendMessage("You cannot execute this command.");
	        	return true;
        	}

        	/**
        	 * Command bkpremove allows admin users to destroy backpacks.
        	 * Useage /bkpremove <backpack> [player]
        	 */
        	if(name.equalsIgnoreCase("bkpremove")) {
        		if( p.hasPermission("backpacks.remove") && args.length > 0 ) {
        			try{ 
        				if( args.length == 1 ) {
        					plugin.removeBackpack(p,args[0]); 
        					p.sendMessage(String.format("%s removed.",args[0]));
        				}
        				else if( args.length == 2 ) {
        					Player target = Bukkit.getServer().getPlayer(args[1]);
            				if(target == null) throw new BackpacksException("Player not found.");
            				plugin.removeBackpack(target,args[0]); 
            				p.sendMessage(String.format("%s removed.",args[0]));
        				}
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				return false;
        			} 
        		}
	        	else p.sendMessage("You cannot execute this command.");
	        	return true;
    		} 
        	
        	/**
        	 * Command bkproute allows the creation of routes. Admin users can create routes for
        	 * other players as well.
        	 * Usage /bkproute <item> <backpack> [player]
        	 */
        	if(name.equalsIgnoreCase("bkproute")) {
        		if( p.hasPermission("backpacks.route") && args.length > 0 ) {
        			try{
        				if( args.length == 2 ) {
        					plugin.addRoute(p,args[0],args[1]);
            				p.sendMessage("Route successfully created.");
        				}
        				else if( p.hasPermission("backpacks.admin.route") && args.length == 3 ) {
        					Player target = Bukkit.getServer().getPlayer(args[2]);
            				if(target == null) throw new BackpacksException("Player not found.");
            				plugin.addRoute(target,args[0],args[1]);
            				p.sendMessage("Route successfully created.");
        				}
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				return false;
        			}
        		}
        		else p.sendMessage("You cannot execute this command.");
	        	return true;
        	}
        	
        	/**
        	 * Command bkplist gives users the ability to see a list of all their 
        	 * backpacks by name. Admin users may see the backpacks of other 
        	 * players.
        	 * Usage /bkplist [player]
        	 */
        	if(name.equalsIgnoreCase("bkplist")) {
    			if( p.hasPermission("backpacks.list") && args.length >= 0 ) {
        			try {
        				if( args.length == 0 ) {
        					if(!plugin.listBackpacks(p, p)) {
        						p.sendMessage("You have no backpacks.");
        					}
        				}
        				else if( p.hasPermission("backpacks.admin.list")){
        					Player target = Bukkit.getServer().getPlayer(args[0]);
            				if(target == null) throw new BackpacksException("Player not found.");
            				if(!plugin.listBackpacks(p, target)) {
            					p.sendMessage(String.format("%s has no backpacks.",target.getName()));
            				}
        				}
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				return false;
        			}
    			} 
    			else p.sendMessage("You cannot execute this command.");
        		return true;
	    	}
        	
        	/**
        	 * Command bkprewards allows users to see how many rewards they have to spend.
        	 * Admin users can see the rewards for other players.
        	 * Usage /bkprewards [player]
        	 */
        	if(name.equalsIgnoreCase("bkprewards")) {
    			if( p.hasPermission("backpacks.rewards") && args.length >= 0 ) {
        			try {
        				if( args.length == 0 ) {
        					p.sendMessage(String.format("You have %d rewards available.",plugin.numRewards(p)));
        				}
        				else if( p.hasPermission("backpacks.admin.rewards") ) {
        					Player target = Bukkit.getServer().getPlayer(args[0]);
            				if(target == null) throw new BackpacksException("Player not found.");
            				p.sendMessage(String.format("%s has %d rewards available.",args[0],plugin.numRewards(target)));
        				}
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkprewards [player]");
        			}
    			} 
    			else p.sendMessage("You cannot execute this command.");
        		return true;
	    	}
        	
        	/**
        	 * Command bkproutes allows users the ability to see all routes they have 
        	 * in place. Admin users may see the list for other users.
        	 * Usage /bkproutes [player]
        	 */
        	if(name.equalsIgnoreCase("bkproutes")) {
    			if( p.hasPermission("backpacks.routes") && args.length >= 0 ) {
        			try {
        				if(args.length == 0 ) {
            				if(!plugin.listRoutes(p, p)) {
            					p.sendMessage("You have no routes.");
            				}
        				}
        				else if( p.hasPermission("backpacks.admin.routes") ) {
        					Player target = Bukkit.getServer().getPlayer(args[0]);
            				if(target == null) throw new BackpacksException("Player not found.");
            				if(!plugin.listRoutes(p, p)) {
            					p.sendMessage(String.format("%s has no routes.",target.getName()));
            				}
        				}
        			}
        			catch(BackpacksException e) { p.sendMessage(e.getMessage()); }
        			catch(ArrayIndexOutOfBoundsException e) {
        				p.sendMessage("/bkproutes [player]");
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
