package myeighthours.database.dao;


public class MehDbState {

    private static final String KEY_DBPATH = "DB_PATH";

    private static final String KEY_DBPASS = "DB_PASS";

    private final String dbPath;

    private final String dbPassword;


    public MehDbState(Builder builder) {
        this.dbPath = builder.dbPath;
        this.dbPassword = builder.dbPassword;
    }

    public String getDbPath() {
        return dbPath;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public static class Builder {
        private String dbPath = "";
        private String dbPassword = "";

        public Builder() {}

        public Builder(MehDbState state) {
            this.dbPath = state.dbPath;
            this.dbPassword = state.dbPassword;
        }

        public Builder dbPath(String dbPath) {
            this.dbPath = dbPath;
            return this;
        }

        public Builder dbPassword(String dbPassword) {
            this.dbPassword = dbPassword;
            return this;
        }

        public MehDbState build() {
            return new MehDbState(this);
        }

    }
}
