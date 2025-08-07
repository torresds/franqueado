// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.view.gerente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.beans.property.ReadOnlyObjectWrapper;
import ufjf.dcc025.franquia.controller.GerenteController;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.service.GerenteService;
import ufjf.dcc025.franquia.util.AlertFactory;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;
import ufjf.dcc025.franquia.util.Spacer;
import ufjf.dcc025.franquia.view.common.AtualizarEstoqueDialog;
import ufjf.dcc025.franquia.view.common.PlaceholderView;
import ufjf.dcc025.franquia.view.common.ProdutoDialog;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class GerenciarEstoqueView extends VBox {

    private final GerenteController gerenteController;
    private final TableView<Map.Entry<Produto, Integer>> table = new TableView<>();
    private final ObservableList<Map.Entry<Produto, Integer>> estoqueList;

    public GerenciarEstoqueView(GerenteService gerenteService) {
        this.gerenteController = new GerenteController(gerenteService);
        this.estoqueList = FXCollections.observableArrayList();

        setPadding(new Insets(10));
        setSpacing(20);

        Text header = new Text("Gerenciar Estoque");
        header.getStyleClass().add("page-header");

        if (gerenteService.getFranquia() == null) {
            getChildren().addAll(header,
                    new PlaceholderView("Nenhuma franquia atribuída.",
                            "Você não pode gerenciar o estoque sem estar alocado a uma franquia."));
            return;
        }

        Button addButton = new Button("Adicionar Produto");
        addButton.getStyleClass().add("action-button");
        addButton.setOnAction(e -> handleAddProduto());
        HBox headerBox = new HBox(header, new Spacer(), addButton);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        setupTable();
        getChildren().addAll(headerBox, table);
        loadEstoque();
    }

    private void setupTable() {
        TableColumn<Map.Entry<Produto, Integer>, String> nameCol = new TableColumn<>("Produto");
        nameCol.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getKey().getNome())
        );

        TableColumn<Map.Entry<Produto, Integer>, String> descCol = new TableColumn<>("Descrição");
        descCol.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getKey().getDescricao())
        );

        TableColumn<Map.Entry<Produto, Integer>, Double> priceCol = new TableColumn<>("Preço Unitário");
        priceCol.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getKey().getPreco())
        );
        priceCol.setCellFactory(column -> new TableCell<>() {
            private final NumberFormat currencyFormat =
                    NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("pt").setRegion("BR").build());
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });

        TableColumn<Map.Entry<Produto, Integer>, Integer> qtyCol = new TableColumn<>("Quantidade");
        qtyCol.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getValue())
        );

        TableColumn<Map.Entry<Produto, Integer>, Void> actionCol = new TableColumn<>("Ações");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button stockButton = new Button();
            private final Button deleteButton = new Button(); // NOVO
            private final HBox pane = new HBox(5, editButton, stockButton, deleteButton); // NOVO
            {
                pane.setAlignment(Pos.CENTER);
                editButton.setGraphic(ComponentFactory.createIcon(IconManager.EDIT));
                editButton.getStyleClass().add("table-action-button");
                Tooltip.install(editButton, new Tooltip("Editar Informações do Produto"));

                stockButton.setGraphic(ComponentFactory.createIcon(IconManager.BOX));
                stockButton.getStyleClass().add("table-action-button");
                Tooltip.install(stockButton, new Tooltip("Atualizar Quantidade"));

                deleteButton.setGraphic(ComponentFactory.createIcon(IconManager.DELETE)); // NOVO
                deleteButton.getStyleClass().add("table-action-button"); // NOVO
                Tooltip.install(deleteButton, new Tooltip("Remover Produto")); // NOVO

                editButton.setOnAction(event -> {
                    Produto produto = getTableView().getItems().get(getIndex()).getKey();
                    handleEditProduto(produto);
                });
                stockButton.setOnAction(event -> {
                    Map.Entry<Produto, Integer> entry =
                            getTableView().getItems().get(getIndex());
                    handleUpdateEstoque(entry.getKey(), entry.getValue());
                });
                deleteButton.setOnAction(event -> { // NOVO
                    Produto produto = getTableView().getItems().get(getIndex()).getKey();
                    handleDeleteProduto(produto);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Lógica para mostrar o botão de apagar apenas se o estoque for 0
                    Map.Entry<Produto, Integer> entry = getTableView().getItems().get(getIndex());
                    deleteButton.setVisible(entry.getValue() == 0);
                    setGraphic(pane);
                }
            }
        });

        table.getColumns().setAll(nameCol, descCol, priceCol, qtyCol, actionCol);
        table.setItems(estoqueList);
        ComponentFactory.configureTable(table);
    }

    private void loadEstoque() {
        estoqueList.setAll(gerenteController.getEstoque().entrySet());
    }

    private void handleAddProduto() {
        ProdutoDialog dialog = new ProdutoDialog();
        dialog.showAndWait().ifPresent(result -> {
            try {
                gerenteController.addProduto(
                        result.codigo(), result.nome(), result.descricao(), result.preco(), result.quantidadeInicial()
                );
                loadEstoque();
                AlertFactory.showInfo("Sucesso", "Produto adicionado com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível adicionar o produto: " + e.getMessage());
            }
        });
    }

    private void handleEditProduto(Produto produto) {
        ProdutoDialog dialog = new ProdutoDialog(produto);
        dialog.showAndWait().ifPresent(result -> {
            try {
                gerenteController.updateProduto(
                        result.codigo(), result.nome(), result.descricao(), result.preco()
                );
                loadEstoque();
                AlertFactory.showInfo("Sucesso", "Produto atualizado com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível atualizar o produto: " + e.getMessage());
            }
        });
    }

    private void handleUpdateEstoque(Produto produto, int quantidadeAtual) {
        AtualizarEstoqueDialog dialog =
                new AtualizarEstoqueDialog(produto.getNome(), quantidadeAtual);
        dialog.showAndWait().ifPresent(novaQuantidade -> {
            try {
                gerenteController.updateEstoque(produto.getCodigo(), novaQuantidade);
                loadEstoque();
                AlertFactory.showInfo("Sucesso", "Estoque atualizado com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível atualizar o estoque: " + e.getMessage());
            }
        });
    }

    private void handleDeleteProduto(Produto produto) {
        boolean confirmed = AlertFactory.showConfirmation("Confirmar Remoção",
                "Tem certeza que deseja remover o produto '" + produto.getNome() + "' do catálogo? Esta ação não pode ser desfeita.");
        if (confirmed) {
            try {
                gerenteController.removerProduto(produto.getCodigo());
                loadEstoque();
                AlertFactory.showInfo("Sucesso", "Produto removido com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível remover o produto: " + e.getMessage());
            }
        }
    }
}
