// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.view.dono;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.controller.DonoController;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.service.DonoService;

import java.util.List;

public class DonoDashboardView extends VBox {

    private final DonoController controller;

    public DonoDashboardView(DonoService donoService) {
        this.controller = new DonoController(donoService);

        setPadding(new Insets(10));
        setSpacing(25);

        // Cabeçalho
        Text header = new Text("Dashboard Geral");
        header.getStyleClass().add("page-header");

        Text subheader = new Text("Visão de desempenho por unidade da rede.");
        subheader.getStyleClass().add("page-subheader");

        // Container para os cards de desempenho das franquias
        VBox performanceCardsContainer = new VBox(20);
        List<Franquia> franquias = controller.getFranquias();

        if (franquias.isEmpty()) {
            performanceCardsContainer.getChildren().add(new Label("Nenhuma franquia cadastrada ainda."));
        } else {
            for (Franquia franquia : franquias) {
                // Usando o novo componente FranquiaPerformanceCard para cada franquia
                performanceCardsContainer.getChildren().add(new FranquiaPerformanceCard(franquia));
            }
        }

        // Adiciona o container a um ScrollPane para lidar com muitas franquias
        ScrollPane scrollPane = new ScrollPane(performanceCardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setPadding(new Insets(10, 0, 10, 0));


        getChildren().addAll(header, subheader, scrollPane);
    }
}
