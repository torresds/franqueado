package ufjf.dcc025.franquia.view.dono;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.util.AlertFactory;

import java.util.List;
import java.util.stream.Collectors;

public class FranquiaDialog extends Dialog<Franquia> {

    public FranquiaDialog(List<Gerente> todosGerentes) {
        this(todosGerentes, null);
    }

    public FranquiaDialog(List<Gerente> todosGerentes, Franquia franquia) {
        setTitle(franquia == null ? "Adicionar Nova Franquia" : "Editar Franquia");
        setHeaderText(franquia == null ? "Preencha os dados da nova franquia." : "Altere os dados da franquia.");

        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome da Franquia");
        TextField enderecoField = new TextField();
        enderecoField.setPromptText("Endereço Completo");
        ComboBox<Gerente> gerenteComboBox = new ComboBox<>();

        // Configura o ComboBox de Gerentes
        List<Gerente> gerentesDisponiveis = todosGerentes.stream()
                .filter(g -> g.getFranquia() == null || (franquia != null && franquia.getGerente() != null && g.getId().equals(franquia.getGerente().getId())))
                .collect(Collectors.toList());
        gerenteComboBox.getItems().add(null); // Opção para não selecionar gerente
        gerenteComboBox.getItems().addAll(gerentesDisponiveis);
        gerenteComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Gerente gerente) {
                return gerente == null ? "Nenhum" : gerente.getNome();
            }
            @Override
            public Gerente fromString(String string) {
                return null; // Não necessário
            }
        });

        // Preenche os campos se estiver editando
        if (franquia != null) {
            nomeField.setText(franquia.getNome());
            enderecoField.setText(franquia.getEndereco());
            gerenteComboBox.setValue(franquia.getGerente());
            // Na edição, não permitimos trocar o gerente por esta dialog.
            gerenteComboBox.setDisable(true);
        } else {
            gerenteComboBox.setDisable(false);
            gerenteComboBox.getSelectionModel().select(null); // Seleciona "Nenhum" por padrão
        }

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Endereço:"), 0, 1);
        grid.add(enderecoField, 1, 1);
        grid.add(new Label("Gerente:"), 0, 2);
        grid.add(gerenteComboBox, 1, 2);

        getDialogPane().setContent(grid);

        // Converte o resultado do diálogo em um objeto Franquia quando o botão Salvar é clicado.
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nomeField.getText().isBlank() || enderecoField.getText().isBlank()) {
                    AlertFactory.showError("Dados Incompletos", "Nome e Endereço são obrigatórios.");
                    return null; // Retorna nulo para não fechar o diálogo
                }

                Gerente selectedManager = gerenteComboBox.getValue();
                Franquia resultFranquia = new Franquia(nomeField.getText(), enderecoField.getText(), selectedManager);

                if (franquia != null) {
                    resultFranquia.setId(franquia.getId()); // Mantém o ID original na edição
                }

                return resultFranquia;
            }
            return null;
        });
    }
}
