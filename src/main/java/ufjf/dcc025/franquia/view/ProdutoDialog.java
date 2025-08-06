package ufjf.dcc025.franquia.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.util.AlertFactory;

/**
 * Dialog para criar ou editar um Produto.
 */
public class ProdutoDialog extends Dialog<ProdutoDialog.ProdutoResult> {

    // Record para encapsular o resultado do diálogo
    public record ProdutoResult(String codigo, String nome, String descricao, double preco, int quantidadeInicial) {}

    public ProdutoDialog() {
        this(null);
    }

    public ProdutoDialog(Produto produto) {
        setTitle(produto == null ? "Adicionar Novo Produto" : "Editar Produto");
        setHeaderText(produto == null ? "Preencha os dados do novo produto." : "Altere os dados do produto.");

        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codigoField = new TextField();
        codigoField.setPromptText("Código (ex: P001)");
        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome do Produto");
        TextField descricaoField = new TextField();
        descricaoField.setPromptText("Descrição");
        Spinner<Double> precoSpinner = new Spinner<>(0.0, 10000.0, 0.0, 0.5);
        precoSpinner.setEditable(true);
        Spinner<Integer> quantidadeSpinner = new Spinner<>(0, 1000, 0);
        quantidadeSpinner.setEditable(true);

        if (produto != null) {
            codigoField.setText(produto.getCodigo());
            codigoField.setDisable(true); // Código não pode ser editado
            nomeField.setText(produto.getNome());
            descricaoField.setText(produto.getDescricao());
            precoSpinner.getValueFactory().setValue(produto.getPreco());
            quantidadeSpinner.setVisible(false); // Não edita quantidade inicial aqui
            grid.getChildren().remove(quantidadeSpinner);
        }

        grid.add(new Label("Código:"), 0, 0);
        grid.add(codigoField, 1, 0);
        grid.add(new Label("Nome:"), 0, 1);
        grid.add(nomeField, 1, 1);
        grid.add(new Label("Descrição:"), 0, 2);
        grid.add(descricaoField, 1, 2);
        grid.add(new Label("Preço:"), 0, 3);
        grid.add(precoSpinner, 1, 3);
        if (produto == null) {
            grid.add(new Label("Qtd. Inicial:"), 0, 4);
            grid.add(quantidadeSpinner, 1, 4);
        }

        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (codigoField.getText().isBlank() || nomeField.getText().isBlank()) {
                    AlertFactory.showError("Dados Incompletos", "Código e Nome são obrigatórios.");
                    return null;
                }
                if (precoSpinner.getValue() <= 0) {
                    AlertFactory.showError("Dados Inválidos", "O preço deve ser maior que zero.");
                    return null;
                }
                return new ProdutoResult(codigoField.getText(), nomeField.getText(), descricaoField.getText(), precoSpinner.getValue(), quantidadeSpinner.getValue());
            }
            return null;
        });
    }
}
