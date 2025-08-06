// FILE: src/main/java/ufjf/dcc025/franquia/controller/DonoController.java
package ufjf.dcc025.franquia.controller;

import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.service.DonoService;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;

import java.util.List;

public class DonoController {
    public final DonoService donoService;

    public DonoController(DonoService donoService) {
        this.donoService = donoService;
    }

    // MÉTODOS PARA A VIEW - FRANQUIAS
    public List<Franquia> getFranquias() {
        return donoService.listarFranquias();
    }
    public void addFranquia(String nome, String endereco, String gerenteId) {
        donoService.cadastrarFranquia(nome, endereco, gerenteId);
    }
    public void updateFranquia(String id, String nome, String endereco) {
        donoService.atualizarFranquia(id, nome, endereco);
    }
    public void deleteFranquia(String id) {
        donoService.removerFranquia(id);
    }

    // MÉTODOS PARA A VIEW - GERENTES
    public List<Gerente> getGerentes() {
        return donoService.listarGerentes();
    }
    public void addGerente(String nome, String cpf, String email, String senha) {
        donoService.cadastrarGerente(nome, cpf, email, senha);
    }
    public void updateGerente(String id, String nome, String cpf, String email, String senha) {
        donoService.atualizarGerente(id, nome, cpf, email, senha, null);
    }
    public void deleteGerente(String id) {
        donoService.removerGerente(id);
    }
    public void assignManagerToFranchise(String gerenteId, String franquiaId) {
        donoService.setGerenteFranquia(franquiaId, gerenteId);
    }
    public void unassignManager(String gerenteId) {
        Gerente gerente = donoService.getGerenteRepo().findById(gerenteId).orElse(null);
        if (gerente != null && gerente.getFranquia() != null) {
            donoService.removerGerenteDaFranquia(gerente.getFranquia().getId());
        }
    }

    // MÉTODOS PARA A VIEW - DESEMPENHO
    public List<Franquia> getFranquiasPorDesempenho() {
        return donoService.listarFranquiasPorDesempenho();
    }
    public List<Vendedor> getVendedoresPorDesempenho() {
        return donoService.rankingVendedores();
    }

    public List<Vendedor> getTopVendedores(int limit) {
        return donoService.getTopVendedores(limit);
    }
}
