package ufjf.dcc025.franquia.view;


import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.FranquiaApp;

public class LoginView extends VBox {
    public LoginView(FranquiaApp app) {
        super(20);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("login-view");

        Text title = new Text("Gerenciador de franquias");
        title.setFont(Font.font("System", FontWeight.BOLD, 48));
        title.setFill(Color.web("#2c3e50"));

        Text subtitle = new Text("Sistema de gestão integrado");
        subtitle.setFont(Font.font("System", 24));
        subtitle.setFill(Color.web("#34495e"));

        VBox formContainer = new VBox(15);
        formContainer.setMaxWidth(400);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.getStyleClass().add("form-container");

        TextField emailField = new TextField();
        emailField.setPromptText("E-mail ou CPF");
        emailField.getStyleClass().add("login-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Senha");
        passwordField.getStyleClass().add("login-field");

        Button donoLoginBtn = new Button("Entrar como dono");
        donoLoginBtn.getStyleClass().add("login-button");
       // donoLoginBtn.setOnAction(e -> app.showDonoDashboard());

        Button gerenteLoginBtn = new Button("Entrar como gerente");
        gerenteLoginBtn.getStyleClass().add("login-button");
       //  gerenteLoginBtn.setOnAction(e -> app.showGerenteDashboard());

        Button vendedorLoginBtn = new Button("Entrar como vendedor");
        vendedorLoginBtn.getStyleClass().add("login-button");
       // vendedorLoginBtn.setOnAction(e -> app.showVendedorDashboard());

        formContainer.getChildren().addAll(emailField, passwordField, donoLoginBtn, gerenteLoginBtn, vendedorLoginBtn);

        this.getChildren().addAll(title, subtitle, formContainer);
    }
}


