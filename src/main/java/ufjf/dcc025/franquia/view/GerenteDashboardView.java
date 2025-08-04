package ufjf.dcc025.franquia.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import ufjf.dcc025.franquia.FranquiaApp;
import ufjf.dcc025.franquia.controller.GerenteController;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.service.GerenteService;

public class GerenteDashboardView extends VBox {
    private final FranquiaApp app;
    private final Gerente gerente;
    private final GerenteController controller;

    public GerenteDashboardView(FranquiaApp app, Gerente gerente, EntityRepository<Vendedor> vendedorRepo, 
                                EntityRepository<Pedido> pedidoRepo, EntityRepository<Franquia> franquiaRepo, 
                                EntityRepository<Cliente> clienteRepo) {
        this.app = app;
        this.gerente = gerente;
        this.controller = new GerenteController(new GerenteService(gerente, vendedorRepo, pedidoRepo, franquiaRepo), 
                                               pedidoRepo, clienteRepo, franquiaRepo);
        initUI();
    }

    private void initUI() {
        setAlignment(Pos.TOP_LEFT);
        setSpacing(10);
        setPadding(new Insets(20));

        Label titleLabel = new Label("Dashboard do Gerente: " + gerente.getNome());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button cadastrarVendedorButton = new Button("Cadastrar Vendedor");
        Button removerVendedorButton = new Button("Remover Vendedor");
        Button listarVendedoresButton = new Button("Listar Vendedores");
        Button aceitarPedidoButton = new Button("Aceitar Pedido");
        Button cancelarPedidoButton = new Button("Cancelar Pedido");
        Button criarProdutoButton = new Button("Criar Produto");
        Button editarProdutoButton = new Button("Editar Produto");
        Button removerProdutoButton = new Button("Remover Produto");
        Button atualizarEstoqueButton = new Button("Atualizar Estoque");
        Button relatorioVendasButton = new Button("Relatório de Vendas");
        Button relatorioEstoqueBaixoButton = new Button("Relatório de Estoque Baixo");
        Button relatorioEstoqueCompletoButton = new Button("Estoque Completo");
        Button logoutButton = new Button("Sair");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(400);

        cadastrarVendedorButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Cadastrar Vendedor");
            dialog.setHeaderText("Digite os dados do novo vendedor:");
            dialog.setContentText("Nome, CPF, Email, Senha (separados por vírgula):");
            dialog.showAndWait().ifPresent(input -> {
                try {
                    String[] parts = input.split(",");
                    if (parts.length != 4) throw new IllegalArgumentException("Formato inválido.");
                    Vendedor vendedor = controller.gerenteService.cadastrarVendedor(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
                    outputArea.setText("Vendedor cadastrado: " + vendedor.getNome());
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        removerVendedorButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Remover Vendedor");
            dialog.setHeaderText("Digite o CPF do vendedor:");
            dialog.setContentText("CPF:");
            dialog.showAndWait().ifPresent(cpf -> {
                try {
                    controller.gerenteService.removerVendedor(cpf.trim());
                    outputArea.setText("Vendedor removido com sucesso.");
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        listarVendedoresButton.setOnAction(e -> {
            outputArea.clear();
            for (Vendedor vendedor : controller.gerenteService.listarVendedoresPorVendas()) {
                outputArea.appendText("CPF: " + vendedor.getId() + " | Nome: " + vendedor.getNome() + " | Vendas: R$ " + vendedor.getTotalVendas() + "\n");
            }
        });

        aceitarPedidoButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Aceitar Pedido");
            dialog.setHeaderText("Digite o ID do pedido:");
            dialog.setContentText("ID:");
            dialog.showAndWait().ifPresent(id -> {
                try {
                    Pedido pedido = controller.gerenteService.aceitarPedido(id.trim());
                    outputArea.setText("Pedido " + id + " aceito com sucesso.");
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        cancelarPedidoButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Cancelar Pedido");
            dialog.setHeaderText("Digite o ID do pedido:");
            dialog.setContentText("ID:");
            dialog.showAndWait().ifPresent(id -> {
                try {
                    Pedido pedido = controller.gerenteService.cancelarPedido(id.trim());
                    outputArea.setText("Pedido " + id + " cancelado com sucesso.");
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        criarProdutoButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Criar Produto");
            dialog.setHeaderText("Digite os dados do novo produto:");
            dialog.setContentText("Código, Nome, Descrição, Preço, Quantidade Inicial (separados por vírgula):");
            dialog.showAndWait().ifPresent(input -> {
                try {
                    String[] parts = input.split(",");
                    if (parts.length != 5) throw new IllegalArgumentException("Formato inválido.");
                    double preco = Double.parseDouble(parts[3].trim());
                    int quantidade = Integer.parseInt(parts[4].trim());
                    controller.gerenteService.criarNovoProduto(parts[0].trim(), parts[1].trim(), parts[2].trim(), preco, quantidade);
                    outputArea.setText("Produto criado: " + parts[1].trim());
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        editarProdutoButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Editar Produto");
            dialog.setHeaderText("Digite os dados do produto:");
            dialog.setContentText("Código, Novo Nome, Nova Descrição, Novo Preço (separados por vírgula):");
            dialog.showAndWait().ifPresent(input -> {
                try {
                    String[] parts = input.split(",");
                    if (parts.length != 4) throw new IllegalArgumentException("Formato inválido.");
                    double preco = Double.parseDouble(parts[3].trim());
                    controller.gerenteService.editarProduto(parts[0].trim(), parts[1].trim(), parts[2].trim(), preco);
                    outputArea.setText("Produto editado: " + parts[1].trim());
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        removerProdutoButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Remover Produto");
            dialog.setHeaderText("Digite o código do produto:");
            dialog.setContentText("Código:");
            dialog.showAndWait().ifPresent(codigo -> {
                try {
                    controller.gerenteService.removerProdutoExistente(codigo.trim());
                    outputArea.setText("Produto removido com sucesso.");
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        atualizarEstoqueButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Atualizar Estoque");
            dialog.setHeaderText("Digite os dados do estoque:");
            dialog.setContentText("Código Produto, Nova Quantidade, Franquia ID (separados por vírgula):");
            dialog.showAndWait().ifPresent(input -> {
                try {
                    String[] parts = input.split(",");
                    if (parts.length != 3) throw new IllegalArgumentException("Formato inválido.");
                    int quantidade = Integer.parseInt(parts[1].trim());
                    controller.gerenteService.atualizarEstoqueProduto(parts[0].trim(), quantidade, parts[2].trim());
                    outputArea.setText("Estoque atualizado para produto " + parts[0].trim());
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        relatorioVendasButton.setOnAction(e -> {
            outputArea.clear();
            controller.visualizarRelatorioVendas();
            for (Pedido pedido : controller.gerenteService.listarPedidosPendentes()) {
                outputArea.appendText("🛒 " + pedido.getId() + " | " + pedido.getCliente().getNome() + " | R$ " + pedido.getValorTotal() + " | " + pedido.getStatus() + "\n");
            }
        });

        relatorioEstoqueBaixoButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Relatório de Estoque Baixo");
            dialog.setHeaderText("Digite o limite mínimo de estoque:");
            dialog.setContentText("Limite:");
            dialog.showAndWait().ifPresent(limite -> {
                try {
                    int limiteMinimo = Integer.parseInt(limite.trim());
                    outputArea.clear();
                    controller.visualizarRelatorioEstoqueBaixo(limiteMinimo);
                    for (String produto : controller.gerenteService.listarProdutosEstoqueBaixo(controller.gerenteService.getFranquia().getId(), limiteMinimo)) {
                        outputArea.appendText(produto + "\n");
                    }
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        relatorioEstoqueCompletoButton.setOnAction(e -> {
            outputArea.clear();
            controller.visualizarEstoqueCompleto();
            for (var entry : controller.gerenteService.getFranquia().getEstoque().entrySet()) {
                outputArea.appendText(String.format("%s | Quantidade: %d | Preço: R$ %.2f\n", 
                    entry.getKey().getNome(), entry.getValue(), entry.getKey().getPreco()));
            }
        });

        logoutButton.setOnAction(e -> app.showLoginScreen());

        getChildren().addAll(titleLabel, cadastrarVendedorButton, removerVendedorButton, listarVendedoresButton, 
                             aceitarPedidoButton, cancelarPedidoButton, criarProdutoButton, editarProdutoButton, 
                             removerProdutoButton, atualizarEstoqueButton, relatorioVendasButton, 
                             relatorioEstoqueBaixoButton, relatorioEstoqueCompletoButton, logoutButton, outputArea);
    }
}