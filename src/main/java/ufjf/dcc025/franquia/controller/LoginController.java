// FILE: src/main/java/ufjf/dcc025/franquia/controller/LoginController.java
package ufjf.dcc025.franquia.controller;

import ufjf.dcc025.franquia.FranquiaApp;
import ufjf.dcc025.franquia.exception.UsuarioInvalidoException;
import ufjf.dcc025.franquia.model.usuarios.Usuario;
import ufjf.dcc025.franquia.service.AuthenticationService;
import ufjf.dcc025.franquia.util.DataSeeder;

import java.util.function.Consumer;

/**
 * Controlador para a tela de login.
 */
public class LoginController {

    private final FranquiaApp app;
    private final AuthenticationService authService;

    public LoginController(FranquiaApp app, AuthenticationService authService) {
        this.app = app;
        this.authService = authService;
    }

    /**
     * Tenta realizar o login do usuário ou executar comandos especiais.
     * @param username O nome de usuário (email ou CPF).
     * @param password A senha.
     * @param onError Callback para executar em caso de erro.
     */
    public void doLogin(String username, String password, Consumer<String> onError) {
        // Checa por comandos secretos
        switch (username.toLowerCase()) {
            case "_seed_initial":
                app.runSeeder(DataSeeder.SeedScenario.INITIAL_SETUP);
                return;
            case "_seed_busy":
                app.runSeeder(DataSeeder.SeedScenario.BUSY_MONTH);
                return;
            case "_seed_expansion":
                app.runSeeder(DataSeeder.SeedScenario.NEW_EXPANSION);
                return;
            case "_clear":
                app.clearDatabase();
                return;
        }

        if (username.isBlank() || password.isBlank()) {
            onError.accept("E-mail/CPF e senha são obrigatórios.");
            return;
        }

        try {
            Usuario usuario = authService.authenticate(username, password);
            app.onLoginSuccess(usuario);
        } catch (UsuarioInvalidoException e) {
            onError.accept("Credenciais inválidas. Tente novamente.");
        }
    }
}
