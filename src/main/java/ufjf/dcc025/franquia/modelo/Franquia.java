package ufjf.dcc025.franquia.modelo;
import ufjf.dcc025.franquia.enums.TipoUsuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        this.estoque = new ArrayList<>();
        this.receita = 0.0;
    }

    public void adicionarVendedor(Vendedor vendedor) {
        if (!vendedores.contains(vendedor)) {
            vendedores.add(vendedor);
        }
    }

    public void adicionarProduto(Produto produto) {
        estoque.add(produto);
    }

    public void atualizarReceita(double valor) {
        this.receita += valor;
    }

    // Getters e Setters
    public String getNome() { 
        return nome; 
    }
    public String getEndereco() { 
        return endereco; 
    }


    public void setGerente(String id, EntityRepository<Gerente> gerentesValidos) {
        Gerente gerente = gerentesValidos.findById(id);
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
    public List<Produto> getEstoque() { 
        return new ArrayList<>(estoque); 
    }
    public double getReceita() { 
        return receita; 
    }

    @Override
    public String toString() {
        return "Franquia: " + nome + " | Gerente: " + gerente.getNome() + " | Receita: R$" + receita;
    }
}