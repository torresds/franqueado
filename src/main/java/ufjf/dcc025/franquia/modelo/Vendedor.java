package ufjf.dcc025.franquia.modelo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.text.html.parser.Entity;

import ufjf.dcc025.franquia.enums.TipoUsuario;
import ufjf.dcc025.franquia.enums.TiposEntrega;
import ufjf.dcc025.franquia.enums.TiposPagamento;

public class Vendedor extends Usuario {
    private List<String> pedidosId;
    private double totalVendas;
    private Franquia franquia;

    public Vendedor(String nome, String cpf, String email, String senha, String id, Franquia franquia) {
        super(nome, cpf, email, senha, id);
        this.franquia = franquia;
        this.pedidosId = new ArrayList<>();
        this.totalVendas = 0;
    }

    public Map<Produto, Integer> criarPedido() {
        Map<Produto, Integer> produtos = new HashMap<>();
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            try {
                System.out.println("Digite o nome do produto (ou 'sair' para finalizar):");
                String input = scanner.nextLine();
                
                if (input.equalsIgnoreCase("sair")) {
                    break;
                }
                
                Produto produto = this.franquia.buscarProduto(input);
                if (produto == null) {
                    throw new IllegalArgumentException("Produto não encontrado!");
                }
                
                System.out.println("Digite a quantidade:");
                int quantidade = Integer.parseInt(scanner.nextLine());
                
                if (quantidade <= 0) {
                    throw new IllegalArgumentException("Quantidade deve ser maior que zero!");
                }
                
                produtos.put(produto, quantidade);
                System.out.println("Produto adicionado: " + produto.getNome());
                
            } catch (IllegalArgumentException | NumberFormatException e) {
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
        return novoPedido;
    }

    //cancelar pedido, não sei como fazer sem precisar criar mais uma lista pro gerente

    public Pedido alterarPedido(String pedidoId, EntityRepository<Pedido> pedidosValidos) {
        Pedido pedidoOriginal = pedidosValidos.findById(pedidoId);
        
        Map<Produto, Integer> produtosQuantidade = criarPedido();

        Pedido copia = new Pedido(pedidoOriginal.getCliente(), new HashMap<>(produtosQuantidade), pedidoOriginal.getFranquia(), pedidoOriginal.getFormaPagamento(), pedidoOriginal.getMetodoEntrega(), pedidoOriginal.getId(), pedidoOriginal.getVendedor());
        
        this.franquia.getGerente().adicionarAlteracaoPedido(copia);
        return copia;
    }

    public Pedido alterarMetodoEntrega(String pedidoId, EntityRepository<Pedido> pedidosValidos, TiposEntrega metodoEntrega){
        Pedido pedidoOriginal = pedidosValidos.findById(pedidoId);
        
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
    	List<Pedido> listaPedidos = new ArrayList<Pedido>();
    	for (String id : pedidosId) {
    		listaPedidos.add(pedidosValidos.findbyId(id));
    	}
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