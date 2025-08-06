// FILE: src/main/java/ufjf/dcc025/franquia/controller/LoginController.java
package ufjf.dcc025.franquia.controller;

import ufjf.dcc025.franquia.FranquiaApp;
import ufjf.dcc025.franquia.exception.UsuarioInvalidoException;
import ufjf.dcc025.franquia.model.usuarios.Usuario;
import ufjf.dcc025.franquia.service.AuthenticationService;
import ufjf.dcc025.franquia.util.DataSeeder;

import java.util.function.Consumer;

public class LoginController {

    private final FranquiaApp app;
    private final AuthenticationService authService;

    public LoginController(FranquiaApp app, AuthenticationService authService) {
        this.app = app;
        this.authService = authService;
    }

    public void doLogin(String username, String password, Consumer<String> onError) {
        switch (username.toLowerCase()) {
            case "_seed_realistic":
                app.runSeeder(DataSeeder.SeedScenario.REALISTIC);
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
