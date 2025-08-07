// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.view.vendedor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import ufjf.dcc025.franquia.controller.VendedorController;
import ufjf.dcc025.franquia.enums.TiposEntrega;
import ufjf.dcc025.franquia.enums.TiposPagamento;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.service.VendedorService;
import ufjf.dcc025.franquia.util.AlertFactory;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;
import ufjf.dcc025.franquia.util.Spacer;
import ufjf.dcc025.franquia.view.common.ClienteDialog;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegistrarPedidoView extends BorderPane {

    private final VendedorController vendedorController;
    private final ObservableList<Produto> produtosDisponiveis;
    private final ObservableList<Map.Entry<Produto, Integer>> carrinhoList;
    private final TableView<Map.Entry<Produto, Integer>> carrinhoTable = new TableView<>();
    private final ComboBox<Cliente> clienteComboBox = new ComboBox<>();
    private final Label subtotalLabel = new Label();
    private final Label freteLabel = new Label();
    private final Label totalLabel = new Label();
    private final Map<Produto, Integer> carrinhoMap = new HashMap<>();

    public RegistrarPedidoView(VendedorService vendedorService) {
        this.vendedorController = new VendedorController(vendedorService);
        this.produtosDisponiveis = FXCollections.observableArrayList();
        this.carrinhoList = FXCollections.observableArrayList();

        setPadding(new Insets(10));

        VBox leftPanel = createLeftPanel();
        setCenter(leftPanel);

        VBox rightPanel = createRightPanel();
        setRight(rightPanel);

        loadInitialData();
    }

    private VBox createLeftPanel() {
        VBox panel = new VBox(20);

        Text header = new Text("Registrar Novo Pedido");
        header.getStyleClass().add("page-header");

        HBox addProductBox = new HBox(10);
        addProductBox.setAlignment(Pos.CENTER_LEFT);
        ComboBox<Produto> produtoComboBox = new ComboBox<>(produtosDisponiveis);
        produtoComboBox.setPromptText("Selecione um produto");
        Spinner<Integer> quantidadeSpinner = new Spinner<>(1, 100, 1);
        quantidadeSpinner.setPrefWidth(70);
        Button addButton = new Button("Adicionar");
        addButton.getStyleClass().add("action-button");
        addButton.setOnAction(e -> {
            Produto p = produtoComboBox.getValue();
            Integer qtd = quantidadeSpinner.getValue();
            if (p != null && qtd > 0) {
                adicionarAoCarrinho(p, qtd);
                produtoComboBox.getSelectionModel().clearSelection();
                quantidadeSpinner.getValueFactory().setValue(1);
            }
        });
        addProductBox.getChildren().addAll(new Label("Produto:"), produtoComboBox, new Label("Qtd:"), quantidadeSpinner, addButton);

        setupCarrinhoTable();

        panel.getChildren().addAll(header, addProductBox, new Label("Itens do Pedido:"), carrinhoTable);
        return panel;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(0, 0, 0, 30));
        panel.setPrefWidth(350);

        VBox clienteBox = new VBox(5);
        HBox clienteHeader = new HBox(5);
        clienteHeader.setAlignment(Pos.CENTER_LEFT);
        Button addClienteButton = new Button();
        addClienteButton.setGraphic(ComponentFactory.createIcon(IconManager.PLUS));
        addClienteButton.getStyleClass().add("table-action-button");
        addClienteButton.setOnAction(e -> handleAddCliente());
        clienteHeader.getChildren().addAll(new Label("Cliente"), new Spacer(), addClienteButton);
        clienteComboBox.setMaxWidth(Double.MAX_VALUE);
        clienteComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Cliente c) { return c == null ? "Selecione..." : c.getNome() + " (" + c.getCpf() + ")"; }
            @Override public Cliente fromString(String s) { return null; }
        });
        clienteBox.getChildren().addAll(clienteHeader, clienteComboBox);

        VBox opcoesBox = new VBox(10);
        ComboBox<TiposPagamento> pagamentoComboBox = new ComboBox<>(FXCollections.observableArrayList(TiposPagamento.values()));
        pagamentoComboBox.getSelectionModel().selectFirst();
        ComboBox<TiposEntrega> entregaComboBox = new ComboBox<>(FXCollections.observableArrayList(TiposEntrega.values()));
        entregaComboBox.getSelectionModel().selectFirst();
        entregaComboBox.valueProperty().addListener((obs, oldVal, newVal) -> atualizarTotal(newVal));
        opcoesBox.getChildren().addAll(new Label("Forma de Pagamento:"), pagamentoComboBox, new Label("Tipo de Entrega:"), entregaComboBox);

        VBox totalBox = createSummaryBox();
        Button finalizarButton = new Button("Finalizar Pedido");
        finalizarButton.getStyleClass().add("action-button");
        finalizarButton.setMaxWidth(Double.MAX_VALUE);
        finalizarButton.setOnAction(e -> handleFinalizarPedido(pagamentoComboBox.getValue(), entregaComboBox.getValue()));

        panel.getChildren().addAll(clienteBox, new Separator(), opcoesBox, new Spacer(), totalBox, finalizarButton);
        return panel;
    }

    private VBox createSummaryBox() {
        VBox summaryBox = new VBox(5);
        summaryBox.getStyleClass().add("summary-box");
        summaryBox.setPadding(new Insets(15));

        HBox subtotalLine = new HBox(new Label("Subtotal:"), new Spacer(), subtotalLabel);
        HBox freteLine = new HBox(new Label("Frete:"), new Spacer(), freteLabel);
        HBox totalLine = new HBox(new Label("Total:"), new Spacer(), totalLabel);
        totalLabel.getStyleClass().add("total-label");

        summaryBox.getChildren().addAll(subtotalLine, freteLine, new Separator(), totalLine);
        atualizarTotal(TiposEntrega.DELIVERY);
        return summaryBox;
    }

    private void setupCarrinhoTable() {
        TableColumn<Map.Entry<Produto, Integer>, String> nameCol = new TableColumn<>("Produto");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey().getNome()));

        TableColumn<Map.Entry<Produto, Integer>, Integer> qtyCol = new TableColumn<>("Qtd");
        qtyCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getValue()).asObject());

        TableColumn<Map.Entry<Produto, Integer>, Double> priceCol = new TableColumn<>("Preço");
        priceCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getKey().getPreco()).asObject());
        priceCol.setCellFactory(column -> new TableCell<>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("pt").setRegion("BR").build());
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });

        TableColumn<Map.Entry<Produto, Integer>, Void> actionCol = new TableColumn<>("Remover");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button();
            {
                deleteButton.setGraphic(ComponentFactory.createIcon(IconManager.DELETE));
                deleteButton.getStyleClass().add("table-action-button");
                deleteButton.setOnAction(event -> {
                    Map.Entry<Produto, Integer> entry = getTableView().getItems().get(getIndex());
                    removerDoCarrinho(entry.getKey());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        carrinhoTable.getColumns().addAll(nameCol, qtyCol, priceCol, actionCol);
        carrinhoTable.setItems(carrinhoList);
        ComponentFactory.configureTable(carrinhoTable);
    }

    private void loadInitialData() {
        produtosDisponiveis.setAll(vendedorController.getProdutosDisponiveis());
        clienteComboBox.setItems(FXCollections.observableArrayList(vendedorController.getClientes()));
    }

    private void adicionarAoCarrinho(Produto produto, int quantidade) {
        carrinhoMap.merge(produto, quantidade, Integer::sum);
        carrinhoList.setAll(carrinhoMap.entrySet());
        atualizarTotal(TiposEntrega.DELIVERY); // Recalcula com frete padrão
    }

    private void removerDoCarrinho(Produto produto) {
        carrinhoMap.remove(produto);
        carrinhoList.setAll(carrinhoMap.entrySet());
        atualizarTotal(TiposEntrega.DELIVERY); // Recalcula com frete padrão
    }

    private void atualizarTotal(TiposEntrega tipoEntrega) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("pt").setRegion("BR").build());
        double subtotal = carrinhoMap.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPreco() * entry.getValue())
                .sum();

        double frete = (tipoEntrega == TiposEntrega.DELIVERY && subtotal > 0 && subtotal < 500) ? 15.0 : 0.0;

        subtotalLabel.setText(currencyFormat.format(subtotal));
        freteLabel.setText(currencyFormat.format(frete));
        totalLabel.setText(currencyFormat.format(subtotal + frete));
    }

    private void handleAddCliente() {
        ClienteDialog dialog = new ClienteDialog();
        dialog.showAndWait().ifPresent(novoCliente -> {
            try {
                Cliente clienteCadastrado = vendedorController.addCliente(novoCliente.getNome(), novoCliente.getCpf(), novoCliente.getEmail(), novoCliente.getTelefone(), novoCliente.getEndereco());
                clienteComboBox.getItems().add(clienteCadastrado);
                clienteComboBox.getSelectionModel().select(clienteCadastrado);
                AlertFactory.showInfo("Sucesso", "Cliente cadastrado com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível cadastrar o cliente: " + e.getMessage());
            }
        });
    }

    private void handleFinalizarPedido(TiposPagamento pagamento, TiposEntrega entrega) {
        if (clienteComboBox.getValue() == null) {
            AlertFactory.showError("Validação", "Por favor, selecione um cliente.");
            return;
        }
        if (carrinhoMap.isEmpty()) {
            AlertFactory.showError("Validação", "O carrinho está vazio.");
            return;
        }

        try {
            vendedorController.criarPedido(clienteComboBox.getValue(), new HashMap<>(carrinhoMap), pagamento, entrega);
            AlertFactory.showInfo("Sucesso", "Pedido registrado com sucesso!");
            resetForm();
        } catch (Exception e) {
            AlertFactory.showError("Erro", "Não foi possível registrar o pedido: " + e.getMessage());
        }
    }

    private void resetForm() {
        carrinhoMap.clear();
        carrinhoList.clear();
        clienteComboBox.getSelectionModel().clearSelection();
        atualizarTotal(TiposEntrega.DELIVERY);
    }
}
