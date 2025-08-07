// Discentes: Ana (202465512B), Miguel (202465506B)

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

public class FranquiaDialog extends Dialog<FranquiaDialog.Result> {

    public record Result(String nome, String endereco, String gerenteId, Franquia original) {}
    private final Franquia originalFranquia;

    public FranquiaDialog(List<Gerente> todosGerentes, Franquia franquia) {
        this.originalFranquia = franquia;
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

        // Lógica para popular a lista de gerentes disponíveis
        List<Gerente> gerentesDisponiveis = todosGerentes.stream()
                .filter(g -> g.getFranquia() == null || (franquia != null && franquia.getGerente() != null && g.getId().equals(franquia.getGerente().getId())))
                .collect(Collectors.toList());

        gerenteComboBox.getItems().add(null); // Opção para "Nenhum"
        gerenteComboBox.getItems().addAll(gerentesDisponiveis);
        gerenteComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Gerente gerente) {
                return gerente == null ? "Nenhum" : gerente.getNome();
            }
            @Override
            public Gerente fromString(String string) {
                return null;
            }
        });

        if (franquia != null) {
            nomeField.setText(franquia.getNome());
            enderecoField.setText(franquia.getEndereco());
            gerenteComboBox.setValue(franquia.getGerente());
            gerenteComboBox.setDisable(false); // Permite a edição do gerente
        } else {
            gerenteComboBox.getSelectionModel().select(null);
        }

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Endereço:"), 0, 1);
        grid.add(enderecoField, 1, 1);
        grid.add(new Label("Gerente:"), 0, 2);
        grid.add(gerenteComboBox, 1, 2);

        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nomeField.getText().isBlank() || enderecoField.getText().isBlank()) {
                    AlertFactory.showError("Dados Incompletos", "Nome e Endereço são obrigatórios.");
                    return null;
                }

                Gerente selectedManager = gerenteComboBox.getValue();
                String gerenteId = (selectedManager != null) ? selectedManager.getId() : null;

                return new Result(nomeField.getText(), enderecoField.getText(), gerenteId, originalFranquia);
            }
            return null;
        });
    }
}
