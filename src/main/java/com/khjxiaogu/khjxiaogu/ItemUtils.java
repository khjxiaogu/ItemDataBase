package com.khjxiaogu.khjxiaogu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.khjxiaogu.ItemDataBase.ItemDataBase;

public class ItemUtils {
	private static Method AsNMSCopy;
	private static Method AsBukkitCopy;
	private static boolean canUseNMS = false;
	static {
		try {
			Class<?> craftitem = Class
					.forName(Bukkit.getServer().getClass().getPackage().getName() + ".inventory.CraftItemStack");
			for (Method met : craftitem.getDeclaredMethods()) {
				if (met.getParameterCount() != 1) {
					continue;
				}
				if (ItemUtils.AsNMSCopy == null && met.getName().equals("asNMSCopy")) {
					ItemUtils.AsNMSCopy = met;
					if (ItemUtils.AsBukkitCopy != null) {
						ItemUtils.canUseNMS = true;
						break;
					}
				} else if (ItemUtils.AsBukkitCopy == null && met.getName().equals("asBukkitCopy")) {
					ItemUtils.AsBukkitCopy = met;
					if (ItemUtils.AsNMSCopy != null) {
						ItemUtils.canUseNMS = true;
						break;
					}
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			ItemDataBase.plugin.getLogger().severe("pre-initiate itemstack failure,NBT data may not be exact!");
		}

	}

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
		Object item = data.readObject();
		data.close();
		return (ItemStack) item;
	}

	public static ItemStack InitializeItemStack(ItemStack ref) {
		if (ItemUtils.canUseNMS) {
			try {
				// Object n =AsNMSCopy.invoke(null,ref);
				Object n = ref;
				return (ItemStack) ItemUtils.AsBukkitCopy.invoke(null,ItemUtils.AsNMSCopy.invoke(null, n));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| SecurityException e) {
				e.printStackTrace();
				ItemDataBase.plugin.getLogger().severe("pre-initiate itemstack failure,NBT data may not be exact!");
			}
		}

		return ref;

	}
}
