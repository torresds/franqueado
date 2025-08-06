package ufjf.dcc025.franquia.view.common;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;

/**
 * Uma view genérica para exibir uma mensagem quando uma funcionalidade não está disponível.
 */
public class PlaceholderView extends VBox {

    public PlaceholderView(String title, String message) {
        super(15);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(50));

        getStyleClass().add("indicator-card"); // Reutiliza o estilo de card

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-subheader");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-alignment: center;");

        getChildren().addAll(
                ComponentFactory.createIcon(IconManager.WARNING),
                titleLabel,
                messageLabel
        );
    }
}
