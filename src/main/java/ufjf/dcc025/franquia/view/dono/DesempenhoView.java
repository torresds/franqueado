package ufjf.dcc025.franquia.view.dono;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.controller.DonoController;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.service.DonoService;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DesempenhoView extends ScrollPane {

    private final DonoController donoController;

    public DesempenhoView(DonoService donoService) {
        this.donoController = new DonoController(donoService);

        VBox content = new VBox(30);
        content.setPadding(new Insets(10));

        Text header = new Text("Desempenho Geral da Rede");
        header.getStyleClass().add("page-header");

        // Seção de Gráficos
        Text chartsHeader = new Text("Gráficos");
        chartsHeader.getStyleClass().add("page-subheader");
        GridPane chartsGrid = createChartsSection();

        // Seção de Insights
        Text insightsHeader = new Text("Informações gerais");
        insightsHeader.getStyleClass().add("page-subheader");
        VBox insightsBox = createInsightsSection();

        // Seção de Rankings
        Text rankingsHeader = new Text("Rankings Detalhados");
        rankingsHeader.getStyleClass().add("page-subheader");
        GridPane rankingsGrid = createRankingsSection();

        content.getChildren().addAll(header, chartsHeader, chartsGrid, insightsHeader, insightsBox, rankingsHeader, rankingsGrid);

        this.setContent(content);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: transparent;");
    }

    private GridPane createChartsSection() {
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(20);

        BarChart<String, Number> franchiseChart = createFranchiseRevenueChart();
        PieChart topSellersChart = createTopSellersChart();

        grid.add(franchiseChart, 0, 0);
        grid.add(topSellersChart, 1, 0);

        return grid;
    }

    private BarChart<String, Number> createFranchiseRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Receita (R$)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Receita por franquia");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        List<Franquia> franquias = donoController.getFranquiasPorDesempenho();
        for (Franquia f : franquias) {
            series.getData().add(new XYChart.Data<>(f.getNome(), f.getReceita()));
        }

        barChart.getData().add(series);
        return barChart;
    }

    private PieChart createTopSellersChart() {
        List<Vendedor> topVendedores = donoController.getTopVendedores(5);
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        double outrosVendedoresTotal = donoController.getVendedoresPorDesempenho().stream()
                .filter(v -> !topVendedores.contains(v))
                .mapToDouble(Vendedor::getTotalVendas)
                .sum();

        for (Vendedor v : topVendedores) {
            pieChartData.add(new PieChart.Data(v.getNome(), v.getTotalVendas()));
        }
        if (outrosVendedoresTotal > 0) {
            pieChartData.add(new PieChart.Data("Outros", outrosVendedoresTotal));
        }

        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Participação dos 5 Melhores Vendedores");
        return chart;
    }

    private GridPane createRankingsSection() {
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.add(createFranquiasRankingPane(), 0, 0);
        grid.add(createVendedoresRankingPane(), 1, 0);
        return grid;
    }

    private VBox createFranquiasRankingPane() {
        VBox container = new VBox(10);
        TableView<Franquia> table = new TableView<>();
        ObservableList<Franquia> franquiasList = FXCollections.observableArrayList(donoController.getFranquiasPorDesempenho());

        TableColumn<Franquia, String> posCol = createPositionColumn();
        TableColumn<Franquia, String> nameCol = new TableColumn<>("Franquia");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        TableColumn<Franquia, Double> revenueCol = createCurrencyColumn("Receita", "receita");

        table.getColumns().addAll(posCol, nameCol, revenueCol);
        table.setItems(franquiasList);
        ComponentFactory.configureTable(table);
        container.getChildren().add(table);
        return container;
    }

    private VBox createVendedoresRankingPane() {
        VBox container = new VBox(10);
        TableView<Vendedor> table = new TableView<>();
        ObservableList<Vendedor> vendedoresList = FXCollections.observableArrayList(donoController.getVendedoresPorDesempenho());

        TableColumn<Vendedor, String> posCol = createPositionColumn();
        TableColumn<Vendedor, String> nameCol = new TableColumn<>("Vendedor");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        TableColumn<Vendedor, String> franchiseCol = new TableColumn<>("Franquia");
        franchiseCol.setCellValueFactory(cellData -> {
            Franquia f = cellData.getValue().getFranquia();
            return new javafx.beans.property.SimpleStringProperty(f != null ? f.getNome() : "N/A");
        });
        TableColumn<Vendedor, Double> salesCol = createCurrencyColumn("Total de Vendas", "totalVendas");

        table.getColumns().addAll(posCol, nameCol, franchiseCol, salesCol);
        table.setItems(vendedoresList);
        ComponentFactory.configureTable(table);
        container.getChildren().add(table);
        return container;
    }

    private VBox createInsightsSection() {
        VBox container = new VBox(10);
        List<Franquia> franquias = donoController.getFranquiasPorDesempenho();
        List<Vendedor> vendedores = donoController.getVendedoresPorDesempenho();
        List<Franquia> semGerente = donoController.donoService.checarFranquias();

        if (!franquias.isEmpty()) {
            container.getChildren().add(createInsightItem(IconManager.TROPHY, "Destaque da Rede",
                    String.format("%s é a franquia com maior receita, totalizando %s.",
                            franquias.get(0).getNome(), formatCurrency(franquias.get(0).getReceita()))));
        }

        if (!vendedores.isEmpty()) {
            Vendedor topVendedor = vendedores.get(0);
            container.getChildren().add(createInsightItem(IconManager.STAR, "Melhor Vendedor",
                    String.format("%s é o vendedor com maior volume de vendas (%s), atuando na franquia %s.",
                            topVendedor.getNome(), formatCurrency(topVendedor.getTotalVendas()), topVendedor.getFranquia().getNome())));
        }

        if (!semGerente.isEmpty()) {
            String nomes = semGerente.stream().map(Franquia::getNome).collect(Collectors.joining(", "));
            container.getChildren().add(createInsightItem(IconManager.WARNING, "Ponto de Atenção Crítico",
                    String.format("%d franquia(s) estão sem gerente atribuído: %s. Isso impacta diretamente a operação e o registro de novos vendedores.",
                            semGerente.size(), nomes)));
        }

        return container;
    }

    private HBox createInsightItem(String svg, String title, String description) {
        HBox itemBox = new HBox(15);
        itemBox.getStyleClass().add("indicator-card");
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(15));

        Node icon = ComponentFactory.createIcon(svg);

        VBox textBox = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        textBox.getChildren().addAll(titleLabel, descLabel);

        itemBox.getChildren().addAll(icon, textBox);
        return itemBox;
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

    private String formatCurrency(double value) {
        return NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("pt").setRegion("BR").build()).format(value);
    }
}
