package com.khjxiaogu.ItemDataBase.Database;

import java.sql.SQLException;
import java.sql.Statement;

import com.khjxiaogu.ItemDataBase.Database.updates.V1Updater;

public class DatabaseHelper {
	public static void setup(Database db) throws SQLException {
		new V1Updater().update(db);
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
		String createTable = "CREATE TABLE items (id TEXT PRIMARY KEY,item TEXT charset utf8);";
		st.execute(createTable);
	}
}