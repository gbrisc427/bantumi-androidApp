package es.upm.miw.bantumi.datos;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "resultados")
public class Resultado {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "nombre_jugador")
    public String nombreJugador;

    @ColumnInfo(name = "fecha")
    public String fecha;

    @ColumnInfo(name = "semillas_j1")
    public int semillasJ1;

    @ColumnInfo(name = "semillas_j2")
    public int semillasJ2;

    public Resultado() {
    }

    public Resultado(String nombreJugador, String fecha, int semillasJ1, int semillasJ2) {
        this.nombreJugador = nombreJugador;
        this.fecha = fecha;
        this.semillasJ1 = semillasJ1;
        this.semillasJ2 = semillasJ2;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getSemillasJ1() {
        return semillasJ1;
    }

    public void setSemillasJ1(int semillasJ1) {
        this.semillasJ1 = semillasJ1;
    }

    public int getSemillasJ2() {
        return semillasJ2;
    }

    public void setSemillasJ2(int semillasJ2) {
        this.semillasJ2 = semillasJ2;
    }

    public int getMaxSemillas() {
        return Math.max(semillasJ1, semillasJ2);
    }
}