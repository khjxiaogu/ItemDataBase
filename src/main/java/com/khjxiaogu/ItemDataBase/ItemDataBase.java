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
import org.bukkit.scheduler.BukkitRunnable;

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
 * file:ItemDataBase.java
 */
public class ItemDataBase extends JavaPlugin implements CommandExecutor {
    public static ItemDataBase plugin;
    private Database database;
    private static ItemDataBaseAPI iddapi;
    //public static Map<String,ItemStack> itemmap=new HashMap();
    public static NBTManager nbtmanager=NBTManager.nbtManager;
	/**
	 * 获取API
	 * @return ItemDataBaseAPI类型的API
	 */
    public ItemDataBaseAPI getAPI() {
    	return iddapi;
    }
    public void onEnable() {
        KHJUtils.SendPluginInfo(this);
        plugin=this;
        File loc = new File(plugin.getDataFolder(), "config.yml");
        if (!loc.exists()) {
            plugin.saveResource("config.yml",true);
            loc = new File(plugin.getDataFolder(), "config.yml");
        }
        try {
            final ConfigurationSection dbCfg = getConfig().getConfigurationSection("database");
            DatabaseCore dbCore;
            if (dbCfg != null && dbCfg.getBoolean("mysql")) {
                getLogger().info("启用MySQL 开始连接数据库...");
                // MySQL database - Required database be created first.
                final String user = dbCfg.getString("user");
                final String pass = dbCfg.getString("password");
                final String host = dbCfg.getString("host");
                final String port = dbCfg.getString("port");
                final String database = dbCfg.getString("database");
                dbCore = new MySQLCore(host, user, pass, database, port);
            } else {
                // SQLite database - Doing this handles file creation
                dbCore = new SQLiteCore(new File(this.getDataFolder(), "ItemData.db"));
            }
            this.database = new Database(dbCore);
            // Make the database up to date
            DatabaseHelper.setup(getDB());
        } catch (final Exception e) {
            getLogger().warning("数据库连接错误或配置错误...");
            getLogger().warning("错误信息: " + e.getMessage());
            e.printStackTrace();
            getLogger().warning("关闭插件");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		iddapi=ItemDataBaseAPI.getAPI();
		/*new BukkitRunnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				ItemDataBase.plugin.getDB().getCore().flush();
			}
		}.runTaskTimerAsynchronously(ItemDataBase.plugin,200L, 200L);*/
        Bukkit.getPluginCommand("idd").setExecutor(this);
        plugin.getLogger().log(Level.INFO,"物品数据库插件已经启用");
    }
    public Database getDB() {
        return this.database;
    }
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (args.length >1) {
            if (sender instanceof Player) {
            	if(args[0].equals("add")) {
            		try {
						if(iddapi.AddItem(args[1], ((Player)sender).getItemInHand()))
							sender.sendMessage("物品"+args[1]+"添加成功");
						else
							throw new SQLException("error");
						
					} catch (SQLException e) {
						sender.sendMessage("添加失败，物品可能已经存在，输入/sx set ID来修改");
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		return true;
            	}else if(args[0].equals("set")){
					try {
						iddapi.SetItem(args[1], ((Player)sender).getItemInHand());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						sender.sendMessage("物品"+args[1]+"修改失败");
						e.printStackTrace();
						return true;
					}
					sender.sendMessage("物品"+args[1]+"修改成功");
					return true;
            	}else if(args[0].equals("getNBT")) {
                	((Player)sender).setItemInHand(iddapi.WriteNBT(args[1],((Player)sender).getItemInHand()));
                	return true;
                }
            }
            if(args[0].equals("delete")) {
            	iddapi.DeleteItem(args[1]);
            	sender.sendMessage("物品"+args[1]+"删除成功");
            	return true;
            }else
            if(args[0].equals("give")) {
            	try {
					if(iddapi.GiveItem(args[1], plugin.getServer().getPlayer(args[2]))) {
						sender.sendMessage("给予成功");
					}else
						throw new SQLException("error");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					sender.sendMessage("给予失败，物品"+args[1]+"可能不存在");
					e.printStackTrace();
				}
            	return true;
            }else if(args[0].equals("setItem")){
				try {
					ItemStack active=iddapi.GetItem(args[1]);
					active.setTypeId(Integer.parseInt(args[2]));
					iddapi.SetItem(args[1],active);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					sender.sendMessage("物品"+args[1]+"修改失败");
					e.printStackTrace();
					return true;
				}
				sender.sendMessage("物品"+args[1]+"修改成功");
				return true;
        	}else if(args[0].equals("list")){
				Set<String> list=iddapi.getList();
				sender.sendMessage("现有物品");
				for(String str:list) {
					sender.sendMessage(str);
				}
				return true;
        	}else
                if(args[0].equals("rename")) {
                	try {
    					if(iddapi.RenameItem(args[1],args[2])) {
    						sender.sendMessage("更名成功");
    					}else
    						throw new SQLException("error");
    				} catch (SQLException e) {
    					// TODO Auto-generated catch block
    					sender.sendMessage("更名失败，物品"+args[1]+"可能不存在");
    					e.printStackTrace();
    				}
                	return true;
                }
                plugin.getLogger().log(Level.INFO,"请使用玩家身份");
                return true;
        }else {
        	//Config.getStatesData();
        	sender.sendMessage("/idd add <物品名> 把手中物品添加到数据库");
        	sender.sendMessage("/idd delete <物品名> 从数据库删除物品");
        	sender.sendMessage("/idd set <物品名> 把手中物品设置为物品名所对应的物品");
        	sender.sendMessage("/idd give <物品名> <玩家ID>把物品名对应的物品给玩家");
        	sender.sendMessage("/idd list 列举已载入物品名");
        	sender.sendMessage("/idd rename <物品名1> <物品名2> 把物品名1代表的物品改名为物品2");
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
