package ufjf.dcc025.franquia.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementação genérica do DAO que usa a biblioteca GSON para persistir em arquivos JSON.
 * @param <T> O tipo da entidade que este DAO irá gerenciar.
 */
public class AsyncFileDAO<T> implements AsyncDAO<T> {

    private final String filename;
    private static final String DIRECTORY = "data";
    private final Gson gson;
    private final Type listType;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public AsyncFileDAO(Class<T> type) {
        File dir = new File(DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.filename = DIRECTORY + File.separator + type.getSimpleName() + ".json";
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.listType = TypeToken.getParameterized(List.class, type).getType();
    }

    @Override
    public CompletableFuture<Void> save(List<T> list) {
        return CompletableFuture.runAsync(() -> {
            lock.writeLock().lock();
            try {
                String json = gson.toJson(list);
                try (FileWriter writer = new FileWriter(filename)) {
                    writer.write(json);
                    System.out.println("Sucesso: " + list.size() + " objetos salvos em " + filename);
                }
            } catch (IOException e) {
                System.err.println("Erro ao salvar o arquivo JSON: " + filename);
                e.printStackTrace();
            } finally {
                lock.writeLock().unlock();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<T>> load() {
        return CompletableFuture.supplyAsync(() -> {
            lock.readLock().lock();
            try {
                File file = new File(filename);
                if (!file.exists()) {
                    return new ArrayList<>();
                }

                try (FileReader reader = new FileReader(filename)) {
                    List<T> list = gson.fromJson(reader, listType);
                    if (list == null) {
                        return new ArrayList<>();
                    }
                    System.out.println("Sucesso: " + list.size() + " objetos carregados de " + filename);
                    return list;
                }
            } catch (IOException e) {
                System.err.println("Erro ao carregar o arquivo JSON: " + filename);
                e.printStackTrace();
                return new ArrayList<>();
            } finally {
                lock.readLock().unlock();
            }
        }, executor);
    }
}
