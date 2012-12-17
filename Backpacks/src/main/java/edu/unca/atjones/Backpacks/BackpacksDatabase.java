package edu.unca.atjones.Backpacks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ListIterator;

import lib.PatPeter.SQLibrary.SQLite;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BackpacksDatabase {

	Backpacks plugin;
	
	public SQLite sqlite;
	
	public BackpacksDatabase(Backpacks plugin) {
		this.plugin = plugin;
        sqlConnection();
	}
	
	public void close() {
		sqlite.close();
	}
	
	public void sqlConnection() {
		sqlite = new SQLite(plugin.getLogger(),
		                plugin.getName(),
		                "database",
		                plugin.getDataFolder().getAbsolutePath());

		try {
			sqlite.open();
		} catch (Exception e) {
			plugin.getLogger().info(e.getMessage());
			plugin.getPluginLoader().disablePlugin(plugin);
		}
	}
	
	public void sqlTableCheck(String tableName) {
		if(sqlite.checkTable(tableName)) {
			plugin.logger.info(String.format("Table %s exists.",tableName));
			return;
	    }
		else {
			String q = "";
			
			if(tableName.equals("backpacks")) {
				q = "CREATE TABLE backpacks (id INTEGER PRIMARY KEY, name VARCHAR(50), size INT, player VARCHAR(50));";
			}
			else if(tableName.equals("slots")) {
				q = "CREATE TABLE slots (backpackId INT, slot INT, material INT, amount INT, PRIMARY KEY(backpackId,slot));";
			}
			
			if(!q.equals("")) {
				if(sqlite.createTable(q)) {
					plugin.logger.info(String.format("Table %s has been created.",tableName));
				}
				else plugin.logger.info(String.format("Failed to create table %s",tableName));
			}
	    }
	}
	
	public void storeBackpack(Player player, BackpacksInventory backpack) {
		String playerName = player.getName();
		String backpackName = backpack.getName();
		
		sqlTableCheck("backpacks");
		sqlTableCheck("slots");
		
		//Create the inventory and get its id.
		Integer backpackId = null;
		try {
			String selectQ = "SELECT * FROM backpacks WHERE name = '%s' AND player = '%s';";
			String insertQ = "INSERT INTO backpacks (name,size,player) VALUES ('%s',%d,'%s');";
			
			ResultSet selectR = sqlite.query(String.format(selectQ,backpackName,playerName));
			if(selectR == null) plugin.logger.info("Select failed");
			else {
				if(selectR.next()) do {
					backpackId = selectR.getInt("id");
					plugin.logger.info(String.format("Backpack found with id %d",backpackId));
				} while(selectR.next());
				else plugin.logger.info("Select gave no results.");
			}
			
			if(backpackId == null) {
				ResultSet insertR = sqlite.query(String.format(insertQ,backpackName,backpack.getSize(),playerName));
				if(insertR == null) plugin.logger.info("Insert failed");
				else {
					plugin.logger.info("Insert succeeded.");
					insertR.close();
				}
				
				selectR = sqlite.query(String.format(selectQ,backpackName,playerName));
				if(selectR == null) plugin.logger.info("Reselect failed");
				else {
					if(selectR.next()) do {
						backpackId = selectR.getInt("id");
						plugin.logger.info(String.format("Backpack created with id %d",backpackId));
					} while(selectR.next());
					else plugin.logger.info("Reselect gave no results.");
				}
			}
			
		} catch(SQLException e) {
			plugin.logger.warn("Failed query for table backpacks.");
			plugin.logger.warn(e.getMessage());
			return;
		}
	
		if(backpackId != null) {
			ListIterator<ItemStack> stackIterator = backpack.iterator();
			while(stackIterator.hasNext()) {
				int slot = stackIterator.nextIndex();
				ItemStack stack = stackIterator.next();
				
				if(stack == null) {
					try{
						String deleteQ = "DELETE FROM slots WHERE inventoryId = %d AND slot = %d;";
						ResultSet deleteR = sqlite.query(String.format(deleteQ,backpackId,slot));
						plugin.logger.info(String.format("Cleared Data for slot %d.",slot ));
						if(deleteR == null) plugin.logger.info("Delete failed");
						else {
							plugin.logger.info("Insert succeeded.");
							deleteR.close();
						}
					} catch(SQLException e) {
						plugin.logger.warn("Failed query for table backpacks.");
						plugin.logger.warn(e.getMessage());
					}
				}
				else {
					int materialId = stack.getTypeId();
					int stackSize = stack.getAmount();
					
					try{
						String insertQ = "REPLACE INTO slots (backpackId, slot, material, amount) VALUES (%d,%d,%d,%d);";
						ResultSet insertR = sqlite.query(String.format(insertQ,backpackId,slot,materialId,stackSize));
						plugin.logger.info(String.format("Save %d %s for user %s in backpack %s.",stackSize,stack.getType().name(),playerName,backpackName ));
						if(insertR == null) plugin.logger.info("Insert failed");
						else {
							plugin.logger.info("Insert succeeded.");
							insertR.close();
						}
					} catch(SQLException e) {
						plugin.logger.warn("Failed query for table backpacks.");
						plugin.logger.warn(e.getMessage());
					}
				}
			}
		}
	}
	
	public HashMap<String,BackpacksInventory> loadBackpacks(Player player ) {
		String playerName = player.getName();
		
		sqlTableCheck("backpacks");
		sqlTableCheck("slots");
		
		try {
			String backpackSelectQ = "SELECT * FROM backpacks WHERE player = '%s';";
			String slotSelectQ = "SELECT * FROM slots WHERE backpackId = %d;";
			
			ResultSet backpackSelectR = sqlite.query(String.format(backpackSelectQ,playerName));
			if(backpackSelectR == null) plugin.logger.info("Select failed");
			else {
				if(backpackSelectR.next()) do {
					int backpackId = backpackSelectR.getInt("id");
					String name = backpackSelectR.getString("name");
					int size = backpackSelectR.getInt("size");
					plugin.logger.info(String.format("Backpack found with id %d",backpackId));
					
					BackpacksInventory backpack = new BackpacksInventory(player, size, name);
					
					ResultSet slotSelectR = sqlite.query(String.format(slotSelectQ,backpackId));
					if(slotSelectR == null) plugin.logger.info("Select failed");
					else {
						if(slotSelectR.next()) do {
							int slot = slotSelectR.getInt("slot");
							int materialId = slotSelectR.getInt("material");
							int amount = slotSelectR.getInt("amount");
						} while(slotSelectR.next());
					}
				} while(backpackSelectR.next());
				else plugin.logger.info("Select gave no results.");
			}
			
			
		} catch(SQLException e) {
			plugin.logger.warn("Failed query for table backpacks.");
			plugin.logger.warn(e.getMessage());
		}
		return null;
	}
}
