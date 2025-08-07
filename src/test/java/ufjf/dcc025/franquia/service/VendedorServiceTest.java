// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ufjf.dcc025.franquia.enums.EstadoPedido;
import ufjf.dcc025.franquia.exception.PermissaoNegadaException;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.persistence.EntityRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para VendedorService")
class VendedorServiceTest {

    @Mock
    private EntityRepository<Pedido> pedidoRepository;
    @Mock
    private EntityRepository<Cliente> clienteRepository;

    private VendedorService vendedorService;

    // Dados de teste
    private Vendedor vendedor;
    private Pedido pedido; // Usado como base para os dados do pedido

    @BeforeEach
    void setUp() {
        Gerente gerente = new Gerente("Gerente", "11111111111", "g@mail.com", "senha123");
        Franquia franquia = new Franquia("Franquia", "End", gerente);
        vendedor = new Vendedor("Vendedor", "22222222222", "v@mail.com", "senha123");
        vendedor.setFranquia(franquia);
        Cliente cliente = new Cliente("Cliente", "33333333333", "c@mail.com", "tel", "end");
        Produto produto = new Produto("P001", "Produto", "Descrição super legal", 10.0);
        Map<Produto, Integer> itens = new HashMap<>();
        itens.put(produto, 1);
        pedido = new Pedido(cliente, itens, franquia, null, null, vendedor);

        vendedorService = new VendedorService(vendedor, pedidoRepository, clienteRepository);
    }

    @Test
    @DisplayName("Deve registrar um novo pedido com sucesso")
    void registrarPedido_comDadosValidos_deveSalvarPedidoECliente() {
        // Ação
        vendedorService.registrarPedido(pedido.getCliente(), pedido.getProdutosQuantidade(), pedido.getFormaPagamento(), pedido.getMetodoEntrega());

        // Verificação
        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository, times(1)).upsert(pedidoCaptor.capture());
        verify(clienteRepository, times(1)).upsert(pedido.getCliente());

        Pedido pedidoSalvo = pedidoCaptor.getValue();
        assertTrue(vendedor.getPedidosId().contains(pedidoSalvo.getId()));
    }

    @Test
    @DisplayName("Deve solicitar a alteração de um pedido e mudar seu status")
    void solicitarAlteracaoPedido_dePedidoValido_deveMudarStatusParaAlteracaoSolicitada() {
        // Preparação
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        Map<Produto, Integer> novosItens = new HashMap<>();

        // Ação
        vendedorService.solicitarAlteracaoPedido(pedido.getId(), novosItens, pedido.getMetodoEntrega());

        // Verificação
        assertEquals(EstadoPedido.ALTERACAO_SOLICITADA, pedido.getStatus());
        verify(pedidoRepository, times(1)).upsert(pedido);
    }

    @Test
    @DisplayName("Não deve permitir que um vendedor altere o pedido de outro")
    void solicitarAlteracaoPedido_deOutroVendedor_deveLancarExcecao() {
        // Preparação
        Vendedor outroVendedor = new Vendedor("Outro", "55555555555", "outro@mail.com", "senha123");
        pedido.setVendedor(outroVendedor); // O pedido pertence a outro vendedor
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));

        // Ação e Verificação
        assertThrows(PermissaoNegadaException.class, () -> {
            vendedorService.solicitarAlteracaoPedido(pedido.getId(), new HashMap<>(), null);
        });
    }

    @Test
    @DisplayName("Deve solicitar o cancelamento de um pedido e mudar seu status")
    void solicitarCancelamentoPedido_dePedidoValido_deveMudarStatusParaCancelamentoSolicitado() {
        // Preparação
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));

        // Ação
        vendedorService.solicitarCancelamentoPedido(pedido.getId());

        // Verificação
        assertEquals(EstadoPedido.CANCELAMENTO_SOLICITADO, pedido.getStatus());
        verify(pedidoRepository, times(1)).upsert(pedido);
    }
}
