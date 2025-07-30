package ufjf.dcc025.franquia.exception;

public class UsuarioInvalidoException extends RuntimeException {
    public UsuarioInvalidoException() {
        super("Usuário ou senha inválidos!");
    }
}
