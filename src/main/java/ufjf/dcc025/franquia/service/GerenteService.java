package ufjf.dcc025.franquia.service;

import ufjf.dcc025.franquia.enums.EstadoPedido;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.exception.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public List<Vendedor> listarVendedoresPorVendas() {
        List<Vendedor> listaVendedores = vendedorRepository.findAll();
        listaVendedores.sort((v1, v2) -> Double.compare(v2.getTotalVendas(), v1.getTotalVendas()));
        return listaVendedores;
    }

    // Gerenciamento de Pedidos
    public Pedido aceitarPedido(String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(pedidoId));
        if (gerente.getPedidosPendentesId().contains(pedidoId)) {
            pedido.aprovarPedido();
            gerente.getPedidosPendentesId().remove(pedidoId);
            Map<Produto, Integer> produtos = pedido.getProdutosQuantidade();
            for (Map.Entry<Produto, Integer> entry : produtos.entrySet()) {
                gerente.getFranquia().atualizarEstoque(entry.getKey(), -entry.getValue());
            }
            pedido.atualizarValores();
            pedido.getVendedor().atualizarTotalVendas(pedido.getValorTotal());
            pedido.getFranquia().atualizarReceita(pedido.getValorTotal());
            pedidoRepository.upsert(pedido);
            franquiaRepository.upsert(gerente.getFranquia());
            vendedorRepository.upsert(pedido.getVendedor());
            pedidoRepository.saveAllAsync();
            franquiaRepository.saveAllAsync();
            vendedorRepository.saveAllAsync();
        }
        if (gerente.getPedidosParaCancelarId().contains(pedidoId)) {
            gerente.getPedidosParaCancelarId().remove(pedidoId);
        }
        return pedido;
    }

    public Pedido cancelarPedido(String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(pedidoId));
        if (gerente.getPedidosPendentesId().contains(pedidoId)) {
            pedido.cancelarPedido();
            gerente.getPedidosPendentesId().remove(pedidoId);
        }
        if (gerente.getPedidosParaCancelarId().contains(pedidoId)) {
            if (pedido.getStatus() == EstadoPedido.APROVADO) {
                Map<Produto, Integer> produtos = pedido.getProdutosQuantidade();
                for (Map.Entry<Produto, Integer> entry : produtos.entrySet()) {
                    gerente.getFranquia().atualizarEstoque(entry.getKey(), entry.getValue());
                }
                pedido.atualizarValores();
                pedido.getVendedor().atualizarTotalVendas(-pedido.getValorTotal());
                pedido.getFranquia().atualizarReceita(-pedido.getValorTotal());
                franquiaRepository.upsert(gerente.getFranquia());
                vendedorRepository.upsert(pedido.getVendedor());
            }
            pedido.cancelarPedido();
            gerente.getPedidosParaCancelarId().remove(pedidoId);
        }
        pedidoRepository.upsert(pedido);
        pedidoRepository.saveAllAsync();
        return pedido;
    }

    public List<Pedido> listarPedidosPendentes() {
        List<Pedido> pedidosPendentes = new ArrayList<>();
        for (String pedidoId : gerente.getPedidosPendentesId()) {
            Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
            if (pedido != null && pedido.isPendente()) {
                pedidosPendentes.add(pedido);
            }
        }
        return pedidosPendentes;
    }

    public List<Pedido> listarPedidosParaCancelar() {
        List<Pedido> pedidosParaCancelar = new ArrayList<>();
        for (String pedidoId : gerente.getPedidosParaCancelarId()) {
            Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
            if (pedido != null) {
                pedidosParaCancelar.add(pedido);
            }
        }
        return pedidosParaCancelar;
    }

    public void aceitarAlteracaoPedido(Pedido pedido) {
        if (gerente.getAlteracoesPedidos().contains(pedido)) {
            Pedido pedidoOriginal = pedidoRepository.findById(pedido.getId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException(pedido.getId()));
            if (pedido.getMetodoEntrega() == pedidoOriginal.getMetodoEntrega()) {
                Map<Produto, Integer> produtosNovos = pedido.getProdutosQuantidade();
                Map<Produto, Integer> produtosAntigos = pedidoOriginal.getProdutosQuantidade();
                for (Produto produto : produtosNovos.keySet()) {
                    if (!produtosAntigos.containsKey(produto)) {
                        gerente.getFranquia().atualizarEstoque(produto, produtosAntigos.getOrDefault(produto, 0) - produtosNovos.get(produto));
                    } else {
                        gerente.getFranquia().atualizarEstoque(produto, -produtosNovos.get(produto));
                    }
                }
                for (Produto produto : produtosAntigos.keySet()) {
                    if (!produtosNovos.containsKey(produto)) {
                        gerente.getFranquia().atualizarEstoque(produto, produtosAntigos.get(produto));
                    }
                }
            }
            double valorAntigo = pedidoOriginal.getValorTotal();
            double valorNovo = pedido.getValorTotal();
            pedido.getVendedor().atualizarTotalVendas(valorNovo - valorAntigo);
            pedido.getFranquia().atualizarReceita(valorNovo - valorAntigo);
            pedido.aprovarPedido();
            pedidoRepository.upsert(pedido);
            gerente.getAlteracoesPedidos().remove(pedido);
            pedidoRepository.saveAllAsync();
            franquiaRepository.saveAllAsync();
            vendedorRepository.saveAllAsync();
        }
    }

    public void cancelarAlteracaoPedido(Pedido pedido) {
        if (gerente.getAlteracoesPedidos().contains(pedido)) {
            pedido.cancelarPedido();
            gerente.getAlteracoesPedidos().remove(pedido);
            pedidoRepository.upsert(pedido);
            pedidoRepository.saveAllAsync();
        }
    }

    // Gerenciamento de Produtos
    public void criarNovoProduto(String codigo, String nome, String descricao, double preco, int quantidadeInicial) {
        for (Franquia franquia : franquiaRepository.findAll()) {
            Produto produtoExistente = franquia.buscarProduto(codigo);
            if (produtoExistente != null) {
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
        Produto produtoExistente = null;
        for (Franquia franquia : franquiaRepository.findAll()) {
            produtoExistente = franquia.buscarProduto(codigo);
            if (produtoExistente != null) {
                break;
            }
        }
        if (produtoExistente == null) {
            throw new EntidadeNaoEncontradaException(codigo);
        }
        Produto produtoAtualizado = new Produto(codigo, novoNome, novaDescricao, novoPreco);
        for (Franquia franquia : franquiaRepository.findAll()) {
            Produto produtoNaFranquia = franquia.buscarProduto(codigo);
            if (produtoNaFranquia != null) {
                int quantidadeAtual = franquia.getEstoque().get(produtoNaFranquia);
                franquia.removerProduto(produtoNaFranquia);
                franquia.adicionarProduto(produtoAtualizado, quantidadeAtual);
                franquiaRepository.upsert(franquia);
            }
        }
        franquiaRepository.saveAllAsync();
    }

    public void removerProdutoExistente(String codigoProduto) {
        boolean produtoEncontrado = false;
        for (Franquia franquia : franquiaRepository.findAll()) {
            Produto produto = franquia.buscarProduto(codigoProduto);
            if (produto != null) {
                produtoEncontrado = true;
                franquia.removerProduto(produto);
                franquiaRepository.upsert(franquia);
            }
        }
        if (!produtoEncontrado) {
            throw new EntidadeNaoEncontradaException(codigoProduto);
        }
        franquiaRepository.saveAllAsync();
    }

    public void atualizarEstoqueProduto(String codigoProduto, int novaQuantidade, String franquiaId) {
        Franquia franquiaEscolhida = franquiaRepository.findById(franquiaId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(franquiaId));
        Produto produto = franquiaEscolhida.buscarProduto(codigoProduto);
        if (produto == null) {
            throw new EntidadeNaoEncontradaException(codigoProduto);
        }
        if (novaQuantidade < 0) {
            throw new DadosInvalidosException("Quantidade não pode ser negativa.");
        }
        franquiaEscolhida.atualizarEstoque(produto, novaQuantidade - franquiaEscolhida.getEstoque().getOrDefault(produto, 0));
        franquiaRepository.upsert(franquiaEscolhida);
        franquiaRepository.saveAllAsync();
    }

    public List<String> listarProdutosEstoqueBaixo(String franquiaId, int limiteMinimo) {
        Franquia franquiaEscolhida = franquiaRepository.findById(franquiaId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(franquiaId));
        List<String> produtosEstoqueBaixo = new ArrayList<>();
        for (Map.Entry<Produto, Integer> entry : franquiaEscolhida.getEstoque().entrySet()) {
            if (entry.getValue() <= limiteMinimo) {
                Produto produto = entry.getKey();
                int quantidade = entry.getValue();
                String detalhes = String.format("⚠️ %s - Estoque: %d unidades (Limite: %d)", 
                    produto.toString(), quantidade, limiteMinimo);
                produtosEstoqueBaixo.add(detalhes);
            }
        }
        return produtosEstoqueBaixo;
    }

    public Franquia getFranquia() {
        return gerente.getFranquia();
    }
}