package com.khjxiaogu.ItemDataBase.API;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.khjxiaogu.ItemDataBase.DatabaseManager;
import com.khjxiaogu.ItemDataBase.ItemDataBase;

import me.dpohvar.powernbt.api.NBTCompound;

/**
 * @author khjxiaogu
 * @time 2019年8月3日
 * file:ItemDataBaseAPI.java
 */
public class ItemDataBaseAPI {
	private Map<String,ItemStack> items;
	private Map<String,NBTCompound> nbt=new ConcurrentHashMap<>();
	private static ItemDataBaseAPI idbapi;
	/**
	 * @throws SQLException 数据库损坏或者错误
	 * 
	 */
	private ItemDataBaseAPI() throws SQLException {
		// TODO Auto-generated constructor stub
		items=DatabaseManager.getAllItems();
		idbapi=this;
	}
	/**
	 * 获取API对象
	 * @return 可用的ItemDataBaseAPI对象
	 */
	public static ItemDataBaseAPI getAPI(){
		if(idbapi==null)
			try {
				new ItemDataBaseAPI();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		        ItemDataBase.plugin.getLogger().warning("数据库错误，关闭插件");
		        Bukkit.getPluginManager().disablePlugin(ItemDataBase.plugin);
			}
		return idbapi;
	}
	/**
	 * 枚举所有物品名
	 * @return 物品名数组
	 */
	public Set<String> getList(){
		return items.keySet();
	}
	/**
	 * 在物品数据库中添加一个物品
	 * @param ID 物品的名字，在数据库中唯一，可以随便设置，不可重复
	 * @param item 物品本身
	 * @return 成功返回true，物品名字存在返回false
	 * @throws SQLException 不应该发生，除非数据库错误
	 */
	public boolean AddItem(String ID,final ItemStack item) throws SQLException{
		
		if(DatabaseManager.AddItem(ID, item)) {
			items.put(ID,DatabaseManager.getItem(ID));
			NBTCompound nc=ItemDataBase.nbtmanager.read(item);
			if(nc!=null)
			nbt.put(ID,nc);
			return true;
		}
		return false;
	}
	/**
	 * 删除物品数据库的一个物品
	 * @param ID 物品的名字
	 */
	public void DeleteItem(String ID) {
		items.remove(ID);
		DatabaseManager.DeleteItem(ID);
	}
	/**
	 * 在物品数据库中修改一个物品
	 * @param ID 物品的名字，如果物品未存在则添加，存在则修改
	 * @param item 物品本身
	 * @throws SQLException 不应该发生，除非数据库错误
	 */
	public void SetItem(String ID,final ItemStack item) throws SQLException {
		DatabaseManager.SetItem(ID, item) ;
		items.put(ID,DatabaseManager.getItem(ID));
		NBTCompound nc=ItemDataBase.nbtmanager.read(item);
		if(nc!=null)
		nbt.put(ID,nc);
	}
	/**
	 * 在物品数据库中修改一个物品名称
	 * @param ID 原物品名
	 * @param ID2 后物品名
	 * @return 是否成功重命名
	 * @throws SQLException 不应该发生，除非数据库错误
	 */
	public boolean RenameItem(String ID,String ID2) throws SQLException {
		if(DatabaseManager.renameItem(ID, ID2)) {
			items.remove(ID);
			NBTCompound nc=nbt.remove(ID);
			if(nc!=null)
			nbt.put(ID2,nc);
			items.put(ID2,DatabaseManager.getItem(ID2));
			return true;
			
		}
		return false;
	}
	/**
	 * 在物品数据库中搜索一个物品
	 * @param item 要搜索的物品
	 * @return 物品存在返回物品名，不存在返回null
	 */
	public String GetID(ItemStack item) {
		Set<String> IDs=items.keySet();
		for(String ID:IDs) {
			/*
			if(ItemDataBaseAPI.ItemEqual(items.get(ID),item))
				return ID;*/
		}
		return null;
	}
	/**
	 * 获取一个物品名对应的物品
	 * @param ID 物品名
	 * @return 物品名存在则返回物品，不存在返回null
	 */
	public ItemStack GetItem(String ID) {
		return items.get(ID);
	}
	/**
	 * 获取一个物品名对应的物品，可用于给予玩家
	 * @param ID 物品名
	 * @return 物品名存在则返回物品，不存在返回null
	 * @deprecated 请使用getItemClone
	 */
	@Deprecated
	public ItemStack GetItemForUse(String ID) {
		return GetItemClone(ID);
	}
	/**
	 * 获取一个物品名对应的物品，可用于给予玩家
	 * @param ID 物品名
	 * @return 物品名存在则返回物品，不存在返回null
	 */
	public ItemStack GetItemClone(String ID) {
		ItemStack is=items.get(ID);
		ItemStack is2;
		if(is!=null)
			is2=is.clone();
		else
			return null;
		if(nbt.containsKey(ID)) {
			ItemDataBase.nbtmanager.write(is2,nbt.get(ID));
		}else {
			NBTCompound nc=null;
			try {
				nc = DatabaseManager.getNBT(ID);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nbt.put(ID, nc);
			if(nc!=null) {
			ItemDataBase.nbtmanager.write(is2,nc);
			//ItemDataBase.plugin.getLogger().info("wrote"+nc.toHashMap().toString());
			}
		}
		return is2;
	}
	/**
	 * 把一个物品给对应的玩家
	 * @param ID 物品名
	 * @param player 玩家
	 * @return 成功给予返回true
	 */
	public boolean GiveItem(String ID,Player player) {
		ItemStack item=GetItem(ID);
		if(item==null) {
			return false;
		}
		ItemStack is2=item.clone();
        final Inventory pInv = player.getInventory();
       // Add the items to the players inventory
        int slot=pInv.firstEmpty();
        //ItemDataBase.nbtmanager.write(is2,nbt.get(ID));
        ItemStack toWrite;
        if(slot>0) {
	        pInv.setItem(slot,is2);
	        toWrite=pInv.getItem(slot);
        }else {
        	toWrite=player.getWorld().dropItem(player.getLocation(),is2).getItemStack();
        }
		if(nbt.containsKey(ID)) {
			NBTCompound nc=nbt.get(ID);
			if(nc!=null)
			ItemDataBase.nbtmanager.write(toWrite,nc);
		}else {
			NBTCompound nc=null;
			try {
				nc = DatabaseManager.getNBT(ID);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nbt.put(ID, nc);
			if(nc!=null) {
				ItemDataBase.nbtmanager.write(toWrite,nc);
			}
		}
        return true;
	}
	public boolean GiveItem(String ID,Player player,int count) {
		ItemStack item=GetItem(ID);
		if(item==null) {
			return false;
		}
		ItemStack is2=item.clone();
        final Inventory pInv = player.getInventory();
       // Add the items to the players inventory
        while(count>0) {
            int slot=pInv.firstEmpty();
            //ItemDataBase.nbtmanager.write(is2,nbt.get(ID));
            ItemStack toWrite;
            if(slot>0) {
    	        pInv.setItem(slot,is2);
    	        toWrite=pInv.getItem(slot);
            }else {
            	toWrite=player.getWorld().dropItem(player.getLocation(),is2).getItemStack();
            }
			if(nbt.containsKey(ID)) {
				NBTCompound nc=nbt.get(ID);
				if(nc!=null)
					ItemDataBase.nbtmanager.write(toWrite,nc);
			}else {
				NBTCompound nc=null;
				try {
					nc = DatabaseManager.getNBT(ID);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				nbt.put(ID, nc);
				if(nc!=null) {
					ItemDataBase.nbtmanager.write(toWrite,nc);
				}
			}
			if(count>=item.getMaxStackSize()) {
				toWrite.setAmount(item.getMaxStackSize());
				count -=item.getMaxStackSize();
			}else {
				toWrite.setAmount(count);
				count=0;
			}
        }
        
        return true;
	}
	/**
	 * 向物品写入固定ID的NBT
	 * @param ID 需要导出NBT的物品名
	 * @param item 需要写入NBT的物品
	 * @return 写入完成NBT的物品(即item本身)
	 */
	public ItemStack WriteNBT(String ID,ItemStack item) {
		NBTCompound nbt=null;
		try {
			nbt = DatabaseManager.getNBT(ID);
		} catch (SQLException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(nbt!=null)
			ItemDataBase.nbtmanager.write(item, nbt);
		return item;
	}
	/**
	 * 比较两个物品是否完全相同
	 * @param A 物品1
	 * @param B 物品2
	 * @return 相同返回true
	 */
	public static boolean ItemEqual(ItemStack A,ItemStack B) {
		if(A==null||B==null)
			return false;
		if(A.isSimilar(B))
			return true;
		/*NBTCompound nbtA=ItemDataBase.nbtmanager.read(A);
		NBTCompound nbtB=ItemDataBase.nbtmanager.read(B);
		if(nbtA.equals(nbtB))
			return true;*/
		return false;
	}
	
}
