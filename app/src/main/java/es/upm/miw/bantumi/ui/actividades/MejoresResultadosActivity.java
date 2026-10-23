package es.upm.miw.bantumi.ui.actividades;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.datos.Resultado;
import es.upm.miw.bantumi.datos.ResultadoRepository;
import es.upm.miw.bantumi.ui.adapters.ResultadosAdapter;

public class MejoresResultadosActivity extends AppCompatActivity {

    private ResultadoRepository repository;
    private ResultadosAdapter adapter;
    private RecyclerView rvResultados;
    private TextView tvSinResultados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mejores_resultados);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.txtOpcionMejoresResultados);
        }

        rvResultados = findViewById(R.id.rvResultados);
        tvSinResultados = findViewById(R.id.tvSinResultados);

        rvResultados.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResultadosAdapter();
        rvResultados.setAdapter(adapter);

        repository = new ResultadoRepository(this);

        EditText etBuscar = findViewById(R.id.etBuscarNombre);
        Spinner spFiltro = findViewById(R.id.spFiltroOrden);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.filtroOrdenEntries, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiltro.setAdapter(spinnerAdapter);

        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cargarResultados();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarResultados();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        cargarResultados();
    }

    private void cargarResultados() {
        EditText etBuscar = findViewById(R.id.etBuscarNombre);
        Spinner spFiltro = findViewById(R.id.spFiltroOrden);

        String textoBusqueda = etBuscar != null && etBuscar.getText() != null ? etBuscar.getText().toString().trim() : "";
        int opcionFiltro = spFiltro != null ? spFiltro.getSelectedItemPosition() : 0;

        ResultadoRepository.Callback<List<Resultado>> callback = resultados -> {
            if (resultados == null || resultados.isEmpty()) {
                tvSinResultados.setVisibility(View.VISIBLE);
                rvResultados.setVisibility(View.GONE);
            } else {
                tvSinResultados.setVisibility(View.GONE);
                rvResultados.setVisibility(View.VISIBLE);
                adapter.setResultados(resultados);
            }
        };

        if (!textoBusqueda.isEmpty()) {
            repository.getResultadosPorNombre(textoBusqueda, callback);
        } else if (opcionFiltro == 1) {
            repository.getTop10PorJugador1(callback);
        } else {
            repository.getTop10(callback);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mejores_resultados_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.opcEliminarResultados) {
            confirmarEliminar();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.txtDialogoEliminarTitulo)
                .setMessage(R.string.txtDialogoEliminarPregunta)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    repository.deleteAll(() -> {
                        cargarResultados();
                        Snackbar.make(findViewById(android.R.id.content),
                                getString(R.string.txtResultadosEliminadosOK),
                                Snackbar.LENGTH_LONG).show();
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}