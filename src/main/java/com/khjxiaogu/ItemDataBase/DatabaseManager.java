package com.khjxiaogu.ItemDataBase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.khjxiaogu.khjxiaogu.ItemUtils;

import me.dpohvar.powernbt.api.NBTCompound;

/**
 * @author khjxiaogu
 * @time 2019年8月1日
 *       file:ItemDataBase.java
 */
public class DatabaseManager {
	public static String serializeItem(ItemStack item) {
		if(item==null)return "";
		ByteArrayOutputStream str = new ByteArrayOutputStream();
		NBTCompound all=new NBTCompound();
		NBTCompound tag;
		if ((tag = ItemDataBase.nbtmanager.read(item)) != null) {
			all.put("tag", tag);
		}
		all.put("id",item.getTypeId());
		all.put("Count",item.getAmount());
		all.put("Damage",item.getDurability());
		ItemDataBase.nbtmanager.writeCompressed(str, all);
		return Base64.getEncoder().encodeToString(str.toByteArray());
	}

	public static ItemStack deserializeItem(String itemData) {
		if(itemData==null||itemData.length()==0)return null;
		if (itemData==null||itemData.length()==0) //$NON-NLS-1$
			return new ItemStack(Material.AIR);
		ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(itemData));
		NBTCompound nc=ItemDataBase.nbtmanager.readCompressed(stream);
		ItemStack is=new ItemStack(nc.getInt("id"),nc.getInt("Count"),nc.getShort("Damage"));
		is=ItemUtils.InitializeItemStack(is);
		if(nc.containsKey("tag"))
			ItemDataBase.nbtmanager.write(is,nc.getCompound("tag"));
		return is;
	}
	public static boolean AddItem(String ID, ItemStack item) throws SQLException {
		try (PreparedStatement ps = ItemDataBase.plugin.getDB().getConnection()
				.prepareStatement("SELECT count(*) FROM items WHERE id = ? LIMIT 1");) { //$NON-NLS-1$
			ps.setString(1, ID);
			try (ResultSet qr = ps.executeQuery()) {
				if (qr.next()) {
					if (qr.getInt(1) != 0)
						return false;
				}
			}
			ItemDataBase.plugin.getDB().execute("INSERT INTO items (id,item) " //$NON-NLS-1$
					+ "VALUES(?,?)", //$NON-NLS-1$
					ID, DatabaseManager.serializeItem(item));
			return true;
		}
	}

	public static void DeleteItem(String ID) {
		ItemDataBase.plugin.getDB().execute("DELETE FROM items WHERE id = ?", //$NON-NLS-1$
				ID);
	}

	public static boolean SetItem(String ID, ItemStack item) throws SQLException {
		DatabaseManager.DeleteItem(ID);
		DatabaseManager.AddItem(ID, item);
		return true;
	}

	public static boolean renameItem(String ID, String ID2) throws SQLException {
		try (PreparedStatement ps = ItemDataBase.plugin.getDB().getConnection()
				.prepareStatement("SELECT count(item) FROM items WHERE id = ? LIMIT 1")) { //$NON-NLS-1$
			ps.setString(1, ID);
			try (ResultSet qr = ps.executeQuery()) {
				if (qr.next()) {
					if (qr.getInt(1) == 0)
						return false;
				}
			}
			ItemDataBase.plugin.getDB().execute("UPDATE items SET id=?" //$NON-NLS-1$
					+ " WHERE id=?", //$NON-NLS-1$
					ID2, ID);
			return true;
		}
	}

	public static ItemStack getItem(String ID) throws SQLException {
		try (PreparedStatement ps = ItemDataBase.plugin.getDB().getConnection()
				.prepareStatement("SELECT * FROM items WHERE id = ? LIMIT 1")) { //$NON-NLS-1$
			ps.setString(1, ID);
			try (ResultSet qr = ps.executeQuery()) {
				if (qr.next()) {
					return deserializeItem(qr.getString("item"));
				}
			}
			return null;
		}
	}

	public static Map<String, ItemStack> getAllItems() throws SQLException {
		try (PreparedStatement ps = ItemDataBase.plugin.getDB().getConnection().prepareStatement("SELECT * FROM items"); //$NON-NLS-1$
				ResultSet qr = ps.executeQuery();) {
			Map<String, ItemStack> items = new ConcurrentHashMap<String, ItemStack>();
			while (qr.next()) {
				String ID = qr.getString("id"); //$NON-NLS-1$
				items.put(ID, deserializeItem(qr.getString("item")));
				// ItemDataBase.plugin.getLogger().info("物品 "+ID+"载入成功！");
			}
			return items;
		}
	}
}
