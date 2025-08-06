package ufjf.dcc025.franquia.model.franquia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Date;

import ufjf.dcc025.franquia.persistence.Identifiable;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.usuarios.*;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.exception.*;

public class Franquia implements Identifiable {
    private String id;
    private String nome;
    private String endereco;
    private Gerente gerente;
    private transient List<Vendedor> vendedores; // Corrigido: Marcado como transient
    private List<String> pedidosId;
    private Map<Produto, Integer> estoque;
    private double receita;

    public Franquia(String nome, String endereco, Gerente gerente) {
        this.nome = nome;
        setId();
        this.endereco = endereco;
        this.gerente = gerente;
        this.vendedores = new ArrayList<>();
        this.pedidosId = new ArrayList<>();
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
        if (this.vendedores == null) {
            this.vendedores = new ArrayList<>();
        }
        if (!vendedores.contains(vendedor)) {
            vendedores.add(vendedor);
        }
    }

    public void removerVendedor(Vendedor vendedor) {
        if (this.vendedores != null) {
            vendedores.remove(vendedor);
        }
    }

    public void adicionarPedido(String pedidoId) {
        if (!pedidosId.contains(pedidoId)) {
            pedidosId.add(pedidoId);
        }
    }

    public List<Pedido> listarPedidos(EntityRepository<Pedido> pedidosValidos){
        List<Pedido> pedidos = new ArrayList<>();
        for (String id : this.pedidosId) {
            pedidos.add(pedidosValidos.findById(id).orElse(null));
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
            throw new EntidadeNaoEncontradaException(produto.getCodigo());
        }
    }

    public void atualizarEstoque(Produto produto, int quantidade) {
        if (estoque.containsKey(produto)) {
            int quantidadeAtual = estoque.get(produto);
            if (quantidadeAtual + quantidade < 0) {
                throw new EstoqueInsuficienteException(produto.getNome(), quantidadeAtual, quantidade );
            }
            estoque.put(produto, quantidadeAtual + quantidade);
        } else {
            if (quantidade < 0) {
                throw new DadosInvalidosException("Produto não encontrado no estoque.");
            }
            estoque.put(produto, quantidade);
        }
    }

    public Produto buscarProduto(String codigo) {
        for (Produto produto : estoque.keySet()) {
            if (produto.getCodigo().equals(codigo)) {
                return produto;
            }
        }
        return null; // Produto não encontrado
    }

    public void checarDisponibilidade(Produto produto, int quantidade) {
        if (!estoque.containsKey(produto) || estoque.get(produto) < quantidade) {
            throw new DadosInvalidosException("Produto não disponível em estoque.");
        }
    }

    //------------ GERENCIAMENTO DE RELATÓRIOS ------------

    public List<String> gerarRelatorioVendas() {
        return new ArrayList<>(pedidosId);
    }

    public List<String> gerarRelatorioVendasPeriodo(Date dataInicio, Date dataFim, EntityRepository<Pedido> repositorioPedidos) {
        if (dataInicio == null || dataFim == null) {
            throw new DadosInvalidosException("Datas de início e fim não podem ser nulas.");
        }

        if (dataInicio.after(dataFim)) {
            throw new DadosInvalidosException("Data de início não pode ser posterior à data fim.");
        }

        List<String> pedidosIdNoPeriodo = new ArrayList<>();

        for (String pedidoId : this.pedidosId) {
            Pedido pedido = repositorioPedidos.findById(pedidoId).orElse(null);
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
            Pedido pedido = repositorioPedidos.findById(pedidoId).orElse(null);
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

    public int quantidadePedidos() {
        return pedidosId.size();
    }

    // Getters e Setters
    @Override
    public String getId() {
        return id;
    }

    private void setId() {
        String newId = "F" + System.currentTimeMillis();
        this.id = newId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public void setGerente(Gerente gerente) {
        this.gerente = gerente;
    }

    public Gerente getGerente() {
        return gerente;
    }
    public List<Vendedor> getVendedores() {
        if (vendedores == null) {
            vendedores = new ArrayList<>();
        }
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
        String nomeGerente = (gerente != null) ? gerente.getNome() : "Nenhum";
        return "Franquia: " + nome + " | Gerente: " + nomeGerente + " | Receita: R$" + receita;
    }
}
