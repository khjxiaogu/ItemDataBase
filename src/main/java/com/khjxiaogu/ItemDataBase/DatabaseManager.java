package com.khjxiaogu.ItemDataBase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.khjxiaogu.khjxiaogu.KHJUtils;

import me.dpohvar.powernbt.api.NBTCompound;

/**
 * @author khjxiaogu
 * @time 2019年8月1日
 * file:ItemDataBase.java
 */
public class DatabaseManager {
	private static String serializeNBT(ItemStack item) {
		ByteArrayOutputStream str = new ByteArrayOutputStream();
		NBTCompound nc;
		if((nc=ItemDataBase.nbtmanager.read(item))!=null) {
     	ItemDataBase.nbtmanager.writeCompressed(str,nc);
     	return Base64.getEncoder().encodeToString(str.toByteArray());
     	}else
     		return "no Tags";
	}
	public static NBTCompound deserializeNBT(String itemData) {
		 if(itemData.equals("no Tags"))
			 return null;
	     ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(itemData));
	     return ItemDataBase.nbtmanager.readCompressed(stream);
	}
	public static boolean AddItem(String ID,ItemStack item) throws SQLException {
		PreparedStatement ps = ItemDataBase.plugin.getDB().getConnection().prepareStatement("SELECT count(*) FROM items WHERE id = ? LIMIT 1");
      	ps.setString(1, ID);
     	ResultSet qr=ps.executeQuery();
     	if(qr.next()) {
     		if(qr.getInt(1)!=0)
     			return false;
     	}
     	ItemDataBase.plugin.getDB().execute(
    			"INSERT INTO items (id,item,nbt) "
    			+ "VALUES(?,?,?)",
    			ID,KHJUtils.serializeItem(item),serializeNBT(item));
    	return true;
	}
	public static void DeleteItem(String ID) {
		ItemDataBase.plugin.getDB().execute(
    			"DELETE FROM items WHERE id = ?",
    			ID);
	}
	public static boolean SetItem(String ID,ItemStack item) throws SQLException {
		DeleteItem(ID);
		AddItem(ID,item);
    	return true;
	}
	public static boolean renameItem(String ID,String ID2) throws SQLException {
		PreparedStatement ps = ItemDataBase.plugin.getDB().getConnection().prepareStatement("SELECT count(*) FROM items WHERE id = ? LIMIT 1");
      	ps.setString(1, ID);
     	ResultSet qr=ps.executeQuery();
     	if(qr.next()) {
     		if(qr.getInt(1)==0)
     			return false;
     	}
     	ItemDataBase.plugin.getDB().execute(
    			"UPDATE items SET id=?"
    			+ " WHERE id=?",
    			ID2,ID);
    	return true;
	}
	public static ItemStack getItem(String ID) throws SQLException {
		PreparedStatement ps = ItemDataBase.plugin.getDB().getConnection().prepareStatement("SELECT * FROM items WHERE id = ? LIMIT 1");
      	ps.setString(1, ID);
     	ResultSet qr=ps.executeQuery();
     	if(qr.next()) {
    		ItemStack item;
			try {
				item = KHJUtils.deserializeItem(qr.getString("item"));
			} catch (ClassNotFoundException |IOException e) {
				return null;
			}
			NBTCompound nc;
			if((nc=deserializeNBT(qr.getString("nbt")))!=null)
    		ItemDataBase.nbtmanager.write(item,nc);
    		return item;
        }
     	return null;
	}
	public static NBTCompound getNBT(String ID) throws SQLException {
		PreparedStatement ps = ItemDataBase.plugin.getDB().getConnection().prepareStatement("SELECT * FROM items WHERE id = ? LIMIT 1");
      	ps.setString(1, ID);
     	ResultSet qr=ps.executeQuery();
     	if(qr.next()) {
			return deserializeNBT(qr.getString("nbt"));
        }
     	return null;
	}
	public static Map<String,ItemStack> getAllItems() throws SQLException{
		PreparedStatement ps = ItemDataBase.plugin.getDB().getConnection().prepareStatement("SELECT * FROM items");
     	ResultSet qr=ps.executeQuery();
     	Map<String,ItemStack> items=new ConcurrentHashMap<String, ItemStack>();
     	while(qr.next()) {
    		ItemStack item;
    		String ID=qr.getString("id");
			try {
				item = KHJUtils.deserializeItem(qr.getString("item"));
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
            	
            	ItemDataBase.plugin.getLogger().warning("物品"+ID+"读取失败，删除中...");
            	DeleteItem(ID);
            	continue;
			}
			NBTCompound nc;
			if((nc=deserializeNBT(qr.getString("nbt")))!=null) {
				ItemDataBase.nbtmanager.write(item,nc);
				//ItemDataBase.plugin.getLogger().info(ID);
			}
    		items.put(ID,item);
    		ItemDataBase.plugin.getLogger().info("物品 "+ID+"载入成功！");
        }
     	return items;
	}
}
