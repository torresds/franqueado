package ufjf.dcc025.franquia.modelo;

public class Produto implements Identifiable {

    private String id; 
    private String nome;
    private String descricao;
    private double preco;
    private int quantidadeEstoque;

    public Produto(String nome, String descricao, double preco, int quantidadeEstoque) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public void atualizarEstoque(int quantidade) {
        this.quantidadeEstoque += quantidade;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public double getPreco() { return preco; }
    public int getQuantidadeEstoque() { return quantidadeEstoque; }

    @Override
    public String toString() {
        return nome + " | R$" + preco + " | Estoque: " + quantidadeEstoque;
    }

      @Override
    public String getId(){
        return id;
    }
}