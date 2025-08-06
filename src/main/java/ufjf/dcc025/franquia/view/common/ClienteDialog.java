package ufjf.dcc025.franquia.view.common;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.util.AlertFactory;

public class ClienteDialog extends Dialog<Cliente> {

    public ClienteDialog() {
        this(null);
    }

    public ClienteDialog(Cliente cliente) {
        setTitle(cliente == null ? "Adicionar Novo Cliente" : "Editar Cliente");
        setHeaderText(cliente == null ? "Preencha os dados do novo cliente." : "Altere os dados do cliente.");

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
        TextField telefoneField = new TextField();
        telefoneField.setPromptText("Telefone");
        TextField enderecoField = new TextField();
        enderecoField.setPromptText("Endereço");

        if (cliente != null) {
            nomeField.setText(cliente.getNome());
            cpfField.setText(cliente.getCpf());
            emailField.setText(cliente.getEmail());
            telefoneField.setText(cliente.getTelefone());
            enderecoField.setText(cliente.getEndereco());
        }

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("CPF:"), 0, 1);
        grid.add(cpfField, 1, 1);
        grid.add(new Label("E-mail:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Telefone:"), 0, 3);
        grid.add(telefoneField, 1, 3);
        grid.add(new Label("Endereço:"), 0, 4);
        grid.add(enderecoField, 1, 4);

        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nomeField.getText().isBlank() || cpfField.getText().isBlank()) {
                    AlertFactory.showError("Dados Incompletos", "Nome e CPF são obrigatórios.");
                    return null;
                }
                Cliente resultCliente = new Cliente(nomeField.getText(), cpfField.getText(), emailField.getText(), telefoneField.getText(), enderecoField.getText());
                return resultCliente;
            }
            return null;
        });
    }
}
