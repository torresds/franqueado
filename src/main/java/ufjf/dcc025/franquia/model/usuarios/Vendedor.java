// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.model.usuarios;

import ufjf.dcc025.franquia.enums.TipoUsuario;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import java.util.ArrayList;
import java.util.List;

public class Vendedor extends Usuario {
    private List<String> pedidosId = new ArrayList<>();
    private double totalVendas;
    private String franquiaId;
    private transient Franquia franquia;

    public Vendedor(String nome, String cpf, String email, String senha) {
        super(nome, cpf, email, senha);
    }

    // Getters
    public Franquia getFranquia() { return franquia; }
    public String getFranquiaId() { return franquiaId; }
    public List<String> getPedidosId() { return new ArrayList<>(pedidosId); }
    public double getTotalVendas() { return totalVendas; }

    // Setters e Modificadores
    public void setFranquia(Franquia franquia) {
        this.franquia = franquia;
        this.franquiaId = (franquia != null) ? franquia.getId() : null;
    }
    public void atualizarTotalVendas(double valor) { this.totalVendas += valor; }
    public void adicionarPedidoId(String pedidoId) {
        if (!this.pedidosId.contains(pedidoId)) {
            this.pedidosId.add(pedidoId);
        }
    }

    @Override
    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.VENDEDOR;
    }
}