package myeighthours;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;


public class Fichaje {

    private static final Logger LOG = LoggerFactory.getLogger(Fichaje.class);

    public static final int TERMINAL_PRODUCCION = -1;

    public static final int TERMINAL_PARKING = 1;

    public static final int TERMINAL_CANTINA = 3;

    public static final int TERMINAL_ID = 4;

    public enum TIPO {
        DESCONOCIDO(-1),
        RELOJ(0),
        AUTOGENERADO(1);

        private final int id;

        TIPO(final int id) {
            this.id = id;
        }

        public int getId() { return id; }

        public static TIPO fromId(int id) {
            TIPO[] enumValues = TIPO.values();
            TIPO foundEnum = null;
            for(int i = 0; i < enumValues.length; i++)
            {
                if(enumValues[i].getId() == id) foundEnum = enumValues[i];
            }
            if (foundEnum==null) throw new IllegalArgumentException("Invalid id for TIPO. id="+id);
            return foundEnum;
        }

    }

    public enum ESTADO {
        DESCONOCIDO(-1),
        PENDIENTE(0);

        private final int id;

        ESTADO(final int id) {
            this.id = id;
        }

        public int getId() { return id; }

        public static ESTADO fromId(int id) {
            ESTADO[] enumValues = ESTADO.values();
            ESTADO foundEnum = null;
            for(int i = 0; i < enumValues.length; i++)
            {
                if(enumValues[i].getId() == id) foundEnum = enumValues[i];
            }
            if (foundEnum==null) throw new IllegalArgumentException("Invalid id for ESTADO. id="+id);
            return foundEnum;
        }
    }

    public enum DIRECCION {
        DESCONOCIDO(-1),
        ENTRADA(0),
        SALIDA(1);

        private final int id;

        DIRECCION(final int id) {
            this.id = id;
        }

        public int getId() { return id; }

        public static DIRECCION fromId(int id) {
            DIRECCION[] enumValues = DIRECCION.values();
            DIRECCION foundEnum = null;
            for(int i = 0; i < enumValues.length; i++)
            {
                if(enumValues[i].getId() == id) foundEnum = enumValues[i];
            }
            if (foundEnum==null) throw new IllegalArgumentException("Invalid id for DIRECCION. id="+id);
            return foundEnum;
        }
    }

    private final long id;
    private final long fechaMarcaje;
    private final DIRECCION direccion;
    private final ESTADO estado;
    private final TIPO tipo;
    private final int terminal;
    private final boolean sincronizado;

    private Fichaje(Builder builder) {
        this.id = builder.id;
        this.fechaMarcaje = builder.fechaMarcaje;
        this.direccion = builder.direccion;
        this.estado = builder.estado;
        this.tipo = builder.tipo;
        this.terminal = builder.terminal;
        this.sincronizado = builder.sincronizado;
    }

    public long getId() {
        return id;
    }

    public long getFechaMarcaje() {
        return fechaMarcaje;
    }

    public boolean isCanteenTerminal() {
        return terminal == TERMINAL_CANTINA;
    }

    public DIRECCION getDireccion() {
        return direccion;
    }

    public ESTADO getEstado() {
        return estado;
    }

    public TIPO getTipo() {
        return tipo;
    }

    public int getTerminal() {
        return terminal;
    }

    public boolean isSincronizado() {
        return sincronizado;
    }

    public static class Builder {

        private long id;
        private long fechaMarcaje;
        private DIRECCION direccion = DIRECCION.DESCONOCIDO;
        private ESTADO estado = ESTADO.DESCONOCIDO;
        private TIPO tipo = TIPO.DESCONOCIDO;
        private int terminal;
        private boolean sincronizado = true;

        public Builder() {
        }

        public Builder(Fichaje builder) {
            this.id = builder.id;
            this.fechaMarcaje = builder.fechaMarcaje;
            this.direccion = builder.direccion;
            this.estado = builder.estado;
            this.tipo = builder.tipo;
            this.terminal = builder.terminal;
            this.sincronizado = builder.sincronizado;
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder fechaMarcaje(long fechaMarcaje) {
            this.fechaMarcaje = fechaMarcaje;
            return this;
        }

        public Builder direccion(DIRECCION direccion) {
            this.direccion = direccion;
            return this;
        }

        public Builder estado(ESTADO estado) {
            this.estado = estado;
            return this;
        }

        public Builder tipo(TIPO tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder terminal(int terminal) {
            this.terminal = terminal;
            return this;
        }

        public Builder sincronizado(boolean sincronizado) {
            this.sincronizado = sincronizado;
            return this;
        }

        public Fichaje build() {
            return new Fichaje(this);
        }
    }


    @Override
    public String toString() {
        Instant fechaMarcajeInstant = Instant.ofEpochMilli(fechaMarcaje);
        return "Fichaje{" +
                "id=" + id + ", " +
                "fechaMarcaje=" + fechaMarcajeInstant + ", " +
                "direccion=" + direccion + ", " +
                "terminal=" + terminal + ", " +
                "sincronizado=" + sincronizado +
                '}';
    }
}
