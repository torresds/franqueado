// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ufjf.dcc025.franquia.exception.DadosInvalidosException;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.usuarios.Dono;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.persistence.EntityRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para DonoService")
class DonoServiceTest {

    @Mock
    private EntityRepository<Franquia> franquiasRepository;
    @Mock
    private EntityRepository<Gerente> gerentesRepository;
    @Mock
    private EntityRepository<Vendedor> vendedoresRepository;
    @Mock
    private Dono dono;

    private DonoService donoService;

    private Gerente gerente1;
    private Franquia franquia1;

    @BeforeEach
    void setUp() {
        donoService = new DonoService(dono, franquiasRepository, gerentesRepository, vendedoresRepository);

        // Configuração inicial para os testes
        gerente1 = new Gerente("Gerente Teste", "12345678901", "gerente@teste.com", "senha123");
        franquia1 = new Franquia("Franquia A", "Endereço A", null);
    }

    @Test
    @DisplayName("Deve cadastrar uma franquia sem gerente com sucesso")
    void cadastrarFranquia_semGerente_deveSalvarFranquia() {
        // Ação
        donoService.cadastrarFranquia("Nova Franquia", "Endereço Novo", null);

        // Verificação
        verify(franquiasRepository, times(1)).upsert(any(Franquia.class));
        verify(franquiasRepository, times(1)).saveAllAsync();
    }

    @Test
    @DisplayName("Deve cadastrar uma franquia com um gerente disponível com sucesso")
    void cadastrarFranquia_comGerenteDisponivel_deveSalvarFranquiaEAssociarGerente() {
        // Preparação
        when(gerentesRepository.findById(gerente1.getId())).thenReturn(Optional.of(gerente1));

        // Ação
        donoService.cadastrarFranquia("Nova Franquia", "Endereço Novo", gerente1.getId());

        // Verificação
        verify(franquiasRepository, times(1)).upsert(any(Franquia.class));
        verify(gerentesRepository, times(1)).upsert(gerente1);
        assertNotNull(gerente1.getFranquia());
    }

    @Test
    @DisplayName("Não deve cadastrar franquia se o gerente já estiver alocado")
    void cadastrarFranquia_comGerenteJaAlocado_deveLancarExcecao() {
        // Preparação
        gerente1.setFranquia(franquia1); // Gerente já tem uma franquia
        when(gerentesRepository.findById(gerente1.getId())).thenReturn(Optional.of(gerente1));

        // Ação e Verificação
        assertThrows(DadosInvalidosException.class, () -> {
            donoService.cadastrarFranquia("Franquia B", "Endereço B", gerente1.getId());
        });
    }

    @Test
    @DisplayName("Deve atualizar uma franquia e trocar seu gerente corretamente")
    void atualizarFranquia_trocandoGerente_deveAtualizarAssociacoes() {
        // Preparação
        Gerente gerenteAntigo = new Gerente("Antigo", "11111111111", "antigo@mail.com", "senha123");
        Gerente gerenteNovo = new Gerente("Novo", "22222222222", "novo@mail.com", "senha123");
        franquia1.setGerente(gerenteAntigo);
        gerenteAntigo.setFranquia(franquia1);

        when(franquiasRepository.findById(franquia1.getId())).thenReturn(Optional.of(franquia1));
        when(gerentesRepository.findById(gerenteNovo.getId())).thenReturn(Optional.of(gerenteNovo));

        // Ação
        donoService.atualizarFranquia(franquia1.getId(), "Nome Novo", "Endereço Novo", gerenteNovo.getId());

        // Verificação
        assertNull(gerenteAntigo.getFranquia()); // Gerente antigo foi desvinculado
        assertEquals(franquia1, gerenteNovo.getFranquia()); // Novo gerente foi vinculado
        assertEquals(gerenteNovo, franquia1.getGerente());
        verify(gerentesRepository, times(2)).upsert(any(Gerente.class)); // Um para o antigo, um para o novo
        verify(franquiasRepository, times(1)).upsert(franquia1);
    }

    @Test
    @DisplayName("Não deve atualizar franquia se o novo gerente já estiver ocupado")
    void atualizarFranquia_paraGerenteOcupado_deveLancarExcecao() {
        // Preparação
        Franquia outraFranquia = new Franquia("Outra", "Outro Endereço", null);
        Gerente gerenteNovo = new Gerente("Novo", "22222222222", "novo@mail.com", "senha123");
        gerenteNovo.setFranquia(outraFranquia); // Gerente novo já está ocupado

        when(franquiasRepository.findById(franquia1.getId())).thenReturn(Optional.of(franquia1));
        when(gerentesRepository.findById(gerenteNovo.getId())).thenReturn(Optional.of(gerenteNovo));

        // Ação e Verificação
        assertThrows(DadosInvalidosException.class, () -> {
            donoService.atualizarFranquia(franquia1.getId(), "Nome Novo", "Endereço Novo", gerenteNovo.getId());
        });
    }

    @Test
    @DisplayName("Deve cadastrar um gerente com dados válidos")
    void cadastrarGerente_comDadosValidos_deveSalvarGerente() {
        // Preparação
        when(gerentesRepository.findAll()).thenReturn(List.of());

        // Ação
        donoService.cadastrarGerente("Novo Gerente", "09876543210", "novo@gerente.com", "senha123");

        // Verificação
        verify(gerentesRepository, times(1)).upsert(any(Gerente.class));
        verify(gerentesRepository, times(1)).saveAllAsync();
    }

    @Test
    @DisplayName("Não deve cadastrar gerente com CPF duplicado")
    void cadastrarGerente_comCpfDuplicado_deveLancarExcecao() {
        // Preparação
        when(gerentesRepository.findAll()).thenReturn(List.of(gerente1));

        // Ação e Verificação
        assertThrows(DadosInvalidosException.class, () -> {
            donoService.cadastrarGerente("Outro Gerente", "12345678901", "outro@gerente.com", "senha123");
        });
    }
}
