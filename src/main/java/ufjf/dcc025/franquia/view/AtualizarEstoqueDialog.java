package ufjf.dcc025.franquia.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ufjf.dcc025.franquia.util.AlertFactory;

public class AtualizarEstoqueDialog extends Dialog<Integer> {

    public AtualizarEstoqueDialog(String nomeProduto, int quantidadeAtual) {
        setTitle("Atualizar Estoque");
        setHeaderText("Atualize a quantidade em estoque para o produto: " + nomeProduto);

        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Spinner<Integer> quantidadeSpinner = new Spinner<>(0, 10000, quantidadeAtual);
        quantidadeSpinner.setEditable(true);

        grid.add(new Label("Nova Quantidade:"), 0, 0);
        grid.add(quantidadeSpinner, 1, 0);

        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (quantidadeSpinner.getValue() < 0) {
                    AlertFactory.showError("Valor Inválido", "A quantidade não pode ser negativa.");
                    return null;
                }
                return quantidadeSpinner.getValue();
            }
            return null;
        });
    }
}
