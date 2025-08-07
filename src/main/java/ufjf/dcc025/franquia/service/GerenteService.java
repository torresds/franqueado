// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.service;

import ufjf.dcc025.franquia.enums.EstadoPedido;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.exception.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GerenteService {
    private final Gerente gerente;
    private final EntityRepository<Vendedor> vendedorRepository;
    private final EntityRepository<Pedido> pedidoRepository;
    private final EntityRepository<Franquia> franquiaRepository;
    private final EntityRepository<Cliente> clienteRepository;
    public GerenteService(Gerente gerente, EntityRepository<Vendedor> vendedorRepository,
                          EntityRepository<Pedido> pedidoRepository, EntityRepository<Franquia> franquiaRepository, EntityRepository<Cliente> clienteRepository) {
        this.gerente = gerente;
        this.vendedorRepository = vendedorRepository;
        this.pedidoRepository = pedidoRepository;
        this.franquiaRepository = franquiaRepository;
        this.clienteRepository = clienteRepository;
    }

    public Vendedor cadastrarVendedor(String nome, String cpf, String email, String senha) {
        if (gerente.getFranquia() == null) {
            throw new DadosInvalidosException("Gerente não está associado a nenhuma franquia.");
        }

        Vendedor novoVendedor = new Vendedor(nome, cpf, email, senha);
        novoVendedor.setFranquia(gerente.getFranquia());
        gerente.getFranquia().adicionarVendedor(novoVendedor);

        vendedorRepository.upsert(novoVendedor);
        franquiaRepository.upsert(gerente.getFranquia());

        vendedorRepository.saveAllAsync();
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

    /**
     * NOVO: Remove um produto do catálogo da franquia.
     * @param codigoProduto O código do produto a ser removido.
     */
    public void removerProduto(String codigoProduto) {
        Franquia franquiaGerente = gerente.getFranquia();
        if (franquiaGerente == null) {
            throw new DadosInvalidosException("Gerente não está associado a nenhuma franquia.");
        }
        Produto produto = franquiaGerente.buscarProduto(codigoProduto);
        if (produto == null) {
            throw new EntidadeNaoEncontradaException(codigoProduto);
        }

        // Regra de negócio: não permitir remover produto se ainda houver em estoque.
        if (franquiaGerente.getEstoque().get(produto) > 0) {
            throw new DadosInvalidosException("Não é possível remover o produto pois ainda existem unidades em estoque.");
        }

        franquiaGerente.removerProduto(produto);
        franquiaRepository.upsert(franquiaGerente);
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

    public List<Map.Entry<Produto, Integer>> listarProdutosComEstoqueBaixo(int limite) {
        if (gerente.getFranquia() == null) {
            return new ArrayList<>();
        }
        return gerente.getFranquia().getEstoque().entrySet().stream()
                .filter(entry -> entry.getValue() < limite)
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());
    }

    public List<Pedido> listarPedidosDaFranquia() {
        if (gerente.getFranquia() == null) {
            return new ArrayList<>();
        }
        String franquiaId = gerente.getFranquia().getId();
        return pedidoRepository.findAll().stream()
                .filter(p -> p.getFranquia().getId().equals(franquiaId))
                .sorted(Comparator.comparing(Pedido::getData).reversed())
                .collect(Collectors.toList());
    }

    public Pedido aceitarPedido(String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(pedidoId));

        if (pedido.isPendente()) {
            pedido.aprovarPedido();
            Map<Produto, Integer> produtos = pedido.getProdutosQuantidade();
            for (Map.Entry<Produto, Integer> entry : produtos.entrySet()) {
                gerente.getFranquia().atualizarEstoque(entry.getKey(), -entry.getValue());
            }
            pedido.getVendedor().atualizarTotalVendas(pedido.getValorTotal());
            pedido.getFranquia().atualizarReceita(pedido.getValorTotal());

            persistAll(pedido);
        }
        return pedido;
    }

    public Pedido cancelarPedido(String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(pedidoId));

        if (pedido.isAprovado()) {
            Map<Produto, Integer> produtos = pedido.getProdutosQuantidade();
            for (Map.Entry<Produto, Integer> entry : produtos.entrySet()) {
                gerente.getFranquia().atualizarEstoque(entry.getKey(), entry.getValue());
            }
            pedido.getVendedor().atualizarTotalVendas(-pedido.getValorTotal());
            pedido.getFranquia().atualizarReceita(-pedido.getValorTotal());
        }

        pedido.cancelarPedido();
        persistAll(pedido);
        return pedido;
    }

    public void aprovarAlteracao(String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(pedidoId));

        if (!pedido.isAlteracaoSolicitada()) {
            throw new DadosInvalidosException("O pedido não está aguardando aprovação de alteração.");
        }

        Map<Produto, Integer> produtosOriginais = pedido.getProdutosQuantidade();
        double valorTotalOriginal = pedido.getValorTotal();
        EstadoPedido estadoOriginal = pedido.getEstadoAnterior();

        if (estadoOriginal == EstadoPedido.APROVADO) {
            for (Map.Entry<Produto, Integer> entry : produtosOriginais.entrySet()) {
                gerente.getFranquia().atualizarEstoque(entry.getKey(), entry.getValue());
            }
            pedido.getVendedor().atualizarTotalVendas(-valorTotalOriginal);
            pedido.getFranquia().atualizarReceita(-valorTotalOriginal);
        }

        pedido.aplicarAlteracaoAprovada();

        Map<Produto, Integer> produtosNovos = pedido.getProdutosQuantidade();
        for (Map.Entry<Produto, Integer> entry : produtosNovos.entrySet()) {
            gerente.getFranquia().atualizarEstoque(entry.getKey(), -entry.getValue());
        }
        pedido.getVendedor().atualizarTotalVendas(pedido.getValorTotal());
        pedido.getFranquia().atualizarReceita(pedido.getValorTotal());

        persistAll(pedido);
    }

    public void aprovarCancelamento(String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(pedidoId));

        if (!pedido.isCancelamentoSolicitado()) {
            throw new DadosInvalidosException("O pedido não está aguardando aprovação de cancelamento.");
        }

        if (pedido.getEstadoAnterior() == EstadoPedido.APROVADO) {
            for (Map.Entry<Produto, Integer> entry : pedido.getProdutosQuantidade().entrySet()) {
                gerente.getFranquia().atualizarEstoque(entry.getKey(), entry.getValue());
            }
            pedido.getVendedor().atualizarTotalVendas(-pedido.getValorTotal());
            pedido.getFranquia().atualizarReceita(-pedido.getValorTotal());
        }

        pedido.cancelarPedido();
        persistAll(pedido);
    }

    public void negarSolicitacao(String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(pedidoId));

        if (!pedido.isAlteracaoSolicitada() && !pedido.isCancelamentoSolicitado()) {
            throw new DadosInvalidosException("O pedido não possui solicitações pendentes.");
        }

        pedido.negarSolicitacao();

        pedidoRepository.upsert(pedido);
        pedidoRepository.saveAllAsync();
    }

    private void persistAll(Pedido pedido) {
        pedidoRepository.upsert(pedido);
        franquiaRepository.upsert(gerente.getFranquia());
        vendedorRepository.upsert(pedido.getVendedor());

        pedidoRepository.saveAllAsync();
        franquiaRepository.saveAllAsync();
        vendedorRepository.saveAllAsync();
    }

    public List<Vendedor> rankingVendedoresDaFranquia() {
        return listarVendedoresDaFranquia().stream()
                .sorted(Comparator.comparingDouble(Vendedor::getTotalVendas).reversed())
                .collect(Collectors.toList());
    }

    public List<Map.Entry<Produto, Long>> relatorioProdutosMaisVendidos() {
        if (getFranquia() == null) return new ArrayList<>();

        return listarPedidosDaFranquia().stream()
                .filter(Pedido::isAprovado)
                .flatMap(p -> p.getProdutosQuantidade().entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)))
                .entrySet().stream()
                .sorted(Map.Entry.<Produto, Long>comparingByValue().reversed())
                .collect(Collectors.toList());
    }

    public List<Map.Entry<Cliente, Long>> relatorioClientesMaisFrequentes() {
        if (getFranquia() == null) return new ArrayList<>();

        return listarPedidosDaFranquia().stream()
                .filter(Pedido::isAprovado)
                .collect(Collectors.groupingBy(Pedido::getCliente, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Cliente, Long>comparingByValue().reversed())
                .collect(Collectors.toList());
    }


    public Gerente getGerente() {
        return gerente;
    }

    public Franquia getFranquia() {
        return gerente.getFranquia();
    }
}
