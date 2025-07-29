package ufjf.dcc025.franquia.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

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
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Responsável pela persistência assíncrona de entidades em arquivos JSON.
 * @param <T> tipo de entidade
 */
public class AsyncFileDAO<T> implements AsyncDAO<T> {
    private static final String DIRECTORY = "data";
    private final Path filePath;
    private final Path walPath;
    private final Gson gson;
    private final Type listType;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final ExecutorService executor;
    private final long cacheTtlMillis;
    private volatile long lastCacheTime = 0;
    private final AtomicReference<List<T>> cache = new AtomicReference<>();


    public AsyncFileDAO(Class<T> type) throws IOException {
        this(type,
                new ThreadPoolExecutor(
                        2, 10, 60, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(100),
                        r -> { Thread t = new Thread(r); t.setDaemon(true); return t; }
                ),
                5_000L); // cache TTL = 5s
    }


    /**
     * Construtor com ExecutorService e TTL de cache configuráveis.
     * @param type tipo da entidade a ser persistida
     * @param executor ExecutorService para operações assíncronas
     * @param cacheTtlMillis tempo de vida do cache em milissegundos
     * @throws IOException se ocorrer erro ao criar diretórios ou arquivos
     */
    public AsyncFileDAO(Class<T> type, ExecutorService executor, long cacheTtlMillis) throws IOException {
        this.executor = executor;
        this.cacheTtlMillis = cacheTtlMillis;

        Files.createDirectories(Paths.get(DIRECTORY));
        this.filePath = Paths.get(DIRECTORY, type.getSimpleName() + ".json");
        this.walPath = Paths.get(DIRECTORY, type.getSimpleName() + ".wal");

        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.listType = TypeToken.getParameterized(List.class, type).getType();

        setFilePermissions(filePath);
    }

    @Override
    public CompletableFuture<Void> save(List<T> list) {
        return CompletableFuture.runAsync(() -> {
            lock.writeLock().lock();
            try {
                try (FileWriter walWriter = new FileWriter(walPath.toFile(), true)) {
                    walWriter.write(gson.toJson(list));
                    walWriter.write(System.lineSeparator());
                }
                String json = gson.toJson(list);
                Path tmp = Files.createTempFile(filePath.getParent(), "tmp-", ".json");
                Files.write(tmp, json.getBytes(StandardCharsets.UTF_8));
                Files.move(tmp, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(walPath);
                cache.set(new ArrayList<>(list));
                lastCacheTime = System.currentTimeMillis();
                setFilePermissions(filePath);
            } catch (IOException e) {
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
                List<T> list = gson.fromJson(content, listType);
                if (list == null) {
                    list = new ArrayList<>();
                }
                cache.set(new ArrayList<>(list));
                lastCacheTime = now;
                return list;
            } catch (IOException e) {
                throw new CompletionException(e);
            } finally {
                lock.readLock().unlock();
            }
        }, executor);
    }

    /**
     * Carrega de forma síncrona, aguardando até o timeout especificado.
     * @param timeout tempo máximo de espera
     * @param unit unidade de tempo do timeout
     * @return lista de entidades
     * @throws InterruptedException se a thread for interrompida
     * @throws ExecutionException se ocorrer erro na tarefa de carregamento
     * @throws TimeoutException se o tempo expirar
     */
    public List<T> loadSync(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return load().get(timeout, unit);
    }

    /**
     * Reaplica última transação pendente do WAL.
     */
    private void replayWAL() throws IOException {
        if (!Files.exists(walPath)) return;
        List<String> lines = Files.readAllLines(walPath, StandardCharsets.UTF_8);
        if (!lines.isEmpty()) {
            String last = lines.get(lines.size() - 1);
            @SuppressWarnings("unchecked")
            List<T> lastList = gson.fromJson(last, listType);
            String json = gson.toJson(lastList);
            Path tmp = Files.createTempFile(filePath.getParent(), "tmp-", ".json");
            Files.write(tmp, json.getBytes(StandardCharsets.UTF_8));
            Files.move(tmp, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.deleteIfExists(walPath);
    }

    /**
     * Define permissões POSIX "rw-------" para o arquivo.
     */
    private void setFilePermissions(Path path) {
        try {
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
            Files.setPosixFilePermissions(path, perms);
        } catch (IOException | UnsupportedOperationException ignored) {
            // sistema nao posix
        }
    }
}
