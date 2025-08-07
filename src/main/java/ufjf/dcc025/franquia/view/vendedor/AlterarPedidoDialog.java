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
import ufjf.dcc025.franquia.controller.VendedorController;
import ufjf.dcc025.franquia.enums.TiposEntrega;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;
import ufjf.dcc025.franquia.util.Spacer;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AlterarPedidoDialog extends Dialog<AlterarPedidoDialog.AlteracaoResult> {

    // Record para retornar os dados alterados
    public record AlteracaoResult(Map<Produto, Integer> novosProdutos, TiposEntrega novaEntrega) {}

    private final VendedorController vendedorController;
    private final ObservableList<Produto> produtosDisponiveis;
    private final ObservableList<Map.Entry<Produto, Integer>> carrinhoList;
    private final TableView<Map.Entry<Produto, Integer>> carrinhoTable = new TableView<>();
    private final Map<Produto, Integer> carrinhoMap;
    private final ComboBox<TiposEntrega> entregaComboBox;

    public AlterarPedidoDialog(Pedido pedido, VendedorController vendedorController) {
        this.vendedorController = vendedorController;
        this.produtosDisponiveis = FXCollections.observableArrayList(vendedorController.getProdutosDisponiveis());
        this.carrinhoMap = new HashMap<>(pedido.getProdutosQuantidade());
        this.carrinhoList = FXCollections.observableArrayList(carrinhoMap.entrySet());

        setTitle("Solicitar Alteração do Pedido");
        setHeaderText("Modifique os itens e a forma de entrega para o pedido ID: " + pedido.getId());

        ButtonType saveButtonType = new ButtonType("Solicitar Alteração", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        getDialogPane().setPrefWidth(600);

        BorderPane content = new BorderPane();
        content.setPadding(new Insets(10));

        // Painel para adicionar produtos
        HBox addProductBox = new HBox(10);
        addProductBox.setAlignment(Pos.CENTER_LEFT);
        addProductBox.setPadding(new Insets(10, 0, 10, 0));
        ComboBox<Produto> produtoComboBox = new ComboBox<>(produtosDisponiveis);
        produtoComboBox.setPromptText("Selecione um produto");
        Spinner<Integer> quantidadeSpinner = new Spinner<>(1, 100, 1);
        quantidadeSpinner.setPrefWidth(70);
        Button addButton = new Button("Adicionar");
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

        // Tabela do carrinho
        setupCarrinhoTable();

        // Painel inferior com opções de entrega
        entregaComboBox = new ComboBox<>(FXCollections.observableArrayList(TiposEntrega.values()));
        entregaComboBox.setValue(pedido.getMetodoEntrega());
        HBox bottomBox = new HBox(10, new Label("Nova Forma de Entrega:"), entregaComboBox);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));

        VBox centerBox = new VBox(10, addProductBox, carrinhoTable, bottomBox);
        getDialogPane().setContent(centerBox);

        // Converte o resultado quando o botão de salvar é clicado
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new AlteracaoResult(new HashMap<>(carrinhoMap), entregaComboBox.getValue());
            }
            return null;
        });
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

    private void adicionarAoCarrinho(Produto produto, int quantidade) {
        carrinhoMap.merge(produto, quantidade, Integer::sum);
        carrinhoList.setAll(carrinhoMap.entrySet());
    }

    private void removerDoCarrinho(Produto produto) {
        carrinhoMap.remove(produto);
        carrinhoList.setAll(carrinhoMap.entrySet());
    }
}
