package ufjf.dcc025.franquia.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ufjf.dcc025.franquia.FranquiaApp;
import ufjf.dcc025.franquia.model.usuarios.Dono;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.persistence.EntityRepository;

public class LoginView extends VBox {
    private final FranquiaApp app;
    private final EntityRepository<Dono> donoRepo;
    private final EntityRepository<Gerente> gerenteRepo;
    private final EntityRepository<Vendedor> vendedorRepo;

    public LoginView(FranquiaApp app, EntityRepository<Dono> donoRepo, 
                    EntityRepository<Gerente> gerenteRepo, EntityRepository<Vendedor> vendedorRepo) {
        this.app = app;
        this.donoRepo = donoRepo;
        this.gerenteRepo = gerenteRepo;
        this.vendedorRepo = vendedorRepo;
        initUI();
    }

    private void initUI() {
        setAlignment(Pos.CENTER);
        setSpacing(10);
        setPadding(new Insets(20));

        Label titleLabel = new Label("Sistema de Franquias - Login");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField cpfField = new TextField();
        cpfField.setPromptText("CPF");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Senha");
        Button loginButton = new Button("Entrar");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        loginButton.setOnAction(e -> {
            String cpf = cpfField.getText();
            String senha = passwordField.getText();
            try {
                // Tenta autenticar como Dono
                Dono dono = donoRepo.findById("D" + cpf).orElse(null);
                if (dono != null && dono.getSenha().equals(senha)) {
                    app.showDonoDashboard(dono);
                    return;
                }
                // Tenta autenticar como Gerente
                Gerente gerente = gerenteRepo.findById("G" + cpf).orElse(null);
                if (gerente != null && gerente.getSenha().equals(senha)) {
                    app.showGerenteDashboard(gerente);
                    return;
                }
                // Tenta autenticar como Vendedor
                Vendedor vendedor = vendedorRepo.findById("V" + cpf).orElse(null);
                if (vendedor != null && vendedor.getSenha().equals(senha)) {
                    app.showVendedorDashboard(vendedor);
                    return;
                }
                errorLabel.setText("CPF ou senha inválidos.");
            } catch (Exception ex) {
                errorLabel.setText("Erro: " + ex.getMessage());
            }
        });

        getChildren().addAll(titleLabel, cpfField, passwordField, loginButton, errorLabel);
    }
}