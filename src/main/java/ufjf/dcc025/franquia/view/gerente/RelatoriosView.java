// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.view.gerente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.controller.GerenteController;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.service.GerenteService;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.view.common.PlaceholderView;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class RelatoriosView extends VBox {

    private final GerenteController gerenteController;

    public RelatoriosView(GerenteService gerenteService) {
        this.gerenteController = new GerenteController(gerenteService);
        setPadding(new Insets(10));
        setSpacing(25);

        Text header = new Text("Relatórios da Franquia");
        header.getStyleClass().add("page-header");

        getChildren().add(header);

        if (gerenteService.getFranquia() == null) {
            getChildren().add(new PlaceholderView("Nenhuma franquia atribuída.", "Você não pode visualizar relatórios."));
            return;
        }

        GridPane reportsGrid = new GridPane();
        reportsGrid.setHgap(30);
        reportsGrid.setVgap(30);

        reportsGrid.add(createVendedoresRankingPane(), 0, 0);
        reportsGrid.add(createProdutosRankingPane(), 1, 0);
        reportsGrid.add(createClientesRankingPane(), 0, 1);

        getChildren().add(reportsGrid);
    }

    private VBox createVendedoresRankingPane() {
        VBox container = new VBox(10);
        Text subheader = new Text("Ranking de Vendedores (Local)");
        subheader.getStyleClass().add("page-subheader");

        TableView<Vendedor> table = new TableView<>();
        ObservableList<Vendedor> vendedoresList = FXCollections.observableArrayList(gerenteController.getRankingVendedoresLocal());

        TableColumn<Vendedor, String> posCol = createPositionColumn();
        TableColumn<Vendedor, String> nameCol = new TableColumn<>("Vendedor");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        TableColumn<Vendedor, Double> salesCol = createCurrencyColumn("Total de Vendas", "totalVendas");

        table.getColumns().addAll(posCol, nameCol, salesCol);
        table.setItems(vendedoresList);
        ComponentFactory.configureTable(table);
        container.getChildren().addAll(subheader, table);
        return container;
    }

    private VBox createProdutosRankingPane() {
        VBox container = new VBox(10);
        Text subheader = new Text("Produtos Mais Vendidos");
        subheader.getStyleClass().add("page-subheader");

        TableView<Map.Entry<Produto, Long>> table = new TableView<>();
        ObservableList<Map.Entry<Produto, Long>> produtosList = FXCollections.observableArrayList(gerenteController.getProdutosMaisVendidos());

        TableColumn<Map.Entry<Produto, Long>, String> posCol = createPositionColumn();
        TableColumn<Map.Entry<Produto, Long>, String> nameCol = new TableColumn<>("Produto");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey().getNome()));
        TableColumn<Map.Entry<Produto, Long>, Long> qtyCol = new TableColumn<>("Unidades Vendidas");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        table.getColumns().addAll(posCol, nameCol, qtyCol);
        table.setItems(produtosList);
        ComponentFactory.configureTable(table);
        container.getChildren().addAll(subheader, table);
        return container;
    }

    private VBox createClientesRankingPane() {
        VBox container = new VBox(10);
        Text subheader = new Text("Clientes Mais Frequentes");
        subheader.getStyleClass().add("page-subheader");

        TableView<Map.Entry<Cliente, Long>> table = new TableView<>();
        ObservableList<Map.Entry<Cliente, Long>> clientesList = FXCollections.observableArrayList(gerenteController.getClientesMaisFrequentes());

        TableColumn<Map.Entry<Cliente, Long>, String> posCol = createPositionColumn();
        TableColumn<Map.Entry<Cliente, Long>, String> nameCol = new TableColumn<>("Cliente");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey().getNome()));
        TableColumn<Map.Entry<Cliente, Long>, String> cpfCol = new TableColumn<>("CPF");
        cpfCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey().getCpf()));
        TableColumn<Map.Entry<Cliente, Long>, Long> qtyCol = new TableColumn<>("Nº de Pedidos");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        table.getColumns().addAll(posCol, nameCol, cpfCol, qtyCol);
        table.setItems(clientesList);
        ComponentFactory.configureTable(table);
        container.getChildren().addAll(subheader, table);
        return container;
    }

    // Métodos utilitários para criar colunas
    private <T> TableColumn<T, String> createPositionColumn() {
        TableColumn<T, String> posCol = new TableColumn<>("#");
        posCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        return posCol;
    }

    private <T> TableColumn<T, Double> createCurrencyColumn(String title, String propertyName) {
        TableColumn<T, Double> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        col.setCellFactory(column -> new TableCell<>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("pt").setRegion("BR").build());
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });
        return col;
    }
}
