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

import java.util.Optional;

public class GerenciarGerentesView extends VBox {

    private final DonoController donoController;
    private final TableView<Gerente> table = new TableView<>();
    private final ObservableList<Gerente> gerentesList;

    public GerenciarGerentesView(DonoService donoService) {
        this.donoController = new DonoController(donoService);
        this.gerentesList = FXCollections.observableArrayList();

        setPadding(new Insets(10));
        setSpacing(20);

        Text header = new Text("Gerenciar Gerentes");
        header.getStyleClass().add("page-header");

        Button addButton = new Button("Adicionar Gerente");
        addButton.getStyleClass().add("action-button");
        addButton.setOnAction(e -> handleAddGerente());
        HBox headerBox = new HBox(header, new Spacer(), addButton);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        setupTable();

        getChildren().addAll(headerBox, table);
        loadGerentes();
    }

    private void setupTable() {
        TableColumn<Gerente, String> nameCol = new TableColumn<>("Nome");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nome"));

        TableColumn<Gerente, String> cpfCol = new TableColumn<>("CPF");
        cpfCol.setCellValueFactory(new PropertyValueFactory<>("cpf"));

        TableColumn<Gerente, String> emailCol = new TableColumn<>("E-mail");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Gerente, String> franchiseCol = new TableColumn<>("Franquia");
        franchiseCol.setCellValueFactory(cellData -> {
            Franquia f = cellData.getValue().getFranquia();
            return new javafx.beans.property.SimpleStringProperty(f != null ? f.getNome() : "Nenhuma");
        });

        TableColumn<Gerente, Void> actionCol = new TableColumn<>("Ações");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final Button unlinkButton = new Button();
            private final HBox pane = new HBox(5, editButton, unlinkButton, deleteButton);

            {
                pane.setAlignment(Pos.CENTER);
                editButton.setGraphic(ComponentFactory.createIcon(IconManager.EDIT));
                editButton.getStyleClass().add("table-action-button");
                deleteButton.setGraphic(ComponentFactory.createIcon(IconManager.DELETE));
                deleteButton.getStyleClass().add("table-action-button");
                unlinkButton.setGraphic(ComponentFactory.createIcon(IconManager.UNLINK));
                unlinkButton.getStyleClass().add("table-action-button");

                editButton.setOnAction(event -> {
                    Gerente gerente = getTableView().getItems().get(getIndex());
                    handleEditGerente(gerente);
                });
                deleteButton.setOnAction(event -> {
                    Gerente gerente = getTableView().getItems().get(getIndex());
                    handleDeleteGerente(gerente);
                });
                unlinkButton.setOnAction(event -> {
                    Gerente gerente = getTableView().getItems().get(getIndex());
                    handleUnlinkGerente(gerente);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Gerente gerente = getTableView().getItems().get(getIndex());
                    unlinkButton.setVisible(gerente.getFranquia() != null);
                    setGraphic(pane);
                }
            }
        });

        table.getColumns().addAll(nameCol, cpfCol, emailCol, franchiseCol, actionCol);
        table.setItems(gerentesList);
        ComponentFactory.configureTable(table);
    }

    private void loadGerentes() {
        gerentesList.setAll(donoController.getGerentes());
    }

    private void handleAddGerente() {
        GerenteDialog dialog = new GerenteDialog();
        Optional<Gerente> result = dialog.showAndWait();
        result.ifPresent(gerente -> {
            try {
                donoController.addGerente(gerente.getNome(), gerente.getCpf(), gerente.getEmail(), gerente.getSenha());
                loadGerentes();
                AlertFactory.showInfo("Sucesso", "Gerente adicionado com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível adicionar o gerente: " + e.getMessage());
            }
        });
    }

    private void handleEditGerente(Gerente gerente) {
        GerenteDialog dialog = new GerenteDialog(gerente);
        Optional<Gerente> result = dialog.showAndWait();
        result.ifPresent(editedGerente -> {
            try {
                donoController.updateGerente(editedGerente.getId(), editedGerente.getNome(), editedGerente.getCpf(), editedGerente.getEmail(), editedGerente.getSenha());
                loadGerentes();
                AlertFactory.showInfo("Sucesso", "Gerente atualizado com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível atualizar o gerente: " + e.getMessage());
            }
        });
    }

    private void handleDeleteGerente(Gerente gerente) {
        boolean confirmed = AlertFactory.showConfirmation("Confirmar Exclusão",
                "Tem certeza que deseja excluir o gerente '" + gerente.getNome() + "'? Esta ação não pode ser desfeita.");
        if (confirmed) {
            try {
                donoController.deleteGerente(gerente.getId());
                loadGerentes();
                AlertFactory.showInfo("Sucesso", "Gerente excluído com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível excluir o gerente: " + e.getMessage());
            }
        }
    }

    private void handleUnlinkGerente(Gerente gerente) {
        if (gerente.getFranquia() == null) return;
        boolean confirmed = AlertFactory.showConfirmation("Confirmar Desvinculação",
                "Tem certeza que deseja desvincular o gerente '" + gerente.getNome() + "' da franquia '" + gerente.getFranquia().getNome() + "'?");
        if (confirmed) {
            try {
                donoController.unassignManager(gerente.getId());
                loadGerentes();
                AlertFactory.showInfo("Sucesso", "Gerente desvinculado com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível desvincular o gerente: " + e.getMessage());
            }
        }
    }
}
