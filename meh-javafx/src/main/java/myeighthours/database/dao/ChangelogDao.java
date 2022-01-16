package myeighthours.database.dao;


public interface ChangelogDao {

    String TBL = "CHANGELOG";

    String COL_ID = "ID";

    String COL_VERSION = "VERSION";

    String COL_TIMESTAMP = "TIMESTAMP";

    long version() throws Exception;

    void insert(long version) throws Exception;

}
