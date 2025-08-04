package ufjf.dcc025.franquia.model.usuarios;

import ufjf.dcc025.franquia.enums.TipoUsuario;
import ufjf.dcc025.franquia.model.franquia.Franquia;

import java.util.ArrayList;
import java.util.List;

public class Vendedor extends Usuario {
    private List<String> pedidosId;
    private double totalVendas;
    private Franquia franquia;

    public Vendedor(String nome, String cpf, String email, String senha, Franquia franquia) {
        super(nome, cpf, email, senha);
        this.franquia = franquia;
        franquia.adicionarVendedor(this);
        this.pedidosId = new ArrayList<>();
        this.totalVendas = 0;
    }

    // Getters e Setters
    public Franquia getFranquia() {
        return franquia;
    }

    public List<String> getPedidosId() {
        return new ArrayList<>(pedidosId);
    }

    public double getTotalVendas() {
        return totalVendas;
    }

    public void atualizarTotalVendas(double valor) {
        this.totalVendas += valor;
    }

    public void adicionarPedidoId(String pedidoId) {
        if (!pedidosId.contains(pedidoId)) {
            pedidosId.add(pedidoId);
        }
    }

    @Override
    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.VENDEDOR;
    }

    @Override
    public String toString() {
        return "Vendedor: " + getNome() + " | Vendas: " + totalVendas;
    }
}