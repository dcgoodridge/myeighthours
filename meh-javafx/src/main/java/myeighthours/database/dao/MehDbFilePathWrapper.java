package myeighthours.database.dao;

import org.h2.store.fs.FilePath;
import org.h2.store.fs.FilePathWrapper;

public class MehDbFilePathWrapper extends FilePathWrapper {

    public static final String SCHEME = "mehdb";

    private static final String[][] MAPPING = {
            {".mv.db", ".mehdb"}, //Necesario en modo MVStore storage
            {".h2.db", ".mehdb"}, //Necesario en modo MVStore desactivado
            {".lock.db", ".mehdb.lock"}
    };

    @Override
    public String getScheme() {
        return SCHEME;
    }

    @Override
    public FilePathWrapper wrap(FilePath base) {
        MehDbFilePathWrapper wrapper = (MehDbFilePathWrapper) super.wrap(base);
        wrapper.name = getPrefix() + wrapExtension(base.toString());
        return wrapper;
    }

    @Override
    protected FilePath unwrap(String path) {
        String newName = path.substring(getScheme().length() + 1);
        newName = unwrapExtension(newName);
        return FilePath.get(newName);
    }

    protected static String wrapExtension(String fileName) {
        String output = fileName;
        for (String[] pair : MAPPING) {
            if (fileName.endsWith(pair[1])) {
                output = fileName.substring(0, fileName.length() - pair[1].length()) + pair[0];
                break;
            }
        }
        return output;
    }

    protected static String unwrapExtension(String fileName) {
        String output = fileName;
        for (String[] pair : MAPPING) {
            if (fileName.endsWith(pair[0])) {
                output = fileName.substring(0, fileName.length() - pair[0].length()) + pair[1];
                break;
            }
        }
        return output;
    }

}
