package ufjf.dcc025.franquia.view.gerente;

import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.service.GerenteService;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;
import ufjf.dcc025.franquia.view.common.PlaceholderView;

public class GerenteDashboardView extends VBox {

    private final GerenteService gerenteService;

    public GerenteDashboardView(GerenteService gerenteService) {
        this.gerenteService = gerenteService;
        Gerente gerente = gerenteService.getGerente();

        setPadding(new Insets(10));
        setSpacing(25);

        Text header = new Text("Dashboard");
        header.getStyleClass().add("page-header");

        String nomeFranquia = gerente.getFranquia() != null ? gerente.getFranquia().getNome() : "Nenhuma franquia atribuída";
        Text subheader = new Text("Visão geral da unidade: " + nomeFranquia);
        subheader.getStyleClass().add("page-subheader");

        getChildren().addAll(header, subheader);

        // Só mostra os indicadores se o gerente estiver em uma franquia
        if (gerente.getFranquia() != null) {
            GridPane indicatorsGrid = createIndicatorsGrid();
            getChildren().add(indicatorsGrid);
        } else {
            getChildren().add(new PlaceholderView("Você não está atribuído a nenhuma franquia.", "Contate o Dono do sistema para ser alocado a uma unidade."));
        }
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
