// FILE: src/main/java/ufjf/dcc025/franquia/view/DesempenhoView.java
package ufjf.dcc025.franquia.view.DonoDashboard;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.service.DonoService;

/**
 * Placeholder para a tela de visualização de desempenho.
 */
public class DesempenhoView extends VBox {
    private final DonoService donoService;
    public DesempenhoView(DonoService donoService) {
        this.donoService = donoService;
        setPadding(new Insets(10));
        Text title = new Text("Relatórios de Desempenho (Em breve)");
        title.getStyleClass().add("page-header");
        getChildren().add(title);
    }
}
