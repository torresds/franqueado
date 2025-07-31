package ufjf.dcc025.franquia.modelo;
import ufjf.dcc025.franquia.enums.TipoUsuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Franquia implements Identifiable {
    private String id;
    private String nome;
    private String endereco;
    private Gerente gerente;
    private List<Vendedor> vendedores;
    private Map<Produto, Integer> estoque;
    private double receita;

    public Franquia(String nome, String endereco, String id, String gerenteId, EntityRepository<Gerente> gerentesValidos) {
        this.nome = nome;
        this.id = id;
        this.endereco = endereco;
        setGerente(gerente, gerentesValidos);
        this.vendedores = new ArrayList<>();
        this.estoque = new HashMap<>();
        this.receita = 0.0;
    }

    
    public void adicionarVendedor(Vendedor vendedor) {
        if (!vendedores.contains(vendedor)) {
            vendedores.add(vendedor);
        }
    }

    public void removerVendedor(Vendedor vendedor) {
        vendedores.remove(vendedor);
    }

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
    
    public void atualizarReceita(double valor) {
        this.receita += valor;
    }
    
    // Getters e Setters
    @Override
    public String getId() {
        return id;
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