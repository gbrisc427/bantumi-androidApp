package es.upm.miw.bantumi.ui.actividades;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import es.upm.miw.bantumi.R;
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

        cargarResultados();
    }

    private void cargarResultados() {
        repository.getTop10(resultados -> {
            if (resultados == null || resultados.isEmpty()) {
                tvSinResultados.setVisibility(View.VISIBLE);
                rvResultados.setVisibility(View.GONE);
            } else {
                tvSinResultados.setVisibility(View.GONE);
                rvResultados.setVisibility(View.VISIBLE);
                adapter.setResultados(resultados);
            }
        });
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