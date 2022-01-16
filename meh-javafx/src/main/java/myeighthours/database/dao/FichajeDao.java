package myeighthours.database.dao;


import myeighthours.Fichaje;

import java.util.List;

public interface FichajeDao {

    String TBL = "FICHAJE";

    String COL_ID = "ID";

    String COL_FECHAMARCAJE = "FECHAMARCAJE";

    String COL_DIRECCION = "DIRECCION";

    String COL_ESTADO = "ESTADO";

    String COL_TIPO = "TIPO";

    String COL_TERMINAL = "TERMINAL";

    String COL_SINCRONIZADO = "SINCRONIZADO";

    Fichaje select(Fichaje fichaje) throws Exception;

    List<Fichaje> selectAll() throws Exception;

    List<Fichaje> selectAll(int limit, int offset) throws Exception;

    List<Fichaje> selectPages(int rows, int page) throws Exception;

    List<Fichaje> selectTimeRange(long timestampStart, long timestampEnd) throws Exception;

    Fichaje insert(Fichaje fichaje) throws Exception;

    void delete(Fichaje fichaje) throws Exception;

    void update(Fichaje fichaje) throws Exception;

}
