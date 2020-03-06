package com.khjxiaogu.khjxiaogu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.khjxiaogu.ItemDataBase.ItemDataBase;

public class ItemUtils {

	public ItemUtils() {
	}
	public static String serializeItem(ItemStack item) {
	    try {
	        ByteArrayOutputStream str = new ByteArrayOutputStream();
	        BukkitObjectOutputStream data = new BukkitObjectOutputStream(str);
	        data.writeObject(item);
	        data.close();
	        return Base64.getEncoder().encodeToString(str.toByteArray());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	
	public static ItemStack deserializeItem(String itemData) throws IOException, ClassNotFoundException {
	        ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(itemData));
	        BukkitObjectInputStream data = new BukkitObjectInputStream(stream);
	        Object item=data.readObject();
	        data.close();
	        return (ItemStack) item;
	}
	public static ItemStack InitializeItemStack(ItemStack ref) {
		try {
			Object n = ref.getClass().getMethod("asNMSCopy",ItemStack.class).invoke(null,ref);
			return (ItemStack) ref.getClass().getMethod("asBukkitCopy",ItemStack.class).invoke(null,n);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			// TODO Auto-generated catch block
			ItemDataBase.plugin.getLogger().severe("pre-initiate itemstack failure,NBT data may not be exact!");
			e.printStackTrace();
		}
		return ref;
		
	}
}
