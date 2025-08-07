// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.exception;

public class PermissaoNegadaException extends RuntimeException {
    public PermissaoNegadaException(String acao) {
        super("Você não tem permissão para " + acao);
    }
}
