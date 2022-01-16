package myeighthours.database.helper;

import java.util.*;


public class JDBCColumnList {

    private List<JDBCColumn> columnsOrdered;

    private Map<String, Integer> columnsMap;

    public JDBCColumnList() {
        columnsOrdered = new ArrayList<>();
        columnsMap = new HashMap<>();
    }

    public void add(JDBCColumn column) {
        columnsOrdered.add(column);
        int position = columnsOrdered.size() - 1;
        columnsMap.put(column.getName(), position);
    }

    public JDBCColumn get(String keyString) {
        Integer position = columnsMap.get(keyString);
        if (position == null) throw new RuntimeException("Column with key = \"" + keyString + "\" does not exist.");
        JDBCColumn column = columnsOrdered.get(position);
        return column;
    }

    public int size() {
        return this.columnsOrdered.size();
    }

    public Iterator<JDBCColumn> iterator() {
        return this.columnsOrdered.iterator();
    }

}
