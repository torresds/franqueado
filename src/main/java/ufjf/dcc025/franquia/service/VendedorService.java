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
import java.util.HashMap;
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

    // Gerenciamento de Pedidos
    public Pedido registrarPedido(Cliente cliente, Map<Produto, Integer> produtos, 
                                 TiposPagamento formaPagamento, TiposEntrega metodoEntrega) {
        String pedidoId = "P" + System.currentTimeMillis();
        Pedido novoPedido = new Pedido(cliente, produtos, vendedor.getFranquia(), formaPagamento, 
                                       metodoEntrega, pedidoId, vendedor);
        vendedor.adicionarPedidoId(pedidoId);
        vendedor.getFranquia().getGerente().adicionarPedidoPendente(pedidoId);
        vendedor.getFranquia().adicionarPedido(pedidoId);
        cliente.adicionarPedido(pedidoId, vendedor.getFranquia().getId());
        pedidoRepository.upsert(novoPedido);
        pedidoRepository.saveAllAsync();
        clienteRepository.upsert(cliente);
        clienteRepository.saveAllAsync();
        return novoPedido;
    }

    public Pedido alterarPedido(String pedidoId, Map<Produto, Integer> produtosQuantidade) {
        Pedido pedidoOriginal = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(pedidoId));
        Pedido copia = new Pedido(pedidoOriginal.getCliente(), new HashMap<>(produtosQuantidade), 
                                  pedidoOriginal.getFranquia(), pedidoOriginal.getFormaPagamento(), 
                                  pedidoOriginal.getMetodoEntrega(), pedidoOriginal.getId(), 
                                  pedidoOriginal.getVendedor());
        vendedor.getFranquia().getGerente().adicionarAlteracaoPedido(copia);
        pedidoRepository.upsert(copia);
        pedidoRepository.saveAllAsync();
        return copia;
    }

    public Pedido alterarMetodoEntrega(String pedidoId, TiposEntrega metodoEntrega) {
        Pedido pedidoOriginal = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(pedidoId));
        Pedido copia = new Pedido(pedidoOriginal.getCliente(), new HashMap<>(pedidoOriginal.getProdutosQuantidade()), 
                                  pedidoOriginal.getFranquia(), pedidoOriginal.getFormaPagamento(), 
                                  metodoEntrega, pedidoOriginal.getId(), pedidoOriginal.getVendedor());
        vendedor.getFranquia().getGerente().adicionarAlteracaoPedido(copia);
        pedidoRepository.upsert(copia);
        pedidoRepository.saveAllAsync();
        return copia;
    }

    public void cancelarPedido(String pedidoId) {
        vendedor.getFranquia().getGerente().adicionarSolicitacaoCancelamento(pedidoId);
    }

    public List<Pedido> listaPedidos() {
        List<Pedido> listaPedidos = new ArrayList<>();
        for (String id : vendedor.getPedidosId()) {
            Pedido pedido = pedidoRepository.findById(id).orElse(null);
            if (pedido != null) {
                listaPedidos.add(pedido);
            }
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

    //getters
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