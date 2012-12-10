package edu.unca.atjones.Backpacks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.IInventory;
import net.minecraft.server.ItemStack;
import net.minecraft.server.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

/**
 * Custom Inventory class created to implement the pickup method as in 
 * net.minecraft.server.PlayerInventory does. This allows the inventories
 * to behave like Player inventories during item pickup and makes handling
 * routing behavior much easier.
 * 
 * This class is a near exact copy of CraftInventoryCustom with the exception
 * of three added methods where marked.
 */
public class BackpacksInventory extends CraftInventory {

    public BackpacksInventory(Player owner, int size, String title) {
        super(new MinecraftInventory(owner, size, title));
    }
    
    static class MinecraftInventory implements IInventory {
        private final ItemStack[] items;
        private int maxStack = MAX_STACK;
        private final List<HumanEntity> viewers;
        private final String title;
        private InventoryType type;
        private final EntityPlayer owner;

        public MinecraftInventory(Player owner, int size, String title) {
            this.items = new ItemStack[size];
            this.title = title;
            this.viewers = new ArrayList<HumanEntity>();
            CraftWorld world = (CraftWorld) Bukkit.getServer().getWorld(owner.getWorld().getName());
            this.owner = (EntityPlayer) world.getHandle().getEntity(owner.getEntityId());
            this.type = InventoryType.CHEST;
        }

        public int getSize() {
            return items.length;
        }

        public ItemStack getItem(int i) {
            return items[i];
        }

        //added method (Taken from net.minecraft.server.PlayerInventory)
        public int emptyIndex() {
            for (int i = 0; i < this.items.length; ++i) {
                if (this.items[i] == null) {
                    return i;
                }
            }

            return -1;
        }
        
        public ItemStack splitStack(int i, int j) {
            ItemStack stack = this.getItem(i);
            ItemStack result;
            if (stack == null) return null;
            if (stack.count <= j) {
                this.setItem(i, null);
                result = stack;
            } else {
                result = new ItemStack(stack.id, j, stack.getData(), stack.getEnchantments());
                stack.count -= j;
            }
            this.update();
            return result;
        }

        //added method (Taken from net.minecraft.server.PlayerInventory)
        private int firstPartial(ItemStack itemstack) {
            for (int i = 0; i < this.items.length; ++i) {
                if (this.items[i] != null && this.items[i].id == itemstack.id && this.items[i].isStackable() && this.items[i].count < this.items[i].getMaxStackSize() && this.items[i].count < this.getMaxStackSize() && (!this.items[i].usesData() || this.items[i].getData() == itemstack.getData()) && ItemStack.equals(this.items[i], itemstack)) {
                    return i;
                }
            }

            return -1;
        }
        
        //Added method (taken from net.minecraft.server.PlayerInventory)
        private int e(ItemStack itemstack) {
            int i = itemstack.id;
            int j = itemstack.count;
            int k;

            if (itemstack.getMaxStackSize() == 1) {
                k = this.emptyIndex();
                if (k < 0) {
                    return j;
                } else {
                    if (this.items[k] == null) {
                        this.items[k] = ItemStack.b(itemstack);
                    }

                    return 0;
                }
            } else {
                k = this.firstPartial(itemstack);
                if (k < 0) {
                    k = this.emptyIndex();
                }

                if (k < 0) {
                    return j;
                } else {
                    if (this.items[k] == null) {
                        this.items[k] = new ItemStack(i, 0, itemstack.getData());
                        if (itemstack.hasTag()) {
                            this.items[k].setTag((NBTTagCompound) itemstack.getTag().clone());
                        }
                    }

                    int l = j;

                    if (j > this.items[k].getMaxStackSize() - this.items[k].count) {
                        l = this.items[k].getMaxStackSize() - this.items[k].count;
                    }

                    if (l > this.getMaxStackSize() - this.items[k].count) {
                        l = this.getMaxStackSize() - this.items[k].count;
                    }

                    if (l == 0) {
                        return j;
                    } else {
                        j -= l;
                        this.items[k].count += l;
                        this.items[k].b = 5;
                        return j;
                    }
                }
            }
        }
        
        //added method (Taken from net.minecraft.server.PlayerInventory)
        public boolean pickup(ItemStack itemstack) {
            int i;

            if (itemstack.h()) {
                i = this.emptyIndex();
                if (i >= 0) {
                    this.items[i] = ItemStack.b(itemstack);
                    this.items[i].b = 5;
                    itemstack.count = 0;
                    return true;
                } else {
                    return false;
                }
            } else {
                do {
                    i = itemstack.count;
                    itemstack.count = this.e(itemstack);
                } while (itemstack.count > 0 && itemstack.count < i);

                return itemstack.count < i;
            }
        }
        public ItemStack splitWithoutUpdate(int i) {
            ItemStack stack = this.getItem(i);
            ItemStack result;
            if (stack == null) return null;
            if (stack.count <= 1) {
                this.setItem(i, null);
                result = stack;
            } else {
                result = new ItemStack(stack.id, 1, stack.getData(), stack.getEnchantments());
                stack.count -= 1;
            }
            return result;
        }

        public void setItem(int i, ItemStack itemstack) {
            items[i] = itemstack;
            if (itemstack != null && this.getMaxStackSize() > 0 && itemstack.count > this.getMaxStackSize()) {
                itemstack.count = this.getMaxStackSize();
            }
        }

        public String getName() {
            return title;
        }

        public int getMaxStackSize() {
            return maxStack;
        }

        public void setMaxStackSize(int size) {
            maxStack = size;
        }

        public void update() {}

        public boolean a(EntityHuman entityhuman) {
            return true;
        }

        public ItemStack[] getContents() {
            return items;
        }

        public void onOpen(CraftHumanEntity who) {
            viewers.add(who);
        }

        public void onClose(CraftHumanEntity who) {
            viewers.remove(who);
        }

        public List<HumanEntity> getViewers() {
            return viewers;
        }

        public InventoryType getType() {
            return type;
        }

        public void f() {}

        public void g() {}

        //Modified
        public InventoryHolder getOwner() {
            return Bukkit.getServer().getPlayer(owner.name);
        }

        public void startOpen() {}
    }
}
