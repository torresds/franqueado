package ufjf.dcc025.franquia.modelo;
import ufjf.dcc025.franquia.enums.TipoUsuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.text.html.parser.Entity;

public class Gerente extends Usuario {
    private Franquia franquia;
    private List<String> pedidosPendentesId;
    private List<Pedido> alteracoesPedidos;

    public Gerente(String nome, String cpf, String email, String senha, String id, Franquia franquia) {
        super(nome, cpf, email, senha, id);
        this.franquia = franquia;
        this.pedidosPendentesId = new ArrayList<>();
        this.alteracoesPedidos = new ArrayList<>();
    }


    //------------ GERENCIAMENTO DE VENDEDORES ------------

    public Vendedor cadastrarVendedor(String nome, String cpf, String email, String senha, String id, EntityRepository<Vendedor> vendedoresValidos) {
        Vendedor novoVendedor = new Vendedor(nome, cpf, email, senha, id, this.franquia);
        vendedoresValidos.upsert(novoVendedor);
        return novoVendedor;
    }

    public void removerVendedor(String idVendedor, EntityRepository<Vendedor> vendedores) {
        vendedores.delete(idVendedor);
    }

    public List<Vendedor> listarVendedoresPorVendas(EntityRepository<Vendedor> vendedores) {
        List<Vendedor> listaVendedores = vendedores.findAll();
        listaVendedores.sort((v1, v2) -> Double.compare(v2.getTotalVendas(), v1.getTotalVendas()));
        return listaVendedores;
    }

    //------------ GERENCIAMENTO DE PEDIDOS ------------

    public Pedido aceitarPedido(String pedidoId, EntityRepository<Pedido> pedidosValidos) {
        Pedido pedido = pedidosValidos.findById(pedidoId);
        if (pedidosPendentesId.contains(pedidoId)) {
            pedido.aprovarPedido();
            pedidosPendentesId.remove(pedidoId);
        }
        Map<Produto, Integer> produtos = pedido.getProdutosQuantidade();
        for (Produto produto : produtos.keySet()) {
            franquia.atualizarEstoque(produto, -produtos.get(produto));
        }
        return pedido;
    }

    public Pedido cancelarPedido(String pedidoId, EntityRepository<Pedido> pedidosValidos) {
        Pedido pedido = pedidosValidos.findById(pedidoId);
        if (pedidosPendentesId.contains(pedidoId)) {
            pedido.cancelarPedido();
            pedidosPendentesId.remove(pedidoId);
        }
        return pedido;
    }

    public List<Pedido> listarPedidosPendentes(EntityRepository<Pedido> pedidosValidos) {
        List<Pedido> pedidosPendentes = new ArrayList<>();
        for (String pedidoId : pedidosPendentesId) {
            Pedido pedido = pedidosValidos.findById(pedidoId);
            if (pedido != null && "Pendente".equals(pedido.getStatus())) {
                pedidosPendentes.add(pedido);
            }
        }
        return pedidosPendentes;
    }

    public void adicionarPedidoPendente(String pedidoId) {
        pedidosPendentesId.add(pedidoId);
    }

    public void adicionarAlteracaoPedido(Pedido pedido) {
        if (!alteracoesPedidos.contains(pedido)) {
            alteracoesPedidos.add(pedido);
        }
    }

    public void aceitarAlteracaoPedido(Pedido pedido, EntityRepository<Pedido> pedidosValidos) {
        if (alteracoesPedidos.contains(pedido)) {
            
            if (pedido.getMetodoEntrega() == pedidosValidos.findById(pedido.getId()).getMetodoEntrega()) {
                Map<Produto, Integer> produtosnovos = pedido.getProdutosQuantidade();
                Map<Produto, Integer> produtosantigos = pedidosValidos.findById(pedido.getId()).getProdutosQuantidade();
                for (Produto produto : produtosnovos.keySet()) {
                    if (!produtosantigos.containsKey(produto)) {
                        franquia.atualizarEstoque(produto, produtosantigos.get(produto) - produtosnovos.get(produto));
                    } else {
                        franquia.atualizarEstoque(produto, -produtosnovos.get(produto));
                    }
                }
                for (Produto produto : produtosantigos.keySet()) {
                    if (!produtosnovos.containsKey(produto)) {
                        franquia.atualizarEstoque(produto, produtosantigos.get(produto));
                    }
                }
            }
        
            pedido.aprovarPedido();
            pedidosValidos.upsert(pedido);
            alteracoesPedidos.remove(pedido);
        }
    }

    public void cancelarAlteracaoPedido(Pedido pedido) {
        if (alteracoesPedidos.contains(pedido)) {
            pedido.cancelarPedido();
            alteracoesPedidos.remove(pedido);
        }
    }


    @Override
    public String toString() {
        return "Gerente: " + getNome();
    }

    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.GERENTE;
    }

    public String getUnidadeFranquiaId() {
        return unidadeFranquiaId;
    }

    public void setUnidadeFranquiaId(String unidadeFranquiaId) {
        this.unidadeFranquiaId = unidadeFranquiaId;
    }
}
