package com.khjxiaogu.ItemDataBase.Database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

public class SQLiteCore implements DatabaseCore {
    private Connection connection;
    private final File dbFile;
    private volatile Thread watcher;
    private final LinkedList<BufferStatement> queue = new LinkedList<BufferStatement>();

    public SQLiteCore(final File dbFile) {
        this.dbFile = dbFile;
    }

    @Override
    public void close() {
        flush();
    }

    @Override
    public void flush() {
        while (!queue.isEmpty()) {
            BufferStatement bs;
            synchronized (queue) {
                bs = queue.removeFirst();
            }
            synchronized (dbFile) {
                try {
                    final PreparedStatement ps = bs.prepareStatement(getConnection());
                    ps.execute();
                    ps.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets the database connection for executing queries on.
     *
     * @return The database connection
     */
    @Override
    public Connection getConnection() {
        try {
            // If we have a current connection, fetch it
            if (this.connection != null && !this.connection.isClosed()) { return this.connection; }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        if (this.dbFile.exists()) {
            // So we need a new connection
            try {
                Class.forName("org.sqlite.JDBC");
                this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbFile);
                return this.connection;
            } catch (final ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        // So we need a new file too.
        try {
            // Create the file
            this.dbFile.createNewFile();
            // Now we won't need a new file, just a connection.
            // This will return that new connection.
            return this.getConnection();
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void queue(final BufferStatement bs) {
        /*new Thread() {
            @Override
            public void run() {*/
		    	try {
			    	final PreparedStatement ps = bs.prepareStatement(getConnection());
			        ps.execute();
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            /*}
           }.start();*/
      /*  if (watcher == null || !watcher.isAlive()) {
            startWatcher();
        }*/
    }

    private void startWatcher() {
        watcher = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                } catch (final InterruptedException ignored) {
                }
                flush();
            }
        };
        watcher.start();
    }
}