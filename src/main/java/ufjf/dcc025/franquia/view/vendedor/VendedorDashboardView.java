// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.view.vendedor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.controller.VendedorController;
import ufjf.dcc025.franquia.enums.EstadoPedido;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.service.VendedorService;
import ufjf.dcc025.franquia.util.AlertFactory;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class VendedorDashboardView extends VBox {

    private final VendedorController vendedorController;
    private final ObservableList<Pedido> pedidosList;

    public VendedorDashboardView(VendedorService vendedorService) {
        this.vendedorController = new VendedorController(vendedorService);
        this.pedidosList = FXCollections.observableArrayList();

        setPadding(new Insets(10));
        setSpacing(25);

        Text header = new Text("Meu Dashboard");
        header.getStyleClass().add("page-header");

        GridPane indicatorsGrid = createIndicatorsGrid();

        Text recentOrdersHeader = new Text("Meus Pedidos Recentes");
        recentOrdersHeader.getStyleClass().add("page-subheader");

        TableView<Pedido> table = createPedidosTable();

        getChildren().addAll(header, indicatorsGrid, recentOrdersHeader, table);
        loadPedidos();
    }

    private GridPane createIndicatorsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("pt").setRegion("BR").build());
        double totalVendas = vendedorController.vendedorService.getVendedor().getTotalVendas();
        int totalPedidos = vendedorController.getPedidosDoVendedor().size();

        VBox totalVendasCard = ComponentFactory.createIndicatorCard("Minhas Vendas", currencyFormat.format(totalVendas), IconManager.MONEY);
        VBox totalPedidosCard = ComponentFactory.createIndicatorCard("Meus Pedidos", String.valueOf(totalPedidos), IconManager.CART);

        grid.add(totalVendasCard, 0, 0);
        grid.add(totalPedidosCard, 1, 0);

        return grid;
    }

    private TableView<Pedido> createPedidosTable() {
        TableView<Pedido> table = new TableView<>();

        TableColumn<Pedido, String> dateCol = new TableColumn<>("Data");
        dateCol.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getData()));
        });

        TableColumn<Pedido, String> clientCol = new TableColumn<>("Cliente");
        clientCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCliente().getNome()));

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
                    setGraphic(null);
                } else {
                    Label statusTag = new Label(item.toString());
                    statusTag.getStyleClass().addAll("status-tag", "status-" + item.toString().toLowerCase());
                    setGraphic(statusTag);
                }
            }
        });

        TableColumn<Pedido, Void> actionCol = new TableColumn<>("Ações");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button cancelButton = new Button();
            private final HBox pane = new HBox(5, editButton, cancelButton);

            {
                pane.setAlignment(Pos.CENTER);
                editButton.setGraphic(ComponentFactory.createIcon(IconManager.EDIT));
                editButton.getStyleClass().add("table-action-button");
                Tooltip.install(editButton, new Tooltip("Solicitar Alteração"));

                cancelButton.setGraphic(ComponentFactory.createIcon(IconManager.DELETE));
                cancelButton.getStyleClass().add("table-action-button");
                Tooltip.install(cancelButton, new Tooltip("Solicitar Cancelamento"));

                editButton.setOnAction(event -> {
                    Pedido pedido = getTableView().getItems().get(getIndex());
                    handleEditRequest(pedido);
                });
                cancelButton.setOnAction(event -> {
                    Pedido pedido = getTableView().getItems().get(getIndex());
                    handleCancelRequest(pedido);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Pedido pedido = getTableView().getItems().get(getIndex());
                    // Vendedor só pode solicitar alteração/cancelamento de pedidos pendentes ou já aprovados
                    boolean canRequest = pedido.isPendente() || pedido.isAprovado();
                    pane.setVisible(canRequest);
                    setGraphic(pane);
                }
            }
        });

        table.getColumns().addAll(dateCol, clientCol, valueCol, statusCol, actionCol);
        table.setItems(pedidosList);
        ComponentFactory.configureTable(table);
        return table;
    }

    private void loadPedidos() {
        pedidosList.setAll(vendedorController.getPedidosDoVendedor());
    }

    private void handleEditRequest(Pedido pedido) {
        AlterarPedidoDialog dialog = new AlterarPedidoDialog(pedido, vendedorController);
        dialog.showAndWait().ifPresent(result -> {
            try {
                vendedorController.solicitarAlteracao(pedido.getId(), result.novosProdutos(), result.novaEntrega());
                loadPedidos();
                AlertFactory.showInfo("Sucesso", "Solicitação de alteração enviada ao gerente.");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível solicitar a alteração: " + e.getMessage());
            }
        });
    }

    private void handleCancelRequest(Pedido pedido) {
        boolean confirmed = AlertFactory.showConfirmation("Solicitar Cancelamento",
                "Tem certeza que deseja solicitar o cancelamento do pedido " + pedido.getId() + "?");
        if (confirmed) {
            try {
                vendedorController.solicitarCancelamento(pedido.getId());
                loadPedidos();
                AlertFactory.showInfo("Sucesso", "Solicitação de cancelamento enviada ao gerente.");
            } catch (Exception e) {
                AlertFactory.showError("Erro", "Não foi possível solicitar o cancelamento: " + e.getMessage());
            }
        }
    }
}
