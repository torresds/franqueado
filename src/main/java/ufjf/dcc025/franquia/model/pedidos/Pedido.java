// FILE: src/main/java/ufjf/dcc025/franquia/model/pedidos/Pedido.java
package ufjf.dcc025.franquia.model.pedidos;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import ufjf.dcc025.franquia.enums.TiposPagamento;
import ufjf.dcc025.franquia.enums.TiposEntrega;
import ufjf.dcc025.franquia.enums.EstadoPedido;
import ufjf.dcc025.franquia.persistence.Identifiable;
import ufjf.dcc025.franquia.model.usuarios.*;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.model.franquia.Franquia;

public class Pedido implements Identifiable {

    private static final AtomicLong idCounter = new AtomicLong(System.currentTimeMillis());
    private final String id; // CORRIGIDO: ID agora é final
    private Date data;
    private final Cliente cliente;
    private final Vendedor vendedor;
    private final Franquia franquia;
    private Map<Produto, Integer> produtosQuantidade;
    private final TiposPagamento formaPagamento;
    private TiposEntrega metodoEntrega;
    private double valorTotal;
    private double valorFrete;
    private EstadoPedido status;

    public Pedido(Cliente cliente, Map<Produto, Integer> produtos, Franquia franquia, TiposPagamento formaPagamento, TiposEntrega metodoEntrega, Vendedor vendedor) {
        this.id = "P" + idCounter.getAndIncrement();
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

    /**
     * Construtor de Cópia: Cria uma cópia de um pedido existente.
     * @param original O pedido a ser copiado.
     */
    public Pedido(Pedido original) {
        this.id = original.id; // Copia o ID final
        this.data = new Date(); // Nova data para a solicitação de alteração
        this.cliente = original.cliente;
        this.vendedor = original.vendedor;
        this.franquia = original.franquia;
        this.produtosQuantidade = new HashMap<>(original.produtosQuantidade);
        this.formaPagamento = original.formaPagamento;
        this.metodoEntrega = original.metodoEntrega;
        this.valorFrete = original.valorFrete;
        this.valorTotal = original.valorTotal;
        this.status = original.status;
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

        if (valorProdutos >= 100.0) {
            return 0.0;
        }
        return 8.0;
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

    // Getters
    @Override
    public String getId() { return id; }

    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }
    public Cliente getCliente() { return cliente; }
    public Vendedor getVendedor() { return vendedor; }
    public EstadoPedido getStatus() { return status; }
    public Franquia getFranquia() { return franquia; }
    public Map<Produto, Integer> getProdutosQuantidade() { return new HashMap<>(produtosQuantidade); }
    public void setProdutosQuantidade(Map<Produto, Integer> produtos) { this.produtosQuantidade = produtos; }
    public TiposPagamento getFormaPagamento() { return formaPagamento; }
    public TiposEntrega getMetodoEntrega() { return metodoEntrega; }
    public double getValorFrete() { return valorFrete; }
    public double getValorTotal() { return valorTotal; }

    // Setters e controle de status
    public void setMetodoEntrega (TiposEntrega metodoEntrega){
        this.metodoEntrega = metodoEntrega;
        this.atualizarValores();
    }
    public void aprovarPedido() { this.status = EstadoPedido.APROVADO; }
    public void cancelarPedido() { this.status = EstadoPedido.CANCELADO; }
    public boolean isPendente() { return status == EstadoPedido.PENDENTE; }
    public boolean isAprovado() { return status == EstadoPedido.APROVADO; }
    public boolean isCancelado() { return status == EstadoPedido.CANCELADO; }

    @Override
    public String toString() {
        return "Pedido #" + id + " | Cliente: " + cliente.getNome() + " | Valor: R$" + valorTotal;
    }
}
