// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.exception;

public class UsuarioInvalidoException extends RuntimeException {
    public UsuarioInvalidoException() {
        super("Usuário ou senha inválidos!");
    }
}
