package myeighthours.database.schema;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MehDbSchema {

    private static final int VERSION = 2;

    private static final Logger LOG = LoggerFactory.getLogger(MehDbSchema.class);

    public MehDbSchema() {
    }

    public int getVersion() {
        return VERSION;
    }

    public List<String> getSqlCreateSchema() throws Exception {
        List<String> sqlList = new ArrayList<>();
        sqlList.addAll(getSqlFirstVersionCreateSchema());
        sqlList.addAll(getSqlUpdateSchema(1, VERSION));
        return sqlList;
    }

    private List<String> getSqlFirstVersionCreateSchema() throws Exception {
        String sqlFilename = "meh.h2.sql";
        InputStream inputStream = getClass().getResourceAsStream(sqlFilename);
        if (inputStream == null) throw new Exception("File \"" + sqlFilename + "\" not found");
        List<String> sqlList = readSql(inputStream);
        return sqlList;
    }

    public List<String> getSqlUpdateSchema(long oldVersion, long newVersion) throws Exception {
        List<String> sqlList = new ArrayList<>();
        long i = oldVersion + 1;
        while (i <= newVersion) {
            String sqlFilename = "meh.h2.changelog-" + i + ".sql";
            InputStream inputStream = getClass().getResourceAsStream(sqlFilename);
            if (inputStream == null) throw new Exception("File \"" + sqlFilename + "\" not found");
            List<String> currentChangelogSqlList = readSql(inputStream);
            sqlList.addAll(currentChangelogSqlList);
            i++;
        }
        return sqlList;
    }

    private static List<String> readSql(InputStream in) {
        List<String> sqlList = new ArrayList<>();
        Scanner s = new Scanner(in);
        s.useDelimiter("(;(\r)?\n)|(--\n)");
        while (s.hasNext()) {
            String line = s.next();
            if (line.startsWith("/*!") && line.endsWith("*/")) {
                int i = line.indexOf(' ');
                line = line.substring(i + 1, line.length() - " */".length());
            }
            if (line.trim().length() > 0) {
                sqlList.add(line);
            }
        }
        return sqlList;
    }

}
