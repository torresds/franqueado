// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.view.dono;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.controller.DonoController;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.service.DonoService;
import ufjf.dcc025.franquia.util.AlertFactory;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;
import ufjf.dcc025.franquia.util.Spacer;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class GerenciarFranquiasView extends VBox {

    private final DonoController donoController;
    private final TableView<Franquia> table = new TableView<>();
    private final ObservableList<Franquia> franquiasList;

    public GerenciarFranquiasView(DonoService donoService) {
        this.donoController = new DonoController(donoService);
        this.franquiasList = FXCollections.observableArrayList();

        setPadding(new Insets(10));
        setSpacing(20);

        // Cabeçalho
        Text header = new Text("Gerenciar Franquias");
        header.getStyleClass().add("page-header");

        // Botão de Adicionar
        Button addButton = new Button("Adicionar Franquia");
        addButton.getStyleClass().add("action-button");
        addButton.setOnAction(e -> handleAddFranquia());
        HBox headerBox = new HBox(header, new Spacer(), addButton);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Tabela de Franquias
        setupTable();

        getChildren().addAll(headerBox, table);
        loadFranquias();
    }

    private void setupTable() {
        // Coluna Nome
        TableColumn<Franquia, String> nameCol = new TableColumn<>("Nome");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nome"));

        // Coluna Endereço
        TableColumn<Franquia, String> addressCol = new TableColumn<>("Endereço");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("endereco"));

        // Coluna Gerente
        TableColumn<Franquia, String> managerCol = new TableColumn<>("Gerente");
        managerCol.setCellValueFactory(cellData -> {
            Gerente gerente = cellData.getValue().getGerente();
            return new javafx.beans.property.SimpleStringProperty(gerente != null ? gerente.getNome() : "N/A");
        });

        // Coluna Receita
        TableColumn<Franquia, Double> revenueCol = new TableColumn<>("Receita");
        revenueCol.setCellValueFactory(new PropertyValueFactory<>("receita"));
        revenueCol.setCellFactory(column -> new TableCell<>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item));
                }
            }
        });

        // Coluna Ações
        TableColumn<Franquia, Void> actionCol = new TableColumn<>("Ações");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                pane.setAlignment(Pos.CENTER);
                editButton.setGraphic(ComponentFactory.createIcon(IconManager.EDIT));
                editButton.getStyleClass().add("table-action-button");
                deleteButton.setGraphic(ComponentFactory.createIcon(IconManager.DELETE));
                deleteButton.getStyleClass().add("table-action-button");

                editButton.setOnAction(event -> {
                    Franquia franquia = getTableView().getItems().get(getIndex());
                    handleEditFranquia(franquia);
                });
                deleteButton.setOnAction(event -> {
                    Franquia franquia = getTableView().getItems().get(getIndex());
                    handleDeleteFranquia(franquia);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        table.getColumns().addAll(nameCol, addressCol, managerCol, revenueCol, actionCol);
        table.setItems(franquiasList);
        ComponentFactory.configureTable(table);
    }

    private void loadFranquias() {
        franquiasList.setAll(donoController.getFranquias());
    }


    private void handleAddFranquia() {
        FranquiaDialog dialog = new FranquiaDialog(donoController.donoService.getGerenteRepo().findAll(), null);
        Optional<FranquiaDialog.Result> result = dialog.showAndWait();
        result.ifPresent(data -> {
            try {
                donoController.addFranquia(data.nome(), data.endereco(), data.gerenteId());
                loadFranquias();
                AlertFactory.showInfo("Sucesso", "Franquia adicionada com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível adicionar a franquia: " + e.getMessage());
            }
        });
    }

    private void handleEditFranquia(Franquia franquia) {
        FranquiaDialog dialog = new FranquiaDialog(donoController.donoService.getGerenteRepo().findAll(), franquia);
        Optional<FranquiaDialog.Result> result = dialog.showAndWait();
        result.ifPresent(data -> {
            try {
                // Utilizando o método de atualização que agora inclui o ID do gerente
                donoController.updateFranquia(data.original().getId(), data.nome(), data.endereco(), data.gerenteId());
                loadFranquias();
                AlertFactory.showInfo("Sucesso", "Franquia atualizada com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível atualizar a franquia: " + e.getMessage());
            }
        });
    }


    private void handleDeleteFranquia(Franquia franquia) {
        boolean confirmed = AlertFactory.showConfirmation("Confirmar Exclusão",
                "Tem certeza que deseja excluir a franquia '" + franquia.getNome() + "'?");
        if (confirmed) {
            try {
                donoController.deleteFranquia(franquia.getId());
                loadFranquias();
                AlertFactory.showInfo("Sucesso", "Franquia excluída com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível excluir a franquia: " + e.getMessage());
            }
        }
    }
}
