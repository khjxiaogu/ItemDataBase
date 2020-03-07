package com.khjxiaogu.ItemDataBase;

import java.io.File;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.khjxiaogu.ItemDataBase.API.ItemDataBaseAPI;
import com.khjxiaogu.ItemDataBase.Database.Database;
import com.khjxiaogu.ItemDataBase.Database.DatabaseCore;
import com.khjxiaogu.ItemDataBase.Database.DatabaseHelper;
import com.khjxiaogu.ItemDataBase.Database.MySQLCore;
import com.khjxiaogu.ItemDataBase.Database.SQLiteCore;
import com.khjxiaogu.khjxiaogu.KHJUtils;

import me.dpohvar.powernbt.api.NBTManager;

/**
 * @author khjxiaogu
 * @time 2019年8月3日
 *       file:ItemDataBase.java
 */
public class ItemDataBase extends JavaPlugin implements CommandExecutor {
	public static ItemDataBase plugin;
	private Database database;
	private static ItemDataBaseAPI iddapi;
	private boolean comparemode;

	public boolean isExactCompare() {
		return comparemode;
	}

	public void setExactCompare(boolean comparemode) {
		this.comparemode = comparemode;
	}

	// public static Map<String,ItemStack> itemmap=new HashMap();
	public static NBTManager nbtmanager = NBTManager.nbtManager;

	/**
	 * 获取API
	 * 
	 * @return ItemDataBaseAPI类型的API
	 */
	public ItemDataBaseAPI getAPI() {
		return ItemDataBase.iddapi;
	}

	@Override
	public void onEnable() {
		KHJUtils.SendPluginInfo(this);
		ItemDataBase.plugin = this;
		saveDefaultConfig();
		try {
			final ConfigurationSection dbCfg = getConfig().getConfigurationSection("database"); //$NON-NLS-1$
			DatabaseCore dbCore;
			if (dbCfg != null && dbCfg.getBoolean("mysql")) { //$NON-NLS-1$
				getLogger().info(Messages.getString("ItemDataBase.enableMySQL")); //$NON-NLS-1$
				// MySQL database - Required database be created first.
				final String user = dbCfg.getString("user"); //$NON-NLS-1$
				final String pass = dbCfg.getString("password"); //$NON-NLS-1$
				final String host = dbCfg.getString("host"); //$NON-NLS-1$
				final String port = dbCfg.getString("port"); //$NON-NLS-1$
				final String database = dbCfg.getString("database"); //$NON-NLS-1$
				dbCore = new MySQLCore(host, user, pass, database, port);
			} else {
				// SQLite database - Doing this handles file creation
				dbCore = new SQLiteCore(new File(getDataFolder(), "ItemData.db")); //$NON-NLS-1$
			}
			database = new Database(dbCore);
			// Make the database up to date
			DatabaseHelper.setup(getDB());
		} catch (final Exception e) {
			getLogger().warning(Messages.getString("ItemDataBase.databaseerror")); //$NON-NLS-1$
			getLogger().warning(Messages.getString("ItemDataBase.errorMessage") + e.getMessage()); //$NON-NLS-1$
			e.printStackTrace();
			getLogger().warning(Messages.getString("ItemDataBase.disabling")); //$NON-NLS-1$
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		comparemode = getConfig().getBoolean("compareMode"); //$NON-NLS-1$
		ItemDataBase.iddapi = ItemDataBaseAPI.getAPI();
		/*
		 * new BukkitRunnable(){
		 * 
		 * @Override
		 * public void run() {
		 * // TODO Auto-generated method stub
		 * ItemDataBase.plugin.getDB().getCore().flush();
		 * }
		 * }.runTaskTimerAsynchronously(ItemDataBase.plugin,200L, 200L);
		 */
		Bukkit.getPluginCommand("idd").setExecutor(this); //$NON-NLS-1$
		ItemDataBase.plugin.getLogger().log(Level.INFO, Messages.getString("ItemDataBase.enabled")); //$NON-NLS-1$
	}

	public Database getDB() {
		return database;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 1) {
			if (sender instanceof Player) {
				if (args[0].equals("add")) { //$NON-NLS-1$
					try {
						if (ItemDataBase.iddapi.AddItem(args[1], ((Player) sender).getItemInHand())) {
							sender.sendMessage(String.format(Messages.getString("ItemDataBase.add_succeed"), args[1])); //$NON-NLS-1$
						} else
							throw new SQLException("error"); //$NON-NLS-1$

					} catch (SQLException e) {
						sender.sendMessage(Messages.getString("ItemDataBase.add_fail")); //$NON-NLS-1$
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return true;
				} else if (args[0].equals("set")) { //$NON-NLS-1$
					try {
						ItemDataBase.iddapi.SetItem(args[1], ((Player) sender).getItemInHand());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						sender.sendMessage(String.format(Messages.getString("ItemDataBase.set_fail"), args[1])); //$NON-NLS-1$
						e.printStackTrace();
						return true;
					}
					sender.sendMessage(String.format(Messages.getString("ItemDataBase.set_succeed"), args[1])); //$NON-NLS-1$
					return true;
				} else if (args[0].equals("getNBT")) { //$NON-NLS-1$
					((Player) sender)
							.setItemInHand(ItemDataBase.iddapi.WriteNBT(args[1], ((Player) sender).getItemInHand()));
					return true;
				}
			}
			if (args[0].equals("delete")) { //$NON-NLS-1$
				ItemDataBase.iddapi.DeleteItem(args[1]);
				sender.sendMessage(String.format(Messages.getString("ItemDataBase.delete_succeed"), args[1])); //$NON-NLS-1$
				return true;
			} else if (args[0].equals("give")) { //$NON-NLS-1$
				try {
					if(args.length > 3&&ItemDataBase.iddapi.GiveItem(args[1], ItemDataBase.plugin.getServer().getPlayer(args[2]),
								Integer.parseInt(args[3]))) {
						sender.sendMessage(Messages.getString("ItemDataBase.give_succeed")); //$NON-NLS-1$
					} else if (ItemDataBase.iddapi.GiveItem(args[1],
							ItemDataBase.plugin.getServer().getPlayer(args[2]))) {
						sender.sendMessage(Messages.getString("ItemDataBase.give_succeed")); //$NON-NLS-1$
					} else
						throw new SQLException("error"); //$NON-NLS-1$
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					sender.sendMessage(String.format(Messages.getString("ItemDataBase.give_fail"), args[1])); //$NON-NLS-1$
					e.printStackTrace();
				}
				return true;
			} else if (args[0].equals("setItem")) { //$NON-NLS-1$
				try {
					ItemStack active = ItemDataBase.iddapi.GetItem(args[1]);
					active.setTypeId(Integer.parseInt(args[2]));
					ItemDataBase.iddapi.SetItem(args[1], active);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					sender.sendMessage(String.format(Messages.getString("ItemDataBase.set_fail"), args[1])); //$NON-NLS-1$
					e.printStackTrace();
					return true;
				}
				sender.sendMessage(String.format(Messages.getString("ItemDataBase.set_succeed"), args[1])); //$NON-NLS-1$
				return true;
			} else if (args[0].equals("rename")) { //$NON-NLS-1$
				try {
					if (ItemDataBase.iddapi.RenameItem(args[1], args[2])) {
						sender.sendMessage(Messages.getString("ItemDataBase.rename_succeed")); //$NON-NLS-1$
					} else
						throw new SQLException("error"); //$NON-NLS-1$
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					sender.sendMessage(String.format(Messages.getString("ItemDataBase.rename_fail"), args[1])); //$NON-NLS-1$
					e.printStackTrace();
				}
				return true;
			} else if (args[0].equals("comparemode")) { //$NON-NLS-1$
				comparemode = Boolean.parseBoolean(args[1]);
				getConfig().set("compareMode", comparemode); //$NON-NLS-1$
				saveConfig();
			}
			ItemDataBase.plugin.getLogger().log(Level.INFO, Messages.getString("ItemDataBase.runasplayer")); //$NON-NLS-1$
			return true;
		} else if (args.length > 0 && args[0].equals("list")) { //$NON-NLS-1$
			Set<String> list = ItemDataBase.iddapi.getList();
			sender.sendMessage(Messages.getString("ItemDataBase.current_item")); //$NON-NLS-1$
			for (String str : list) {
				sender.sendMessage(str);
			}
			return true;
		} else {
			// Config.getStatesData();
			sender.sendMessage(Messages.getString("ItemDataBase.40")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("ItemDataBase.41")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("ItemDataBase.42")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("ItemDataBase.43")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("ItemDataBase.44")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("ItemDataBase.45")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("ItemDataBase.46")); //$NON-NLS-1$
		}
		return true;
	}

	@Override
	public void onDisable() {
		if (database != null) {
			database.close();
			try {
				database.getConnection().close();
			} catch (final SQLException ignored) {
			}
		}
		super.onDisable();
	}
}
