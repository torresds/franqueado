package ufjf.dcc025.franquia;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ufjf.dcc025.franquia.persistence.AsyncFileDAO;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.util.CssManager;
import ufjf.dcc025.franquia.view.LoginView;

import java.util.concurrent.TimeUnit;

public class FranquiaApp extends Application {

    // to-do: implementar repositórios
    // private EntityRepository<Franquia> franquiaRepo;
    // private EntityRepository<Vendedor> vendedorRepo;
    // private EntityRepository<Gerente> gerenteRepo;
    // private EntityRepository<Dono> donoRepo;
    // private EntityRepository<Produto> produtoRepo;
    // private EntityRepository<Pedido> pedidoRepo;

    private Stage primaryStage;

    public FranquiaApp() {
        super();
    }
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        try {
            // AsyncFileDao<Franquia> franquiaDao = new AsyncFileDAO<>(Franquia.class);
            // AsyncFileDao<Vendedor> vendedorDao = new AsyncFileDAO<>(Vendedor.class);
            // AsyncFileDao<Gerente> gerenteDao = new AsyncFileDAO<>(Gerente.class);
            // AsyncFileDao<Dono> donoDao = new AsyncFileDAO<>(Dono.class);
            // AsyncFileDao<Produto> produtoDao = new AsyncFileDAO<>(Produto.class);
            // AsyncFileDao<Pedido> pedidoDao = new AsyncFileDAO<>(Pedido.class);

            // franquiaRepo = new EntityRepository<>(franquiaDao);
            // vendedorRepo = new EntityRepository<>(vendedorDao);
            // gerenteRepo = new EntityRepository<>(gerenteDao);
            // donoRepo = new EntityRepository<>(donoDao);
            // produtoRepo = new EntityRepository<>(produtoDao);
            // pedidoRepo = new EntityRepository<>(pedidoDao);
            // franquiaRepo.loadAllSync(5, TimeUnit.SECONDS);
            // vendedorRepo.loadAllSync(5, TimeUnit.SECONDS);
            // gerenteRepo.loadAllSync(5, TimeUnit.SECONDS);
            // donoRepo.loadAllSync(5, TimeUnit.SECONDS);
            // produtoRepo.loadAllSync(5, TimeUnit.SECONDS);
            // pedidoRepo.loadAllSync(5, TimeUnit.SECONDS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        primaryStage.setTitle("Franqueado");
        showLoginScreen();
        primaryStage.show();
    }

    public void showLoginScreen() {
        LoginView loginView = new LoginView(this);
        Scene scene = new Scene(loginView, 1280, 800);
        scene.getStylesheets().add("data:text/css," + CssManager.getStyles().replace("%", "%%"));
        primaryStage.setScene(scene);
    }

    public void showDonoDashboard() {
    }

    public void showGerenteDashboard() {
    }

    public void showVendedorDashboard() {
    }
}
