package myeighthours.database.dao;

public interface MehDb {

    String FILE_EXTENSION = "mehdb";

    String getPath();

    void open() throws Exception;

    void create() throws Exception;

    void close() throws Exception;

    FichajeDao fichaje();
}
