// FILE: src/main/java/ufjf/dcc025/franquia/view/CreateOwnerView.java
package ufjf.dcc025.franquia.view;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.FranquiaApp;
import ufjf.dcc025.franquia.exception.DadosInvalidosException;
import ufjf.dcc025.franquia.model.usuarios.Dono;
import ufjf.dcc025.franquia.util.AlertFactory;

public class CreateOwnerView extends VBox {

    public CreateOwnerView(FranquiaApp app) {
        super(20);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("login-view"); // Reutiliza o estilo de fundo

        Text title = new Text("Bem-vindo!");
        title.getStyleClass().add("login-title");

        Text subtitle = new Text("Parece que esta é a primeira vez que você executa o sistema.\nPor favor, crie o primeiro administrador (Dono).");
        subtitle.getStyleClass().add("login-subtitle");
        subtitle.setStyle("-fx-text-alignment: center;");

        VBox formContainer = new VBox(15);
        formContainer.setMaxWidth(400);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.getStyleClass().add("login-form-container");

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome Completo");
        nomeField.getStyleClass().add("login-field");

        TextField cpfField = new TextField();
        cpfField.setPromptText("CPF (somente números)");
        cpfField.getStyleClass().add("login-field");

        TextField emailField = new TextField();
        emailField.setPromptText("E-mail");
        emailField.getStyleClass().add("login-field");

        PasswordField senhaField = new PasswordField();
        senhaField.setPromptText("Senha (mín. 6 caracteres)");
        senhaField.getStyleClass().add("login-field");

        PasswordField confirmarSenhaField = new PasswordField();
        confirmarSenhaField.setPromptText("Confirmar Senha");
        confirmarSenhaField.getStyleClass().add("login-field");

        Button createButton = new Button("Criar Administrador");
        createButton.getStyleClass().add("login-button");
        createButton.setMaxWidth(Double.MAX_VALUE);

        createButton.setOnAction(e -> {
            String senha = senhaField.getText();
            String confirmarSenha = confirmarSenhaField.getText();

            if (!senha.equals(confirmarSenha)) {
                AlertFactory.showError("Erro de Validação", "As senhas não coincidem.");
                return;
            }

            try {
                Dono novoDono = new Dono(
                        nomeField.getText(),
                        cpfField.getText(),
                        emailField.getText(),
                        senhaField.getText()
                );
                app.onOwnerCreationSuccess(novoDono);
            } catch (DadosInvalidosException ex) {
                AlertFactory.showError("Erro de Validação", ex.getMessage());
            }
        });

        formContainer.getChildren().addAll(
                nomeField, cpfField, emailField, senhaField, confirmarSenhaField, createButton
        );

        this.getChildren().addAll(title, subtitle, formContainer);
    }
}
