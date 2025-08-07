// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.view.dono;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.util.AlertFactory;

public class GerenteDialog extends Dialog<Gerente> {

    private PasswordField senhaField;
    private PasswordField confirmarSenhaField;

    public GerenteDialog() {
        this(null);
    }

    public GerenteDialog(Gerente gerente) {
        setTitle(gerente == null ? "Adicionar Novo Gerente" : "Editar Gerente");
        setHeaderText(gerente == null ? "Preencha os dados do novo gerente." : "Altere os dados do gerente.");

        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome Completo");
        TextField cpfField = new TextField();
        cpfField.setPromptText("CPF (somente números)");
        TextField emailField = new TextField();
        emailField.setPromptText("E-mail");
        senhaField = new PasswordField();
        senhaField.setPromptText("Senha (mín. 6 caracteres)");
        confirmarSenhaField = new PasswordField();
        confirmarSenhaField.setPromptText("Confirmar Senha");

        if (gerente != null) {
            nomeField.setText(gerente.getNome());
            cpfField.setText(gerente.getCpf());
            emailField.setText(gerente.getEmail());
            // A senha não é preenchida por segurança. Deixe em branco para manter a atual.
            senhaField.setPromptText("Nova senha (deixe em branco para manter)");
            confirmarSenhaField.setPromptText("Confirme a nova senha");
        }

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("CPF:"), 0, 1);
        grid.add(cpfField, 1, 1);
        grid.add(new Label("E-mail:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Senha:"), 0, 3);
        grid.add(senhaField, 1, 3);
        grid.add(new Label("Confirmar Senha:"), 0, 4);
        grid.add(confirmarSenhaField, 1, 4);

        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (!validateInput(gerente, nomeField, cpfField, emailField)) {
                    return null; // Mantém o diálogo aberto
                }

                String senha = senhaField.getText();
                if (senha.isBlank() && gerente != null) {
                    senha = gerente.getSenha(); // Mantém a senha antiga se o campo estiver vazio na edição
                }

                Gerente resultGerente = new Gerente(nomeField.getText(), cpfField.getText(), emailField.getText(), senha);
                return resultGerente;
            }
            return null;
        });
    }

    private boolean validateInput(Gerente gerente, TextField nome, TextField cpf, TextField email) {
        // Validação de campos vazios
        if (nome.getText().isBlank() || cpf.getText().isBlank() || email.getText().isBlank()) {
            AlertFactory.showError("Campos Obrigatórios", "Nome, CPF e E-mail não podem estar vazios.");
            return false;
        }

        // Validação de senha
        String senha = senhaField.getText();
        String confirmarSenha = confirmarSenhaField.getText();

        // Se for um novo gerente, a senha é obrigatória
        if (gerente == null && senha.isBlank()) {
            AlertFactory.showError("Senha Inválida", "A senha é obrigatória para novos gerentes.");
            return false;
        }

        // Se uma nova senha foi digitada, ela deve ser válida e confirmada
        if (!senha.isBlank()) {
            if (senha.length() < 6) {
                AlertFactory.showError("Senha Inválida", "A senha deve ter no mínimo 6 caracteres.");
                return false;
            }
            if (!senha.equals(confirmarSenha)) {
                AlertFactory.showError("Senha Inválida", "As senhas não coincidem.");
                return false;
            }
        }

        return true;
    }
}
