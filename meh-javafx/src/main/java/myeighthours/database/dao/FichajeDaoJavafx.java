package myeighthours.database.dao;

import myeighthours.Fichaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class FichajeDaoJavafx implements FichajeDao {

    private static final Logger LOG = LoggerFactory.getLogger(FichajeDaoJavafx.class);

    private Connection connection;

    private static final int SELECT_LIMIT = 50;

    public FichajeDaoJavafx(Connection connection) {
        this.connection = connection;
    }


    @Override
    public Fichaje select(Fichaje fichaje) throws Exception {
        Fichaje fichajeSelected = null;
        String sql = "SELECT * FROM " + TBL + " WHERE id=" + fichaje.getId();
        try (Statement stmt = connection.createStatement();ResultSet rs = stmt.executeQuery(sql);){
            while (rs.next()) {
                fichajeSelected = resultsetToObject(rs);
            }
        }
        return fichajeSelected;
    }

    @Override
    public List<Fichaje> selectAll() throws Exception {
        return selectAll(SELECT_LIMIT, 0);
    }

    @Override
    public List<Fichaje> selectAll(int limit, int offset) throws Exception {
        LOG.debug("selectAll("+limit+","+offset+")");
        List<Fichaje> fichajeList = new ArrayList<>();
        String sql = "SELECT * FROM " + TBL + " ORDER BY " + COL_FECHAMARCAJE + " DESC LIMIT "+limit+ " OFFSET "+offset+";";
        try(Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql);){
            while (rs.next()) {
                Fichaje fichaje = resultsetToObject(rs);
                fichajeList.add(fichaje);
            }
        }
        return fichajeList;
    }

    @Override
    public List<Fichaje> selectPages(int rows, int page) throws Exception {
        int offset = page*rows;
        return selectAll(rows, offset);
    }

    @Override
    public List<Fichaje> selectTimeRange(long timestampStart, long timestampEnd) throws Exception {
        LOG.debug("selectTimeRange("+timestampStart+","+timestampEnd+")");
        List<Fichaje> fichajeList = new ArrayList<>();
        String sql = "SELECT * FROM " + TBL + " WHERE "+COL_FECHAMARCAJE+">="+timestampStart+" AND "+COL_FECHAMARCAJE+"<="+timestampEnd+" ORDER BY " + COL_FECHAMARCAJE + " DESC;";
        LOG.debug(sql);
        try(Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql);){
            while (rs.next()) {
                Fichaje fichaje = resultsetToObject(rs);
                fichajeList.add(fichaje);
            }
        }
        return fichajeList;
    }

    @Override
    public Fichaje insert(Fichaje fichaje) throws Exception {
        String sql = "INSERT INTO " + TBL + " " +
                "(" +
                COL_FECHAMARCAJE + "," +
                COL_DIRECCION + "," +
                COL_ESTADO + "," +
                COL_TIPO + "," +
                COL_TERMINAL + "," +
                COL_SINCRONIZADO + ")" +
                "VALUES (" +
                fichaje.getFechaMarcaje() + "," +
                fichaje.getDireccion().getId() + "," +
                fichaje.getEstado().getId() + "," +
                fichaje.getTipo().getId() + "," +
                fichaje.getTerminal() + "," +
                fichaje.isSincronizado() + ")";
        long insertedId = -1;
        try(Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    insertedId = rs.getLong(1);
                }
            }
        }
        return new Fichaje.Builder(fichaje).id(insertedId).build();
    }

    @Override
    public void delete(Fichaje fichaje) throws Exception {
        String sql = "DELETE FROM " + TBL + " " +
                "WHERE " + COL_ID + " = " + fichaje.getId();
        try (Statement stmt = connection.createStatement();){
            stmt.executeUpdate(sql);
        }
    }

    @Override
    public void update(Fichaje fichaje) throws Exception {
        String updateTableSQL = "UPDATE " + TBL + " SET " +
                COL_FECHAMARCAJE + " = ?, " +
                COL_DIRECCION + " = ?, " +
                COL_ESTADO + " = ?, " +
                COL_TIPO + " = ?, " +
                COL_TERMINAL + " = ?, " +
                COL_SINCRONIZADO + " = ? " +
                " WHERE " + COL_ID + " = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateTableSQL);){
            int i = 1;
            preparedStatement.setLong(i++, fichaje.getFechaMarcaje());
            preparedStatement.setInt(i++, fichaje.getDireccion().getId());
            preparedStatement.setInt(i++, fichaje.getEstado().getId());
            preparedStatement.setInt(i++, fichaje.getTipo().getId());
            preparedStatement.setInt(i++, fichaje.getTerminal());
            preparedStatement.setBoolean(i++, fichaje.isSincronizado());
            preparedStatement.setLong(i++, fichaje.getId());
            LOG.debug(preparedStatement.toString());
            preparedStatement.executeUpdate();
        }
    }

    private Fichaje resultsetToObject(ResultSet rs) throws Exception {
        long id = rs.getLong(COL_ID);
        long fechamarcaje = rs.getLong(COL_FECHAMARCAJE);
        int direccionId = rs.getInt(COL_DIRECCION);
        int estadoId = rs.getInt(COL_ESTADO);
        int tipoId = rs.getInt(COL_TIPO);
        int terminal = rs.getInt(COL_TERMINAL);
        boolean sincronizado = rs.getBoolean(COL_SINCRONIZADO);

        Fichaje fichaje = new Fichaje.Builder()
                .id(id)
                .fechaMarcaje(fechamarcaje)
                .direccion(Fichaje.DIRECCION.fromId(direccionId))
                .estado(Fichaje.ESTADO.fromId(estadoId))
                .tipo(Fichaje.TIPO.fromId(tipoId))
                .terminal(terminal)
                .sincronizado(sincronizado)
                .build();
        return fichaje;
    }
}
