package com.khjxiaogu.ItemDataBase.Database;

import java.sql.SQLException;

public interface Updater {
	public boolean update(Database db) throws SQLException;
}
