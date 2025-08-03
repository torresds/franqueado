package ufjf.dcc025.franquia.modelo;
import ufjf.dcc025.franquia.enums.TipoUsuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

public class Franquia implements Identifiable {
    private final String id;
    private String nome;
    private String endereco;
    private Gerente gerente;
    private List<Vendedor> vendedores;
    private List<String> pedidosId;
    private Map<Produto, Integer> estoque;
    private double receita;

    public Franquia(String nome, String endereco, String gerenteId, EntityRepository<Gerente> gerentesValidos) {
        this.nome = nome;
        setId();
        this.endereco = endereco;
        setGerente(gerente, gerentesValidos);
        this.vendedores = new ArrayList<>();
        this.vendasId = new ArrayList<>();
        this.estoque = new HashMap<>();
        this.receita = 0.0;
    }

 //------------ GERENCIAMENTO DE FATURAMENTO ------------

    public void atualizarReceita(double valor) {
        this.receita += valor;
    }

    public double calcularTicketMedio(EntityRepository<Pedido> pedidosValidos) {
        List<Pedido> pedidos = pedidosValidos.findAll().stream()
                .filter(pedido -> pedido.getFranquia().getId().equals(this.id))
                .collect(Collectors.toList());
        
        if (pedidos.isEmpty()) {
            return 0.0;
        }
        
        double totalVendas = pedidos.stream()
                .mapToDouble(Pedido::getValorTotal)
                .sum();
        
        return totalVendas / pedidos.size();
    }
    
 //------------ GERENCIAMENTO DE VENDEDORES E PEDIDOS ------------ 

    public void adicionarVendedor(Vendedor vendedor) {
        if (!vendedores.contains(vendedor)) {
            vendedores.add(vendedor);
        }
    }

    public void removerVendedor(Vendedor vendedor) {
        vendedores.remove(vendedor);
    }
    
    public void adicionarPedido(String pedidoId) {
    	if (!pedidosId.contains(pedidoId)) {
    		pedidosId.add(pedidoId);
    	}
    }
    
    public List<Pedido> listarPedidos(EntityRepository<Pedido> pedidosValidos){
    	List<Pedido> pedidos = new ArrayList<>();
    	for (String id : this.pedidosId) {
    		pedidos.add(pedidosValidos.findbyId(id));
    	}
    	return pedidos;
    }
 //------------ GERENCIAMENTO DE ESTOQUE ------------  

    public void adicionarProduto(Produto produto, int quantidade) {
        if (estoque.containsKey(produto)) {
            estoque.put(produto, estoque.get(produto) + quantidade);
        } else {
            estoque.put(produto, quantidade);
        }  
    }

    public void removerProduto(Produto produto) {
        if (estoque.containsKey(produto)) {
            estoque.remove(produto);
        } else {
            throw new IllegalArgumentException("Produto não encontrado no estoque.");
        }
    }

    public void atualizarEstoque(Produto produto, int quantidade) {
        if (estoque.containsKey(produto)) {
            int quantidadeAtual = estoque.get(produto);
            if (quantidadeAtual + quantidade < 0) {
                throw new IllegalArgumentException("Quantidade insuficiente no estoque.");
            }
            estoque.put(produto, quantidadeAtual + quantidade);
        } else {
            if (quantidade < 0) {
                throw new IllegalArgumentException("Produto não encontrado no estoque.");
            }
            estoque.put(produto, quantidade);
        }
    }

    public Produto buscarProduto(String nome) {
        for (Produto produto : estoque.keySet()) {
            if (produto.getNome().equalsIgnoreCase(nome)) {
                return produto;
            }
        }
        return null; // Produto não encontrado
    }

    public void checarDisponibilidade(Produto produto, int quantidade) {
        if (!estoque.containsKey(produto) || estoque.get(produto) < quantidade) {
            throw new IllegalArgumentException("Produto não disponível em estoque.");
        }
    }
    
  //------------ GERENCIAMENTO DE RELATÓRIOS ------------

    public List<String> gerarRelatorioVendas() {
        return new ArrayList<>(pedidosId);
    }

    public List<String> gerarRelatorioVendasPeriodo(Date dataInicio, Date dataFim, EntityRepository<Pedido> repositorioPedidos) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas de início e fim não podem ser nulas.");
        }
        
        if (dataInicio.after(dataFim)) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data fim.");
        }
        
        List<String> pedidosIdNoPeriodo = new ArrayList<>();
        
        for (String pedidoId : this.pedidosId) {
            Pedido pedido = repositorioPedidos.findById(pedidoId);
            if (pedido != null) {
                Date dataPedido = pedido.getData();
                if ((dataPedido.equals(dataInicio) || dataPedido.after(dataInicio)) && 
                    (dataPedido.equals(dataFim) || dataPedido.before(dataFim))) {
                    pedidosIdNoPeriodo.add(pedidoId);
                }
            }
        }
        
        return pedidosIdNoPeriodo;
    }

    public List<String> gerarRelatorioClientesFrequencia(EntityRepository<Cliente> repositorioClientes) {
    Map<Cliente, Integer> clientesComPedidos = new HashMap<>();
    List<Cliente> todosClientes = repositorioClientes.findAll();
    
    for (Cliente cliente : todosClientes) {
        int totalPedidosNaFranquia = cliente.getTotalPedidosNaFranquia(this.id);
        if (totalPedidosNaFranquia > 0) {
            clientesComPedidos.put(cliente, totalPedidosNaFranquia);
        }
    }
    
    return clientesComPedidos.entrySet().stream()
            .sorted(Map.Entry.<Cliente, Integer>comparingByValue().reversed())
            .map(entry -> String.format("%s - %d pedidos", 
                                      entry.getKey().getNome(), 
                                      entry.getValue()))
            .collect(Collectors.toList());
}

    public List<String> gerarRelatorioProdutosMaisVendidos(EntityRepository<Pedido> repositorioPedidos) {
        
        Map<Produto, Integer> produtosVendidos = new HashMap<>();
        
        for (String pedidoId : pedidosId) {
            Pedido pedido = repositorioPedidos.findById(pedidoId);
            if (pedido != null) {
                for (Map.Entry<Produto, Integer> entry : pedido.getProdutosQuantidade().entrySet()) {
                    produtosVendidos.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
            }
        }
        
        return produtosVendidos.entrySet().stream()
                .sorted(Map.Entry.<Produto, Integer>comparingByValue().reversed())
                .map(entry -> String.format("%s - %d unidades", 
                                          entry.getKey().getNome(), 
                                          entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<String> gerarRelatorioClientesdaLoja (EntityRepository<Cliente> repositorioClientes) {
        List<Cliente> clientes = repositorioClientes.findAll();
        return clientes.stream()
                .filter(cliente -> cliente.getTotalPedidosNaFranquia(this.id) > 0)
                .map(cliente -> String.format("%s - %s", cliente.getNome(), cliente.getEmail()))
                .collect(Collectors.toList());
    }

    public List<String> gerarRelatorioVendedores(EntityRepository<Vendedor> repositorioVendedores) {
        List<Vendedor> vendedores = repositorioVendedores.findAll();
        return vendedores.stream()
                .filter(vendedor -> vendedor.getFranquia().getId().equals(this.id))
                .map(vendedor -> String.format("%s - Total Vendas: %.2f", vendedor.getNome(), vendedor.getTotalVendas()))
                .collect(Collectors.toList());
    }

    // Getters e Setters
    @Override
    public String getId() {
        return id;
    }
    private void setId(String id) {
        //colocar metodo de criar o id aqui
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID não pode ser vazio.");
        }
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    public String getEndereco() { 
        return endereco; 
    }

    public void setGerente(String gerenteId, EntityRepository<Gerente> gerentesValidos) {
        Gerente gerente = gerentesValidos.findById(gerenteId);
        if (gerente == null) {
            throw new IllegalArgumentException("Gerente não encontrado.");
        }
        this.gerente = gerente;
    }
    public Gerente getGerente() { 
        return gerente; 
    }
    public List<Vendedor> getVendedores() { 
        return new ArrayList<>(vendedores); 
    }
    public Map<Produto, Integer> getEstoque() { 
        return estoque;
    }
    public double getReceita() { 
        return receita; 
    }

    @Override
    public String toString() {
        return "Franquia: " + nome + " | Gerente: " + gerente.getNome() + " | Receita: R$" + receita;
    }
}