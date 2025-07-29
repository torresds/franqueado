package ufjf.dcc025.franquia;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ufjf.dcc025.franquia.util.CssManager;
import ufjf.dcc025.franquia.view.LoginView;

public class FranquiaApp extends Application {

    private Stage primaryStage;

    public FranquiaApp() {
        super();
    }
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Franqueado");
        showLoginScreen();
        primaryStage.show();
    }

    public void showLoginScreen() {
        LoginView loginView = new LoginView(this);
        Scene scene = new Scene(loginView, 1280, 800);
        scene.getStylesheets().add("data:text/css," + CssManager.getStyles().replace("%", "%%"));
        primaryStage.setScene(scene);
    }

    public void showDonoDashboard() {
    }

    public void showGerenteDashboard() {
    }

    public void showVendedorDashboard() {
    }
}
