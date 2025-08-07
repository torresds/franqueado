// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.view.dono;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.controller.DonoController;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.service.DonoService;
import ufjf.dcc025.franquia.util.ComponentFactory;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DesempenhoView extends ScrollPane {

    private final DonoController donoController;

    public DesempenhoView(DonoService donoService) {
        this.donoController = new DonoController(donoService);

        VBox content = new VBox(30);
        content.setPadding(new Insets(10));

        Text header = new Text("Desempenho da Rede");
        header.getStyleClass().add("page-header");

        // Seção de Gráfico de Faturamento
        Text chartsHeader = new Text("Faturamento por Unidade");
        chartsHeader.getStyleClass().add("page-subheader");
        Node revenueChart = createFranchiseRevenueChart();

        // Seção de Rankings
        Text rankingsHeader = new Text("Rankings Detalhados");
        rankingsHeader.getStyleClass().add("page-subheader");
        VBox rankingsBox = createRankingsSection();

        content.getChildren().addAll(header, chartsHeader, revenueChart, rankingsHeader, rankingsBox);

        this.setContent(content);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: transparent;");
    }

    private BarChart<String, Number> createFranchiseRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Receita (R$)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Receita por Franquia");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        List<Franquia> franquias = donoController.getFranquiasPorDesempenho();
        for (Franquia f : franquias) {
            series.getData().add(new XYChart.Data<>(f.getNome(), f.getReceita()));
        }

        barChart.getData().add(series);
        return barChart;
    }

    private VBox createRankingsSection() {
        VBox container = new VBox(25);
        List<Franquia> franquias = donoController.getFranquiasPorDesempenho();

        // Ranking de Franquias
        container.getChildren().add(createFranquiasRankingPane(franquias));

        // Ranking de Vendedores por Franquia
        for (Franquia franquia : franquias) {
            container.getChildren().add(createVendedoresRankingPaneForFranquia(franquia));
        }

        return container;
    }

    private VBox createFranquiasRankingPane(List<Franquia> franquias) {
        VBox container = new VBox(10);
        Text subheader = new Text("Ranking de Franquias por Faturamento");
        subheader.getStyleClass().add("page-subheader");

        TableView<Franquia> table = new TableView<>();
        ObservableList<Franquia> franquiasList = FXCollections.observableArrayList(franquias);

        TableColumn<Franquia, String> posCol = createPositionColumn();
        TableColumn<Franquia, String> nameCol = new TableColumn<>("Franquia");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        TableColumn<Franquia, Double> revenueCol = createCurrencyColumn("Receita", "receita");

        table.getColumns().addAll(posCol, nameCol, revenueCol);
        table.setItems(franquiasList);
        ComponentFactory.configureTable(table);
        container.getChildren().addAll(subheader, table);
        return container;
    }

    private VBox createVendedoresRankingPaneForFranquia(Franquia franquia) {
        VBox container = new VBox(10);
        Text subheader = new Text("Ranking de Vendedores - " + franquia.getNome());
        subheader.getStyleClass().add("page-subheader");

        TableView<Vendedor> table = new TableView<>();
        List<Vendedor> vendedores = donoController.getVendedoresPorDesempenho(franquia.getId());
        ObservableList<Vendedor> vendedoresList = FXCollections.observableArrayList(vendedores);

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


    private <T> TableColumn<T, String> createPositionColumn() {
        TableColumn<T, String> posCol = new TableColumn<>("#");
        posCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        posCol.setMaxWidth(50);
        posCol.setMinWidth(50);
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
