// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.model.pedidos;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
    private Date data;
    private final String clienteId;
    private final String vendedorId;
    private final String franquiaId;
    private Map<Produto, Integer> produtosQuantidade;
    private final TiposPagamento formaPagamento;
    private TiposEntrega metodoEntrega;
    private double valorTotal;
    private double valorFrete;
    private EstadoPedido status;

    // Campos para armazenar alterações propostas
    private Map<Produto, Integer> produtosAlteracao;
    private TiposEntrega entregaAlteracao;
    private EstadoPedido estadoAnterior; // NOVO: Armazena o estado antes da solicitação

    private transient Cliente cliente;
    private transient Vendedor vendedor;
    private transient Franquia franquia;

    public Pedido(Cliente cliente, Map<Produto, Integer> produtos, Franquia franquia, TiposPagamento formaPagamento, TiposEntrega metodoEntrega, Vendedor vendedor) {
        this.id = UUID.randomUUID().toString();
        this.data = new Date();
        this.cliente = cliente;
        this.clienteId = cliente.getId();
        this.vendedor = vendedor;
        this.vendedorId = vendedor.getId();
        this.franquia = franquia;
        this.franquiaId = franquia.getId();
        this.produtosQuantidade = new HashMap<>(produtos);
        this.formaPagamento = formaPagamento;
        this.metodoEntrega = metodoEntrega;
        this.status = EstadoPedido.PENDENTE;
        this.produtosAlteracao = null;
        this.entregaAlteracao = null;
        this.estadoAnterior = null;
        atualizarValores();
    }

    private double calcularValorTotal() {
        return produtosQuantidade.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPreco() * entry.getValue())
                .sum();
    }

    private double calcularFrete() {
        if (metodoEntrega == TiposEntrega.RETIRADA) {
            return 0.0;
        }
        double valorProdutos = calcularValorTotal();

        if (valorProdutos >= 100.0) {
            return 0.0;
        }
        return 8.0;
    }

    public void atualizarValores() {
        this.valorFrete = calcularFrete();
        this.valorTotal = calcularValorTotal() + this.valorFrete;
    }

    // MÉTODOS PARA GERENCIAR ESTADOS E ALTERAÇÕES

    public void solicitarAlteracao(Map<Produto, Integer> novosProdutos, TiposEntrega novaEntrega) {
        if (this.status == EstadoPedido.APROVADO || this.status == EstadoPedido.PENDENTE) {
            this.estadoAnterior = this.status; // Armazena o estado atual
            this.produtosAlteracao = new HashMap<>(novosProdutos);
            this.entregaAlteracao = novaEntrega;
            this.status = EstadoPedido.ALTERACAO_SOLICITADA;
        }
    }

    public void solicitarCancelamento() {
        if (this.status == EstadoPedido.APROVADO || this.status == EstadoPedido.PENDENTE) {
            this.estadoAnterior = this.status; // Armazena o estado atual
            this.status = EstadoPedido.CANCELAMENTO_SOLICITADO;
        }
    }

    public void aplicarAlteracaoAprovada() {
        if (this.status == EstadoPedido.ALTERACAO_SOLICITADA) {
            this.produtosQuantidade = this.produtosAlteracao;
            this.metodoEntrega = this.entregaAlteracao;
            this.produtosAlteracao = null;
            this.entregaAlteracao = null;
            this.estadoAnterior = null;
            this.status = EstadoPedido.APROVADO;
            atualizarValores();
        }
    }

    public void negarSolicitacao() {
        if (this.status == EstadoPedido.ALTERACAO_SOLICITADA || this.status == EstadoPedido.CANCELAMENTO_SOLICITADO) {
            this.produtosAlteracao = null;
            this.entregaAlteracao = null;
            if (this.estadoAnterior != null) {
                this.status = this.estadoAnterior; // Restaura o estado anterior
                this.estadoAnterior = null;
            }
        }
    }

    // Getters
    @Override
    public String getId() { return id; }
    public Date getData() { return data; }
    public Cliente getCliente() { return cliente; }
    public Vendedor getVendedor() { return vendedor; }
    public EstadoPedido getStatus() { return status; }
    public Franquia getFranquia() { return franquia; }
    public String getClienteId() { return clienteId; }
    public String getVendedorId() { return vendedorId; }
    public String getFranquiaId() { return franquiaId; }
    public Map<Produto, Integer> getProdutosQuantidade() { return new HashMap<>(produtosQuantidade); }
    public TiposPagamento getFormaPagamento() { return formaPagamento; }
    public TiposEntrega getMetodoEntrega() { return metodoEntrega; }
    public double getValorFrete() { return valorFrete; }
    public double getValorTotal() { return valorTotal; }
    public Map<Produto, Integer> getProdutosAlteracao() {
        return produtosAlteracao != null ? new HashMap<>(produtosAlteracao) : null;
    }
    public TiposEntrega getEntregaAlteracao() {
        return entregaAlteracao;
    }
    public EstadoPedido getEstadoAnterior() {
        return estadoAnterior;
    }

    // Setters
    public void setData(Date data) { this.data = data; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public void setVendedor(Vendedor vendedor) { this.vendedor = vendedor; }
    public void setFranquia(Franquia franquia) { this.franquia = franquia; }

    public void aprovarPedido() { this.status = EstadoPedido.APROVADO; }
    public void cancelarPedido() { this.status = EstadoPedido.CANCELADO; }

    // Métodos de verificação de estado
    public boolean isPendente() { return status == EstadoPedido.PENDENTE; }
    public boolean isAprovado() { return status == EstadoPedido.APROVADO; }
    public boolean isCancelado() { return status == EstadoPedido.CANCELADO; }
    public boolean isAlteracaoSolicitada() { return status == EstadoPedido.ALTERACAO_SOLICITADA; }
    public boolean isCancelamentoSolicitado() { return status == EstadoPedido.CANCELAMENTO_SOLICITADO; }
}
