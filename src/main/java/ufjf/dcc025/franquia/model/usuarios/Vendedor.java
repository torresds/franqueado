package ufjf.dcc025.franquia.model.usuarios;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ufjf.dcc025.franquia.enums.TipoUsuario;
import ufjf.dcc025.franquia.enums.TiposEntrega;
import ufjf.dcc025.franquia.enums.TiposPagamento;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.exception.*;

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

    //------------ GERENCIAMENTO DE PEDIDOS ------------
    
    public Map<Produto, Integer> criarPedido() {
        Map<Produto, Integer> produtos = new HashMap<>();
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            try {
                System.out.println("Digite o codigo do produto (ou 'sair' para finalizar):");
                String input = scanner.nextLine();
                
                if (input.equalsIgnoreCase("sair")) {
                    break;
                }
                
                Produto produto = this.franquia.buscarProduto(input);
                if (produto == null) {
                    throw new EntidadeNaoEncontradaException(input);
                }
                
                System.out.println("Digite a quantidade:");
                int quantidade = Integer.parseInt(scanner.nextLine());
                
                if (quantidade <= 0) {
                    throw new DadosInvalidosException("Quantidade deve ser maior que zero!");
                }
                
                produtos.put(produto, quantidade);
                System.out.println("Produto adicionado: " + produto.getNome());
                
            } catch (IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
                // Continue o loop - não encerra a função
            }
        }
        
        return produtos;
    }

    public Pedido registrarPedido(Cliente cliente, TiposPagamento formaPagamento, TiposEntrega metodoEntrega, String pedidoId, EntityRepository<Pedido> pedidosValidos) {
        Map<Produto, Integer> produtos = criarPedido();
        Pedido novoPedido = new Pedido(cliente, produtos, this.franquia, formaPagamento, metodoEntrega, pedidoId, this);
        pedidosId.add(pedidoId);
        pedidosValidos.upsert(novoPedido);
        this.franquia.getGerente().adicionarPedidoPendente(pedidoId);
        this.franquia.adicionarPedido(pedidoId);
        
        cliente.adicionarPedido(pedidoId, this.franquia.getId());
        
        return novoPedido;
    }

    public Pedido alterarPedido(String pedidoId, EntityRepository<Pedido> pedidosValidos) {
        Pedido pedidoOriginal = pedidosValidos.findById(pedidoId).orElse(null);
        
        Map<Produto, Integer> produtosQuantidade = criarPedido();

        Pedido copia = new Pedido(pedidoOriginal.getCliente(), new HashMap<>(produtosQuantidade), pedidoOriginal.getFranquia(), pedidoOriginal.getFormaPagamento(), pedidoOriginal.getMetodoEntrega(), pedidoOriginal.getId(), pedidoOriginal.getVendedor());
        
        this.franquia.getGerente().adicionarAlteracaoPedido(copia);
        return copia;
    }

    public Pedido alterarMetodoEntrega(String pedidoId, EntityRepository<Pedido> pedidosValidos, TiposEntrega metodoEntrega){
        Pedido pedidoOriginal = pedidosValidos.findById(pedidoId).orElse(null);
        
        // Criando cópia
        Pedido copia = new Pedido(
            pedidoOriginal.getCliente(), new HashMap<>(pedidoOriginal.getProdutosQuantidade()), pedidoOriginal.getFranquia(), pedidoOriginal.getFormaPagamento(), metodoEntrega, pedidoOriginal.getId(), pedidoOriginal.getVendedor()
        );
        
        this.franquia.getGerente().adicionarAlteracaoPedido(copia);
        return copia;
    }

    public double getTotalVendas() {
        return totalVendas;
    }

    public void atualizarTotalVendas(double valor) {
        this.totalVendas = this.totalVendas + valor;
    }
    
    public List<Pedido> listaPedidos (EntityRepository<Pedido> pedidosValidos){
    	List<Pedido> listaPedidos = new ArrayList<>();
    	for (String id : pedidosId) {
    		listaPedidos.add(pedidosValidos.findById(id).orElse(null));
    	}
    	return listaPedidos;
    }
    
    //------------ GERENCIAMENTO DE CLIENTES ------------
    
    public Cliente cadastrarCliente(String nome, String cpf, String email, String telefone, String endereco, EntityRepository<Cliente> clientes) {
        Cliente novoCliente = new Cliente(nome, cpf, email, telefone, endereco);
        clientes.upsert(novoCliente);
        return novoCliente;
    }
    
    public void removerCliente(String clienteId, EntityRepository<Cliente> clientes) {
        clientes.delete(clienteId);
    }

    public Cliente editarCliente(String clienteId, String novoNome, String novoCpf, String novoEmail, String novoTelefone, String novoEndereco, EntityRepository<Cliente> clientes) {
        Cliente cliente = clientes.findById(clienteId).orElse(null);
        if (cliente == null) {
            throw new EntidadeNaoEncontradaException(clienteId);
        }
        cliente.setNome(novoNome);
        cliente.setCpf(novoCpf);
        cliente.setEmail(novoEmail);
        cliente.setTelefone(novoTelefone);
        cliente.setEndereco(novoEndereco);
        
        return cliente;
    }
    
    
    //getters e setters
    public Franquia getFranquia() {
    	return this.franquia;
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