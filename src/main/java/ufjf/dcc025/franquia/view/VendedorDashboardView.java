package ufjf.dcc025.franquia.view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;

public class VendedorDashboardView extends VBox {

    private Vendedor vendedor;

    public VendedorDashboardView(Vendedor vendedor) {
        this.vendedor = vendedor;
        setPadding(new Insets(10));
        setSpacing(20);

        Text header = new Text("Dashboard do Vendedor");
        header.getStyleClass().add("page-header");

        Label welcomeLabel = new Label("Bem-vindo, " + vendedor.getNome() + "!");
        welcomeLabel.setStyle("-fx-font-size: 18px;");

        getChildren().addAll(header, welcomeLabel);
    }
}