package ufjf.dcc025.franquia.service;

import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.exception.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GerenteService {
    private final Gerente gerente;
    private final EntityRepository<Vendedor> vendedorRepository;
    private final EntityRepository<Pedido> pedidoRepository;
    private final EntityRepository<Franquia> franquiaRepository;

    public GerenteService(Gerente gerente, EntityRepository<Vendedor> vendedorRepository,
                          EntityRepository<Pedido> pedidoRepository, EntityRepository<Franquia> franquiaRepository) {
        this.gerente = gerente;
        this.vendedorRepository = vendedorRepository;
        this.pedidoRepository = pedidoRepository;
        this.franquiaRepository = franquiaRepository;
    }

    // Gerenciamento de Vendedores
    public Vendedor cadastrarVendedor(String nome, String cpf, String email, String senha) {
        Vendedor novoVendedor = new Vendedor(nome, cpf, email, senha, gerente.getFranquia());
        vendedorRepository.upsert(novoVendedor);
        vendedorRepository.saveAllAsync();
        franquiaRepository.upsert(gerente.getFranquia());
        franquiaRepository.saveAllAsync();
        return novoVendedor;
    }

    public void removerVendedor(String vendedorId) {
        Vendedor vendedor = vendedorRepository.findById(vendedorId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(vendedorId));
        gerente.getFranquia().removerVendedor(vendedor);
        vendedorRepository.delete(vendedorId);
        vendedorRepository.saveAllAsync();
        franquiaRepository.saveAllAsync();
    }

    public Vendedor editarVendedor(String idVendedor, String novoNome, String novoCpf, String novoEmail, String novaSenha) {
        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(idVendedor));
        vendedor.setNome(novoNome);
        vendedor.setCpf(novoCpf);
        vendedor.setEmail(novoEmail);
        vendedor.setSenha(novaSenha);
        vendedorRepository.upsert(vendedor);
        vendedorRepository.saveAllAsync();
        return vendedor;
    }

    public List<Vendedor> listarVendedoresDaFranquia() {
        if (gerente.getFranquia() == null) {
            return new ArrayList<>();
        }
        String franquiaId = gerente.getFranquia().getId();
        return vendedorRepository.findAll().stream()
                .filter(v -> v.getFranquia() != null && v.getFranquia().getId().equals(franquiaId))
                .collect(Collectors.toList());
    }

    public void criarNovoProduto(String codigo, String nome, String descricao, double preco, int quantidadeInicial) {
        for (Franquia franquia : franquiaRepository.findAll()) {
            if (franquia.buscarProduto(codigo) != null) {
                throw new DadosInvalidosException("Código '" + codigo + "' já cadastrado no sistema.");
            }
        }
        Produto novoProduto = new Produto(codigo, nome, descricao, preco);
        for (Franquia franquia : franquiaRepository.findAll()) {
            franquia.adicionarProduto(novoProduto, 0);
            franquiaRepository.upsert(franquia);
        }
        gerente.getFranquia().atualizarEstoque(novoProduto, quantidadeInicial);
        franquiaRepository.upsert(gerente.getFranquia());
        franquiaRepository.saveAllAsync();
    }

    public void editarProduto(String codigo, String novoNome, String novaDescricao, double novoPreco) {
        Produto produtoAtualizado = new Produto(codigo, novoNome, novaDescricao, novoPreco);
        for (Franquia franquia : franquiaRepository.findAll()) {
            Produto produtoAntigo = franquia.buscarProduto(codigo);
            if (produtoAntigo != null) {
                int quantidadeAtual = franquia.getEstoque().get(produtoAntigo);
                franquia.removerProduto(produtoAntigo);
                franquia.adicionarProduto(produtoAtualizado, quantidadeAtual);
                franquiaRepository.upsert(franquia);
            }
        }
        franquiaRepository.saveAllAsync();
    }

    public void atualizarEstoqueProduto(String codigoProduto, int novaQuantidade) {
        Franquia franquiaGerente = gerente.getFranquia();
        Produto produto = franquiaGerente.buscarProduto(codigoProduto);
        if (produto == null) {
            throw new EntidadeNaoEncontradaException(codigoProduto);
        }
        if (novaQuantidade < 0) {
            throw new DadosInvalidosException("Quantidade não pode ser negativa.");
        }
        int quantidadeAtual = franquiaGerente.getEstoque().getOrDefault(produto, 0);
        franquiaGerente.atualizarEstoque(produto, novaQuantidade - quantidadeAtual);
        franquiaRepository.upsert(franquiaGerente);
        franquiaRepository.saveAllAsync();
    }

    public Gerente getGerente() {
        return gerente;
    }

    public Franquia getFranquia() {
        return gerente.getFranquia();
    }
}
