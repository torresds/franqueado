// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.controller;

import ufjf.dcc025.franquia.enums.TiposEntrega;
import ufjf.dcc025.franquia.enums.TiposPagamento;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.service.VendedorService;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.pedidos.Pedido;

import java.util.List;
import java.util.Map;

public class VendedorController {
    public final VendedorService vendedorService;

    public VendedorController(VendedorService vendedorService) {
        this.vendedorService = vendedorService;
    }

    public List<Pedido> getPedidosDoVendedor() {
        return vendedorService.listaPedidos();
    }

    public List<Cliente> getClientes() {
        return vendedorService.getClienteRepo().findAll();
    }

    public List<Produto> getProdutosDisponiveis() {
        if (vendedorService.getVendedor().getFranquia() == null) {
            return List.of();
        }
        return vendedorService.getVendedor().getFranquia().getEstoque().keySet().stream().toList();
    }

    public Cliente addCliente(String nome, String cpf, String email, String telefone, String endereco) {
        return vendedorService.cadastrarCliente(nome, cpf, email, telefone, endereco);
    }

    public Pedido criarPedido(Cliente cliente, Map<Produto, Integer> produtos, TiposPagamento pagamento, TiposEntrega entrega) {
        return vendedorService.registrarPedido(cliente, produtos, pagamento, entrega);
    }

    public void solicitarAlteracao(String pedidoId, Map<Produto, Integer> novosProdutos, TiposEntrega novaEntrega) {
        vendedorService.solicitarAlteracaoPedido(pedidoId, novosProdutos, novaEntrega);
    }

    public void solicitarCancelamento(String pedidoId) {
        vendedorService.solicitarCancelamentoPedido(pedidoId);
    }
}
