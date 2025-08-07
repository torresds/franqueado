// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.view.gerente;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.produtos.Produto;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dialog para visualizar as alterações propostas em um pedido.
 */
public class VisualizarAlteracaoDialog extends Dialog<Boolean> {

    public VisualizarAlteracaoDialog(Pedido pedido) {
        setTitle("Revisar Alteração de Pedido");
        setHeaderText("O vendedor solicitou alterações para o pedido ID: " + pedido.getId());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));

        // Coluna Original
        VBox originalBox = createDetailsColumn("Itens Originais", pedido.getProdutosQuantidade());
        Text entregaOriginal = new Text("Entrega: " + pedido.getMetodoEntrega().toString());
        originalBox.getChildren().add(entregaOriginal);


        // Coluna Alteração Proposta
        VBox alteracaoBox = createDetailsColumn("Itens Propostos", pedido.getProdutosAlteracao());
        Text entregaProposta = new Text("Entrega: " + pedido.getEntregaAlteracao().toString());
        alteracaoBox.getChildren().add(entregaProposta);

        grid.add(originalBox, 0, 0);
        grid.add(alteracaoBox, 1, 0);

        getDialogPane().setContent(grid);

        // Retorna true se o botão OK (Aprovar) for clicado
        setResultConverter(dialogButton -> dialogButton == ButtonType.OK);
    }

    private VBox createDetailsColumn(String title, Map<Produto, Integer> produtos) {
        VBox column = new VBox(10);
        Text header = new Text(title);
        header.setFont(Font.font("System", FontWeight.BOLD, 14));

        ListView<String> listView = new ListView<>();
        if (produtos != null) {
            listView.getItems().setAll(produtos.entrySet().stream()
                    .map(entry -> String.format("%dx %s", entry.getValue(), entry.getKey().getNome()))
                    .collect(Collectors.toList()));
        } else {
            listView.getItems().add("Nenhum item.");
        }
        listView.setPrefHeight(150);

        column.getChildren().addAll(header, listView);
        return column;
    }
}
