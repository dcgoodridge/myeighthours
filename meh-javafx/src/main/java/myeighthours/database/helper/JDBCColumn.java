package myeighthours.database.helper;

/**
 * Sirve para especificar una Columna en entorno SQL con tipos JDBC y generar las cadenas SQL apropiadas.
 */
public class JDBCColumn {

    private final String name;

    private final TYPE type;

    private final String typeValue;

    private final String typeDefault;

    private final boolean notnull;

    private final boolean primary;

    private final boolean autoincrement;

    private JDBCColumn(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.typeValue = builder.typeValue;
        this.typeDefault = builder.typeDefault;
        this.notnull = builder.notnull;
        this.primary = builder.primary;
        this.autoincrement = builder.autoincrement;
    }

    public String getName() {
        return this.name;
    }

    public TYPE getType() {
        return type;
    }

    public String getTypeValue() {
        return typeValue;
    }

    public String getTypeDefault() {
        return typeDefault;
    }

    public boolean isNotnull() {
        return notnull;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isAutoincrement() {
        return autoincrement;
    }

    // https://db.apache.org/ojb/docu/guides/jdbc-types.html
    public enum TYPE {
        VARCHAR,
        /**
         * En H2: Mapeado a Int
         */
        INTEGER,
        /**
         * En H2: Mapeado a Long
         */
        BIGINT,
        TEXT,
        BLOB,
    }

    public static class Builder {
        private String name;
        private TYPE type;
        private String typeValue = null;
        private String typeDefault = null;
        private boolean notnull = false;
        private boolean primary = false;
        private boolean autoincrement = false;

        public Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(TYPE type) {
            this.type = type;
            return this;
        }

        public Builder typeValue(String typeValue) {
            this.typeValue = typeValue;
            return this;
        }

        public Builder typeDefault(String typeDefault) {
            this.typeDefault = typeDefault;
            return this;
        }

        public Builder notnull() {
            this.notnull = true;
            return this;
        }

        public Builder primary() {
            this.primary = true;
            return this;
        }

        public Builder autoincrement() {
            this.autoincrement = true;
            return this;
        }

        public JDBCColumn build() {
            return new JDBCColumn(this);
        }
    }

}
