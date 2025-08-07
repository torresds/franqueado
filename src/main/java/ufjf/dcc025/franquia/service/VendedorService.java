// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.service;

import ufjf.dcc025.franquia.enums.TiposEntrega;
import ufjf.dcc025.franquia.enums.TiposPagamento;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.exception.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VendedorService {
    private final Vendedor vendedor;
    private final EntityRepository<Pedido> pedidoRepository;
    private final EntityRepository<Cliente> clienteRepository;

    public VendedorService(Vendedor vendedor, EntityRepository<Pedido> pedidoRepository,
                           EntityRepository<Cliente> clienteRepository) {
        this.vendedor = vendedor;
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
    }

    public Pedido registrarPedido(Cliente cliente, Map<Produto, Integer> produtos,
                                  TiposPagamento formaPagamento, TiposEntrega metodoEntrega) {
        Pedido novoPedido = new Pedido(cliente, produtos, vendedor.getFranquia(), formaPagamento,
                metodoEntrega, vendedor);

        vendedor.adicionarPedidoId(novoPedido.getId());
        vendedor.getFranquia().adicionarPedido(novoPedido.getId());
        cliente.adicionarPedido(novoPedido.getId(), vendedor.getFranquia().getId());

        pedidoRepository.upsert(novoPedido);
        pedidoRepository.saveAllAsync();
        clienteRepository.upsert(cliente);
        clienteRepository.saveAllAsync();
        return novoPedido;
    }

    /**
     * Solicita a alteração de um pedido existente.
     * Muda o status do pedido para ALTERACAO_SOLICITADA.
     */
    public Pedido solicitarAlteracaoPedido(String pedidoId, Map<Produto, Integer> novosProdutos, TiposEntrega novaEntrega) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(pedidoId));

        if (!pedido.getVendedor().getId().equals(vendedor.getId())) {
            throw new PermissaoNegadaException("alterar um pedido de outro vendedor.");
        }

        pedido.solicitarAlteracao(novosProdutos, novaEntrega);
        pedidoRepository.upsert(pedido);
        pedidoRepository.saveAllAsync();
        return pedido;
    }

    /**
     * Solicita o cancelamento de um pedido.
     * Muda o status do pedido para CANCELAMENTO_SOLICITADO.
     */
    public void solicitarCancelamentoPedido(String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(pedidoId));

        if (!pedido.getVendedor().getId().equals(vendedor.getId())) {
            throw new PermissaoNegadaException("cancelar um pedido de outro vendedor.");
        }

        pedido.solicitarCancelamento();
        pedidoRepository.upsert(pedido);
        pedidoRepository.saveAllAsync();
    }

    public List<Pedido> listaPedidos() {
        List<Pedido> listaPedidos = new ArrayList<>();
        for (String id : vendedor.getPedidosId()) {
            pedidoRepository.findById(id).ifPresent(listaPedidos::add);
        }
        return listaPedidos;
    }

    // Gerenciamento de Clientes
    public Cliente cadastrarCliente(String nome, String cpf, String email, String telefone, String endereco) {
        Cliente novoCliente = new Cliente(nome, cpf, email, telefone, endereco);
        clienteRepository.upsert(novoCliente);
        clienteRepository.saveAllAsync();
        return novoCliente;
    }

    public void removerCliente(String clienteId) {
        clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(clienteId));
        clienteRepository.delete(clienteId);
        clienteRepository.saveAllAsync();
    }

    public Cliente editarCliente(String clienteId, String novoNome, String novoCpf, String novoEmail,
                                 String novoTelefone, String novoEndereco) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(clienteId));
        cliente.setNome(novoNome);
        cliente.setCpf(novoCpf);
        cliente.setEmail(novoEmail);
        cliente.setTelefone(novoTelefone);
        cliente.setEndereco(novoEndereco);
        clienteRepository.upsert(cliente);
        clienteRepository.saveAllAsync();
        return cliente;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }
    public EntityRepository<Pedido> getPedidoRepo() {
        return pedidoRepository;
    }
    public EntityRepository<Cliente> getClienteRepo() {
        return clienteRepository;
    }
}
