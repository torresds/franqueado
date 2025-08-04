package ufjf.dcc025.franquia.model.usuarios;

import ufjf.dcc025.franquia.enums.TipoUsuario;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.pedidos.Pedido;

import java.util.ArrayList;
import java.util.List;

public class Gerente extends Usuario {
    private Franquia franquia;
    private List<String> pedidosPendentesId;
    private List<String> pedidosParaCancelarId;
    private List<Pedido> alteracoesPedidos;

    public Gerente(String nome, String cpf, String email, String senha) {
        super(nome, cpf, email, senha);
        this.pedidosPendentesId = new ArrayList<>();
        this.pedidosParaCancelarId = new ArrayList<>();
        this.alteracoesPedidos = new ArrayList<>();
    }

    // Getters e Setters
    public Franquia getFranquia() {
        return franquia;
    }

    public void setFranquia(Franquia franquia) {
        this.franquia = franquia;
    }

    public List<String> getPedidosPendentesId() {
        return new ArrayList<>(pedidosPendentesId);
    }

    public List<String> getPedidosParaCancelarId() {
        return new ArrayList<>(pedidosParaCancelarId);
    }

    public List<Pedido> getAlteracoesPedidos() {
        return new ArrayList<>(alteracoesPedidos);
    }

    public void adicionarPedidoPendente(String pedidoId) {
        if (!pedidosPendentesId.contains(pedidoId)) {
            pedidosPendentesId.add(pedidoId);
        }
    }

    public void adicionarSolicitacaoCancelamento(String pedidoId) {
        if (!pedidosParaCancelarId.contains(pedidoId)) {
            pedidosParaCancelarId.add(pedidoId);
        }
    }

    public void adicionarAlteracaoPedido(Pedido pedido) {
        if (!alteracoesPedidos.contains(pedido)) {
            alteracoesPedidos.add(pedido);
        }
    }

    @Override
    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.GERENTE;
    }

    @Override
    public String toString() {
        return "Gerente: " + getNome() + " | Franquia Gerenciada: " + franquia.getNome();
    }
}