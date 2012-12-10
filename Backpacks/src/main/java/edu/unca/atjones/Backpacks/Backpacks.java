package edu.unca.atjones.Backpacks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Backpacks extends JavaPlugin {
    
	HashMap<String,HashMap<String,BackpacksInventory>> backpacks;
	HashMap<String,HashMap<Integer,String>> routes;
	HashMap<String,Integer> blocks;
	HashMap<String,Integer> rewards;
	
	BackpacksLogger logger;
	
	//public BackpacksDatabase database;
	
    @Override
    public void onEnable() {

		logger = new BackpacksLogger(this);
		logger.info("plugin enabled");
		
		//database = new BackpacksDatabase(this);
		
		backpacks = new HashMap<String,HashMap<String,BackpacksInventory>>();
		routes = new HashMap<String,HashMap<Integer,String>>();
		blocks = new HashMap<String,Integer>();
		rewards = new HashMap<String,Integer>();
		
        saveDefaultConfig();
        
        // Create Listener
        new BackpacksListener(this);
        
        // set the command executors
        this.getCommand("bkpcreate").setExecutor(new BackpacksCommandExecutor(this));
        this.getCommand("bkpgrant").setExecutor(new BackpacksCommandExecutor(this));        
        this.getCommand("bkphelp").setExecutor(new BackpacksCommandExecutor(this));
        this.getCommand("bkplist").setExecutor(new BackpacksCommandExecutor(this));
        this.getCommand("bkpopen").setExecutor(new BackpacksCommandExecutor(this));        
        this.getCommand("bkpremove").setExecutor(new BackpacksCommandExecutor(this));
        this.getCommand("bkprename").setExecutor(new BackpacksCommandExecutor(this));
        this.getCommand("bkproute").setExecutor(new BackpacksCommandExecutor(this));
        this.getCommand("bkproutes").setExecutor(new BackpacksCommandExecutor(this));
        this.getCommand("bkpupgrade").setExecutor(new BackpacksCommandExecutor(this));
        this.getCommand("bkprewards").setExecutor(new BackpacksCommandExecutor(this));
        
    }
    
    @Override
    public void onDisable() {
    	//database.close();
    }
    
    /**
     * Opens the backpack specified by the given name owned by the given player
     * 
     * TODO: This method doesn't allow a non-owner viewer.
     */
    public void openBackpack(Player viewer, Player owner,String name) throws BackpacksException {
    	if(name == null) {
    		throw new BackpacksException("Must provide a backpack name.");
    	}
    	if(!backpacks.containsKey(owner.getName())) {
    		throw new BackpacksException("You have no backpacks.");
    	}
    	HashMap<String,BackpacksInventory> invs = backpacks.get(owner.getName());
    	if(!invs.containsKey(name)){
    		throw new BackpacksException("You have no backpack called " + name);
    	}
    	BackpacksInventory inv = invs.get(name);
    	viewer.openInventory(inv);
    }
    
    /**
     * Creates a backpack with the given parameters and stores it in the HashMap
     */
    public void createBackpack(Player owner,String name,String type) throws BackpacksException {
    	if(name == null) throw new BackpacksException("Must provide a name for this backpack.");
    	int size = 0;
    	if(type != null) {
	    	if(type.equalsIgnoreCase("small")) size = 9;
	    	else if(type.equalsIgnoreCase("medium")) size = 18;
	    	else if(type.equalsIgnoreCase("large")) size = 27;
	    	else throw new BackpacksException(type + "is not a valid backpack type");
    	} else size = 9;
    	
    	HashMap<String,BackpacksInventory> invs;
    	if(backpacks.containsKey(owner.getName())) {
    		invs = backpacks.get(owner.getName());
    	} else {
    		invs = new HashMap<String,BackpacksInventory>();
    	}
		if(invs.containsKey(name)) throw new BackpacksException("A backpack by that name already exists");
    	BackpacksInventory newInv = new BackpacksInventory(owner, size, name);
		invs.put(name, newInv);
		backpacks.put(owner.getName(), invs);
    }
    
    /**
     * Upgrades the backpack specified if possible.
     */
    public void upgradeBackpack(Player owner, String name) throws BackpacksException {
    	if(backpacks.containsKey(owner.getName())) {
    		HashMap<String,BackpacksInventory> invs = backpacks.get(owner.getName());
    		if(invs.containsKey(name)) {
    			BackpacksInventory backpack = invs.get(name);
    			if(backpack.getSize() != 27){
    				int size = backpack.getSize() + 9;
    				ItemStack[] contents = backpack.getContents();
    				backpack = new BackpacksInventory(owner, size, name);
    				backpack.setContents(contents);
    				invs.put(name, backpack);
    				backpacks.put(owner.getName(), invs);
    			} else throw new BackpacksException("Backpack cannot be upgraded (Too large)");
    		}
    		else throw new BackpacksException("Backpack not found.");
    	} 
    	else throw new BackpacksException("Player has no backpacks.");
    }
    
    /**
     * Destroys the specified backpack.
     */
    public void removeBackpack(Player owner, String name) throws BackpacksException {
    	if(backpacks.containsKey(owner.getName())) {
    		HashMap<String,BackpacksInventory> invs = backpacks.get(owner.getName());
    		if(invs.containsKey(name)) {
    			invs.remove(name);
    		}
    		else throw new BackpacksException("Backpack not found.");
    	} 
    	else throw new BackpacksException(String.format("%s has no backpacks.",owner.getName()));
    }
    
    /**
     * Renames a backpack.
     */
    public void renameBackpack(Player owner, String oldName, String newName) throws BackpacksException {
    	if(backpacks.containsKey(owner.getName())) {
    		HashMap<String,BackpacksInventory> invs = backpacks.get(owner.getName());
    		if(invs.containsKey(oldName)) {
    			BackpacksInventory backpack = invs.get(oldName);
    			int size = backpack.getSize();
    			ItemStack[] contents = backpack.getContents();
    			backpack = new BackpacksInventory(owner, size, newName);
    			backpack.setContents(contents);
    			invs.put(newName, backpack);
    			invs.remove(oldName);
    			backpacks.put(owner.getName(), invs);
    		}
    		else throw new BackpacksException("Backpack not found.");
    	} 
    	else throw new BackpacksException(String.format("%s has no backpacks.",owner.getName()));
    }
    
    /**
     * Shows viewer a list of backpacks for owner.
     */
    public boolean listBackpacks(Player viewer, Player owner) {
    	if(backpacks.containsKey(owner.getName())) {
    		HashMap<String,BackpacksInventory> invs = backpacks.get(owner.getName());
    		Set<String> names = invs.keySet();
			Iterator<String> iterator = names.iterator();
			while(iterator.hasNext()) {
				viewer.sendMessage(iterator.next());
			}
			return true;
    	} 
    	else return false;
    }
    
    /**
     * Shows viewer a list of routes for owner.
     */
    public boolean listRoutes(Player viewer, Player owner) {
    	String playerName = owner.getName();
		if(routes.containsKey(playerName)) {
			HashMap<Integer,String> routeMap = routes.get(playerName);
			Set<Integer> keys = routeMap.keySet();
			Iterator<Integer> iterator = keys.iterator();
			while(iterator.hasNext()) {
				int id = iterator.next();
				String backpack = routeMap.get(id);
				String str = Material.getMaterial(id).name();
				viewer.sendMessage(String.format("  %s -> %s",str,backpack));
			}
			return true;
		}
		else return false;
    }
    
    /**
     * Gives the specified player rewards.
     */
    public void grantReward(Player owner,int num) {
    	String playerName = owner.getName();
    	if(rewards.containsKey(playerName)) {
    		int count = rewards.get(playerName) + num;
    		rewards.put(playerName,count);
    	}
    	else rewards.put(playerName,num);
    }
    
    /**
     * Returns true if the specified player has at least 1 reward to spend.
     */
    public boolean hasReward(Player owner) {
    	String playerName = owner.getName();
    	if(rewards.containsKey(playerName)) {
    		if(rewards.get(playerName) > 0) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Gets the number of rewards available to the specified player.
     */
    public int numRewards(Player owner) {
    	String playerName = owner.getName();
    	if(rewards.containsKey(playerName)) {
    		return rewards.get(playerName);
    	}
    	return 0;
    }
    
    /**
     * Attempts to use a player's reward.
     */
    public void useReward(Player owner) throws BackpacksException {
    	String playerName = owner.getName();
    	if(rewards.containsKey(playerName)) {
    		int count = rewards.get(playerName);
    		if( count > 0) {
    			count--;
        		rewards.put(playerName,count);
    		}
    		else throw new BackpacksException("Player has no upgrades");
    	}
    	else throw new BackpacksException("Player has no upgrades");
    }
    
    /**
     * Creates a route for the given player.
     */
    public void addRoute(Player owner, String item, String inventory) throws BackpacksException {
		HashMap<Integer,String> playerRoutes;
		HashMap<String,BackpacksInventory> playerInventories;
		String player = owner.getName();
		
		if(routes.containsKey(player)) {
			playerRoutes = routes.get(player);
		} else {
			playerRoutes = new HashMap<Integer,String>();
		}
		
		if(backpacks.containsKey(player)) {
			playerInventories = backpacks.get(player);
		} else throw new BackpacksException("No Backpacks found!");
		
		if(playerInventories.containsKey(inventory)) {
			//First see if item is the item name string
			Material itemMaterial = Material.matchMaterial(item);
			
			//If that doesn't work try it as an integer item id
			if(itemMaterial == null) {
				try {
					itemMaterial = Material.getMaterial(Integer.parseInt(item));
				} catch(NumberFormatException e) {
					throw new BackpacksException("Unrecognized Item");
				}	
			}
			
			//If the item still hasn't been recognized throw an exception
			if(itemMaterial == null) throw new BackpacksException("Unrecognized Item");
			
			//Otherwise add route
			playerRoutes.put(itemMaterial.getId(),inventory);
			routes.put(player,playerRoutes);
		}
				
	}

}