package myeighthours.database.helper;


import java.util.Iterator;


public class JDBCTable {

    public enum CONNECTION_TYPE {JDBC, ANDROIDSQLITE}

    private static final CONNECTION_TYPE connectionType = CONNECTION_TYPE.JDBC;

    private JDBCColumnList columns;

    private String name;

    public JDBCTable(String name) {
        this.name = name;
        columns = new JDBCColumnList();
    }

    public String getName() {
        return this.name;
    }

    public JDBCColumn getColumn(String keyString) {
        return this.columns.get(keyString);
    }

    public void addColumn(JDBCColumn column) {
        this.columns.add(column);
    }

    public String getSQL() {
        Iterator<JDBCColumn> iterator = columns.iterator();
        JDBCColumn firstColumn = iterator.next();
        String columnsSQL = getColumnSQL(firstColumn);
        while (iterator.hasNext()) {
            columnsSQL += ", " + getColumnSQL(iterator.next());
        }
        String sql = "CREATE TABLE IF NOT EXISTS " + getName() + "(" + columnsSQL + ");";
        return sql;
    }

    private String getAutoincrementKeyword(CONNECTION_TYPE connectionType){
        String keyword = "";
        switch (connectionType){
            case ANDROIDSQLITE:
                keyword = "AUTOINCREMENT";
                break;
            case JDBC:
                keyword = "AUTO_INCREMENT";
                break;
        }
        return keyword;
    }

    private String getColumnSQL(JDBCColumn column) {
        String name = column.getName();
        JDBCColumn.TYPE type = column.getType();
        String typeValue = column.getTypeValue();
        boolean primary = column.isPrimary();
        boolean autoincrement = column.isAutoincrement();
        boolean notnull = column.isNotnull();
        String typeDefault = column.getTypeDefault();
        String sql = name + " " + type;
        if (typeValue != null) sql += "(" + typeValue + ")";
        if (primary) sql += " PRIMARY KEY";
        if (autoincrement) sql += " " + getAutoincrementKeyword(connectionType);
        if (notnull) sql += " NOT NULL";
        if (typeDefault != null) sql += " DEFAULT \'" + typeDefault + "\'";
        return sql;
    }

}