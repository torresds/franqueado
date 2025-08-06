package ufjf.dcc025.franquia.view.DonoDashboard;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.controller.DonoController;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.service.DonoService;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DonoDashboardView extends VBox {

    private final DonoController controller;
    private final DonoService donoService;

    public DonoDashboardView(DonoService donoService) {
        this.donoService = donoService;
        this.controller = new DonoController(donoService);

        setPadding(new Insets(10));
        setSpacing(25);

        // Cabeçalho
        Text header = new Text("Dashboard Geral");
        header.getStyleClass().add("page-header");

        Text subheader = new Text("Visão consolidada de toda a rede de franquias.");
        subheader.getStyleClass().add("page-subheader");

        // Grid de Indicadores
        GridPane indicatorsGrid = createIndicatorsGrid();

        // Alertas
        VBox alertsBox = createAlertsBox();

        getChildren().addAll(header, subheader, indicatorsGrid, alertsBox);
    }

    private GridPane createIndicatorsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        // Formatação de moeda
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // Faturamento Bruto
        double faturamento = donoService.calcularFaturamentoBruto();
        VBox faturamentoCard = ComponentFactory.createIndicatorCard("Faturamento Bruto", currencyFormat.format(faturamento), IconManager.MONEY);

        // Total de Pedidos
        int totalPedidos = donoService.calcularTotalPedidos();
        VBox pedidosCard = ComponentFactory.createIndicatorCard("Total de Pedidos", String.valueOf(totalPedidos), IconManager.CART);

        // Ticket Médio
        double ticketMedio = donoService.calcularTicketMedio();
        VBox ticketMedioCard = ComponentFactory.createIndicatorCard("Ticket Médio", currencyFormat.format(ticketMedio), IconManager._TICKET);

        grid.add(faturamentoCard, 0, 0);
        grid.add(pedidosCard, 1, 0);
        grid.add(ticketMedioCard, 2, 0);

        return grid;
    }

    private VBox createAlertsBox() {
        VBox alertsContainer = new VBox(10);

        List<Franquia> franquiasSemGerente = donoService.checarFranquias();

        if (!franquiasSemGerente.isEmpty()) {
            HBox alert = new HBox(10);
            alert.getStyleClass().add("alert-box");

            String mensagem;
            if (franquiasSemGerente.size() == 1) {
                mensagem = "Atenção: Existe 1 franquia sem gerente atribuído.";
            } else {
                mensagem = "Atenção: Existem " + franquiasSemGerente.size() + " franquias sem gerente atribuído.";
            }

            alert.getChildren().addAll(ComponentFactory.createIcon(IconManager.WARNING), new Label(mensagem));
            alertsContainer.getChildren().add(alert);
        }

        return alertsContainer;
    }
}