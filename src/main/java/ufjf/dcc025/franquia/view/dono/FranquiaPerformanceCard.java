// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.view.dono;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Um card customizado para exibir os indicadores de desempenho de uma única franquia.
 */
public class FranquiaPerformanceCard extends VBox {

    public FranquiaPerformanceCard(Franquia franquia) {

        boolean hasManager = franquia.getGerente() != null;

        getStyleClass().add("indicator-card");
        if (!hasManager) {
            getStyleClass().add("no-manager-warning"); // Estilo de aviso para franquia sem gerente
        }
        setPadding(new Insets(15));
        setSpacing(15);

        // Título do Card com aviso opcional
        Text title = new Text(franquia.getNome());
        title.getStyleClass().add("page-subheader");

        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(10);

        // Adiciona aviso se não houver gerente
        if (!hasManager) {
            Label warningLabel = new Label("Franquia sem gerente");
            warningLabel.getStyleClass().add("warning-label-small");
            HBox warningBox = new HBox(ComponentFactory.createIcon(IconManager.WARNING), warningLabel);
            warningBox.setSpacing(5);
            warningBox.setAlignment(Pos.CENTER_LEFT);
            titleBox.getChildren().add(warningBox);
        }

        // Grid com os indicadores de performance
        GridPane indicatorsGrid = createIndicatorsGrid(franquia);

        getChildren().addAll(titleBox, indicatorsGrid);
    }

    private GridPane createIndicatorsGrid(Franquia franquia) {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // Card de Faturamento
        double faturamento = franquia.getReceita();
        VBox faturamentoCard = ComponentFactory.createIndicatorCard("Faturamento", currencyFormat.format(faturamento), IconManager.MONEY);

        // Card de Pedidos
        int totalPedidos = franquia.quantidadePedidos();
        VBox pedidosCard = ComponentFactory.createIndicatorCard("Pedidos", String.valueOf(totalPedidos), IconManager.CART);

        // Card de Ticket Médio
        double ticketMedio = (totalPedidos > 0) ? faturamento / totalPedidos : 0;
        VBox ticketMedioCard = ComponentFactory.createIndicatorCard("Ticket Médio", currencyFormat.format(ticketMedio), IconManager._TICKET);

        grid.add(faturamentoCard, 0, 0);
        grid.add(pedidosCard, 1, 0);
        grid.add(ticketMedioCard, 2, 0);

        return grid;
    }
}
