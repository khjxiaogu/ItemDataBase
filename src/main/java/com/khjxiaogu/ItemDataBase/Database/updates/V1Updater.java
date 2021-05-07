package com.khjxiaogu.ItemDataBase.Database.updates;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.inventory.ItemStack;

import com.khjxiaogu.ItemDataBase.DatabaseManager;
import com.khjxiaogu.ItemDataBase.ItemDataBase;
import com.khjxiaogu.ItemDataBase.Messages;
import com.khjxiaogu.ItemDataBase.Database.Database;
import com.khjxiaogu.ItemDataBase.Database.DatabaseHelper;
import com.khjxiaogu.ItemDataBase.Database.Updater;
import com.khjxiaogu.khjxiaogu.ItemUtils;

import me.dpohvar.powernbt.api.NBTCompound;

public class V1Updater implements Updater {
	private static String serializeNBT(ItemStack item) {
		ByteArrayOutputStream str = new ByteArrayOutputStream();
		NBTCompound nc;
		if ((nc = ItemDataBase.nbtmanager.read(item)) != null) {
			ItemDataBase.nbtmanager.writeCompressed(str, nc);
			return Base64.getEncoder().encodeToString(str.toByteArray());
		}
		return "no Tags"; //$NON-NLS-1$
	}

	public static NBTCompound deserializeNBT(String itemData) {
		if (itemData.equals("no Tags")) //$NON-NLS-1$
			return null;
		ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(itemData));
		return ItemDataBase.nbtmanager.readCompressed(stream);
	}
	@Override
	public boolean update(Database db) throws SQLException {
		if (!db.hasTable("items")||!db.hasColumn("items","nbt")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}
		db.execute("ALTER TABLE items RENAME TO itemold"); //$NON-NLS-1$
		DatabaseHelper.createItemTable(db);
		try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT * FROM itemold"); //$NON-NLS-1$
				ResultSet qr = ps.executeQuery()) {
			while (qr.next()) {
				ItemStack item;
				String ID = qr.getString("id"); //$NON-NLS-1$
				try {
					item = ItemUtils.deserializeItem(qr.getString("item")); //$NON-NLS-1$
				} catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					ItemDataBase.plugin.getLogger()
							.warning(String.format(Messages.getString("ItemDataBase.item_read_failed"), ID)); //$NON-NLS-1$
					DatabaseManager.DeleteItem(ID);
					continue;
				}
				item = ItemUtils.InitializeItemStack(item);
				NBTCompound nc;
				if ((nc = deserializeNBT(qr.getString("nbt"))) != null) { //$NON-NLS-1$
					ItemDataBase.nbtmanager.write(item, nc);
				}
				db.execute("INSERT INTO items (id,item) " //$NON-NLS-1$
						+ "VALUES(?,?)", //$NON-NLS-1$
						ID, DatabaseManager.serializeItem(item));
			}
		}
		db.getConnection().close();
		db.execute("DROP TABLE itemold;"); //$NON-NLS-1$
		ItemDataBase.plugin.getLogger().info(Messages.getString("ItemDataBase.V1Update")); //$NON-NLS-1$
		return true;
	}

}
