// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.view.gerente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.controller.GerenteController;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.util.ComponentFactory;

import java.util.List;
import java.util.Map;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * Um card para exibir a lista de produtos com estoque baixo no dashboard do gerente.
 */
public class EstoqueBaixoCard extends VBox {

    private final GerenteController gerenteController;
    private final TableView<Map.Entry<Produto, Integer>> table = new TableView<>();
    private final ObservableList<Map.Entry<Produto, Integer>> estoqueBaixoList;

    public EstoqueBaixoCard(GerenteController gerenteController) {
        this.gerenteController = gerenteController;
        this.estoqueBaixoList = FXCollections.observableArrayList();

        getStyleClass().add("indicator-card");
        setPadding(new Insets(15));
        setSpacing(10);

        Text title = new Text("Produtos com Estoque Baixo (Menos de 20 unidades)");
        title.getStyleClass().add("card-title");

        setupTable();
        loadData();

        getChildren().addAll(title, table);
    }

    private void setupTable() {
        TableColumn<Map.Entry<Produto, Integer>, String> produtoCol = new TableColumn<>("Produto");
        produtoCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getKey().getNome()));

        TableColumn<Map.Entry<Produto, Integer>, Integer> qtdCol = new TableColumn<>("Quantidade Restante");
        qtdCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getValue()));

        table.getColumns().addAll(produtoCol, qtdCol);
        table.setItems(estoqueBaixoList);
        ComponentFactory.configureTable(table);
        table.setPlaceholder(new Label("Nenhum produto com estoque baixo."));
    }

    private void loadData() {
        List<Map.Entry<Produto, Integer>> produtos = gerenteController.getProdutosComEstoqueBaixo(20);
        estoqueBaixoList.setAll(produtos);
    }

    /**
     * Verifica se a lista de produtos com estoque baixo está vazia.
     * @return true se não houver produtos com estoque baixo, false caso contrário.
     */
    public boolean isEmpty() {
        return estoqueBaixoList.isEmpty();
    }
}
