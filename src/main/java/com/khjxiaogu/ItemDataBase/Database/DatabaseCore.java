package com.khjxiaogu.ItemDataBase.Database;

import java.sql.Connection;

public interface DatabaseCore {
	Connection getConnection();

	void queue(BufferStatement bs);

	void flush();

	void close();
}