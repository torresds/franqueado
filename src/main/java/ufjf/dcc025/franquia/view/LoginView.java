
// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.FranquiaApp;
import ufjf.dcc025.franquia.controller.LoginController;
import ufjf.dcc025.franquia.service.AuthenticationService;

public class LoginView extends VBox {

    private final LoginController controller;

    public LoginView(FranquiaApp app, AuthenticationService authService) {
        super(20);
        this.controller = new LoginController(app, authService);

        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("login-view");

        Text title = new Text("Gerenciador de Franquias");
        title.getStyleClass().add("login-title");

        Text subtitle = new Text("Sistema de gestão integrado");
        subtitle.getStyleClass().add("login-subtitle");

        VBox formContainer = new VBox(15);
        formContainer.setMaxWidth(400);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.getStyleClass().add("login-form-container");

        TextField emailField = new TextField();
        emailField.setPromptText("E-mail ou CPF");
        emailField.getStyleClass().add("login-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Senha");
        passwordField.getStyleClass().add("login-field");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");

        Button loginBtn = new Button("Entrar");
        loginBtn.getStyleClass().add("login-button");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        // Ação do botão de login
        loginBtn.setOnAction(e -> {
            errorLabel.setText(""); // Limpa erros anteriores
            controller.doLogin(
                    emailField.getText(),
                    passwordField.getText(),
                    // Callback de erro
                    errorMessage -> errorLabel.setText(errorMessage)
            );
        });

        // Permite login com a tecla Enter
        passwordField.setOnAction(e -> loginBtn.fire());
        emailField.setOnAction(e -> passwordField.requestFocus());

        formContainer.getChildren().addAll(emailField, passwordField, errorLabel, loginBtn);

        this.getChildren().addAll(title, subtitle, formContainer);
    }
}