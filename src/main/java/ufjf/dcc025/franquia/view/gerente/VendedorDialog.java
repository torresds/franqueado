package ufjf.dcc025.franquia.view.gerente;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.util.AlertFactory;

public class VendedorDialog extends Dialog<Vendedor> {

    private final PasswordField senhaField;
    private final PasswordField confirmarSenhaField;

    public VendedorDialog() {
        this(null);
    }

    public VendedorDialog(Vendedor vendedor) {
        setTitle(vendedor == null ? "Adicionar Novo Vendedor" : "Editar Vendedor");
        setHeaderText(vendedor == null ? "Preencha os dados do novo vendedor." : "Altere os dados do vendedor.");

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

        if (vendedor != null) {
            nomeField.setText(vendedor.getNome());
            cpfField.setText(vendedor.getCpf());
            emailField.setText(vendedor.getEmail());
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
                if (!validateInput(vendedor, nomeField, cpfField, emailField)) {
                    return null;
                }

                String senha = senhaField.getText();
                if (senha.isBlank() && vendedor != null) {
                    senha = vendedor.getSenha();
                }

                Vendedor resultVendedor = new Vendedor(nomeField.getText(), cpfField.getText(), emailField.getText(), senha, null);
                return resultVendedor;
            }
            return null;
        });
    }

    private boolean validateInput(Vendedor vendedor, TextField nome, TextField cpf, TextField email) {
        if (nome.getText().isBlank() || cpf.getText().isBlank() || email.getText().isBlank()) {
            AlertFactory.showError("Campos Obrigatórios", "Nome, CPF e E-mail não podem estar vazios.");
            return false;
        }

        String senha = senhaField.getText();
        String confirmarSenha = confirmarSenhaField.getText();

        if (vendedor == null && senha.isBlank()) {
            AlertFactory.showError("Senha Inválida", "A senha é obrigatória para novos vendedores.");
            return false;
        }

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
