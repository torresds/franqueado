package ufjf.dcc025.franquia.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.controller.GerenteController;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.service.GerenteService;
import ufjf.dcc025.franquia.util.AlertFactory;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;
import ufjf.dcc025.franquia.util.Spacer;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class GerenciarVendedoresView extends VBox {

    private final GerenteController gerenteController;
    private final TableView<Vendedor> table = new TableView<>();
    private final ObservableList<Vendedor> vendedoresList;

    public GerenciarVendedoresView(GerenteService gerenteService) {
        this.gerenteController = new GerenteController(gerenteService);
        this.vendedoresList = FXCollections.observableArrayList();

        setPadding(new Insets(10));
        setSpacing(20);

        Text header = new Text("Gerenciar Vendedores");
        header.getStyleClass().add("page-header");

        Button addButton = new Button("Adicionar Vendedor");
        addButton.getStyleClass().add("action-button");
        addButton.setOnAction(e -> handleAddVendedor());
        HBox headerBox = new HBox(header, new Spacer(), addButton);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        setupTable();

        getChildren().addAll(headerBox, table);
        loadVendedores();
    }

    private void setupTable() {
        TableColumn<Vendedor, String> nameCol = new TableColumn<>("Nome");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nome"));

        TableColumn<Vendedor, String> cpfCol = new TableColumn<>("CPF");
        cpfCol.setCellValueFactory(new PropertyValueFactory<>("cpf"));

        TableColumn<Vendedor, String> emailCol = new TableColumn<>("E-mail");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Vendedor, Double> salesCol = new TableColumn<>("Total de Vendas");
        salesCol.setCellValueFactory(new PropertyValueFactory<>("totalVendas"));
        salesCol.setCellFactory(column -> new TableCell<>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("pt").setRegion("BR").build());
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });

        TableColumn<Vendedor, Void> actionCol = new TableColumn<>("Ações");
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
                    Vendedor vendedor = getTableView().getItems().get(getIndex());
                    handleEditVendedor(vendedor);
                });
                deleteButton.setOnAction(event -> {
                    Vendedor vendedor = getTableView().getItems().get(getIndex());
                    handleDeleteVendedor(vendedor);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(nameCol, cpfCol, emailCol, salesCol, actionCol);
        table.setItems(vendedoresList);
        ComponentFactory.configureTable(table);
    }

    private void loadVendedores() {
        vendedoresList.setAll(gerenteController.getVendedoresDaFranquia());
    }

    private void handleAddVendedor() {
        VendedorDialog dialog = new VendedorDialog();
        Optional<Vendedor> result = dialog.showAndWait();
        result.ifPresent(vendedor -> {
            try {
                gerenteController.addVendedor(vendedor.getNome(), vendedor.getCpf(), vendedor.getEmail(), vendedor.getSenha());
                loadVendedores();
                AlertFactory.showInfo("Sucesso", "Vendedor adicionado com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível adicionar o vendedor: " + e.getMessage());
            }
        });
    }

    private void handleEditVendedor(Vendedor vendedor) {
        VendedorDialog dialog = new VendedorDialog(vendedor);
        Optional<Vendedor> result = dialog.showAndWait();
        result.ifPresent(editedVendedor -> {
            try {
                gerenteController.updateVendedor(editedVendedor.getId(), editedVendedor.getNome(), editedVendedor.getCpf(), editedVendedor.getEmail(), editedVendedor.getSenha());
                loadVendedores();
                AlertFactory.showInfo("Sucesso", "Vendedor atualizado com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível atualizar o vendedor: " + e.getMessage());
            }
        });
    }

    private void handleDeleteVendedor(Vendedor vendedor) {
        boolean confirmed = AlertFactory.showConfirmation("Confirmar Exclusão",
                "Tem certeza que deseja excluir o vendedor '" + vendedor.getNome() + "'?");
        if (confirmed) {
            try {
                gerenteController.deleteVendedor(vendedor.getId());
                loadVendedores();
                AlertFactory.showInfo("Sucesso", "Vendedor excluído com sucesso!");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível excluir o vendedor: " + e.getMessage());
            }
        }
    }
}
