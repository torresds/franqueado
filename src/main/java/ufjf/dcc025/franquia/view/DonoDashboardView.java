package ufjf.dcc025.franquia.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ufjf.dcc025.franquia.FranquiaApp;
import ufjf.dcc025.franquia.controller.DonoController;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.usuarios.Dono;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.service.DonoService;

public class DonoDashboardView extends VBox {
    private final FranquiaApp app;
    private final Dono dono;
    private final DonoController controller;

    public DonoDashboardView(FranquiaApp app, Dono dono, EntityRepository<Franquia> franquiaRepo, 
                             EntityRepository<Gerente> gerenteRepo, EntityRepository<Vendedor> vendedorRepo) {
        this.app = app;
        this.dono = dono;
        this.controller = new DonoController(new DonoService(dono, franquiaRepo, gerenteRepo, vendedorRepo));
        initUI();
    }

    private void initUI() {
        setAlignment(Pos.TOP_LEFT);
        setSpacing(10);
        setPadding(new Insets(20));

        Label titleLabel = new Label("Dashboard do Dono: " + dono.getNome());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button franquiasSemGerenteButton = new Button("Franquias sem Gerente");
        Button cadastrarFranquiaButton = new Button("Cadastrar Franquia");
        Button removerFranquiaButton = new Button("Remover Franquia");
        Button listarFranquiasButton = new Button("Listar Franquias");
        Button cadastrarGerenteButton = new Button("Cadastrar Gerente");
        Button removerGerenteButton = new Button("Remover Gerente");
        Button listarGerentesButton = new Button("Listar Gerentes");
        Button desempenhoButton = new Button("Desempenho Geral");
        Button rankingVendedoresButton = new Button("Ranking de Vendedores");
        Button logoutButton = new Button("Sair");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(400);

        franquiasSemGerenteButton.setOnAction(e -> {
            outputArea.clear();
            controller.visualizarFranquiasSemGerente();
            for (Franquia franquia : controller.donoService.checarFranquias()) {
                outputArea.appendText("⚠️ " + franquia.getNome() + " | Endereço: " + franquia.getEndereco() + "\n");
            }
            if (controller.donoService.checarFranquias().isEmpty()) {
                outputArea.appendText("✅ Todas as franquias possuem gerente.\n");
            }
        });

        cadastrarFranquiaButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Cadastrar Franquia");
            dialog.setHeaderText("Digite os dados da nova franquia:");
            dialog.setContentText("Nome, Endereço, Gerente CPF (separados por vírgula):");
            dialog.showAndWait().ifPresent(input -> {
                try {
                    String[] parts = input.split(",");
                    if (parts.length != 3) throw new IllegalArgumentException("Formato inválido.");
                    Franquia franquia = controller.donoService.cadastrarFranquia(parts[0].trim(), parts[1].trim(), parts[2].trim());
                    outputArea.setText("Franquia cadastrada: " + franquia.getNome());
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        removerFranquiaButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Remover Franquia");
            dialog.setHeaderText("Digite o ID da franquia:");
            dialog.setContentText("ID:");
            dialog.showAndWait().ifPresent(id -> {
                try {
                    controller.donoService.removerFranquia(id.trim());
                    outputArea.setText("Franquia removida com sucesso.");
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        listarFranquiasButton.setOnAction(e -> {
            outputArea.clear();
            for (Franquia franquia : controller.donoService.listarFranquias()) {
                outputArea.appendText("ID: " + franquia.getId() + " | Nome: " + franquia.getNome() + " | Endereço: " + franquia.getEndereco() + "\n");
            }
        });

        cadastrarGerenteButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Cadastrar Gerente");
            dialog.setHeaderText("Digite os dados do novo gerente:");
            dialog.setContentText("Nome, CPF, Email, Senha, Franquia ID (separados por vírgula):");
            dialog.showAndWait().ifPresent(input -> {
                try {
                    String[] parts = input.split(",");
                    if (parts.length != 5) throw new IllegalArgumentException("Formato inválido.");
                    Gerente gerente = controller.donoService.cadastrarGerente(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim());
                    outputArea.setText("Gerente cadastrado: " + gerente.getNome());
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        removerGerenteButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Remover Gerente");
            dialog.setHeaderText("Digite o CPF do gerente:");
            dialog.setContentText("CPF:");
            dialog.showAndWait().ifPresent(cpf -> {
                try {
                    controller.donoService.removerGerente(cpf.trim());
                    outputArea.setText("Gerente removido com sucesso.");
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        listarGerentesButton.setOnAction(e -> {
            outputArea.clear();
            for (Gerente gerente : controller.donoService.listarGerentes()) {
                outputArea.appendText("CPF: " + gerente.getId() + " | Nome: " + gerente.getNome() + " | Franquia: " + gerente.getFranquia().getNome() + "\n");
            }
        });

        desempenhoButton.setOnAction(e -> {
            outputArea.clear();
            controller.visualizarDesempenhoGeral();
            outputArea.appendText(String.format("💰 Faturamento Bruto Total: R$ %.2f\n", controller.donoService.calcularFaturamentoBruto()));
            outputArea.appendText(String.format("🛒 Total de Pedidos: %d\n", controller.donoService.calcularTotalPedidos()));
            outputArea.appendText(String.format("🎯 Ticket Médio: R$ %.2f\n", controller.donoService.calcularTicketMedio()));
            outputArea.appendText("🏆 RANKING DE FRANQUIAS POR RECEITA\n");
            int posicao = 1;
            for (var entry : controller.donoService.listarFranquiasPorDesempenho().entrySet()) {
                outputArea.appendText(String.format("%dº %s - R$ %.2f\n", posicao++, entry.getKey(), entry.getValue()));
            }
        });

        rankingVendedoresButton.setOnAction(e -> {
            outputArea.clear();
            controller.visualizarRankingVendedores();
            int posicao = 1;
            for (String vendedorId : controller.donoService.rankingVendedores()) {
                Vendedor vendedor = controller.donoService.getVendedorRepo().findById(vendedorId).orElse(null);
                if (vendedor != null) {
                    outputArea.appendText(String.format("%dº %s - R$ %.2f\n", posicao++, vendedor.getNome(), vendedor.getTotalVendas()));
                }
            }
        });

        logoutButton.setOnAction(e -> app.showLoginScreen());

        getChildren().addAll(titleLabel, franquiasSemGerenteButton, cadastrarFranquiaButton, removerFranquiaButton, 
                             listarFranquiasButton, cadastrarGerenteButton, removerGerenteButton, listarGerentesButton, 
                             desempenhoButton, rankingVendedoresButton, logoutButton, outputArea);
    }
}