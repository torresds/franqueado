package ufjf.dcc025.franquia.modelo;
import ufjf.dcc025.franquia.enums.TipoUsuario;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.html.parser.Entity;

public class Gerente extends Usuario {
    private Franquia franquia;

    public Gerente(String nome, String cpf, String email, String senha, String id, Franquia franquia) {
        super(nome, cpf, email, senha, id);
        this.franquia = franquia;
    }


    //------------ GERENCIAMENTO DE VENDEDORES ------------

    public Vendedor cadastrarVendedor(String nome, String cpf, String email, String senha, String id, EntityRepository<Vendedor> vendedoresValidos) {
        Vendedor novoVendedor = new Vendedor(nome, cpf, email, senha, id, this.franquia);
        vendedoresValidos.upsert(novoVendedor);
        return novoVendedor;
    }

    public void removerVendedor(String idVendedor, EntityRepository<Vendedor> vendedores) {
        vendedores.delete(idVendedor);
    }

    public List<Vendedor> listarVendedoresPorVendas(EntityRepository<Vendedor> vendedores) {
        List<Vendedor> listaVendedores = vendedores.findAll();
        listaVendedores.sort((v1, v2) -> Double.compare(v2.getTotalVendas(), v1.getTotalVendas()));
        return listaVendedores;
    }

    //------------ GERENCIAMENTO DE PEDIDOS ------------



    @Override
    public String toString() {
        return "Gerente: " + getNome();
    }

    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.GERENTE;
    }

    public String getUnidadeFranquiaId() {
        return unidadeFranquiaId;
    }

    public void setUnidadeFranquiaId(String unidadeFranquiaId) {
        this.unidadeFranquiaId = unidadeFranquiaId;
    }
}
