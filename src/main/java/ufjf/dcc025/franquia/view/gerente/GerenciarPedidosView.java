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
import ufjf.dcc025.franquia.controller.GerenteController;
import ufjf.dcc025.franquia.enums.EstadoPedido;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.service.GerenteService;
import ufjf.dcc025.franquia.util.AlertFactory;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;
import ufjf.dcc025.franquia.view.common.PlaceholderView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class GerenciarPedidosView extends VBox {

    private final GerenteController gerenteController;
    private final TableView<Pedido> table = new TableView<>();
    private final ObservableList<Pedido> pedidosList;


    public GerenciarPedidosView(GerenteService gerenteService) {
        this.gerenteController = new GerenteController(gerenteService);
        this.pedidosList = FXCollections.observableArrayList();

        setPadding(new Insets(10));
        setSpacing(20);

        Text header = new Text("Gerenciar Pedidos da Franquia");
        header.getStyleClass().add("page-header");

        if (gerenteService.getFranquia() == null) {
            getChildren().addAll(header, new PlaceholderView("Nenhuma franquia atribuída.", "Você não pode gerenciar pedidos sem estar alocado a uma franquia."));
            return;
        }

        setupTable();

        getChildren().addAll(header, table);
        loadPedidos();
    }


    private void setupTable() {
        TableColumn<Pedido, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Pedido, String> dateCol = new TableColumn<>("Data");
        dateCol.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getData()));
        });

        TableColumn<Pedido, String> clientCol = new TableColumn<>("Cliente");
        clientCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCliente().getNome()));

        TableColumn<Pedido, String> sellerCol = new TableColumn<>("Vendedor");
        sellerCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getVendedor().getNome()));

        TableColumn<Pedido, Double> valueCol = new TableColumn<>("Valor Total");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));
        valueCol.setCellFactory(column -> new TableCell<>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("pt").setRegion("BR").build());
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });

        TableColumn<Pedido, EstadoPedido> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(EstadoPedido item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusTag = new Label(item.toString());
                    statusTag.getStyleClass().add("status-tag");
                    statusTag.getStyleClass().add("status-" + item.toString().toLowerCase());
                    setGraphic(statusTag);
                }
            }
        });

        TableColumn<Pedido, Void> actionCol = new TableColumn<>("Ações");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveButton = new Button();
            private final Button cancelButton = new Button();
            private final Button denyButton = new Button();
            private final HBox pane = new HBox(5);

            {
                pane.setAlignment(Pos.CENTER);
                // Botão Aprovar Padrão
                approveButton.setGraphic(ComponentFactory.createIcon(IconManager.CHECK));
                approveButton.getStyleClass().add("table-action-button");
                Tooltip.install(approveButton, new Tooltip("Aprovar Pedido"));
                approveButton.setOnAction(event -> handleApprove(getTableView().getItems().get(getIndex())));

                // Botão Cancelar Padrão
                cancelButton.setGraphic(ComponentFactory.createIcon(IconManager.DELETE));
                cancelButton.getStyleClass().add("table-action-button");
                Tooltip.install(cancelButton, new Tooltip("Cancelar Pedido"));
                cancelButton.setOnAction(event -> handleCancel(getTableView().getItems().get(getIndex())));

                // Botão Negar Solicitação
                denyButton.setGraphic(ComponentFactory.createIcon(IconManager.CLOSE));
                denyButton.getStyleClass().add("table-action-button");
                Tooltip.install(denyButton, new Tooltip("Negar Solicitação"));
                denyButton.setOnAction(event -> handleDeny(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Pedido pedido = getTableView().getItems().get(getIndex());
                    pane.getChildren().clear();
                    switch (pedido.getStatus()) {
                        case PENDENTE:
                            pane.getChildren().addAll(approveButton, cancelButton);
                            break;
                        case APROVADO:
                            // Gerente pode cancelar diretamente um pedido aprovado
                            pane.getChildren().add(cancelButton);
                            break;
                        case ALTERACAO_SOLICITADA:
                            Tooltip.install(approveButton, new Tooltip("Aprovar Alteração"));
                            pane.getChildren().addAll(approveButton, denyButton);
                            break;
                        case CANCELAMENTO_SOLICITADO:
                            Tooltip.install(approveButton, new Tooltip("Confirmar Cancelamento"));
                            pane.getChildren().addAll(approveButton, denyButton);
                            break;
                        default:
                            break;
                    }
                    setGraphic(pane);
                }
            }
        });

        table.getColumns().addAll(idCol, dateCol, clientCol, sellerCol, valueCol, statusCol, actionCol);
        table.setItems(pedidosList);
        ComponentFactory.configureTable(table);
    }

    private void loadPedidos() {
        pedidosList.setAll(gerenteController.getPedidosDaFranquia());
    }

    private void handleApprove(Pedido pedido) {
        try {
            switch (pedido.getStatus()) {
                case PENDENTE:
                    gerenteController.aprovarPedido(pedido.getId());
                    break;
                case ALTERACAO_SOLICITADA:
                    VisualizarAlteracaoDialog dialog = new VisualizarAlteracaoDialog(pedido);
                    dialog.showAndWait().ifPresent(confirmed -> {
                        if (confirmed) {
                            gerenteController.aprovarAlteracaoPedido(pedido.getId());
                        }
                    });
                    break;
                case CANCELAMENTO_SOLICITADO:
                    gerenteController.aprovarCancelamentoPedido(pedido.getId());
                    break;
            }
            loadPedidos();
        } catch (Exception e) {
            AlertFactory.showError("Erro", "Não foi possível aprovar a ação: " + e.getMessage());
        }
    }

    private void handleCancel(Pedido pedido) {
        boolean confirmed = AlertFactory.showConfirmation("Confirmar Cancelamento",
                "Tem certeza que deseja cancelar o pedido " + pedido.getId() + "? Esta ação pode ser irreversível.");
        if (confirmed) {
            try {
                gerenteController.cancelarPedido(pedido.getId());
                loadPedidos();
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível cancelar o pedido: " + e.getMessage());
            }
        }
    }

    private void handleDeny(Pedido pedido) {
        try {
            gerenteController.negarSolicitacaoPedido(pedido.getId());
            loadPedidos();
        } catch (Exception e) {
            AlertFactory.showError("Erro", "Não foi possível negar a solicitação: " + e.getMessage());
        }
    }
}
