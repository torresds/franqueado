// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ufjf.dcc025.franquia.exception.UsuarioInvalidoException;
import ufjf.dcc025.franquia.model.usuarios.Dono;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Usuario;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.persistence.EntityRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AuthenticationService")
class AuthenticationServiceTest {

    @Mock
    private EntityRepository<Dono> donoRepo;
    @Mock
    private EntityRepository<Gerente> gerenteRepo;
    @Mock
    private EntityRepository<Vendedor> vendedorRepo;

    @InjectMocks
    private AuthenticationService authService;

    private Dono dono;
    private Gerente gerente;
    private Vendedor vendedor;

    @BeforeEach
    void setUp() {
        dono = new Dono("Dono", "111", "dono@mail.com", "senha123");
        gerente = new Gerente("Gerente", "222", "gerente@mail.com", "senha123");
        vendedor = new Vendedor("Vendedor", "333", "vendedor@mail.com", "senha123");

        when(donoRepo.findAll()).thenReturn(List.of(dono));
        when(gerenteRepo.findAll()).thenReturn(List.of(gerente));
        when(vendedorRepo.findAll()).thenReturn(List.of(vendedor));
    }

    @Test
    @DisplayName("Deve autenticar o Dono com email e senha corretos")
    void authenticate_donoComEmailCorreto_deveRetornarUsuarioDono() throws UsuarioInvalidoException {
        Usuario usuario = authService.authenticate("dono@mail.com", "senha123");
        assertNotNull(usuario);
        assertEquals(dono.getId(), usuario.getId());
    }

    @Test
    @DisplayName("Deve autenticar o Gerente com CPF e senha corretos")
    void authenticate_gerenteComCpfCorreto_deveRetornarUsuarioGerente() throws UsuarioInvalidoException {
        Usuario usuario = authService.authenticate("222", "senha123");
        assertNotNull(usuario);
        assertEquals(gerente.getId(), usuario.getId());
    }

    @Test
    @DisplayName("Deve autenticar o Vendedor com email e senha corretos")
    void authenticate_vendedorComEmailCorreto_deveRetornarUsuarioVendedor() throws UsuarioInvalidoException {
        Usuario usuario = authService.authenticate("vendedor@mail.com", "senha123");
        assertNotNull(usuario);
        assertEquals(vendedor.getId(), usuario.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção para senha incorreta")
    void authenticate_comSenhaIncorreta_deveLancarUsuarioInvalidoException() {
        assertThrows(UsuarioInvalidoException.class, () -> {
            authService.authenticate("dono@mail.com", "senhaErrada");
        });
    }

    @Test
    @DisplayName("Deve lançar exceção para login inexistente")
    void authenticate_comLoginInexistente_deveLancarUsuarioInvalidoException() {
        assertThrows(UsuarioInvalidoException.class, () -> {
            authService.authenticate("naoexiste@mail.com", "senha123");
        });
    }
}
