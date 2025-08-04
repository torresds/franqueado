package ufjf.dcc025.franquia.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import ufjf.dcc025.franquia.FranquiaApp;
import ufjf.dcc025.franquia.controller.VendedorController;
import ufjf.dcc025.franquia.enums.TiposEntrega;
import ufjf.dcc025.franquia.enums.TiposPagamento;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.service.VendedorService;
import ufjf.dcc025.franquia.model.usuarios.Vendedor; 

import java.util.HashMap;
import java.util.Map;

public class VendedorDashboardView extends VBox {
    private final FranquiaApp app;
    private final Vendedor vendedor;
    private final VendedorController controller;

    public VendedorDashboardView(FranquiaApp app, Vendedor vendedor, EntityRepository<Pedido> pedidoRepo, 
                                 EntityRepository<Cliente> clienteRepo) {
        this.app = app;
        this.vendedor = vendedor;
        this.controller = new VendedorController(new VendedorService(vendedor, pedidoRepo, clienteRepo));
        initUI();
    }

    private void initUI() {
        setAlignment(Pos.TOP_LEFT);
        setSpacing(10);
        setPadding(new Insets(20));

        Label titleLabel = new Label("Dashboard do Vendedor: " + vendedor.getNome());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button cadastrarClienteButton = new Button("Cadastrar Cliente");
        Button criarPedidoButton = new Button("Criar Pedido");
        Button listarPedidosButton = new Button("Listar Pedidos");
        Button logoutButton = new Button("Sair");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(400);

        cadastrarClienteButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Cadastrar Cliente");
            dialog.setHeaderText("Digite os dados do novo cliente:");
            dialog.setContentText("Nome, CPF, Email, Telefone, Endereço (separados por vírgula):");
            dialog.showAndWait().ifPresent(input -> {
                try {
                    String[] parts = input.split(",");
                    if (parts.length != 5) throw new IllegalArgumentException("Formato inválido.");
                    Cliente cliente = controller.vendedorService.cadastrarCliente(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim());
                    outputArea.setText("Cliente cadastrado: " + cliente.getNome());
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        criarPedidoButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Criar Pedido");
            dialog.setHeaderText("Digite os dados do pedido:");
            dialog.setContentText("CPF Cliente, Forma Pagamento (CREDITO/DEBITO/PIX), Método Entrega (RETIRADA/ENTREGA), Produtos (código:quantidade, separados por ;):");
            dialog.showAndWait().ifPresent(input -> {
                try {
                    String[] parts = input.split(",");
                    if (parts.length != 4) throw new IllegalArgumentException("Formato inválido.");
                    Cliente cliente = controller.vendedorService.getClienteRepo().findById(parts[0].trim())
                        .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
                    TiposPagamento formaPagamento = TiposPagamento.valueOf(parts[1].trim().toUpperCase());
                    TiposEntrega metodoEntrega = TiposEntrega.valueOf(parts[2].trim().toUpperCase());
                    Map<Produto, Integer> produtos = new HashMap<>();
                    String[] produtoEntries = parts[3].trim().split(";");
                    for (String entry : produtoEntries) {
                        String[] produtoData = entry.split(":");
                        if (produtoData.length != 2) throw new IllegalArgumentException("Formato de produto inválido.");
                        Produto produto = vendedor.getFranquia().buscarProduto(produtoData[0].trim());
                        if (produto == null) throw new IllegalArgumentException("Produto " + produtoData[0] + " não encontrado.");
                        int quantidade = Integer.parseInt(produtoData[1].trim());
                        produtos.put(produto, quantidade);
                    }
                    Pedido pedido = controller.vendedorService.registrarPedido(cliente, produtos, formaPagamento, metodoEntrega);
                    outputArea.setText("Pedido registrado: " + pedido.getId());
                } catch (Exception ex) {
                    outputArea.setText("Erro: " + ex.getMessage());
                }
            });
        });

        listarPedidosButton.setOnAction(e -> {
            outputArea.clear();
            controller.visualizarPedidos();
            for (Pedido pedido : controller.vendedorService.listaPedidos()) {
                outputArea.appendText("🛒 " + pedido.getId() + " | " + pedido.getCliente().getNome() + " | R$ " + pedido.getValorTotal() + " | " + pedido.getStatus() + "\n");
            }
        });

        logoutButton.setOnAction(e -> app.showLoginScreen());

        getChildren().addAll(titleLabel, cadastrarClienteButton, criarPedidoButton, listarPedidosButton, logoutButton, outputArea);
    }
}