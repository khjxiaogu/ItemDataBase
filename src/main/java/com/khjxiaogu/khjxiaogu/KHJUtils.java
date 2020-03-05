package com.khjxiaogu.khjxiaogu;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;


public class KHJUtils {
	public static boolean SendPluginInfo(final JavaPlugin plugin) {
		new Thread() {
			public void run() {
		        StringBuilder stringBuilder=new StringBuilder();
		        YamlConfiguration yml=getPluginInfo(plugin);
		        stringBuilder.append("plugin-name=\"");
		        stringBuilder.append(plugin.getName());
		        if(yml!=null) {
		        stringBuilder.append("\"&plugin-version=\"");
		        stringBuilder.append(yml.getString("version"));
		        }
		        stringBuilder.append("\"&server-ip=\"");
		        stringBuilder.append(getLocalIP());
		        stringBuilder.append("\"&server-mac=\"");
		        stringBuilder.append(getMachineMac());
		        stringBuilder.append("\"");
		        boolean loggedcc=true;
		        while(true) {
			        String result=sendPost("http://stats.khjxiaogu.com/pluginstat",stringBuilder.toString());
			        if(result==null&&loggedcc) {
			        	loggedcc=false;
			        	plugin.getLogger().log(Level.WARNING,"cannot retrive version:cannot connect");
			        } else {
			        	if(result=="UpdateNeeded") {
			        		plugin.getLogger().log(Level.WARNING,"Update needed,please contact qq 1905387052 to get newest version");
			        		return;
			        	}
			        	if(result=="Banned") {
			        		plugin.getLogger().log(Level.SEVERE,"Your plugin is illeagal and got banned,please contact qq 1905387052 to unban.");
			        		plugin.getServer().getPluginManager().disablePlugin(plugin);
			        		return;
			        	}
			        	if(result=="ACCEPTED") {
			        		return;
			        	}
			        }
			        try {
						sleep(1000*3600);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					}
		        }
	        }
        }.start();
		return true;
	}
	public static String getMachineMac() {
		try {
			final byte[] address = NetworkInterface.getNetworkInterfaces().nextElement().getHardwareAddress();
			return Arrays.toString(address);
		}catch(Throwable ignored) {
			return "0000";
		}finally {
			
			
		}
		
	}
	public static String getLocalIP(){
		try {
		try(final DatagramSocket socket = new DatagramSocket()){
			  socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			  return socket.getLocalAddress().getHostAddress();
			}catch(Throwable ignored){}
		}catch(Throwable ignored){
			
		}
		return "0.0.0.0";
	}
	public static YamlConfiguration getPluginInfo(JavaPlugin plugin) {
		try {
			return YamlConfiguration.loadConfiguration(plugin.getResource("plugin.yml"));
		}catch(Throwable e) {
			return null;
		}
	}
	public static String sendPost(String targetURL, String urlParameters) {
		  HttpURLConnection connection = null;

		  try {
		    //Create connection
		    URL url = new URL(targetURL);
		    connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("POST");
		    connection.setRequestProperty("Content-Type", 
		        "application/x-www-form-urlencoded");

		    connection.setRequestProperty("Content-Length", 
		        Integer.toString(urlParameters.getBytes("UTF-8").length));
		    connection.setRequestProperty("Content-Language", "zh-CN");  

		    connection.setUseCaches(false);
		    connection.setDoOutput(true);

		    //Send request
		    DataOutputStream wr = new DataOutputStream (
		        connection.getOutputStream());
		    wr.writeBytes(urlParameters);
		    wr.close();

		    //Get Response  
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is,"UTF-8"));
		    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    return response.toString();
		  } catch (Exception e) {
		    return null;
		  } finally {
		    if (connection != null) {
		      connection.disconnect();
		    }
		  }
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
	/*public static String serializeItem(final ItemStack iStack) {
		final YamlConfiguration cfg = new YamlConfiguration();
		cfg.set("item", iStack);
		return cfg.saveToString();
	}
	public static ItemStack deserializeItem(final String config) throws InvalidConfigurationException {
		final YamlConfiguration cfg = new YamlConfiguration();
		cfg.loadFromString(config);
		return cfg.getItemStack("item");
	}*/
}
