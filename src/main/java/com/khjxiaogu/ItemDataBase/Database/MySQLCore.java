package com.khjxiaogu.ItemDataBase.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class MySQLCore implements DatabaseCore {
	private String url;
	/**
	 * The connection properties... user, pass, autoReconnect..
	 */
	private Properties info;
	private static final int MAX_CONNECTIONS = 8;
	private static final List<Connection> POOL = Collections.synchronizedList(new ArrayList<Connection>());

	public MySQLCore(String host, String user, String pass, String database, String port) {
		info = new Properties();
		info.put("autoReconnect", "true");
		info.put("user", user);
		info.put("password", pass);
		info.put("useUnicode", "true");
		info.put("characterEncoding", "utf8");
		url = "jdbc:mysql://" + host + ":" + port + "/" + database;
		for (int i = 0; i < MySQLCore.MAX_CONNECTIONS; i++) {
			MySQLCore.POOL.add(null);
		}
	}

	/**
	 * Gets the database connection for executing queries on.
	 *
	 * @return The database connection
	 */
	@Override
	public Connection getConnection() {
		for (int i = 0; i < MySQLCore.MAX_CONNECTIONS; i++) {
			Connection connection = MySQLCore.POOL.get(i);
			try {
				// If we have a current connection, fetch it
				if (connection != null && !connection.isClosed()) {
					if (connection.isValid(10))
						return connection;
				}
				connection = DriverManager.getConnection(url, info);
				MySQLCore.POOL.set(i, connection);
				return connection;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void queue(BufferStatement bs) {
		try {
			Connection con = getConnection();
			while (con == null) {
				try {
					Thread.sleep(15);
				} catch (InterruptedException ignored) {
				}
				// Try again
				getConnection();
			}
			PreparedStatement ps = bs.prepareStatement(con);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		// Nothing, because queries are executed immediately for MySQL
	}

	@Override
	public void flush() {
		// Nothing, because queries are executed immediately for MySQL
	}
}