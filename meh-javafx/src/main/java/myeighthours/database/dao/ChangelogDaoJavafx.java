package myeighthours.database.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ChangelogDaoJavafx implements ChangelogDao {

    private static final Logger LOG = LoggerFactory.getLogger(ChangelogDaoJavafx.class);

    private Connection connection;

    public ChangelogDaoJavafx(Connection connection) {
        this.connection = connection;
    }

    @Override
    public long version() throws Exception {
        boolean tableChangelogExists = changelogTableExists();
        long version = 1;
        if (!tableChangelogExists) return version;
        String sql = "SELECT * FROM " + TBL + " ORDER BY " + COL_VERSION + " DESC LIMIT 1;";
        LOG.debug(sql);
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql);) {
            while (rs.next()) {
                long id = rs.getLong(COL_ID);
                version = rs.getLong(COL_VERSION);
            }
        }
        return version;
    }

    @Override
    public void insert(long version) throws Exception {
        String sql = "INSERT INTO " + TBL + " " +
                "(" + COL_VERSION + "," + COL_TIMESTAMP + ")" +
                "VALUES (" + version + "," + System.currentTimeMillis() + ")";
        LOG.debug(sql);
        long insertedId = -1;
        try (Statement stmt = connection.createStatement();) {
            stmt.executeUpdate(sql);
            try (ResultSet rs = stmt.getGeneratedKeys();) {
                if (rs.next()) {
                    insertedId = rs.getLong(1);
                }
            }
        }
    }

    public static boolean tableExist(Connection conn, String tableName) throws Exception {
        boolean tExists = false;
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            while (rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if (tName != null && tName.equals(tableName)) {
                    tExists = true;
                    break;
                }
            }
        }
        return tExists;
    }

    private boolean changelogTableExists() throws Exception {
        return tableExist(connection, TBL);
    }

}
