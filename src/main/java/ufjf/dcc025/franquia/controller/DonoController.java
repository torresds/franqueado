package ufjf.dcc025.franquia.controller;

import ufjf.dcc025.franquia.service.DonoService;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;

import java.util.List;
import java.util.Map;

public class DonoController {
    public final DonoService donoService;

    public DonoController(DonoService donoService) {
        this.donoService = donoService;
    }

    public void visualizarFranquiasSemGerente() {
        List<Franquia> franquiasSemGerente = donoService.checarFranquias();
        System.out.println("📍 FRANQUIAS SEM GERENTE");
        System.out.println("=" .repeat(60));
        if (franquiasSemGerente.isEmpty()) {
            System.out.println("✅ Todas as franquias possuem gerente.");
        } else {
            for (Franquia franquia : franquiasSemGerente) {
                System.out.printf("⚠️ %s | Endereço: %s%n", franquia.getNome(), franquia.getEndereco());
            }
        }
        System.out.println("=" .repeat(60));
    }

    public void visualizarDesempenhoGeral() {
        double faturamento = donoService.calcularFaturamentoBruto();
        int totalPedidos = donoService.calcularTotalPedidos();
        double ticketMedio = donoService.calcularTicketMedio();
        Map<String, Double> desempenho = donoService.listarFranquiasPorDesempenho();

        System.out.println("📊 DESEMPENHO GERAL DAS FRANQUIAS");
        System.out.println("=" .repeat(60));
        System.out.printf("💰 Faturamento Bruto Total: R$ %.2f%n", faturamento);
        System.out.printf("🛒 Total de Pedidos: %d%n", totalPedidos);
        System.out.printf("🎯 Ticket Médio: R$ %.2f%n", ticketMedio);
        System.out.println("-" .repeat(60));
        System.out.println("🏆 RANKING DE FRANQUIAS POR RECEITA");
        int posicao = 1;
        for (Map.Entry<String, Double> entry : desempenho.entrySet()) {
            String medalha = posicao == 1 ? "🥇" : posicao == 2 ? "🥈" : posicao == 3 ? "🥉" : "  ";
            System.out.printf("%s %dº %s - R$ %.2f%n", medalha, posicao++, entry.getKey(), entry.getValue());
        }
        System.out.println("=" .repeat(60));
    }

    public void visualizarRankingVendedores() {
        List<String> ranking = donoService.rankingVendedores();
        System.out.println("🏆 RANKING GERAL DE VENDEDORES");
        System.out.println("=" .repeat(60));
        if (ranking.isEmpty()) {
            System.out.println("❌ Nenhum vendedor encontrado.");
        } else {
            int posicao = 1;
            for (String vendedorId : ranking) {
                Vendedor vendedor = donoService.getVendedorRepo().findById(vendedorId).orElse(null);
                if (vendedor != null) {
                    String medalha = posicao == 1 ? "🥇" : posicao == 2 ? "🥈" : posicao == 3 ? "🥉" : "  ";
                    System.out.printf("%s %dº %s - R$ %.2f%n", 
                        medalha, posicao++, vendedor.getNome(), vendedor.getTotalVendas());
                }
            }
        }
        System.out.println("=" .repeat(60));
    }

    public void visualizarRankingVendedoresPorFranquia(String franquiaId) {
        List<String> ranking = donoService.rankingVendedoresPorFranquia(franquiaId);
        Franquia franquia = donoService.getFranquiaRepo().findById(franquiaId).orElse(null);
        System.out.printf("🏆 RANKING DE VENDEDORES - %s%n", franquia != null ? franquia.getNome().toUpperCase() : "FRANQUIA DESCONHECIDA");
        System.out.println("=" .repeat(60));
        if (ranking.isEmpty()) {
            System.out.println("❌ Nenhum vendedor encontrado.");
        } else {
            int posicao = 1;
            for (String vendedorId : ranking) {
                Vendedor vendedor = donoService.getVendedorRepo().findById(vendedorId).orElse(null);
                if (vendedor != null) {
                    String medalha = posicao == 1 ? "🥇" : posicao == 2 ? "🥈" : posicao == 3 ? "🥉" : "  ";
                    System.out.printf("%s %dº %s - R$ %.2f%n", 
                        medalha, posicao++, vendedor.getNome(), vendedor.getTotalVendas());
                }
            }
        }
        System.out.println("=" .repeat(60));
    }
}