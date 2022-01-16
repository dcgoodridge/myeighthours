package myeighthours.helper;


import myeighthours.database.dao.MehDbFilePathWrapper;

public class HelperH2 {

    public enum CIPHER {NONE, AES, XTEA, FOG}

    public static String getJdbcUrlString(String databaseName, CIPHER cipher) {
        String jdbcUrlString = "";
        switch (cipher) {
            case NONE:
                jdbcUrlString = "jdbc:h2:" + MehDbFilePathWrapper.SCHEME + ":" + databaseName + ";"
                        + "FILE_LOCK=FS;" + "PAGE_SIZE=1024;" + "CACHE_SIZE=8192";
                break;
            default:
                jdbcUrlString = "jdbc:h2:" + MehDbFilePathWrapper.SCHEME + ":" + databaseName + ";"
                        + "FILE_LOCK=FS;" + "CIPHER=" + cipher + ";" + "PAGE_SIZE=1024;" + "CACHE_SIZE=8192";
        }
        return jdbcUrlString;
    }
}
