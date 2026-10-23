package es.upm.miw.bantumi.datos;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResultadoRepository {

    private final ResultadoDao resultadoDao;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface Callback<T> {
        void onResult(T result);
    }

    public ResultadoRepository(Context context) {
        BantumiDatabase db = BantumiDatabase.getInstance(context);
        this.resultadoDao = db.resultadoDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void insert(Resultado resultado) {
        executor.execute(() -> resultadoDao.insert(resultado));
    }

    public void getTop10(Callback<List<Resultado>> callback) {
        executor.execute(() -> {
            List<Resultado> lista = resultadoDao.getTop10();
            mainHandler.post(() -> callback.onResult(lista));
        });
    }

    public void deleteAll(Runnable callback) {
        executor.execute(() -> {
            resultadoDao.deleteAll();
            if (callback != null) {
                mainHandler.post(callback);
            }
        });
    }
}