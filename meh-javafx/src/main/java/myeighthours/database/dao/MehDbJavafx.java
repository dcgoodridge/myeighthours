package myeighthours.database.dao;


import myeighthours.database.schema.MehDbSchema;
import myeighthours.helper.HelperH2;
import org.h2.store.fs.FilePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class MehDbJavafx implements MehDb {

    private static final Logger LOG = LoggerFactory.getLogger(MehDbJavafx.class);

    private static final int DATABASE_VERSION = 1;

    private static final String FOLDER = "mehdb";

    private boolean writeable = false;

    private FichajeDao fichajeDao;

    private ChangelogDao changelogDao;

    private MehDbSchema schema;

    private Connection connection;

    private static final HelperH2.CIPHER DEFAULT_CYPHER = HelperH2.CIPHER.AES;

    private MehDbState state;


    public MehDbJavafx(MehDbState state) {
        this.state = state;
        schema = new MehDbSchema();
    }

    @Override
    public String getPath() {
        return state.getDbPath();
    }

    @Override
    public void open() throws Exception {
        if (connection != null) {
            LOG.warn("Trying to open database, but a connection is already open");
            return;
        }
        File myFile = new File(getPath());
        this.writeable = myFile.canWrite();
        if (!myFile.exists()) {
            throw new Exception("File: \"" + myFile.toString() + "\" doesn't exist. Have you called db.create()?");
        }
        connection = openConnection(state, DEFAULT_CYPHER);
        fichajeDao = new FichajeDaoJavafx(this.connection);
        changelogDao = new ChangelogDaoJavafx(this.connection);
        checkDbSchemaVersion();
    }

    private void checkDbSchemaVersion() throws Exception {
        long dbVersion = changelogDao.version();
        long schemaVersion = schema.getVersion();
        if (dbVersion == schemaVersion) {
            //DB version is OK. Check completed
        } else {
            if (dbVersion < schemaVersion) {
                LOG.info("Actualizando la BD: De versión " + dbVersion+" a " + schemaVersion);
                updateDatabaseSchema(dbVersion, schemaVersion);
            } else {
                throw new RuntimeException("La version de la BD ( actual = " + dbVersion + " ) es superior a la compatible con la APP ( " + schemaVersion + " )");
            }
        }
    }

    private void updateDatabaseSchema(long oldVersion, long newVersion) throws Exception {
        try {
            executeUpdateSchema(oldVersion, newVersion);
        } catch (SQLException e) {
            throw new Exception("Exception updating database schema (" + oldVersion + " to " + newVersion + ")");
        }
    }

    private void executeUpdateSchema(long oldVersion, long newVersion) throws Exception {
        List<String> sqlList = schema.getSqlUpdateSchema(oldVersion, newVersion);
        executeSqlList(sqlList);
        changelogDao.insert(newVersion);
    }

    private void executeSqlList(List<String> sqlList) throws Exception {
        Statement stmt = null;
        stmt = this.connection.createStatement();
        for (String sql : sqlList) {
            LOG.debug("Executing SQL: \n" + sql);
            stmt.executeUpdate(sql);
        }
        this.connection.commit();
        stmt.close();
    }

    private String removeDatabaseFileExtensionToPath(String s) {
        String SUFFIX = "." + FILE_EXTENSION;
        String out = s;
        if (s.endsWith(SUFFIX)) {
            out = out.substring(0, out.length() - SUFFIX.length());
        }
        return out;
    }

    private Connection openConnection(MehDbState state, HelperH2.CIPHER cipher) throws Exception {
        String databasePath = state.getDbPath();
        String databaseFilePassword = state.getDbPassword();
        String username = "mehuser";
        String password = "mehpass";
        String databasePathCleanExtension = removeDatabaseFileExtensionToPath(databasePath);
        String jdbcUrlString = HelperH2.getJdbcUrlString(databasePathCleanExtension, cipher);
        Connection connection;
        Class.forName("org.h2.Driver");
        LOG.debug("Opennig connection to database: " + databasePath);
        MehDbFilePathWrapper wrapper = new MehDbFilePathWrapper();
        FilePath.register(wrapper);
        if (cipher.equals(HelperH2.CIPHER.NONE)) {
            connection = DriverManager.getConnection(jdbcUrlString);
        } else {
            connection = DriverManager.getConnection(jdbcUrlString, username, databaseFilePassword + " " + password);
        }
        return connection;
    }

    public boolean isWritteable() {
        return writeable;
    }

    @Override
    public void create() throws Exception {
        if (connection != null) {
            LOG.warn("Trying to create database, but a connection is still open");
            return;
        }
        File myFile = new File(getPath());
        if (myFile.exists()) {
            throw new Exception("File: \"" + myFile.toString() + "\" already exists");
        }
        initDirectories(myFile);
        this.writeable = myFile.canWrite();
        myFile.setWritable(true);
        connection = openConnection(state, DEFAULT_CYPHER);
        fichajeDao = new FichajeDaoJavafx(this.connection);
        changelogDao = new ChangelogDaoJavafx(this.connection);
        createSchema(connection);
        close();
    }

    private void initDirectories(File file) {
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
    }

    private void createSchema(Connection connection) throws Exception {
        List<String> sqlList = schema.getSqlCreateSchema();
        try (Statement statement = connection.createStatement();) {
            for (String sql : sqlList) {
                LOG.debug(sql);
                statement.executeUpdate(sql);
            }
            changelogDao.insert(schema.getVersion());
            LOG.info("Schema created successfully");
        } catch (Exception e) {
            throw new Exception("Error creating db scheema", e);
        }
    }

    @Override
    public void close() throws Exception {
        try {
            if (connection != null) connection.close();
        } catch (SQLException se) {
            LOG.error("Error closing database connection", se);
        }
    }

    public static File getFilesDir() {
        String userHome = System.getProperty("user.home");
        File mehHomeFolder = new File(userHome, ".meh");
        File mehDbFolder = new File(mehHomeFolder, "mehdb");
        return mehDbFolder;
    }

    @Override
    public FichajeDao fichaje() {
        return this.fichajeDao;
    }

}
