package es.upm.miw.bantumi.ui.actividades;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import es.upm.miw.bantumi.datos.Resultado;
import es.upm.miw.bantumi.datos.ResultadoRepository;

import es.upm.miw.bantumi.ui.fragmentos.FinalAlertDialog;
import es.upm.miw.bantumi.ui.fragmentos.RecuperarAlertDialog;
import es.upm.miw.bantumi.ui.fragmentos.ReiniciarAlertDialog;
import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.dominio.logica.JuegoBantumi;
import es.upm.miw.bantumi.ui.viewmodel.BantumiViewModel;

public class MainActivity extends AppCompatActivity {

    protected final String LOG_TAG = "MiW";
    public JuegoBantumi juegoBantumi;
    private BantumiViewModel bantumiVM;
    int numInicialSemillas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Instancia el ViewModel y el juego, y asigna observadores a los huecos
        bantumiVM = new ViewModelProvider(this).get(BantumiViewModel.class);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String strSemillas = pref.getString(getString(R.string.prefClaveNumSemillas), getString(R.string.prefDefaultNumSemillas));
        try {
            numInicialSemillas = Integer.parseInt(strSemillas);
        } catch (NumberFormatException e) {
            numInicialSemillas = getResources().getInteger(R.integer.intNumInicialSemillas);
        }
        String primerTurnoStr = pref.getString(getString(R.string.prefClavePrimerTurno), getString(R.string.prefDefaultPrimerTurno));
        JuegoBantumi.Turno turnoInicial = "J2".equals(primerTurnoStr) ? JuegoBantumi.Turno.turnoJ2 : JuegoBantumi.Turno.turnoJ1;

        juegoBantumi = new JuegoBantumi(bantumiVM, turnoInicial, numInicialSemillas);
        crearObservadores();
        actualizarNombreJugador();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarNombreJugador();
    }

    private void actualizarNombreJugador() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String nombre = pref.getString(
                getString(R.string.prefClaveNombreJugador),
                getString(R.string.prefDefaultNombreJugador)
        );
        TextView tvPlayer1 = findViewById(R.id.tvPlayer1);
        if (tvPlayer1 != null && nombre != null && !nombre.trim().isEmpty()) {
            tvPlayer1.setText(nombre);
        }
    }

    public void reiniciarPartidaConPreferencias() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String strSemillas = pref.getString(getString(R.string.prefClaveNumSemillas), getString(R.string.prefDefaultNumSemillas));
        try {
            numInicialSemillas = Integer.parseInt(strSemillas);
        } catch (NumberFormatException e) {
            numInicialSemillas = getResources().getInteger(R.integer.intNumInicialSemillas);
        }
        String primerTurnoStr = pref.getString(getString(R.string.prefClavePrimerTurno), getString(R.string.prefDefaultPrimerTurno));
        JuegoBantumi.Turno turnoInicial = "J2".equals(primerTurnoStr) ? JuegoBantumi.Turno.turnoJ2 : JuegoBantumi.Turno.turnoJ1;

        juegoBantumi.inicializar(turnoInicial, numInicialSemillas);
    }

    /**
     * Crea y subscribe los observadores asignados a las posiciones del tablero.
     * Si se modifica el contenido del tablero -> se actualiza la vista.
     */
    private void crearObservadores() {
        for (int i = 0; i < JuegoBantumi.NUM_POSICIONES; i++) {
            int finalI = i;
            bantumiVM.getNumSemillas(i).observe(    // Huecos y almacenes
                    this,
                    new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            mostrarValor(finalI, juegoBantumi.getSemillas(finalI));
                        }
                    });
        }
        bantumiVM.getTurno().observe(   // Turno
                this,
                new Observer<JuegoBantumi.Turno>() {
                    @Override
                    public void onChanged(JuegoBantumi.Turno turno) {
                        marcarTurno(juegoBantumi.turnoActual());
                    }
                }
        );
    }

    /**
     * Indica el turno actual cambiando el color del texto
     *
     * @param turnoActual turno actual
     */
    private void marcarTurno(@NonNull JuegoBantumi.Turno turnoActual) {
        TextView tvJugador1 = findViewById(R.id.tvPlayer1);
        TextView tvJugador2 = findViewById(R.id.tvPlayer2);
        switch (turnoActual) {
            case turnoJ1:
                tvJugador1.setTextColor(getColor(R.color.white));
                tvJugador1.setBackgroundColor(getColor(android.R.color.holo_blue_light));
                tvJugador2.setTextColor(getColor(R.color.black));
                tvJugador2.setBackgroundColor(getColor(R.color.white));
                break;
            case turnoJ2:
                tvJugador1.setTextColor(getColor(R.color.black));
                tvJugador1.setBackgroundColor(getColor(R.color.white));
                tvJugador2.setTextColor(getColor(R.color.white));
                tvJugador2.setBackgroundColor(getColor(android.R.color.holo_blue_light));
                break;
            default:
                tvJugador1.setTextColor(getColor(R.color.black));
                tvJugador2.setTextColor(getColor(R.color.black));
        }
    }

    /**
     * Muestra el valor <i>valor</i> en la posición <i>pos</i>
     *
     * @param pos posición a actualizar
     * @param valor valor a mostrar
     */
    private void mostrarValor(int pos, int valor) {
        String num2digitos = String.format(Locale.getDefault(), "%02d", pos);
        // Los identificadores de los huecos tienen el formato casilla_XX
        int idBoton = getResources().getIdentifier("casilla_" + num2digitos, "id", getPackageName());
        if (0 != idBoton) {
            TextView viewHueco = findViewById(idBoton);
            viewHueco.setText(String.valueOf(valor));
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.opciones_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.opcAcercaDe) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.aboutTitle)
                    .setMessage(R.string.aboutMessage)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        } else if (itemId == R.id.opcReiniciarPartida) {
            new ReiniciarAlertDialog().show(getSupportFragmentManager(), "ALERT_DIALOG_REINICIAR");
        } else if (itemId == R.id.opcGuardarPartida) {
            guardarPartida();
        } else if (itemId == R.id.opcRecuperarPartida) {
            recuperarPartida();
        } else if (itemId == R.id.opcMejoresResultados) {
            startActivity(new Intent(this, MejoresResultadosActivity.class));
        } else if (itemId == R.id.opcAjustes) {
            startActivity(new Intent(this, BantumiPrefsActivity.class));
        } else {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.txtSinImplementar),
                    Snackbar.LENGTH_LONG
            ).show();
        }
        return true;
    }

    private static final String FICHERO_PARTIDA = "partida_guardada.txt";

    public void guardarPartida() {
        try (FileOutputStream fos = openFileOutput(FICHERO_PARTIDA, Context.MODE_PRIVATE)) {
            String datos = juegoBantumi.serializa();
            fos.write(datos.getBytes());
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.txtPartidaGuardadaOK), Snackbar.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error al guardar partida", e);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.txtPartidaErrorGuardar), Snackbar.LENGTH_LONG).show();
        }
    }

    public void recuperarPartida() {
        File file = getFileStreamPath(FICHERO_PARTIDA);
        if (!file.exists()) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.txtPartidaNoExiste), Snackbar.LENGTH_LONG).show();
            return;
        }

        if (juegoBantumi.estaModificado()) {
            new RecuperarAlertDialog().show(getSupportFragmentManager(), "ALERT_DIALOG_RECUPERAR");
        } else {
            recuperarPartidaConfirmado();
        }
    }

    public void recuperarPartidaConfirmado() {
        try (BufferedReader fin = new BufferedReader(new InputStreamReader(openFileInput(FICHERO_PARTIDA)))) {
            String linea = fin.readLine();
            if (linea != null) {
                juegoBantumi.deserializa(linea);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.txtPartidaRecuperadaOK), Snackbar.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error al recuperar partida", e);
        }
    }

    /**
     * Acción que se ejecuta al pulsar sobre cualquier hueco
     *
     * @param v Vista pulsada (hueco)
     */
    public void huecoPulsado(@NonNull View v) {
        String resourceName = getResources().getResourceEntryName(v.getId()); // pXY
        int num = Integer.parseInt(resourceName.substring(resourceName.length() - 2));
        Log.i(LOG_TAG, "huecoPulsado(" + resourceName + ") num=" + num);
        switch (juegoBantumi.turnoActual()) {
            case turnoJ1:
                Log.i(LOG_TAG, "* Juega Jugador");
                juegoBantumi.jugar(num);
                break;
            case turnoJ2:
                Log.i(LOG_TAG, "* Juega Computador");
                juegoBantumi.juegaComputador();
                break;
            default:    // JUEGO TERMINADO
                finJuego();
        }
        if (juegoBantumi.juegoTerminado()) {
            finJuego();
        }
    }

    /**
     * El juego ha terminado. Volver a jugar?
     */
    private void finJuego() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String nombreJugador = pref.getString(
                getString(R.string.prefClaveNombreJugador),
                getString(R.string.prefDefaultNombreJugador)
        );

        int semillasJ1 = juegoBantumi.getSemillas(6);
        int semillasJ2 = juegoBantumi.getSemillas(13);

        String texto;
        if (semillasJ1 > semillasJ2) {
            texto = "Gana " + nombreJugador;
        } else if (semillasJ2 > semillasJ1) {
            texto = "Gana Jugador 2";
        } else {
            texto = "¡¡¡ EMPATE !!!";
        }

        guardarPuntuacionBD();

        // terminar
        new FinalAlertDialog(texto).show(getSupportFragmentManager(), "ALERT_DIALOG");
    }

    private void guardarPuntuacionBD() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String nombreJugador = pref.getString(
                getString(R.string.prefClaveNombreJugador),
                getString(R.string.prefDefaultNombreJugador)
        );

        String fechaActual = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        int semillasJ1 = juegoBantumi.getSemillas(6);
        int semillasJ2 = juegoBantumi.getSemillas(13);

        Resultado resultado = new Resultado(nombreJugador, fechaActual, semillasJ1, semillasJ2);
        ResultadoRepository repository = new ResultadoRepository(this);
        repository.insert(resultado);
    }
}