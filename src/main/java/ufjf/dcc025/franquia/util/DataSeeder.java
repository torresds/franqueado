// FILE: src/main/java/ufjf/dcc025/franquia/util/DataSeeder.java
package ufjf.dcc025.franquia.util;

import ufjf.dcc025.franquia.enums.TiposEntrega;
import ufjf.dcc025.franquia.enums.TiposPagamento;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.persistence.EntityRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Classe utilitária para popular ou limpar o banco de dados com dados de teste.
 */
public class DataSeeder {

    public enum SeedScenario {
        INITIAL_SETUP, // Cenário básico com pouca movimentação
        BUSY_MONTH,    // Cenário de um mês agitado, com muitas vendas
        NEW_EXPANSION  // Cenário de expansão, com novas lojas
    }

    private final EntityRepository<Franquia> franquiaRepo;
    private final EntityRepository<Vendedor> vendedorRepo;
    private final EntityRepository<Gerente> gerenteRepo;
    private final EntityRepository<Pedido> pedidoRepo;
    private final EntityRepository<Cliente> clienteRepo;
    private final Random random = new Random();

    public DataSeeder(EntityRepository<Franquia> franquiaRepo, EntityRepository<Vendedor> vendedorRepo,
                      EntityRepository<Gerente> gerenteRepo, EntityRepository<Pedido> pedidoRepo,
                      EntityRepository<Cliente> clienteRepo) {
        this.franquiaRepo = franquiaRepo;
        this.vendedorRepo = vendedorRepo;
        this.gerenteRepo = gerenteRepo;
        this.pedidoRepo = pedidoRepo;
        this.clienteRepo = clienteRepo;
    }

    public void clearDatabase() {
        System.out.println("Limpando o banco de dados (exceto Donos)...");
        franquiaRepo.findAll().forEach(f -> franquiaRepo.delete(f.getId()));
        gerenteRepo.findAll().forEach(g -> gerenteRepo.delete(g.getId()));
        vendedorRepo.findAll().forEach(v -> vendedorRepo.delete(v.getId()));
        pedidoRepo.findAll().forEach(p -> pedidoRepo.delete(p.getId()));
        clienteRepo.findAll().forEach(c -> clienteRepo.delete(c.getId()));
        saveAll();
        System.out.println("Banco de dados limpo.");
    }

    public void seedDatabase(SeedScenario scenario) {
        System.out.println("Populando o banco de dados com o cenário: " + scenario);
        clearDatabase();

        List<Produto> produtos = createProducts();
        List<Cliente> clientes = createClientes();

        switch (scenario) {
            case INITIAL_SETUP:
                seedInitialSetup(produtos, clientes);
                break;
            case BUSY_MONTH:
                seedBusyMonth(produtos, clientes);
                break;
            case NEW_EXPANSION:
                seedNewExpansion(produtos, clientes);
                break;
        }

        saveAll();
        System.out.println("Banco de dados populado com sucesso.");
    }

    private void seedInitialSetup(List<Produto> produtos, List<Cliente> clientes) {
        Gerente g1 = new Gerente("Carlos Pereira", "85632348045", "carlos@franquia.com", "senha123");
        gerenteRepo.upsert(g1);

        Franquia f1 = new Franquia("Pizzaria Centro", "Rua Principal, 123", g1);
        g1.setFranquia(f1);
        produtos.forEach(p -> f1.adicionarProduto(p, 30));
        franquiaRepo.upsert(f1);

        Vendedor v1 = new Vendedor("Ana Silva", "99518169047", "ana@franquia.com", "senha123", f1);
        vendedorRepo.upsert(v1);

        createAndProcessOrder(v1, g1, clientes.get(0), Map.of(produtos.get(0), 1, produtos.get(2), 2));
        createAndProcessOrder(v1, g1, clientes.get(1), Map.of(produtos.get(1), 1));
    }

    private void seedBusyMonth(List<Produto> produtos, List<Cliente> clientes) {
        Gerente g1 = new Gerente("Sofia Almeida", "72179856070", "sofia@franquia.com", "senha123");
        Gerente g2 = new Gerente("Mariana Costa", "13275291002", "mariana@franquia.com", "senha123");
        gerenteRepo.upsert(g1);
        gerenteRepo.upsert(g2);

        Franquia f1 = new Franquia("Pizzaria Zona Norte", "Av. Norte, 456", g1);
        g1.setFranquia(f1);
        produtos.forEach(p -> f1.adicionarProduto(p, 150));
        franquiaRepo.upsert(f1);

        Franquia f2 = new Franquia("Pizzaria Zona Sul", "Av. Sul, 789", g2);
        g2.setFranquia(f2);
        produtos.forEach(p -> f2.adicionarProduto(p, 200));
        franquiaRepo.upsert(f2);

        Vendedor v1_1 = new Vendedor("Bruno Souza", "21559890065", "bruno@franquia.com", "senha123", f1);
        Vendedor v1_2 = new Vendedor("Clara Lima", "88393892007", "clara@franquia.com", "senha123", f1);
        Vendedor v2_1 = new Vendedor("Rafael Martins", "72150182093", "rafael@franquia.com", "senha123", f2);
        vendedorRepo.upsert(v1_1);
        vendedorRepo.upsert(v1_2);
        vendedorRepo.upsert(v2_1);

        for (int i = 0; i < 25; i++) {
            createAndProcessOrder(v1_1, g1, clientes.get(i % clientes.size()), Map.of(produtos.get(i % produtos.size()), 1 + random.nextInt(2)));
            createAndProcessOrder(v1_2, g1, clientes.get(i % clientes.size()), Map.of(produtos.get(i % produtos.size()), 1));
            createAndProcessOrder(v2_1, g2, clientes.get(i % clientes.size()), Map.of(produtos.get(i % produtos.size()), 1 + random.nextInt(3)));
        }
    }

    private void seedNewExpansion(List<Produto> produtos, List<Cliente> clientes) {
        Gerente g1 = new Gerente("Heitor Bernardes", "58932599049", "heitor@franquia.com", "senha123");
        gerenteRepo.upsert(g1);

        Franquia f1 = new Franquia("Matriz Histórica", "Praça da Matriz, 10", g1);
        g1.setFranquia(f1);
        produtos.forEach(p -> f1.adicionarProduto(p, 100));
        franquiaRepo.upsert(f1);

        Franquia f2 = new Franquia("Expansão Aeroporto", "Av. dos Viajantes, S/N", null);
        produtos.forEach(p -> f2.adicionarProduto(p, 20));
        franquiaRepo.upsert(f2);

        Franquia f3 = new Franquia("Unidade Shopping", "Shopping Central, Loja 15", null);
        produtos.forEach(p -> f3.adicionarProduto(p, 50));
        franquiaRepo.upsert(f3);

        Vendedor v1 = new Vendedor("Laura Campos", "53939949038", "laura@franquia.com", "senha123", f1);
        vendedorRepo.upsert(v1);

        createAndProcessOrder(v1, g1, clientes.get(0), Map.of(produtos.get(0), 1));
        createAndProcessOrder(v1, g1, clientes.get(1), Map.of(produtos.get(1), 2));
    }

    private List<Produto> createProducts() {
        return List.of(
                new Produto("P001", "Pizza Margherita", "Molho, mussarela e manjericão", 45.00),
                new Produto("P002", "Pizza Pepperoni", "Molho, mussarela e pepperoni", 55.00),
                new Produto("P003", "Refrigerante 2L", "Coca-Cola, Guaraná ou Fanta", 12.00),
                new Produto("P004", "Suco Natural 1L", "Laranja, Abacaxi ou Morango", 15.00),
                new Produto("P005", "Brownie de Chocolate", "Brownie com nozes e calda", 18.00)
        );
    }

    private List<Cliente> createClientes() {
        List<Cliente> clientes = new ArrayList<>();
        clientes.add(new Cliente("João Mendes", "26321899036", "joao@email.com", "3299998888", "Rua A, 1"));
        clientes.add(new Cliente("Fernanda Lima", "73219484085", "fernanda@email.com", "3298887777", "Rua B, 2"));
        clientes.add(new Cliente("Lucas Martins", "34228318048", "lucas@email.com", "3297776666", "Rua C, 3"));
        clientes.forEach(clienteRepo::upsert);
        return clientes;
    }

    /**
     * Cria e processa um pedido em memória, sem chamar os serviços que salvam em disco a cada passo.
     */
    private void createAndProcessOrder(Vendedor vendedor, Gerente gerente, Cliente cliente, Map<Produto, Integer> produtos) {
        // Lógica de VendedorService.registrarPedido
        String pedidoId = "P" + System.currentTimeMillis() + random.nextInt(1000);
        Pedido novoPedido = new Pedido(cliente, produtos, vendedor.getFranquia(), TiposPagamento.PIX, TiposEntrega.DELIVERY, pedidoId, vendedor);

        vendedor.adicionarPedidoId(pedidoId);
        vendedor.getFranquia().adicionarPedido(pedidoId);
        cliente.adicionarPedido(pedidoId, vendedor.getFranquia().getId());
        pedidoRepo.upsert(novoPedido);

        // Lógica de GerenteService.aceitarPedido
        novoPedido.aprovarPedido();
        for (Map.Entry<Produto, Integer> entry : novoPedido.getProdutosQuantidade().entrySet()) {
            gerente.getFranquia().atualizarEstoque(entry.getKey(), -entry.getValue());
        }
        novoPedido.getVendedor().atualizarTotalVendas(novoPedido.getValorTotal());
        novoPedido.getFranquia().atualizarReceita(novoPedido.getValorTotal());

        // Atualiza as entidades modificadas no repositório em memória
        pedidoRepo.upsert(novoPedido);
        franquiaRepo.upsert(gerente.getFranquia());
        vendedorRepo.upsert(vendedor);
        clienteRepo.upsert(cliente);
    }

    private void saveAll() {
        try {
            gerenteRepo.saveAllSync(5, TimeUnit.SECONDS);
            franquiaRepo.saveAllSync(5, TimeUnit.SECONDS);
            vendedorRepo.saveAllSync(5, TimeUnit.SECONDS);
            clienteRepo.saveAllSync(5, TimeUnit.SECONDS);
            pedidoRepo.saveAllSync(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
