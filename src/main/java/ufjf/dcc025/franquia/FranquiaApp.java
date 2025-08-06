package ufjf.dcc025.franquia;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.usuarios.Dono;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Usuario;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.persistence.AsyncFileDAO;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.service.AuthenticationService;
import ufjf.dcc025.franquia.service.DonoService;
import ufjf.dcc025.franquia.service.GerenteService;
import ufjf.dcc025.franquia.util.AlertFactory;
import ufjf.dcc025.franquia.util.CssManager;
import ufjf.dcc025.franquia.util.DataSeeder;
import ufjf.dcc025.franquia.view.CreateOwnerView;
import ufjf.dcc025.franquia.view.DonoDashboard.DonoDashboardView;
import ufjf.dcc025.franquia.view.GerenteDashboardView;
import ufjf.dcc025.franquia.view.LoginView;
import ufjf.dcc025.franquia.view.MainView;
import ufjf.dcc025.franquia.view.VendedorDashboardView;

import java.util.concurrent.TimeUnit;

public class FranquiaApp extends Application {
    // Repositórios
    private EntityRepository<Franquia> franquiaRepo;
    private EntityRepository<Vendedor> vendedorRepo;
    private EntityRepository<Gerente> gerenteRepo;
    private EntityRepository<Dono> donoRepo;
    private EntityRepository<Pedido> pedidoRepo;
    private EntityRepository<Cliente> clienteRepo;

    // Serviços
    private AuthenticationService authService;
    private DonoService donoService;
    private GerenteService gerenteService;
    private DataSeeder dataSeeder;

    // UI
    private Stage primaryStage;
    private Usuario usuarioLogado;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        try {
            initializeRepositories();
            loadData();
            initializeServices();
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertFactory.showError("Erro Crítico na Inicialização", "Não foi possível iniciar a aplicação.");
            return;
        }

        primaryStage.setTitle("Franqueado");

        if (donoRepo.findAll().isEmpty()) {
            showOwnerCreationScreen();
        } else {
            showLoginScreen();
        }

        primaryStage.show();
    }

    private void initializeRepositories() throws java.io.IOException {
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
    }

    private void loadData() throws Exception {
        franquiaRepo.loadAllSync(5, TimeUnit.SECONDS);
        vendedorRepo.loadAllSync(5, TimeUnit.SECONDS);
        gerenteRepo.loadAllSync(5, TimeUnit.SECONDS);
        donoRepo.loadAllSync(5, TimeUnit.SECONDS);
        pedidoRepo.loadAllSync(5, TimeUnit.SECONDS);
        clienteRepo.loadAllSync(5, TimeUnit.SECONDS);
    }

    private void initializeServices() {
        this.authService = new AuthenticationService(donoRepo, gerenteRepo, vendedorRepo);
        this.dataSeeder = new DataSeeder(franquiaRepo, vendedorRepo, gerenteRepo, pedidoRepo, clienteRepo);
    }

    public void showLoginScreen() {
        this.usuarioLogado = null;
        LoginView loginView = new LoginView(this, authService);
        Scene scene = new Scene(loginView, 1280, 800);
        scene.getStylesheets().add(CssManager.getStylesheetURL());
        primaryStage.setScene(scene);
    }

    private void showOwnerCreationScreen() {
        CreateOwnerView ownerView = new CreateOwnerView(this);
        Scene scene = new Scene(ownerView, 1280, 800);
        scene.getStylesheets().add(CssManager.getStylesheetURL());
        primaryStage.setScene(scene);
    }

    public void onOwnerCreationSuccess(Dono novoDono) {
        donoRepo.upsert(novoDono);
        try {
            donoRepo.saveAllSync(5, TimeUnit.SECONDS);
            AlertFactory.showInfo("Sucesso", "Administrador criado com sucesso! Por favor, faça o login.");
            showLoginScreen();
        } catch (Exception e) {
            AlertFactory.showError("Erro ao Salvar", "Não foi possível salvar o novo administrador.");
        }
    }

    public void onLoginSuccess(Usuario usuario) {
        this.usuarioLogado = usuario;
        MainView mainView = new MainView(this);

        switch (usuario.getTipoUsuario()) {
            case DONO:
                this.donoService = new DonoService((Dono) usuario, franquiaRepo, gerenteRepo, vendedorRepo);
                mainView.setDashboard(new DonoDashboardView(donoService));
                break;
            case GERENTE:
                this.gerenteService = new GerenteService((Gerente) usuario, vendedorRepo, pedidoRepo, franquiaRepo);
                mainView.setDashboard(new GerenteDashboardView(gerenteService));
                break;
            case VENDEDOR:
                mainView.setDashboard(new VendedorDashboardView((Vendedor) usuario));
                break;
        }
        primaryStage.getScene().setRoot(mainView);
    }

    public void runSeeder(DataSeeder.SeedScenario scenario) {
        dataSeeder.seedDatabase(scenario);
        AlertFactory.showInfo("Banco Populado",
                "O banco de dados foi populado com o cenário '" + scenario + "'. Recarregando aplicação.");
        reloadAndGoToLogin();
    }

    public void clearDatabase() {
        dataSeeder.clearDatabase();
        AlertFactory.showInfo("Banco Limpo",
                "Os dados foram limpos. Recarregando aplicação.");
        reloadAndGoToLogin();
    }

    private void reloadAndGoToLogin() {
        try {
            loadData();
            initializeServices();
            showLoginScreen();
        } catch (Exception e) {
            e.printStackTrace();
            AlertFactory.showError("Erro ao Recarregar", "Não foi possível recarregar os dados da aplicação.");
        }
    }

    // Getters
    public DonoService getDonoService() { return donoService; }
    public GerenteService getGerenteService() { return gerenteService; }
    public Usuario getUsuarioLogado() { return usuarioLogado; }
}
