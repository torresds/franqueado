package ufjf.dcc025.franquia.controller;

import ufjf.dcc025.franquia.service.GerenteService;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.model.franquia.Franquia;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GerenteController {
    private final GerenteService gerenteService;
    private final EntityRepository<Pedido> repositorioPedidos;
    private final EntityRepository<Cliente> repositorioClientes;
    private final EntityRepository<Franquia> todasFranquias;

    public GerenteController(GerenteService gerenteService, EntityRepository<Pedido> repositorioPedidos,
                             EntityRepository<Cliente> repositorioClientes, EntityRepository<Franquia> todasFranquias) {
        this.gerenteService = gerenteService;
        this.repositorioPedidos = repositorioPedidos;
        this.repositorioClientes = repositorioClientes;
        this.todasFranquias = todasFranquias;
    }

    public void visualizarRelatorioVendas() {
        List<String> pedidosId = gerenteService.getFranquia().gerarRelatorioVendas();
        System.out.println("📊 RELATÓRIO DE VENDAS - " + gerenteService.getFranquia().getNome().toUpperCase());
        System.out.println("=" .repeat(60));
        System.out.println("Total de pedidos: " + pedidosId.size());
        System.out.printf("💰 Receita Acumulada: R$ %.2f%n", gerenteService.getFranquia().getReceita());
        System.out.println("=" .repeat(60));
        if (pedidosId.isEmpty()) {
            System.out.println("❌ Nenhum pedido encontrado.");
            return;
        }
        int pedidosAprovados = 0;
        int pedidosCancelados = 0;
        int pedidosPendentes = 0;
        for (String pedidoId : pedidosId) {
            Pedido pedido = repositorioPedidos.findById(pedidoId).orElse(null);
            if (pedido != null) {
                System.out.printf("🛒 %s | %s | R$ %.2f | %s%n", 
                    pedidoId, pedido.getCliente().getNome(), pedido.getValorTotal(), pedido.getStatus());
                if (pedido.isAprovado()) {
                    pedidosAprovados++;
                } else if (pedido.isCancelado()) {
                    pedidosCancelados++;
                } else if (pedido.isPendente()) {
                    pedidosPendentes++;
                }
            }
        }
        System.out.println("=" .repeat(60));
        System.out.printf("✅ Pedidos Aprovados: %d%n", pedidosAprovados);
        System.out.printf("⏳ Pedidos Pendentes: %d%n", pedidosPendentes);
        System.out.printf("❌ Pedidos Cancelados: %d%n", pedidosCancelados);
        System.out.println("=" .repeat(60));
    }

    public void visualizarRelatorioVendasPeriodo(Date dataInicio, Date dataFim) {
        List<String> pedidosId = gerenteService.getFranquia().gerarRelatorioVendasPeriodo(dataInicio, dataFim, repositorioPedidos);
        System.out.println("📅 RELATÓRIO DE VENDAS POR PERÍODO - " + gerenteService.getFranquia().getNome().toUpperCase());
        System.out.printf("Período: %s até %s%n", dataInicio.toString(), dataFim.toString());
        System.out.println("=" .repeat(60));
        System.out.println("Total de pedidos no período: " + pedidosId.size());
        System.out.println("=" .repeat(60));
        if (pedidosId.isEmpty()) {
            System.out.println("❌ Nenhum pedido encontrado no período especificado.");
            return;
        }
        double receitaPeriodo = 0.0;
        int pedidosAprovadosPeriodo = 0;
        for (String pedidoId : pedidosId) {
            Pedido pedido = repositorioPedidos.findById(pedidoId).orElse(null);
            if (pedido != null) {
                System.out.printf("🛒 %s | %s | %s | R$ %.2f | %s%n", 
                    pedidoId, pedido.getData().toString(), pedido.getCliente().getNome(), 
                    pedido.getValorTotal(), pedido.getStatus());
                if (pedido.isAprovado()) {
                    receitaPeriodo += pedido.getValorTotal();
                    pedidosAprovadosPeriodo++;
                }
            }
        }
        System.out.println("=" .repeat(60));
        System.out.printf("💰 Receita do Período: R$ %.2f%n", receitaPeriodo);
        System.out.printf("✅ Pedidos Aprovados no Período: %d%n", pedidosAprovadosPeriodo);
        double ticketMedio = pedidosAprovadosPeriodo > 0 ? receitaPeriodo / pedidosAprovadosPeriodo : 0;
        System.out.printf("🎯 Ticket Médio: R$ %.2f%n", ticketMedio);
        System.out.println("=" .repeat(60));
    }

    public void visualizarRelatorioClientesFrequencia() {
        List<String> clientesRanking = gerenteService.getFranquia().gerarRelatorioClientesFrequencia(repositorioClientes);
        System.out.println("👥 RELATÓRIO DE CLIENTES POR FREQUÊNCIA - " + gerenteService.getFranquia().getNome().toUpperCase());
        System.out.println("=" .repeat(60));
        System.out.println("Total de clientes ativos: " + clientesRanking.size());
        System.out.println("=" .repeat(60));
        if (clientesRanking.isEmpty()) {
            System.out.println("❌ Nenhum cliente ativo encontrado.");
            return;
        }
        for (int i = 0; i < clientesRanking.size(); i++) {
            String posicao = String.format("%2d°", i + 1);
            String medalha = i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : "👤";
            System.out.printf("%s %s %s%n", medalha, posicao, clientesRanking.get(i));
        }
        System.out.println("=" .repeat(60));
    }

    public void visualizarRelatorioEstoqueBaixo(int limiteMinimo) {
        List<String> produtosEstoqueBaixo = gerenteService.listarProdutosEstoqueBaixo(gerenteService.getFranquia().getId(), limiteMinimo);
        System.out.println("⚠️ RELATÓRIO DE ESTOQUE BAIXO - " + gerenteService.getFranquia().getNome().toUpperCase());
        System.out.println("=" .repeat(60));
        System.out.println("Limite mínimo: " + limiteMinimo + " unidades");
        System.out.println("Produtos com estoque baixo: " + produtosEstoqueBaixo.size());
        System.out.println("=" .repeat(60));
        if (produtosEstoqueBaixo.isEmpty()) {
            System.out.println("✅ Todos os produtos estão com estoque adequado!");
            return;
        }
        for (String produto : produtosEstoqueBaixo) {
            System.out.println(produto);
        }
        System.out.println("=" .repeat(60));
    }

    public void visualizarEstoqueCompleto() {
        Map<Produto, Integer> estoque = gerenteService.getFranquia().getEstoque();
        System.out.println("📦 ESTOQUE COMPLETO - " + gerenteService.getFranquia().getNome().toUpperCase());
        System.out.println("=" .repeat(70));
        System.out.println("Total de produtos: " + estoque.size());
        System.out.println("=" .repeat(70));
        if (estoque.isEmpty()) {
            System.out.println("❌ Nenhum produto em estoque.");
            return;
        }
        List<Map.Entry<Produto, Integer>> produtosOrdenados = estoque.entrySet().stream()
            .sorted(Map.Entry.comparingByKey((p1, p2) -> p1.getNome().compareToIgnoreCase(p2.getNome())))
            .collect(Collectors.toList());
        double valorTotalEstoque = 0.0;
        int totalUnidades = 0;
        System.out.printf("%-30s | %-10s | %-12s | %-15s%n", "PRODUTO", "QUANTIDADE", "PREÇO UNIT.", "VALOR TOTAL");
        System.out.println("-" .repeat(70));
        for (Map.Entry<Produto, Integer> entry : produtosOrdenados) {
            Produto produto = entry.getKey();
            int quantidade = entry.getValue();
            double precoUnitario = produto.getPreco();
            double valorTotal = precoUnitario * quantidade;
            String indicador = quantidade <= 5 ? "⚠️ " : quantidade <= 10 ? "🟡 " : "✅ ";
            System.out.printf("%s%-28s | %10d | R$ %9.2f | R$ %12.2f%n", 
                indicador, produto.getNome(), quantidade, precoUnitario, valorTotal);
            valorTotalEstoque += valorTotal;
            totalUnidades += quantidade;
        }
        System.out.println("-" .repeat(70));
        System.out.printf("TOTAIS: %d produtos | %d unidades | R$ %.2f%n", 
            estoque.size(), totalUnidades, valorTotalEstoque);
        System.out.println("=" .repeat(70));
        System.out.println("📊 LEGENDA:");
        System.out.println("✅ Estoque adequado (> 10 unidades)");
        System.out.println("🟡 Estoque baixo (6-10 unidades)");
        System.out.println("⚠️ Estoque crítico (≤ 5 unidades)");
        System.out.println("=" .repeat(70));
    }
}