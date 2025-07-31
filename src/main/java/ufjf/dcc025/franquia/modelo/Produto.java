package ufjf.dcc025.franquia.modelo;

public class Produto {

    private String nome;
    private String descricao;
    private double preco;

    public Produto(String nome, String descricao, double preco) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public double getPreco() { return preco; }

    @Override
    public String toString() {
        return nome + " | R$" + preco;
    }
}