package ufjf.dcc025.franquia.modelo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.parser.Entity;

import ufjf.dcc025.franquia.enums.TipoUsuario;
import ufjf.dcc025.franquia.enums.TiposEntrega;
import ufjf.dcc025.franquia.enums.TiposPagamento;

public class Vendedor extends Usuario {
    private List<String> pedidosId;
    private int totalVendas;
    private Franquia franquia;

    public Vendedor(String nome, String cpf, String email, String senha, String id, Franquia franquia) {
        super(nome, cpf, email, senha, id);
        this.franquia = franquia;
        this.pedidosId = new ArrayList<>();
        this.totalVendas = 0;
    }

    public Pedido registrarPedido(Cliente cliente, Map<Produto, Integer> produtos, String franquiaId, TiposPagamento formaPagamento, TiposEntrega metodoEntrega, String pedidoId, EntityRepository<Pedido> pedidosValidos) {
        Pedido novoPedido = new Pedido(cliente, produtos, franquiaId, formaPagamento, metodoEntrega, pedidoId, this);
        pedidosId.add(pedidoId);
        pedidosValidos.upsert(novoPedido);
        totalVendas += novoPedido.getValorTotal();
        return novoPedido;
    }

    public Pedido excluirPedido (String pedidoId, EntityRepository<Pedido> pedidosValidos){
        pedidosValidos.delete(pedidoId);
        pedidosId.remove(pedidoId);
        return null;
    }

    public List<Pedido> getPedidos(EntityRepository<Pedido> pedidosValidos) {
        List<Pedido> pedidos = new ArrayList<>();
        for (String pedidoId : pedidosId) {
            Pedido pedido = pedidosValidos.findById(pedidoId);
            if (pedido != null) {
                pedidos.add(pedido);
            }
        }
        return pedidos;
    }

    public void alterarMetodoEntrega(String pedidoId, EntityRepository<Pedido> pedidosValidos, TiposEntrega metodoEntrega){
        Pedido pedido = pedidosValidos.findById(pedidoId);
        pedido.setMetodoEntrega(metodoEntrega);
    }

    public void adicionarProduto(Produto produto, int quantidade, String pedidoId, EntityRepository<Pedido> pedidosValidos) {
        Pedido pedido = pedidosValidos.findById(pedidoId);
        if (pedido != null) {
            pedido.adicionarProduto(produto, quantidade);
        }
        this.franquia.estoque.entrySet(produto, this.franquia.estoque.get(produto)-quantidade);
    }

    public int getTotalVendas() {
        return totalVendas;
    }

    @Override
    public String toString() {
        return "Vendedor: " + getNome() + " | Vendas: " + totalVendas;
    }

    @Override
    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.VENDEDOR;
    }
}