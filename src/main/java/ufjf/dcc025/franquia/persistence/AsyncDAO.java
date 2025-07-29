package ufjf.dcc025.franquia.persistence;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Define as operações padrão de persistência para qualquer entidade do sistema.
 * @param <T> O tipo da entidade
 */
public interface AsyncDAO<T> {

    /**
     * Salva uma lista de objetos de forma assíncrona.
     * @param list A lista de objetos a ser salva.
     * @return um CompletableFuture<Void> que será completado quando a operação terminar.
     */
    CompletableFuture<Void> save(List<T> list);

    /**
     * Carrega uma lista de objetos de forma assíncrona.
     * @return um CompletableFuture<List<T>> que conterá a lista de objetos quando a operação terminar.
     */
    CompletableFuture<List<T>> load();
}
