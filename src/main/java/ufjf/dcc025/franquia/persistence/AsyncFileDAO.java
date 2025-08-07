// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Versão robusta de AsyncFileDAO com:
 *  - cache configurável e invalidação manual
 *  - fallback em desserialização
 *  - WAL truncado após commit
 *  - logs detalhados
 *  - diretório de dados configurável
 */
public class AsyncFileDAO<T> implements AsyncDAO<T> {
    private static final Logger logger = Logger.getLogger(AsyncFileDAO.class.getName());
    private final Path dataDirectory;
    private final Path filePath;
    private final Path walPath;
    private final Gson gson;
    private final Type listType;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final ExecutorService executor;
    private final long cacheTtlMillis;
    private volatile long lastCacheTime = 0;
    private final AtomicReference<List<T>> cache = new AtomicReference<>();

    /**
     * Construtor principal.
     * @param type tipo da entidade
     * @param dataDir diretório base para arquivos JSON
     * @param executor Executor para operações assíncronas
     * @param cacheTtlMillis TTL do cache em ms
     * @throws IOException se falhar ao criar diretórios
     */
    public AsyncFileDAO(Class<T> type,
                        Path dataDir,
                        ExecutorService executor,
                        long cacheTtlMillis) throws IOException {
        this.executor = executor;
        this.cacheTtlMillis = cacheTtlMillis;
        this.dataDirectory = dataDir;

        Files.createDirectories(dataDirectory);
        this.filePath = dataDirectory.resolve(type.getSimpleName() + ".json");
        this.walPath = dataDirectory.resolve(type.getSimpleName() + ".wal");

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .create();

        this.listType = TypeToken.getParameterized(List.class, type).getType();
        setFilePermissions(filePath);
    }

    /**
     * Construtor padrão (dataDir="data", pool fixo, TTL 5s)
     */
    public AsyncFileDAO(Class<T> type) throws IOException {
        this(type,
                Paths.get("data"),
                new ThreadPoolExecutor(
                        2, 10, 60, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(100),
                        r -> { Thread t = new Thread(r); t.setDaemon(true); return t; }
                ),
                5_000L);
    }

    @Override
    public CompletableFuture<Void> save(List<T> list) {
        return CompletableFuture.runAsync(() -> {
            lock.writeLock().lock();
            try {
                // grava WAL apenas última operação
                try (FileWriter walWriter = new FileWriter(walPath.toFile(), false)) {
                    walWriter.write(gson.toJson(list));
                    walWriter.write(System.lineSeparator());
                }

                String json = gson.toJson(list);
                Path tmp = Files.createTempFile(dataDirectory, "tmp-", ".json");
                Files.write(tmp, json.getBytes(StandardCharsets.UTF_8));
                Files.move(tmp, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

                // truncate WAL
                try {
                    Files.newBufferedWriter(walPath, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING).close();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Falha ao truncar WAL: " + walPath, e);
                }

                cache.set(new ArrayList<>(list));
                lastCacheTime = System.currentTimeMillis();
                setFilePermissions(filePath);
                logger.log(Level.INFO, "Save completed for " + filePath);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Erro ao salvar JSON", e);
                throw new CompletionException(e);
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
                long now = System.currentTimeMillis();
                List<T> cached = cache.get();
                if (cached != null && (now - lastCacheTime) < cacheTtlMillis) {
                    return new ArrayList<>(cached);
                }

                replayWAL();

                if (!Files.exists(filePath)) {
                    cache.set(new ArrayList<>());
                    lastCacheTime = now;
                    return new ArrayList<>();
                }

                String content = Files.readString(filePath, StandardCharsets.UTF_8);
                List<T> list;
                try {
                    list = gson.fromJson(content, listType);
                } catch (JsonSyntaxException e) {
                    logger.log(Level.WARNING, "JSON inválido em " + filePath + ", usando lista vazia", e);
                    list = new ArrayList<>();
                }

                if (list == null) {
                    list = new ArrayList<>();
                }

                cache.set(new ArrayList<>(list));
                lastCacheTime = now;
                return list;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Erro ao ler JSON", e);
                throw new CompletionException(e);
            } finally {
                lock.readLock().unlock();
            }
        }, executor);
    }

    /**
     * Invalida manualmente o cache em memória.
     */
    public void invalidateCache() {
        cache.set(null);
        lastCacheTime = 0;
        logger.log(Level.INFO, "Cache invalidado para " + filePath);
    }

    private void replayWAL() throws IOException {
        if (!Files.exists(walPath)) return;
        List<String> lines = Files.readAllLines(walPath, StandardCharsets.UTF_8);
        if (!lines.isEmpty()) {
            String last = lines.get(lines.size() - 1);
            @SuppressWarnings("unchecked")
            List<T> lastList = gson.fromJson(last, listType);
            String json = gson.toJson(lastList);
            Path tmp = Files.createTempFile(dataDirectory, "tmp-", ".json");
            Files.write(tmp, json.getBytes(StandardCharsets.UTF_8));
            Files.move(tmp, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            // após replay, truncamos WAL
            Files.newBufferedWriter(walPath, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING).close();
        }
    }

    private void setFilePermissions(Path path) {
        try {
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
            Files.setPosixFilePermissions(path, perms);
        } catch (IOException | UnsupportedOperationException ignored) {
            // ambiente não POSIX
        }
    }
}
