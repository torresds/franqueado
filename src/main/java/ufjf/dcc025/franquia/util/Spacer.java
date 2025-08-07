// Discentes: Ana (202465512B), Miguel (202465506B)

// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.util;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class Spacer extends Region {
    public Spacer() {
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
    }
}