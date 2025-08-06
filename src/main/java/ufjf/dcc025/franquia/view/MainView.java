package ufjf.dcc025.franquia.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ufjf.dcc025.franquia.FranquiaApp;
import ufjf.dcc025.franquia.model.usuarios.Usuario;
import ufjf.dcc025.franquia.util.ComponentFactory;
import ufjf.dcc025.franquia.util.IconManager;
import ufjf.dcc025.franquia.util.Spacer;
import ufjf.dcc025.franquia.view.DonoDashboard.*;
import ufjf.dcc025.franquia.view.*;

public class MainView extends BorderPane {

    private final FranquiaApp app;
    private final StackPane contentArea;
    private final VBox sidebar;
    private final ToggleGroup menuGroup = new ToggleGroup();

    public MainView(FranquiaApp app) {
        this.app = app;
        this.getStyleClass().add("main-view");

        this.contentArea = new StackPane();
        this.contentArea.getStyleClass().add("content-area");

        this.sidebar = createSidebar();

        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color:transparent;");

        setLeft(sidebar);
        setCenter(scrollPane);
    }

    private VBox createSidebar() {
        VBox sidebarPane = new VBox();
        sidebarPane.getStyleClass().add("sidebar");

        Text appTitle = new Text("Franqueado");
        appTitle.getStyleClass().add("sidebar-title");

        VBox.setMargin(appTitle, new Insets(0, 0, 10, 0));
        sidebarPane.getChildren().add(appTitle);

        Usuario usuario = app.getUsuarioLogado();
        if (usuario != null) {
            switch (usuario.getTipoUsuario()) {
                case DONO:
                    createDonoMenu(sidebarPane);
                    break;
                case GERENTE:
                    createGerenteMenu(sidebarPane);
                    break;
                case VENDEDOR:
                    // createVendedorMenu(sidebarPane);
                    break;
            }
        }

        sidebarPane.getChildren().add(new Spacer());

        Button btnLogout = ComponentFactory.createMenuButton("Sair", IconManager.LOGOUT, () -> app.showLoginScreen());
        sidebarPane.getChildren().add(btnLogout);

        return sidebarPane;
    }

    private void createDonoMenu(VBox container) {
        ToggleButton btnHome = createMenuToggle("Dashboard", IconManager.HOME, () -> setContent(new DonoDashboardView(app.getDonoService())));
        ToggleButton btnFranquias = createMenuToggle("Franquias", IconManager.STORE, () -> setContent(new GerenciarFranquiasView(app.getDonoService())));
        ToggleButton btnGerentes = createMenuToggle("Gerentes", IconManager.USERS, () -> setContent(new GerenciarGerentesView(app.getDonoService())));
        ToggleButton btnDesempenho = createMenuToggle("Desempenho", IconManager.CHART, () -> setContent(new DesempenhoView(app.getDonoService())));

        container.getChildren().addAll(btnHome, btnFranquias, btnGerentes, btnDesempenho);
        btnHome.setSelected(true);
    }

    private void createGerenteMenu(VBox container) {
        ToggleButton btnHome = createMenuToggle("Dashboard", IconManager.HOME, () -> setContent(new GerenteDashboardView(app.getGerenteService())));
        ToggleButton btnVendedores = createMenuToggle("Vendedores", IconManager.USERS, () -> setContent(new GerenciarVendedoresView(app.getGerenteService())));
        ToggleButton btnEstoque = createMenuToggle("Estoque", IconManager.BOX_PACKAGE, () -> setContent(new GerenciarEstoqueView(app.getGerenteService())));
        ToggleButton btnPedidos = createMenuToggle("Pedidos", IconManager.CLIPBOARD, () -> setContent(new GerenciarPedidosView(app.getGerenteService())));
        ToggleButton btnRelatorios = createMenuToggle("Relatórios", IconManager.CHART, () -> {}); // Placeholder

        container.getChildren().addAll(btnHome, btnVendedores, btnEstoque, btnPedidos, btnRelatorios);
        btnHome.setSelected(true);
    }


    private ToggleButton createMenuToggle(String text, String svgPath, Runnable action) {
        ToggleButton button = new ToggleButton(text);
        button.setGraphic(ComponentFactory.createIcon(svgPath));
        button.getStyleClass().add("menu-button");
        button.setToggleGroup(menuGroup);
        button.setOnAction(e -> {
            if (button.isSelected()) {
                action.run();
            }
        });
        return button;
    }

    public void setContent(Node contentNode) {
        this.contentArea.getChildren().clear();
        this.contentArea.getChildren().add(contentNode);
    }
    public void setDashboard(Node contentNode) {
        setContent(contentNode);
    }
}
