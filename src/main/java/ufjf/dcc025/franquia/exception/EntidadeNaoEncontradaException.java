// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.exception;

public class EntidadeNaoEncontradaException extends RuntimeException {
    public EntidadeNaoEncontradaException(String id) {
        super("Não foi possível encontrar a entidade com o ID: " + id);
    }
}
