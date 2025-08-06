// FILE: src/main/java/ufjf/dcc025/franquia/util/DataSeeder.java
package ufjf.dcc025.franquia.util;

import ufjf.dcc025.franquia.enums.EstadoPedido;
import ufjf.dcc025.franquia.enums.TiposEntrega;
import ufjf.dcc025.franquia.enums.TiposPagamento;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.persistence.EntityRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DataSeeder {

    public enum SeedScenario {
        REALISTIC
    }

    private final EntityRepository<Franquia> franquiaRepo;
    private final EntityRepository<Vendedor> vendedorRepo;
    private final EntityRepository<Gerente> gerenteRepo;
    private final EntityRepository<Pedido> pedidoRepo;
    private final EntityRepository<Cliente> clienteRepo;
    private final Random random = new Random();

    // Listas de nomes para geração de dados realistas
    private final List<String> nomes = List.of("Miguel", "Arthur", "Gael", "Heitor", "Theo", "Davi", "Alice", "Laura", "Maria Alice", "Helena", "Sophia", "Isabella");
    private final List<String> sobrenomes = List.of("Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves", "Pereira", "Lima", "Gomes", "Ribeiro", "Martins");
    private final List<String> nomesPizzas = List.of("Calabresa", "Mussarela", "Frango com Catupiry", "Portuguesa", "Marguerita", "Quatro Queijos", "Pepperoni", "Atum", "Bacon", "Lombo Canadense");
    private final List<String> nomesBebidas = List.of("Coca-Cola 2L", "Guaraná Antarctica 2L", "Suco de Laranja 1L", "Água Mineral 500ml", "Cerveja Heineken Long Neck");
    private final List<String> nomesRuas = List.of("Rua Principal", "Avenida Brasil", "Rua das Flores", "Avenida Getúlio Vargas", "Rua Sete de Setembro", "Rua da Matriz");

    public DataSeeder(EntityRepository<Franquia> fr, EntityRepository<Vendedor> vr, EntityRepository<Gerente> gr, EntityRepository<Pedido> pr, EntityRepository<Cliente> cr) {
        this.franquiaRepo = fr;
        this.vendedorRepo = vr;
        this.gerenteRepo = gr;
        this.pedidoRepo = pr;
        this.clienteRepo = cr;
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
        if (scenario == SeedScenario.REALISTIC) {
            seedRealistic();
        }
        saveAll();
        System.out.println("Banco de dados populado com sucesso.");
    }

    private void seedRealistic() {
        List<Produto> produtos = createProducts(15);
        List<Cliente> clientes = createClientes(50);
        List<Gerente> gerentes = createGerentes(8);
        List<Franquia> franquias = createFranquias(10, gerentes, produtos);
        List<Vendedor> vendedores = createVendedores(15, franquias);
        createRealisticOrders(200, vendedores, clientes, produtos);
    }

    private List<Produto> createProducts(int count) {
        List<Produto> productList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String nome;
            String desc;
            double preco;
            if (i < nomesPizzas.size()) {
                nome = "Pizza de " + nomesPizzas.get(i);
                desc = "Deliciosa pizza com ingredientes selecionados.";
                preco = 40 + random.nextDouble() * 25;
            } else {
                nome = nomesBebidas.get(i % nomesBebidas.size());
                desc = "Bebida gelada para acompanhar.";
                preco = 8 + random.nextDouble() * 10;
            }
            productList.add(new Produto("P" + String.format("%03d", i + 1), nome, desc, preco));
        }
        return productList;
    }

    private List<Cliente> createClientes(int count) {
        List<Cliente> clientList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String nome = nomes.get(random.nextInt(nomes.size())) + " " + sobrenomes.get(random.nextInt(sobrenomes.size()));
            String cpf = generateRandomCpf();
            String email = nome.toLowerCase().replace(" ", ".") + "@email.com";
            String telefone = "329" + (10000000 + random.nextInt(90000000));
            String endereco = nomesRuas.get(random.nextInt(nomesRuas.size())) + ", " + (1 + random.nextInt(1000));
            Cliente c = new Cliente(nome, cpf, email, telefone, endereco);
            clientList.add(c);
            clienteRepo.upsert(c);
        }
        return clientList;
    }

    private List<Gerente> createGerentes(int count) {
        List<Gerente> gerentesList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String nome = nomes.get(random.nextInt(nomes.size())) + " " + sobrenomes.get(random.nextInt(sobrenomes.size()));
            String cpf = generateRandomCpf();
            String email = "gerente." + nome.toLowerCase().replace(" ", "") + "@franquia.com";
            Gerente g = new Gerente(nome, cpf, email, "senha123");
            gerentesList.add(g);
            gerenteRepo.upsert(g);
        }
        return gerentesList;
    }

    private List<Franquia> createFranquias(int count, List<Gerente> gerentes, List<Produto> produtos) {
        List<Franquia> franquiasList = new ArrayList<>();
        String[] bairros = {"Centro", "Zona Norte", "Zona Sul", "Leste", "Oeste", "Aeroporto", "Shopping", "Universidade", "Industrial", "Praça"};
        List<Gerente> gerentesDisponiveis = new ArrayList<>(gerentes);

        for (int i = 0; i < count; i++) {
            String nome = "Pizzaria " + bairros[i % bairros.length];
            String endereco = nomesRuas.get(random.nextInt(nomesRuas.size())) + ", " + (100 + random.nextInt(500)) + ", " + bairros[i % bairros.length];

            Gerente gerente = null;
            if (!gerentesDisponiveis.isEmpty() && random.nextBoolean()) { // Nem toda franquia terá gerente
                gerente = gerentesDisponiveis.remove(random.nextInt(gerentesDisponiveis.size()));
            }

            Franquia f = new Franquia(nome, endereco, gerente);
            if (gerente != null) {
                gerente.setFranquia(f);
            }

            produtos.forEach(p -> f.adicionarProduto(p, 20 + random.nextInt(80)));
            franquiasList.add(f);
            franquiaRepo.upsert(f);
        }
        return franquiasList;
    }

    private List<Vendedor> createVendedores(int count, List<Franquia> franquias) {
        List<Vendedor> vendedoresList = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            String nome = nomes.get(random.nextInt(nomes.size())) + " " + sobrenomes.get(random.nextInt(sobrenomes.size()));
            String cpf = generateRandomCpf();
            String email = "vendedor." + nome.toLowerCase().replace(" ", "") + "@franquia.com";
            Franquia f = franquias.get(random.nextInt(franquias.size()));
            Vendedor v = new Vendedor(nome, cpf, email, "senha123", f);
            vendedoresList.add(v);
            vendedorRepo.upsert(v);
        }
        return vendedoresList;
    }

    private void createRealisticOrders(int count, List<Vendedor> vendedores, List<Cliente> clientes, List<Produto> produtos) {
        List<Vendedor> vendedoresComFranquia = vendedores.stream()
                .filter(v -> v.getFranquia() != null && v.getFranquia().getGerente() != null)
                .collect(Collectors.toList());

        for (int i = 0; i < count; i++) {
            Vendedor vendedor = vendedoresComFranquia.get(random.nextInt(vendedoresComFranquia.size()));
            Cliente cliente = clientes.get(random.nextInt(clientes.size()));

            Map<Produto, Integer> itensPedido = new HashMap<>();
            int numItens = 1 + random.nextInt(4); // 1 a 4 produtos por pedido
            for (int j = 0; j < numItens; j++) {
                Produto p = produtos.get(random.nextInt(produtos.size()));
                itensPedido.put(p, 1 + random.nextInt(2)); // 1 a 2 unidades por produto
            }

            TiposPagamento pagamento = TiposPagamento.values()[random.nextInt(TiposPagamento.values().length)];
            TiposEntrega entrega = TiposEntrega.values()[random.nextInt(TiposEntrega.values().length)];

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -random.nextInt(60)); // Pedidos nos últimos 60 dias
            cal.add(Calendar.HOUR_OF_DAY, -random.nextInt(12));
            Date dataPedido = cal.getTime();

            createAndProcessOrder(vendedor, vendedor.getFranquia().getGerente(), cliente, itensPedido, pagamento, entrega, dataPedido);
        }
    }

    private void createAndProcessOrder(Vendedor v, Gerente g, Cliente c, Map<Produto, Integer> produtos, TiposPagamento pag, TiposEntrega ent, Date data) {
        Pedido novoPedido = new Pedido(c, produtos, v.getFranquia(), pag, ent, v);
        novoPedido.setData(data);
        String pedidoId = novoPedido.getId();
        v.adicionarPedidoId(pedidoId);
        v.getFranquia().adicionarPedido(pedidoId);
        c.adicionarPedido(pedidoId, v.getFranquia().getId());

        // Simula status variados
        double chance = random.nextDouble();
        if (chance < 0.85) { // 85% de chance de ser aprovado
            novoPedido.aprovarPedido();
            for (Map.Entry<Produto, Integer> entry : novoPedido.getProdutosQuantidade().entrySet()) {
                g.getFranquia().atualizarEstoque(entry.getKey(), -entry.getValue());
            }
            v.atualizarTotalVendas(novoPedido.getValorTotal());
            v.getFranquia().atualizarReceita(novoPedido.getValorTotal());
        } else if (chance < 0.95) { // 10% de chance de ser cancelado
            novoPedido.cancelarPedido();
        } // 5% restante fica como pendente

        pedidoRepo.upsert(novoPedido);
        franquiaRepo.upsert(g.getFranquia());
        vendedorRepo.upsert(v);
        clienteRepo.upsert(c);
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

    private String generateRandomCpf() {
        // Gera um CPF aleatório válido (algoritmo simplificado)
        int[] cpf = new int[11];
        for (int i = 0; i < 9; i++) {
            cpf[i] = random.nextInt(10);
        }
        cpf[9] = calcularDigito(cpf, 10);
        cpf[10] = calcularDigito(cpf, 11);
        StringBuilder sb = new StringBuilder();
        for (int i : cpf) {
            sb.append(i);
        }
        return sb.toString();
    }

    private int calcularDigito(int[] cpf, int peso) {
        int soma = 0;
        for (int i = 0; i < peso - 1; i++) {
            soma += cpf[i] * (peso - i);
        }
        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }
}
