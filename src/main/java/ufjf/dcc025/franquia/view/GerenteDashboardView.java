package ufjf.dcc025.franquia.view;

import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.service.GerenteService;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;

public class GerenteDashboardView extends VBox {

    private final GerenteService gerenteService;

    public GerenteDashboardView(GerenteService gerenteService) {
        this.gerenteService = gerenteService;
        Gerente gerente = gerenteService.getGerente();

        setPadding(new Insets(10));
        setSpacing(25);

        // Cabeçalho
        Text header = new Text("Dashboard da Franquia");
        header.getStyleClass().add("page-header");

        String nomeFranquia = gerente.getFranquia() != null ? gerente.getFranquia().getNome() : "Franquia não definida";
        Text subheader = new Text("Visão geral da unidade: " + nomeFranquia);
        subheader.getStyleClass().add("page-subheader");

        // Grid de Indicadores (placeholders por enquanto)
        GridPane indicatorsGrid = createIndicatorsGrid();

        getChildren().addAll(header, subheader, indicatorsGrid);
    }

    private GridPane createIndicatorsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        VBox pedidosPendentesCard = ComponentFactory.createIndicatorCard("Pedidos Pendentes", "0", IconManager.CLIPBOARD);
        VBox estoqueBaixoCard = ComponentFactory.createIndicatorCard("Itens com Estoque Baixo", "0", IconManager.WARNING);
        VBox totalVendedoresCard = ComponentFactory.createIndicatorCard("Total de Vendedores",
                String.valueOf(gerenteService.listarVendedoresDaFranquia().size()), IconManager.USERS);

        grid.add(pedidosPendentesCard, 0, 0);
        grid.add(estoqueBaixoCard, 1, 0);
        grid.add(totalVendedoresCard, 2, 0);

        return grid;
    }
}
