package es.upm.miw.bantumi.ui.actividades;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.os.SystemClock;
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
import java.util.Arrays;
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
    private Chronometer cronometro;
    private boolean cronometroIniciado = false;
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
        aplicarColorTema();

        cronometro = findViewById(R.id.cronometro);
        resetCronometro();
        if (turnoInicial == JuegoBantumi.Turno.turnoJ2) {
            iniciarCronometroSiNoIniciado();
        }
    }

    public void iniciarCronometroSiNoIniciado() {
        if (!cronometroIniciado && cronometro != null) {
            cronometro.setBase(SystemClock.elapsedRealtime());
            cronometro.start();
            cronometroIniciado = true;
        }
    }

    public void resetCronometro() {
        cronometroIniciado = false;
        if (cronometro != null) {
            cronometro.stop();
            cronometro.setBase(SystemClock.elapsedRealtime());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarNombreJugador();
        aplicarColorTema();
    }

    private void aplicarColorTema() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String colorTheme = pref.getString(getString(R.string.prefClaveColor), getString(R.string.prefDefaultColor));

        int storeColor;
        int[] buttonColorsJ1 = new int[6];
        int[] buttonColorsJ2 = new int[6];

        switch (colorTheme) {
            case "red":
                storeColor = Color.parseColor("#D32F2F"); // Red 700
                int redJ1 = Color.parseColor("#E57373");   // Red 300
                int redJ2 = Color.parseColor("#C62828");   // Red 800
                Arrays.fill(buttonColorsJ1, redJ1);
                Arrays.fill(buttonColorsJ2, redJ2);
                break;
            case "green":
                storeColor = Color.parseColor("#2E7D32"); // Green 800
                int greenJ1 = Color.parseColor("#81C784"); // Green 300
                int greenJ2 = Color.parseColor("#1B5E20"); // Green 900
                Arrays.fill(buttonColorsJ1, greenJ1);
                Arrays.fill(buttonColorsJ2, greenJ2);
                break;
            case "purple":
                storeColor = Color.parseColor("#6A1B9A"); // Purple 800
                int purpleJ1 = Color.parseColor("#BA68C8"); // Purple 300
                int purpleJ2 = Color.parseColor("#4A148C"); // Purple 900
                Arrays.fill(buttonColorsJ1, purpleJ1);
                Arrays.fill(buttonColorsJ2, purpleJ2);
                break;
            case "multicolor":
                storeColor = Color.parseColor("#E91E63"); // Pink 500
                int[] multiColors = new int[]{
                        Color.parseColor("#F44336"), // Red
                        Color.parseColor("#FF9800"), // Orange
                        Color.parseColor("#FFEB3B"), // Yellow
                        Color.parseColor("#4CAF50"), // Green
                        Color.parseColor("#2196F3"), // Blue
                        Color.parseColor("#9C27B0")  // Purple
                };
                for (int i = 0; i < 6; i++) {
                    buttonColorsJ1[i] = multiColors[i];
                    buttonColorsJ2[i] = multiColors[5 - i];
                }
                break;
            case "standard":
            default:
                storeColor = getColor(R.color.teal_700);
                int defaultColor = Color.parseColor("#8A5000");
                Arrays.fill(buttonColorsJ1, defaultColor);
                Arrays.fill(buttonColorsJ2, defaultColor);
                break;
        }

        // Aplicar color a los depósitos y cronómetro
        TextView store06 = findViewById(R.id.casilla_06);
        TextView store13 = findViewById(R.id.casilla_13);
        if (store06 != null) store06.setTextColor(storeColor);
        if (store13 != null) store13.setTextColor(storeColor);
        if (cronometro != null) cronometro.setTextColor(storeColor);

        // Aplicar color a las casillas de Jugador 1 (00 .. 05)
        for (int i = 0; i < 6; i++) {
            String idBotonStr = String.format(Locale.getDefault(), "casilla_%02d", i);
            int idBoton = getResources().getIdentifier(idBotonStr, "id", getPackageName());
            if (idBoton != 0) {
                View btn = findViewById(idBoton);
                if (btn != null) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(buttonColorsJ1[i]));
                }
            }
        }

        // Aplicar color a las casillas de Jugador 2 (07 .. 12)
        for (int i = 7; i <= 12; i++) {
            String idBotonStr = String.format(Locale.getDefault(), "casilla_%02d", i);
            int idBoton = getResources().getIdentifier(idBotonStr, "id", getPackageName());
            if (idBoton != 0) {
                View btn = findViewById(idBoton);
                if (btn != null) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(buttonColorsJ2[i - 7]));
                }
            }
        }
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

        resetCronometro();

        juegoBantumi.inicializar(turnoInicial, numInicialSemillas);
        if (turnoInicial == JuegoBantumi.Turno.turnoJ2) {
            iniciarCronometroSiNoIniciado();
        }
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
            guardarPartidaConNombre();
        } else if (itemId == R.id.opcRecuperarPartida) {
            recuperarPartidaSeleccion();
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

    public void guardarPartidaConNombre() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.txtOpcionGuardar);

        final EditText input = new EditText(this);
        input.setHint("Nombre de la partida");
        String defaultName = "Partida " + new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date());
        input.setText(defaultName);
        builder.setView(input);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String nombrePartida = input.getText().toString().trim();
            if (nombrePartida.isEmpty()) {
                nombrePartida = defaultName;
            }
            String nombreFichero = "partida_" + nombrePartida.replaceAll("[^a-zA-Z0-9_-]", "_") + ".txt";
            try (FileOutputStream fos = openFileOutput(nombreFichero, Context.MODE_PRIVATE)) {
                String datos = juegoBantumi.serializa() + "\n" + (cronometro != null ? cronometro.getText().toString() : "00:00");
                fos.write(datos.getBytes());
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.txtPartidaGuardadaOK), Snackbar.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error al guardar partida", e);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.txtPartidaErrorGuardar), Snackbar.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    public void recuperarPartidaSeleccion() {
        File filesDir = getFilesDir();
        File[] matchingFiles = filesDir.listFiles((dir, name) -> name.startsWith("partida_") && name.endsWith(".txt"));

        if (matchingFiles == null || matchingFiles.length == 0) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.txtPartidaNoExiste), Snackbar.LENGTH_LONG).show();
            return;
        }

        String[] nombresPartidas = new String[matchingFiles.length];
        for (int i = 0; i < matchingFiles.length; i++) {
            String fname = matchingFiles[i].getName();
            nombresPartidas[i] = fname.substring(8, fname.length() - 4).replace("_", " ");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.txtOpcionRecuperar);
        builder.setItems(nombresPartidas, (dialog, which) -> {
            File selectedFile = matchingFiles[which];
            if (juegoBantumi.estaModificado()) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.txtDialogoRecuperarTitulo)
                        .setMessage(R.string.txtDialogoRecuperarPregunta)
                        .setPositiveButton(android.R.string.ok, (d, w) -> cargarPartidaDesdeFichero(selectedFile))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            } else {
                cargarPartidaDesdeFichero(selectedFile);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void cargarPartidaDesdeFichero(File file) {
        try (BufferedReader fin = new BufferedReader(new InputStreamReader(openFileInput(file.getName())))) {
            String lineaEstado = fin.readLine();
            if (lineaEstado != null) {
                juegoBantumi.deserializa(lineaEstado);
                marcarUltimaCasilla(-1);
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
        iniciarCronometroSiNoIniciado();
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
        marcarUltimaCasilla(juegoBantumi.getUltimaCasillaJugada());
        if (juegoBantumi.juegoTerminado()) {
            finJuego();
        }
    }

    private int ultimaCasillaMarcada = -1;

    public void marcarUltimaCasilla(int pos) {
        if (ultimaCasillaMarcada != -1) {
            String idAnterior = String.format(Locale.getDefault(), "%02d", ultimaCasillaMarcada);
            int idBotonAnterior = getResources().getIdentifier("casilla_" + idAnterior, "id", getPackageName());
            if (idBotonAnterior != 0) {
                View btnAnterior = findViewById(idBotonAnterior);
                if (btnAnterior != null) {
                    btnAnterior.setAlpha(1.0f);
                }
            }
        }

        ultimaCasillaMarcada = pos;
        if (pos >= 0) {
            String idActual = String.format(Locale.getDefault(), "%02d", pos);
            int idBotonActual = getResources().getIdentifier("casilla_" + idActual, "id", getPackageName());
            if (idBotonActual != 0) {
                View btnActual = findViewById(idBotonActual);
                if (btnActual != null) {
                    btnActual.setAlpha(0.5f);
                }
            }
        }
    }

    /**
     * El juego ha terminado. Volver a jugar?
     */
    private void finJuego() {
        if (cronometro != null) {
            cronometro.stop();
        }

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
        String tiempoTexto = (cronometro != null) ? cronometro.getText().toString() : "00:00";

        int semillasJ1 = juegoBantumi.getSemillas(6);
        int semillasJ2 = juegoBantumi.getSemillas(13);

        Resultado resultado = new Resultado(nombreJugador, fechaActual, semillasJ1, semillasJ2, tiempoTexto);
        ResultadoRepository repository = new ResultadoRepository(this);
        repository.insert(resultado);
    }
}