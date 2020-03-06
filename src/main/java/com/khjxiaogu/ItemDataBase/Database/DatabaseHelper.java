package com.khjxiaogu.ItemDataBase.Database;

import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
	public static void setup(Database db) throws SQLException {
		if (!db.hasTable("items")) {
			DatabaseHelper.createItemTable(db);
		}
	}

	/**
	 * Creates the database table 'items'.
	 * 
	 * @throws SQLException
	 *                      If the connection is invalid.
	 */
	public static void createItemTable(Database db) throws SQLException {
		Statement st = db.getConnection().createStatement();
		String createTable = "CREATE TABLE items (" + "id TEXT PRIMARY KEY," + "item TEXT charset utf8,"
				+ "nbt TEXT charset utf8" + ");";
		st.execute(createTable);
	}
}