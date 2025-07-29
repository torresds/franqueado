package ufjf.dcc025.franquia.persistence;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Repositório de entidades que implementam a interface Identifiable.
 * @param <T> tipo da entidade que implementa Identifiable
 */
public class EntityRepository<T extends Identifiable> {
    private final AsyncDAO<T> dao;
    private final Map<String, T> identityMap = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    public EntityRepository(AsyncDAO<T> dao) {
        this.dao = dao;
    }

    /**
     * Carrega todas as entidades do DAO para o cache em memória.
     */
    public CompletableFuture<Void> loadAllAsync() {
        return dao.load().thenAccept(list -> {
            lock.writeLock().lock();
            try {
                identityMap.clear();
                list.forEach(e -> identityMap.put(e.getId(), e));
            } finally {
                lock.writeLock().unlock();
            }
        });
    }

    /**
     * Retorna todas as entidades atualmente em cache.
     */
    public List<T> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(identityMap.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Busca uma entidade por seu ID.
     */
    public Optional<T> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(identityMap.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Adiciona ou atualiza uma entidade no cache.
     */
    public void upsert(T entity) {
        lock.writeLock().lock();
        try {
            identityMap.put(entity.getId(), entity);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Remove entidade do cache pelo ID.
     */
    public void delete(String id) {
        lock.writeLock().lock();
        try {
            identityMap.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Persiste no disco todas as entidades atualmente em cache.
     */
    public CompletableFuture<Void> saveAllAsync() {
        List<T> snapshot;
        lock.readLock().lock();
        try {
            snapshot = new ArrayList<>(identityMap.values());
        } finally {
            lock.readLock().unlock();
        }
        return dao.save(snapshot);
    }

    /**
     * Carrega e bloqueia até completar o carregamento.
     */
    public void loadAllSync(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        loadAllAsync().get(timeout, unit);
    }

    /**
     * Persiste e bloqueia até finalizar.
     */
    public void saveAllSync(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        saveAllAsync().get(timeout, unit);
    }

    /**
     * Retorna entidades filtradas pela propriedade dada.
     */
    public List<T> filter(Predicate<T> predicate) {
        return findAll().stream().filter(predicate).collect(Collectors.toList());
    }
}