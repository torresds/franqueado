package ufjf.dcc025.franquia.exception;

public class EstoqueInsuficienteException extends RuntimeException {
    public EstoqueInsuficienteException(String produto, int disponivel, int solicitado) {
        super("Estoque insuficiente para o produto " + produto + 
              ". Disponível: " + disponivel + ", solicitado: " + solicitado);
    }
}
