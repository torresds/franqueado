package ufjf.dcc025.franquia.modelo;

public class Produto {

    private String nome;
    private String descricao;
    private double preco;

    public Produto(String nome, String descricao, double preco) {
        setNome(nome);
        setDescricao(descricao);
        setPreco(preco);
    }

    // Métodos de validação e setters privados
    private void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto não pode ser vazio.");
        }
        if (nome.trim().length() < 2) {
            throw new IllegalArgumentException("Nome do produto deve ter pelo menos 2 caracteres.");
        }
        this.nome = nome.trim();
    }

    private void setDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição do produto não pode ser vazia.");
        }
        if (descricao.trim().length() < 5) {
            throw new IllegalArgumentException("Descrição deve ter pelo menos 5 caracteres.");
        }
        this.descricao = descricao.trim();
    }

    private void setPreco(double preco) {
        if (preco <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero.");
        }
        if (preco > 999999.99) {
            throw new IllegalArgumentException("Preço não pode exceder R$ 999.999,99.");
        }
        this.preco = preco;
    }

    public void atualizarNome(String nome) {
        setNome(nome);
    }

    public void atualizarDescricao(String descricao) {
        setDescricao(descricao);
    }

    public void atualizarPreco(double preco) {
        setPreco(preco);
    }

    public boolean isValido() {
        try {
            validarProduto();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void validarProduto() {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome inválido");
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição inválida");
        }
        if (preco <= 0) {
            throw new IllegalArgumentException("Preço inválido");
        }
    }

    // Getters
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public double getPreco() { return preco; }

    @Override
    public String toString() {
        return nome + " | R$" + String.format("%.2f", preco);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Produto produto = (Produto) obj;
        return nome.equals(produto.nome);
    }

    @Override
    public int hashCode() {
        return nome.hashCode();
    }
}