package edu.unca.atjones.Backpacks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ListIterator;

import lib.PatPeter.SQLibrary.SQLite;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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
			
			if(tableName.equals("inventories")) {
				q = "CREATE TABLE inventories (id INTEGER PRIMARY KEY, name VARCHAR(50), player VARCHAR(50));";
			}
			else if(tableName.equals("slots")) {
				q = "CREATE TABLE slots (inventoryId INT, slot INT, material INT, amount INT, PRIMARY KEY(inventoryId,slot));";
			}
			
			if(!q.equals("")) {
				if(sqlite.createTable(q)) {
					plugin.logger.info(String.format("Table %s has been created.",tableName));
				}
				else plugin.logger.info(String.format("Failed to create table %s",tableName));
			}
	    }
	}
	
	public void storeInventory(Player player, Inventory inventory) {
		String playerName = player.getName();
		String inventoryName = inventory.getName();
		
		sqlTableCheck("inventories");
		sqlTableCheck("slots");
		
		//Create the inventory and get its id.
		Integer inventoryId = null;
		try {
			String selectQ = "SELECT * FROM inventories WHERE name = '%s' AND player = '%s';";
			String insertQ = "INSERT INTO inventories (name,player) VALUES ('%s','%s');";
			
			ResultSet selectR = sqlite.query(String.format(selectQ,inventoryName,playerName));
			if(selectR == null) plugin.logger.info("Select failed");
			else {
				if(selectR.next()) do {
					inventoryId = selectR.getInt("id");
					plugin.logger.info(String.format("Inventory found with id %d",inventoryId));
				} while(selectR.next());
				else plugin.logger.info("Select gave no results.");
			}
			
			if(inventoryId == null) {
				ResultSet insertR = sqlite.query(String.format(insertQ,inventoryName,playerName));
				if(insertR == null) plugin.logger.info("Insert failed");
				else {
					plugin.logger.info("Insert succeeded.");
					insertR.close();
				}
				
				selectR = sqlite.query(String.format(selectQ,inventoryName,playerName));
				if(selectR == null) plugin.logger.info("Reselect failed");
				else {
					if(selectR.next()) do {
						inventoryId = selectR.getInt("id");
						plugin.logger.info(String.format("Inventory created with id %d",inventoryId));
					} while(selectR.next());
					else plugin.logger.info("Reselect gave no results.");
				}
			}
			
		} catch(SQLException e) {
			plugin.logger.warn("Failed query for table inventories.");
			plugin.logger.warn(e.getMessage());
			return;
		}
	
		if(inventoryId != null) {
			ListIterator<ItemStack> stackIterator = inventory.iterator();
			while(stackIterator.hasNext()) {
				int slot = stackIterator.nextIndex();
				ItemStack stack = stackIterator.next();
				if(stack == null) {
					try{
						String deleteQ = "DELETE FROM slots WHERE inventoryId = %d AND slot = %d;";
						ResultSet deleteR = sqlite.query(String.format(deleteQ,inventoryId,slot));
						plugin.logger.info(String.format("Cleared Data for slot %d.",slot ));
						if(deleteR == null) plugin.logger.info("Delete failed");
						else {
							plugin.logger.info("Insert succeeded.");
							deleteR.close();
						}
					} catch(SQLException e) {
						plugin.logger.warn("Failed query for table inventories.");
						plugin.logger.warn(e.getMessage());
					}
				}
				else {
					int materialId = stack.getTypeId();
					int stackSize = stack.getAmount();
					
					try{
						String insertQ = "REPLACE INTO slots (inventoryId, slot, material, amount) VALUES (%d,%d,%d,%d);";
						ResultSet insertR = sqlite.query(String.format(insertQ,inventoryId,slot,materialId,stackSize));
						plugin.logger.info(String.format("Save %d %s for user %s in inventory %s.",stackSize,stack.getType().name(),playerName,inventoryName ));
						if(insertR == null) plugin.logger.info("Insert failed");
						else {
							plugin.logger.info("Insert succeeded.");
							insertR.close();
						}
					} catch(SQLException e) {
						plugin.logger.warn("Failed query for table inventories.");
						plugin.logger.warn(e.getMessage());
					}
				}
			}
		}
	}
	
	public Inventory getInventory(Player player, String inventoryName ) {
		return null;
	}
}
