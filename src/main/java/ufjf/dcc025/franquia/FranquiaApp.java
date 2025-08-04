package ufjf.dcc025.franquia;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.usuarios.Dono;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.persistence.AsyncFileDAO;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.util.CssManager;
import ufjf.dcc025.franquia.view.LoginView;
//import ufjf.dcc025.franquia.view.DonoDashboardView;
//import ufjf.dcc025.franquia.view.GerenteDashboardView;
//import ufjf.dcc025.franquia.view.VendedorDashboardView;

import java.util.concurrent.TimeUnit;

public class FranquiaApp extends Application {
    private EntityRepository<Franquia> franquiaRepo;
    private EntityRepository<Vendedor> vendedorRepo;
    private EntityRepository<Gerente> gerenteRepo;
    private EntityRepository<Dono> donoRepo;
    private EntityRepository<Pedido> pedidoRepo;
    private EntityRepository<Cliente> clienteRepo;

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
        	AsyncFileDAO<Franquia> franquiaDao = new AsyncFileDAO<>(Franquia.class);
            AsyncFileDAO<Vendedor> vendedorDao = new AsyncFileDAO<>(Vendedor.class);
            AsyncFileDAO<Gerente> gerenteDao = new AsyncFileDAO<>(Gerente.class);
            AsyncFileDAO<Dono> donoDao = new AsyncFileDAO<>(Dono.class);
            AsyncFileDAO<Pedido> pedidoDao = new AsyncFileDAO<>(Pedido.class);
            AsyncFileDAO<Cliente> clienteDao = new AsyncFileDAO<>(Cliente.class);

            franquiaRepo = new EntityRepository<>(franquiaDao);
            vendedorRepo = new EntityRepository<>(vendedorDao);
            gerenteRepo = new EntityRepository<>(gerenteDao);
            donoRepo = new EntityRepository<>(donoDao);
            pedidoRepo = new EntityRepository<>(pedidoDao);
            clienteRepo = new EntityRepository<>(clienteDao);
            
            franquiaRepo.loadAllSync(5, TimeUnit.SECONDS);
            vendedorRepo.loadAllSync(5, TimeUnit.SECONDS);
            gerenteRepo.loadAllSync(5, TimeUnit.SECONDS);
            donoRepo.loadAllSync(5, TimeUnit.SECONDS);
            pedidoRepo.loadAllSync(5, TimeUnit.SECONDS);
            clienteRepo.loadAllSync(5, TimeUnit.SECONDS);
            
            if (donoRepo.findAll().isEmpty()) {
                Dono donoPadrao = new Dono("Admin", "12345678909", "admin@franquia.com", "senha123");
                donoRepo.upsert(donoPadrao);
                donoRepo.saveAllAsync();
                System.out.println("Dono padrão criado: CPF=12345678909, Senha=senha123");
            }
            
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

    // Getters para repositórios
    public EntityRepository<Franquia> getFranquiaRepo() {
        return franquiaRepo;
    }

    public EntityRepository<Vendedor> getVendedorRepo() {
        return vendedorRepo;
    }

    public EntityRepository<Gerente> getGerenteRepo() {
        return gerenteRepo;
    }

    public EntityRepository<Dono> getDonoRepo() {
        return donoRepo;
    }

    public EntityRepository<Pedido> getPedidoRepo() {
        return pedidoRepo;
    }

    public EntityRepository<Cliente> getClienteRepo() {
        return clienteRepo;
    }
}