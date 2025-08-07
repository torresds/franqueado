// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.model.franquia;

import ufjf.dcc025.franquia.exception.DadosInvalidosException;
import ufjf.dcc025.franquia.exception.EntidadeNaoEncontradaException;
import ufjf.dcc025.franquia.exception.EstoqueInsuficienteException;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.persistence.Identifiable;
import java.util.*;

public class Franquia implements Identifiable {
    private final String id;
    private String nome;
    private String endereco;
    private String gerenteId;
    private List<String> vendedorIds = new ArrayList<>();
    private List<String> pedidosId = new ArrayList<>();
    private Map<Produto, Integer> estoque = new HashMap<>();
    private double receita;

    private transient Gerente gerente;
    private transient List<Vendedor> vendedores = new ArrayList<>();

    public Franquia(String nome, String endereco, Gerente gerente) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.endereco = endereco;
        setGerente(gerente);
    }

    // GETTERS
    @Override
    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getGerenteId() { return gerenteId; }
    public Gerente getGerente() { return gerente; }
    public List<Vendedor> getVendedores() { return vendedores != null ? new ArrayList<>(vendedores) : new ArrayList<>(); }
    public Map<Produto, Integer> getEstoque() { return estoque; }
    public double getReceita() { return receita; }
    public int quantidadePedidos() { return pedidosId.size(); }
    public List<String> getPedidosId() { return new ArrayList<>(pedidosId); }

    // SETTERS E MODIFICADORES
    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public void setGerente(Gerente gerente) {
        this.gerente = gerente;
        this.gerenteId = (gerente != null) ? gerente.getId() : null;
    }

    public void adicionarVendedor(Vendedor vendedor) {
        if (this.vendedores == null) this.vendedores = new ArrayList<>();
        if (this.vendedorIds == null) this.vendedorIds = new ArrayList<>();
        if (vendedor != null && !vendedorIds.contains(vendedor.getId())) {
            this.vendedores.add(vendedor);
            this.vendedorIds.add(vendedor.getId());
        }
    }

    public void removerVendedor(Vendedor vendedor) {
        if (vendedor != null) {
            if (this.vendedores != null) this.vendedores.remove(vendedor);
            if (this.vendedorIds != null) this.vendedorIds.remove(vendedor.getId());
        }
    }

    public void adicionarPedido(String pedidoId) {
        if (this.pedidosId == null) this.pedidosId = new ArrayList<>();
        if (!this.pedidosId.contains(pedidoId)) {
            this.pedidosId.add(pedidoId);
        }
    }

    public void adicionarProduto(Produto produto, int quantidade) {
        this.estoque.put(produto, this.estoque.getOrDefault(produto, 0) + quantidade);
    }

    public void removerProduto(Produto produto) {
        if (!this.estoque.containsKey(produto)) {
            throw new EntidadeNaoEncontradaException(produto.getCodigo());
        }
        this.estoque.remove(produto);
    }

    public void atualizarEstoque(Produto produto, int quantidadeDelta) {
        int quantidadeAtual = this.estoque.getOrDefault(produto, 0);
        int novaQuantidade = quantidadeAtual + quantidadeDelta;
        if (novaQuantidade < 0) {
            throw new EstoqueInsuficienteException(produto.getNome(), quantidadeAtual, Math.abs(quantidadeDelta));
        }
        this.estoque.put(produto, novaQuantidade);
    }

    public void atualizarReceita(double valor) {
        this.receita += valor;
    }

    public Produto buscarProduto(String codigo) {
        return this.estoque.keySet().stream()
                .filter(p -> p.getCodigo().equals(codigo))
                .findFirst().orElse(null);
    }
}