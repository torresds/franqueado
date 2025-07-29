package ufjf.dcc025.franquia.util;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public class ComponentFactory {

    public static Button createMenuButton(String text, String svgPath, Runnable action) {
        Button button = new Button(text);
        button.setGraphic(createIcon(svgPath));
        button.getStyleClass().add("menu-button");
        button.setOnAction(e -> action.run());
        return button;
    }

    public static Node createIcon(String svgPath) {
        SVGPath path = new SVGPath();
        path.setContent(svgPath);
        StackPane iconPane = new StackPane(path);
        iconPane.getStyleClass().add("icon-pane");
        return iconPane;
    }

    public static VBox createIndicatorCard(String title, String value, String svgPath) {
        VBox card = new VBox(10);
        card.getStyleClass().add("indicator-card");
        card.setAlignment(Pos.CENTER_LEFT);
        Text titleText = new Text(title);
        titleText.getStyleClass().add("card-title");
        Text valueText = new Text(value);
        valueText.getStyleClass().add("card-value");
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.getStyleClass().add("card-icon");
        HBox topRow = new HBox(titleText);
        HBox bottomRow = new HBox(10, valueText, new Spacer(), icon);
        bottomRow.setAlignment(Pos.BOTTOM_LEFT);
        card.getChildren().addAll(topRow, bottomRow);
        return card;
    }

    public static <S> void configureTable(TableView<S> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Nenhum dado para exibir."));
    }
}
