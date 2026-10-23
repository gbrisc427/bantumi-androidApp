package es.upm.miw.bantumi.datos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ResultadoDao {

    @Insert
    long insert(Resultado resultado);

    @Query("SELECT * FROM resultados ORDER BY CASE WHEN semillas_j1 > semillas_j2 THEN semillas_j1 ELSE semillas_j2 END DESC LIMIT 10")
    List<Resultado> getTop10();

    @Query("SELECT * FROM resultados ORDER BY semillas_j1 DESC LIMIT 10")
    List<Resultado> getTop10PorJugador1();

    @Query("SELECT * FROM resultados WHERE nombre_jugador LIKE '%' || :nombre || '%' ORDER BY CASE WHEN semillas_j1 > semillas_j2 THEN semillas_j1 ELSE semillas_j2 END DESC LIMIT 10")
    List<Resultado> getResultadosPorNombre(String nombre);

    @Query("DELETE FROM resultados")
    void deleteAll();
}