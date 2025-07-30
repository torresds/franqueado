package ufjf.dcc025.franquia.modelo;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import ufjf.dcc025.franquia.enums.TiposPagamento;
import ufjf.dcc025.franquia.enums.TiposEntrega;

public class Pedido implements Identifiable {

    private final String id;
    private final Date data;
    private final Cliente cliente;
    private final Vendedor vendedor;
    private Map<Produto, Integer> produtosQuantidade;
    private final TiposPagamento formaPagamento;
    private TiposEntrega metodoEntrega;
    private double valorTotal;
    private double valorFrete;
    private String status; // "Pendente", "Aprovado", "Cancelado"

    public Pedido(Cliente cliente, Map<Produto, Integer> produtos, String franquiaId, TiposPagamento formaPagamento, TiposEntrega metodoEntrega, String id, Vendedor vendedor) {
        this.id = id;
        this.data = new Date();
        this.cliente = cliente;
        this.vendedor = vendedor;
        this.produtosQuantidade = new HashMap<>(produtos);
        this.formaPagamento = formaPagamento;
        this.metodoEntrega = metodoEntrega;
        this.valorFrete = calcularFrete();
        this.valorTotal = calcularValorTotal();
        this.status = "Pendente";
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
        } else {
            return 15; 
        }
    }

    public void adicionarProduto(Produto produto, int quantidade) {
        produtosQuantidade.merge(produto, quantidade, Integer::sum);
        atualizarValores();
    }

    public void removerProduto(Produto produto) {
        produtosQuantidade.remove(produto);
        atualizarValores();
    }

    private void atualizarValores() {
        this.valorFrete = calcularFrete();
        this.valorTotal = calcularValorTotal();
    }

    // Getters básicos
    @Override
    public String getId() { return id; }
    public Date getData() { return data; }
    public Cliente getCliente() { return cliente; }
    public Vendedor getVendedor() { return vendedor; }
    public String getStatus() { return status; }
    
    // Getters de produtos e pagamento
    public Map<Produto, Integer> getProdutosQuantidade() { return new HashMap<>(produtosQuantidade); }
    public TiposPagamento getFormaPagamento() { return formaPagamento; }
    
    // Getters de entrega
    public TiposEntrega getMetodoEntrega() { return metodoEntrega; }
    public double getValorFrete() { return valorFrete; }
    
    // Getters de valores
    public double getValorTotal() { return valorTotal; }
    
    // Métodos de controle do pedido
    public void aprovarPedido() { this.status = "Aprovado"; }
    public void cancelarPedido() { this.status = "Cancelado"; }

    //Setters
    public void setMetodoEntrega (TiposEntrega metodoEntrega){
        this.metodoEntrega = metodoEntrega;
    }

    @Override
    public String toString() {
        return "Pedido #" + id + " | Cliente: " + cliente.getNome() + " | Valor: R$" + valorTotal;
    }
}