package ufjf.dcc025.franquia.modelo;
import java.util.List;

import ufjf.dcc025.franquia.enums.TipoUsuario;

public class Dono extends Usuario {
    
    public Dono(String nome, String cpf, String email, String senha, String id) {
        super(nome, cpf, email, senha, id);
    }

    //------------ GERENCIAMENTO DE FRANQUIAS ------------

    public Franquia cadastrarFranquia(String nome, String endereco, String id, String gerenteId, EntityRepository<Gerente> gerentesValidos, EntityRepository<Franquia> franquiasValidas) {
        Franquia novaFranquia = new Franquia(nome, endereco, id, gerenteId, gerentesValidos);
        franquiasValidas.upsert(novaFranquia);
        return novaFranquia;
    }
    public void removerFranquia(EntityRepository<Franquia> franquias, String idFranquia) {
        franquias.delete(idFranquia);
    }
    public List<Franquia> listarFranquias(EntityRepository<Franquia> franquias) {
        return franquias.findAll();
    }
 
    //------------ GERENCIAMENTO DE USUÁRIOS ------------

    public Gerente cadastrarGerente(String nome, String cpf, String email, String senha, String id, EntityRepository<Gerente> gerentesValidos) {
        Gerente novoGerente = new Gerente(nome, cpf, email, senha, id);
        gerentesValidos.upsert(novoGerente);
        return novoGerente;
    }

    public void removerGerente(EntityRepository<Gerente> gerentes, String idGerente) {
        gerentes.delete(idGerente);
    }

    public List<Gerente> listarGerentes(EntityRepository<Gerente> gerentes) {
        return gerentes.findAll();
    }

    //botar função de atualizar informações de franquia e gerente, se necessário
    //botar função de ver desempenho de franquias o faturamento bruto, o número total de pedidos e o ticket médio
    //função de ver ranking de vendedores por franquia
    //Adicionar logica de colocar a franquia no gerente
    

    @Override
    public String toString() {
        return "Dono: " + getNome();
    }

    @Override
    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.DONO;
    }
}