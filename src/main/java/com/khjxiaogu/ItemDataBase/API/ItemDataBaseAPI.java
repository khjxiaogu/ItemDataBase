package com.khjxiaogu.ItemDataBase.API;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.khjxiaogu.ItemDataBase.DatabaseManager;
import com.khjxiaogu.ItemDataBase.ItemDataBase;
import com.khjxiaogu.ItemDataBase.Messages;
import com.khjxiaogu.ItemDataBase.events.ItemGivenEvent;
import com.khjxiaogu.khjxiaogu.ItemUtils;

import me.dpohvar.powernbt.api.NBTCompound;

/**
 * @author khjxiaogu
 *         file:ItemDataBaseAPI.java
 */
public class ItemDataBaseAPI {
	private Map<String, ItemStack> items;
	private Map<String, ItemStack> itemsForCompare=new ConcurrentHashMap<>();
	private Map<String, NBTCompound> nbt = new ConcurrentHashMap<>();
	private static ItemDataBaseAPI idbapi;
	private static List<ItemCompareHandler> handlers=new CopyOnWriteArrayList<>();
	private ItemDataBaseAPI() throws SQLException {
		// TODO Auto-generated constructor stub
		items = DatabaseManager.getAllItems();
		for(Entry<String, ItemStack> s:items.entrySet()) {
			itemsForCompare.put(s.getKey(),HandleItemEqual(s.getValue()));
		}
		ItemDataBaseAPI.idbapi = this;
	}

	/**
	 * 获取API对象
	 * get API instance
	 * 
	 * @return 可用的ItemDataBaseAPI对象/API instance
	 */
	public static ItemDataBaseAPI getAPI() {
		if (ItemDataBaseAPI.idbapi == null) {
			try {
				new ItemDataBaseAPI();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ItemDataBase.plugin.getLogger().warning(Messages.getString("ItemDataBase.disabling"));
				Bukkit.getPluginManager().disablePlugin(ItemDataBase.plugin);
			}
		}
		return ItemDataBaseAPI.idbapi;
	}

	/**
	 * 枚举所有物品名
	 * list all item names
	 * 
	 * @return 物品名集合/set of item name
	 */
	public Set<String> getList() {
		return items.keySet();

	}

	/**
	 * 在物品数据库中添加一个物品，名字在数据库中唯一，如果已存在，返回失败
	 * add an item to database,name should be unique in database.if name
	 * exists,return failed.
	 * 
	 * @param ID   物品的名字/item name
	 * @param item 物品/item to add
	 * @return 成功返回true，物品名字存在返回false/true if succeed and false if failed.
	 */
	public boolean AddItem(String ID, final ItemStack item) throws SQLException {

		if (DatabaseManager.AddItem(ID, item)) {
			items.put(ID,DatabaseManager.getItem(ID));
			itemsForCompare.put(ID,HandleItemEqual(DatabaseManager.getItem(ID)));
			NBTCompound nc = ItemDataBase.nbtmanager.read(item);
			if (nc != null) {
				nbt.put(ID, nc);
			} else {
				nbt.put(ID, new NBTCompound());
			}
			return true;
		}
		return false;
	}

	/**
	 * 删除物品数据库的一个物品
	 * delete an item from database
	 * 
	 * @param ID 物品的名字/item name
	 */
	public void DeleteItem(String ID) {
		items.remove(ID);
		itemsForCompare.remove(ID);
		nbt.remove(ID);
		DatabaseManager.DeleteItem(ID);
	}

	/**
	 * 在物品数据库中修改一个物品，如果物品未存在则添加，存在则修改
	 * change an item in database,create if not exists.
	 * 
	 * @param ID   物品的名字/item name
	 * @param item 物品/item to set
	 */
	public void SetItem(String ID, final ItemStack item) throws SQLException {
		DatabaseManager.SetItem(ID,item);
		items.put(ID, DatabaseManager.getItem(ID));
		itemsForCompare.put(ID,HandleItemEqual(DatabaseManager.getItem(ID)));
		NBTCompound nc = ItemDataBase.nbtmanager.read(item);
		
		if (nc != null) {
			nbt.put(ID, nc);
		} else {
			nbt.put(ID, new NBTCompound());
		}
	}

	/**
	 * 在物品数据库中修改一个物品名称
	 * rename an item in database
	 * 
	 * @param ID  原物品名/original item name
	 * @param ID2 后物品名/new item name
	 * @return 是否成功重命名/if it is succeed
	 */
	public boolean RenameItem(String ID, String ID2) throws SQLException {
		if (DatabaseManager.renameItem(ID, ID2)) {
			items.remove(ID);
			itemsForCompare.remove(ID);
			NBTCompound nc = nbt.remove(ID);
			if (nc != null) {
				nbt.put(ID2, nc);
			} else {
				nbt.put(ID2, new NBTCompound());
			}
			items.put(ID2, DatabaseManager.getItem(ID2));
			itemsForCompare.put(ID2,HandleItemEqual(DatabaseManager.getItem(ID2)));
			return true;
		}
		return false;
	}

	/**
	 * 在物品数据库中搜索一个物品，获取它的名字
	 * search an item in database to get its name
	 * 
	 * @param item 要搜索的物品/item to search
	 * @return 物品存在返回物品名，不存在返回null/item name if item exists,else null.
	 */
	public String GetID(ItemStack item) {
		item=HandleItemEqual(item);
		for (Entry<String, ItemStack> ID : itemsForCompare.entrySet()) {
			if (ItemDataBaseAPI.ItemEqualNoHandle(ID.getValue(), item))
				return ID.getKey();
		}
		return null;
	}

	/**
	 * 获取一个物品名对应的物品，该物品只读，对本物品的修改将不会生效
	 * get a read only item,can be used for comparing,changes has been made on this
	 * item may not take any effect.
	 * 
	 * @param ID 物品名/item name
	 * @return 物品名存在则返回物品，不存在返回null/item if item exists,else null.
	 */
	public ItemStack GetItem(String ID) {
		return items.get(ID);
	}
	public ItemStack GetItemForCompare(String ID) {
		return itemsForCompare.get(ID);
	}
	/**
	 * 获取一个物品名对应的物品，可用于给予玩家
	 * get item for use
	 * 
	 * @param ID 物品名/item name
	 * @return 物品名存在则返回物品，不存在返回null/item if item exists,else null.
	 * @deprecated 请使用{@link #GetItemClone(String) GetItemClone}/use
	 *             {@link #GetItemClone(String) GetItemClone} instead.
	 */
	@Deprecated
	public ItemStack GetItemForUse(String ID) {
		return GetItemClone(ID);
	}

	/**
	 * 获取一个物品名对应的物品，可用于放入物品栏或者世界并写入数据
	 * getting item of the specific name,is applicable to be placed into
	 * inventory,drop on world,or write data.
	 * 
	 * @param ID 物品名/item name.
	 * @return 物品名存在则返回物品，不存在返回null/item if item exists,else null.
	 */
	public ItemStack GetItemClone(String ID) {
		return CloneItem(items.get(ID));
	}
	public static ItemStack CloneItem(ItemStack is) {
		ItemStack is2;

		if (is != null&&is.getType()!=Material.AIR) {
			is2 = is.clone();
		} else
			return null;
		
		is2 = ItemUtils.InitializeItemStack(is2);
		if(is.hasItemMeta()) {
			NBTCompound tag=ItemDataBase.nbtmanager.read(is);
			if(tag!=null) {
				ItemDataBase.nbtmanager.write(is2,tag);
			}
		}
		return is2;
	}
	/**
	 * 把一个物品给对应的玩家
	 * giving player an item,exceeded would be dropped to the floor.
	 *
	 * @param ID     物品名/item name.
	 * @param player 玩家/player.
	 * @return 成功给予返回true/return true if succeed.
	 */
	public boolean GiveItem(String ID, Player player) {
		ItemStack is2 = GetItemClone(ID);
		if (is2 == null)
			return false;
		final Inventory pInv = player.getInventory();// get player's inventory
		// Add the items to the players inventory
		ItemGivenEvent ige=new ItemGivenEvent(player,is2);
		Bukkit.getPluginManager().callEvent(ige);
		if(ige.isCancelled())return false;
		
		ArrayList<ItemStack> overflow = new ArrayList<>();
		overflow.addAll(pInv.addItem(ige.getItem()).values());
		for (ItemStack is : overflow) {
			player.getWorld().dropItem(player.getLocation(), is);
		}

		return true;
	}

	/**
	 * 给予玩家一定数目的指定物品，多余的会掉落在地面上
	 * giving player certain amount of item,exceeded would be dropped to the floor.
	 * 
	 * @param ID     物品名/item name.
	 * @param player 玩家/player.
	 * @param count  物品数量/amount of item.
	 * @return 成功给予返回true/return true if succeed.
	 */
	public boolean GiveItem(String ID, Player player, int count) {
		final Inventory pInv = player.getInventory();
		ItemStack is2 = GetItemClone(ID);
		if (is2 == null)
			return false;
		is2.setAmount(count);
		ItemGivenEvent ige=new ItemGivenEvent(player,is2);
		Bukkit.getPluginManager().callEvent(ige);
		if(ige.isCancelled())return false;
		// Add the items to the players inventory
		ArrayList<ItemStack> overflow = new ArrayList<>();
		overflow.addAll(pInv.addItem(ige.getItem()).values());
		for (ItemStack is : overflow) {
			player.getWorld().dropItem(player.getLocation(), is);
		}
		return true;
	}

	/**
	 * 向物品写入固定ID的NBT
	 * copy nbt from itemname to certain item.
	 * 
	 * @param ID   itemname.
	 * @param item item to write.
	 * @return item itself.
	 */
	public ItemStack WriteNBT(String ID, ItemStack item) {
		NBTCompound nbt = null;
		nbt = ItemDataBase.nbtmanager.read(GetItem(ID));
		if (nbt != null) {
			ItemDataBase.nbtmanager.write(item, nbt);
		}
		return item;
	}
	public static void registerCompareHandler(ItemCompareHandler ich) {
		handlers.add(ich);
	}
	/**
	 * 比较两个物品是否完全相同，与配置相关
	 * compare two item is equals,comparing mode depends on config.
	 * 
	 * @param A 物品1/item A.
	 * @param B 物品2/item B.
	 * @return 相同返回true/return true if equals.
	 */
	public static boolean ItemEqual(ItemStack A, ItemStack B) {
		if(A==B)return true;
		if (A == null || B == null)
			return false;
		if(handlers.size()>0) {
			A=CloneItem(A);
			B=CloneItem(B);
			for(ItemCompareHandler handler:handlers) {
				A=handler.apply(A);
				B=handler.apply(B);
			}
		}
		if (A.isSimilar(B)) {
			if (ItemDataBase.plugin.isExactCompare()) {
				NBTCompound nbtA = ItemDataBase.nbtmanager.read(A);
				NBTCompound nbtB = ItemDataBase.nbtmanager.read(B);
				if(nbtA==nbtB)return true;
				return nbtA.equals(nbtB);
			}
			return true;
		}
		return false;
	}
	public static ItemStack HandleItemEqual(ItemStack is) {
		if(handlers.size()>0) {
			is=CloneItem(is);
			for(ItemCompareHandler handler:handlers) {
				is=handler.apply(is);
			}
		}
		return is;
	}
	public static boolean ItemEqualNoHandle(ItemStack A, ItemStack B) {
		if(A==B)return true;
		if (A == null || B == null)
			return false;
		if (A.isSimilar(B)) {
			if (ItemDataBase.plugin.isExactCompare()) {
				NBTCompound nbtA = ItemDataBase.nbtmanager.read(A);
				NBTCompound nbtB = ItemDataBase.nbtmanager.read(B);
				if(nbtA==nbtB)return true;
				return nbtA.equals(nbtB);
			}
			return true;
		}
		return false;
	}
	/**
	 * 比较两个物品包括NBT是否完全相同
	 * compare two item is equals including nbt
	 * 
	 * @param A 物品1/item A
	 * @param B 物品2/item B
	 * @return 相同返回true/return true if equals
	 */
	public static boolean ItemExactEqual(ItemStack A, ItemStack B) {
		if (A == null || B == null)
			return false;
		if (A.isSimilar(B))
			return true;
		NBTCompound nbtA = ItemDataBase.nbtmanager.read(A);
		NBTCompound nbtB = ItemDataBase.nbtmanager.read(B);
		if (nbtA.equals(nbtB))
			return true;
		return false;
	}
}
