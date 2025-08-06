// FILE: src/main/java/ufjf/dcc025/franquia/controller/GerenteController.java
package ufjf.dcc025.franquia.controller;

import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.service.GerenteService;
import ufjf.dcc025.franquia.model.produtos.Produto;

import java.util.List;
import java.util.Map;

public class GerenteController {
    public final GerenteService gerenteService;

    public GerenteController(GerenteService gerenteService) {
        this.gerenteService = gerenteService;
    }

    // MÉTODOS PARA A VIEW - VENDEDORES
    public List<Vendedor> getVendedoresDaFranquia() {
        return gerenteService.listarVendedoresDaFranquia();
    }

    public void addVendedor(String nome, String cpf, String email, String senha) {
        gerenteService.cadastrarVendedor(nome, cpf, email, senha);
    }

    public void updateVendedor(String id, String nome, String cpf, String email, String senha) {
        gerenteService.editarVendedor(id, nome, cpf, email, senha);
    }

    public void deleteVendedor(String id) {
        gerenteService.removerVendedor(id);
    }

    // MÉTODOS PARA A VIEW - ESTOQUE/PRODUTOS
    public Map<Produto, Integer> getEstoque() {
        return gerenteService.getFranquia().getEstoque();
    }

    public void addProduto(String codigo, String nome, String descricao, double preco, int quantidade) {
        gerenteService.criarNovoProduto(codigo, nome, descricao, preco, quantidade);
    }

    public void updateProduto(String codigo, String nome, String descricao, double preco) {
        gerenteService.editarProduto(codigo, nome, descricao, preco);
    }

    public void updateEstoque(String codigoProduto, int novaQuantidade) {
        gerenteService.atualizarEstoqueProduto(codigoProduto, novaQuantidade);
    }
}
