package ufjf.dcc025.franquia.model.pedidos;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ufjf.dcc025.franquia.enums.TiposPagamento;
import ufjf.dcc025.franquia.enums.TiposEntrega;
import ufjf.dcc025.franquia.enums.EstadoPedido;
import ufjf.dcc025.franquia.persistence.Identifiable;
import ufjf.dcc025.franquia.model.usuarios.*;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.model.franquia.Franquia;

public class Pedido implements Identifiable {

    private final String id;
    private final Date data;
    private final Cliente cliente;
    private final Vendedor vendedor;
    private final Franquia franquia;
    private Map<Produto, Integer> produtosQuantidade;
    private final TiposPagamento formaPagamento;
    private TiposEntrega metodoEntrega;
    private double valorTotal;
    private double valorFrete;
    private EstadoPedido status;

    public Pedido(Cliente cliente, Map<Produto, Integer> produtos, Franquia franquia, TiposPagamento formaPagamento, TiposEntrega metodoEntrega, String id, Vendedor vendedor) {
        this.id = id;
        this.data = new Date();
        this.cliente = cliente;
        this.vendedor = vendedor;
        this.franquia = franquia;
        this.produtosQuantidade = new HashMap<>(produtos);
        this.formaPagamento = formaPagamento;
        this.metodoEntrega = metodoEntrega;
        this.valorFrete = calcularFrete();
        this.valorTotal = calcularValorTotal();
        this.status = EstadoPedido.PENDENTE;
    }

    private double calcularValorTotal() {
        this.valorTotal = produtosQuantidade.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPreco() * entry.getValue())
                .sum();
        return valorTotal + valorFrete;
    }

     private double calcularFrete() { 
    if (metodoEntrega == TiposEntrega.RETIRADA) {
        return 0.0;
    }    
    double valorProdutos = produtosQuantidade.entrySet().stream()
            .mapToDouble(entry -> entry.getKey().getPreco() * entry.getValue())
            .sum();
    
    if (valorProdutos >= 500.0) {
        return 0.0;
    }
    return 15.0;
}

    public void adicionarProduto(Produto produto, int quantidade) {
        produtosQuantidade.merge(produto, quantidade, Integer::sum);
        atualizarValores();
    }

    public void removerProduto(Produto produto) {
        produtosQuantidade.remove(produto);
        atualizarValores();
    }

    public void atualizarValores() {
        this.valorFrete = calcularFrete();
        this.valorTotal = calcularValorTotal();
    }

    // Getters básicos
    @Override
    public String getId() { return id; }
    public Date getData() { return data; }
    public Cliente getCliente() { return cliente; }
    public Vendedor getVendedor() { return vendedor; }
    public EstadoPedido getStatus() { return status; }
    public Franquia getFranquia() { return franquia; }
    
    // Getters de produtos e pagamento
    public Map<Produto, Integer> getProdutosQuantidade() { return new HashMap<>(produtosQuantidade); }
    public TiposPagamento getFormaPagamento() { return formaPagamento; }
    
    // Getters de entrega
    public TiposEntrega getMetodoEntrega() { return metodoEntrega; }
    public double getValorFrete() { return valorFrete; }
    
    // Getters de valores
    public double getValorTotal() { return valorTotal; }
    
    // Métodos de controle do pedido
    public void aprovarPedido() { this.status = EstadoPedido.APROVADO; }
    public void cancelarPedido() { this.status = EstadoPedido.CANCELADO; }

    // Métodos utilitários para verificar status
    public boolean isPendente() { return status == EstadoPedido.PENDENTE; }
    public boolean isAprovado() { return status == EstadoPedido.APROVADO; }
    public boolean isCancelado() { return status == EstadoPedido.CANCELADO; }

    //Setters
    public void setMetodoEntrega (TiposEntrega metodoEntrega){
        this.metodoEntrega = metodoEntrega;
        this.atualizarValores();
    }

    @Override
    public String toString() {
        return "Pedido #" + id + " | Cliente: " + cliente.getNome() + " | Valor: R$" + valorTotal;
    }
}