package ufjf.dcc025.franquia.model.usuarios;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ufjf.dcc025.franquia.enums.TipoUsuario;
import ufjf.dcc025.franquia.enums.EstadoPedido;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.model.clientes.Cliente;
import ufjf.dcc025.franquia.exception.*;


public class Gerente extends Usuario {
    private Franquia franquia;
    private List<String> pedidosPendentesId;
    private List<Pedido> alteracoesPedidos;

    public Gerente(String nome, String cpf, String email, String senha, String franquiaId, EntityRepository<Franquia> franquiasValidas) {
        super(nome, cpf, email, senha);
        this.franquia = franquiasValidas.findById(franquiaId).orElse(null);
        this.pedidosPendentesId = new ArrayList<>();
        this.alteracoesPedidos = new ArrayList<>();
    }

    //Alterar informação de vendedores cadastrados (feito)
    //Cadastrar, editar e remover produtos (feito)
    //Ver produtos que estão com estoque baixo (fazer essa função na Franquia) (fiz aq msm)
    //Acessar relatórios da Franquia, histórico, clientes recorrentes, etc. (fiz os metodos na franquia e chamei aq)

    //------------ GERENCIAMENTO DE VENDEDORES ------------

    public Vendedor cadastrarVendedor(String nome, String cpf, String email, String senha, EntityRepository<Vendedor> vendedoresValidos) {
        Vendedor novoVendedor = new Vendedor(nome, cpf, email, senha, this.franquia);
        vendedoresValidos.upsert(novoVendedor);
        return novoVendedor;
    }
    
    public void removerVendedor(String vendedorId, EntityRepository<Vendedor> vendedores) {
        Vendedor vendedor = vendedores.findById(vendedorId).orElse(null);
        if (vendedor == null) {
            throw new EntidadeNaoEncontradaException(vendedorId);
        }
        franquia.removerVendedor(vendedor);
        vendedores.delete(vendedorId);
    }

    public Vendedor editarVendedor(String idVendedor, String novoNome, String novoCpf, String novoEmail, String novaSenha, EntityRepository<Vendedor> vendedoresValidos) {
        Vendedor vendedor = vendedoresValidos.findById(idVendedor).orElse(null);
        if (vendedor == null) {
            throw new EntidadeNaoEncontradaException(idVendedor);
        }
        vendedor.setNome(novoNome);
        vendedor.setCpf(novoCpf);
        vendedor.setEmail(novoEmail);
        vendedor.setSenha(novaSenha);
        vendedoresValidos.upsert(vendedor);
        return vendedor;
    }

    public List<Vendedor> listarVendedoresPorVendas(EntityRepository<Vendedor> vendedores) {
        List<Vendedor> listaVendedores = vendedores.findAll();
        listaVendedores.sort((v1, v2) -> Double.compare(v2.getTotalVendas(), v1.getTotalVendas()));
        return listaVendedores;
    }


    //------------ GERENCIAMENTO DE PEDIDOS ------------

    public Pedido aceitarPedido(String pedidoId, EntityRepository<Pedido> pedidosValidos) {
        Pedido pedido = pedidosValidos.findById(pedidoId).orElse(null);
        if (pedidosPendentesId.contains(pedidoId)) {
            pedido.aprovarPedido();
            pedidosPendentesId.remove(pedidoId);
        }
        Map<Produto, Integer> produtos = pedido.getProdutosQuantidade();
        for (Produto produto : produtos.keySet()) {
            franquia.atualizarEstoque(produto, -produtos.get(produto));
        }
        pedido.atualizarValores();
        pedido.getVendedor().atualizarTotalVendas(pedido.getValorTotal());
        pedido.getFranquia().atualizarReceita(pedido.getValorTotal());
        return pedido;
    }

    public Pedido cancelarPedido(String pedidoId, EntityRepository<Pedido> pedidosValidos) {
        Pedido pedido = pedidosValidos.findById(pedidoId).orElse(null);
        if (pedidosPendentesId.contains(pedidoId)) {
            pedido.cancelarPedido();
            pedidosPendentesId.remove(pedidoId);
        }
        return pedido;
    }

    public List<Pedido> listarPedidosPendentes(EntityRepository<Pedido> pedidosValidos) {
        List<Pedido> pedidosPendentes = new ArrayList<>();
        for (String pedidoId : pedidosPendentesId) {
            Pedido pedido = pedidosValidos.findById(pedidoId).orElse(null);
            if (pedido != null && pedido.isPendente()) {
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
            Pedido pedidoOriginal = pedidosValidos.findById(pedido.getId()).orElse(null);
            if (pedidoOriginal == null) {
                throw new EntidadeNaoEncontradaException(pedido.getId());
            }
            if (pedido.getMetodoEntrega() == pedidoOriginal.getMetodoEntrega()) {
                Map<Produto, Integer> produtosnovos = pedido.getProdutosQuantidade();
                Map<Produto, Integer> produtosantigos = pedidoOriginal.getProdutosQuantidade();
                for (Produto produto : produtosnovos.keySet()) {
                    if (!produtosantigos.containsKey(produto)) {
                        franquia.atualizarEstoque(produto, produtosantigos.getOrDefault(produto, 0) - produtosnovos.get(produto));
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
            double valorAntigo = pedidoOriginal.getValorTotal();
            double valorNovo = pedido.getValorTotal();
            pedido.getVendedor().atualizarTotalVendas(valorNovo - valorAntigo);
            pedido.getFranquia().atualizarReceita(valorNovo - valorAntigo);
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

   //------------ GERENCIAMENTO DE PRODUTOS ------------

    public void criarNovoProduto(String codigo, String nome, String descricao, double preco, int quantidadeInicial, EntityRepository<Franquia> todasFranquias) {
        for (Franquia franquia : todasFranquias.findAll()) {
            Produto produtoExistente = franquia.buscarProduto(codigo);
            if (produtoExistente != null) {
                throw new DadosInvalidosException("Codigo '" + codigo + "' já cadastrado no sistema.");
            }
        }
        Produto novoProduto = new Produto(codigo, nome, descricao, preco);
        
        for (Franquia franquia : todasFranquias.findAll()) {
            franquia.adicionarProduto(novoProduto, 0);
        }
        this.getFranquia().atualizarEstoque(novoProduto, quantidadeInicial);
        }


    public void editarProduto(String codigo, String novoNome, String novaDescricao,double novoPreco, EntityRepository<Franquia> todasFranquias) {
        Produto produtoExistente = null;
        for (Franquia franquia : todasFranquias.findAll()) {
            produtoExistente = franquia.buscarProduto(codigo);
            if (produtoExistente != null) {
                break;
            }
        }
    
        if (produtoExistente == null) {
            throw new EntidadeNaoEncontradaException(codigo);
     }
    /*
        if (!nomeAntigo.equals(novoNome)) {
            for (Franquia franquia : todasFranquias.findAll()) {
                Produto produtoComNovoNome = franquia.buscarProduto(novoNome);
                if (produtoComNovoNome != null) {
                    throw new DadosInvalidosException("Já existe um produto com o nome '" + novoNome + "'.");
                }
            }
    }
    */
        Produto produtoAtualizado = new Produto(codigo, novoNome, novaDescricao, novoPreco);
        
        for (Franquia franquia : todasFranquias.findAll()) {
            Produto produtoNaFranquia = franquia.buscarProduto(codigo);
            if (produtoNaFranquia != null) {
            
                int quantidadeAtual = franquia.getEstoque().get(produtoNaFranquia);
                
            
                franquia.removerProduto(produtoNaFranquia);
                
            
                franquia.adicionarProduto(produtoAtualizado, quantidadeAtual);
            }
        }
    }


    public void removerProdutoExistente(String codigoProduto, EntityRepository<Franquia> todasFranquias) {
        boolean produtoEncontrado = false;
        for (Franquia franquia : todasFranquias.findAll()) {
            Produto produto = franquia.buscarProduto(codigoProduto);
            if (produto != null) {
                produtoEncontrado = true;
                break;
            }
        }

        if (!produtoEncontrado) {
            throw new EntidadeNaoEncontradaException(codigoProduto);
        }
        
        for (Franquia franquia : todasFranquias.findAll()) {
            Produto produto = franquia.buscarProduto(codigoProduto);
            
            if (produto != null) {
                franquia.removerProduto(produto);
            }
        }
    }


    public void atualizarEstoqueProduto(String codigoProduto, int novaQuantidade, String franquiaId, EntityRepository<Franquia> todasFranquias) {
        
        Franquia franquiaEscolhida = todasFranquias.findById(franquiaId).orElse(null);
        if (franquiaEscolhida == null) {
            throw new EntidadeNaoEncontradaException(franquiaId);
        }
        
        Produto produto = franquiaEscolhida.buscarProduto(codigoProduto);
        if (produto == null) {
            throw new EntidadeNaoEncontradaException(codigoProduto);
        }
        
        if (novaQuantidade < 0) {
            throw new DadosInvalidosException("Quantidade não pode ser negativa.");
        }
        
        franquiaEscolhida.removerProduto(produto);
        
        if (novaQuantidade > 0) {
            franquiaEscolhida.adicionarProduto(produto, novaQuantidade);
        }
    }

    public List<String> listarProdutosEstoqueBaixo(String franquiaId, int limiteMinimo, EntityRepository<Franquia> todasFranquias) {

        Franquia franquiaEscolhida = todasFranquias.findById(franquiaId).orElse(null);
        if (franquiaEscolhida == null) {
            throw new EntidadeNaoEncontradaException(franquiaId);
        }
        
        List<String> produtosEstoqueBaixo = new ArrayList<>();
        
        for (Map.Entry<Produto, Integer> entry : franquiaEscolhida.getEstoque().entrySet()) {
            if (entry.getValue() <= limiteMinimo) {
                Produto produto = entry.getKey();
                int quantidade = entry.getValue();
                String detalhes = String.format("⚠️ %s - Estoque: %d unidades (Limite: %d)", 
                                            produto.toString(), quantidade, limiteMinimo);
                produtosEstoqueBaixo.add(detalhes);
            }
        }
        
        return produtosEstoqueBaixo;
    }

    
     //------------ RELATÓRIOS ------------

    public void visualizarRelatorioVendas(EntityRepository<Pedido> repositorioPedidos) {
	    List<String> pedidosId = this.franquia.gerarRelatorioVendas();
	    
	    System.out.println("📊 RELATÓRIO DE VENDAS - " + this.franquia.getNome().toUpperCase());
	    System.out.println("=" .repeat(60));
	    System.out.println("Total de pedidos: " + pedidosId.size());
	    System.out.printf("💰 Receita Acumulada: R$ %.2f%n", this.franquia.getReceita());
	    System.out.println("=" .repeat(60));
	    
	    if (pedidosId.isEmpty()) {
	        System.out.println("❌ Nenhum pedido encontrado.");
	        return;
	    }
	    
	    int pedidosAprovados = 0;
	    int pedidosCancelados = 0;
	    int pedidosPendentes = 0;
	    
	    for (String pedidoId : pedidosId) {
	        Pedido pedido = repositorioPedidos.findById(pedidoId).orElse(null);
	        if (pedido != null) {
	            System.out.printf("🛒 %s | %s | R$ %.2f | %s%n", 
	                            pedidoId, 
	                            pedido.getCliente().getNome(), 
	                            pedido.getValorTotal(),
	                            pedido.getStatus());
	            
	            if (pedido.isAprovado()) {
	                pedidosAprovados++;
	            } else if (pedido.isCancelado()) {
	                pedidosCancelados++;
	            } else if (pedido.isPendente()) {
	                pedidosPendentes++;
	            }
	        }
	    }
	    
	    System.out.println("=" .repeat(60));
	    System.out.printf("✅ Pedidos Aprovados: %d%n", pedidosAprovados);
	    System.out.printf("⏳ Pedidos Pendentes: %d%n", pedidosPendentes);
	    System.out.printf("❌ Pedidos Cancelados: %d%n", pedidosCancelados);
	    System.out.println("=" .repeat(60));
	}

    public void visualizarRelatorioVendasPeriodo(Date dataInicio, Date dataFim,EntityRepository<Pedido> repositorioPedidos) {
	    List<String> pedidosId = this.franquia.gerarRelatorioVendasPeriodo(dataInicio, dataFim, repositorioPedidos);
	    
	    System.out.println("📅 RELATÓRIO DE VENDAS POR PERÍODO - " + this.franquia.getNome().toUpperCase());
	    System.out.printf("Período: %s até %s%n", dataInicio.toString(), dataFim.toString());
	    System.out.println("=" .repeat(60));
	    System.out.println("Total de pedidos no período: " + pedidosId.size());
	    System.out.println("=" .repeat(60));
	    
	    if (pedidosId.isEmpty()) {
	        System.out.println("❌ Nenhum pedido encontrado no período especificado.");
	        return;
	    }
	    
	    double receitaPeriodo = 0.0;
	    int pedidosAprovadosPeriodo = 0;
	    
	    for (String pedidoId : pedidosId) {
	        Pedido pedido = repositorioPedidos.findById(pedidoId).orElse(null);
	        if (pedido != null) {
	            System.out.printf("🛒 %s | %s | %s | R$ %.2f | %s%n", 
	                            pedidoId, 
	                            pedido.getData().toString(),
	                            pedido.getCliente().getNome(), 
	                            pedido.getValorTotal(),
	                            pedido.getStatus());
	            
	            if (pedido.isAprovado()) {
	                receitaPeriodo += pedido.getValorTotal();
	                pedidosAprovadosPeriodo++;
	            }
	        }
	    }
	    
	    System.out.println("=" .repeat(60));
	    System.out.printf("💰 Receita do Período: R$ %.2f%n", receitaPeriodo);
	    System.out.printf("✅ Pedidos Aprovados no Período: %d%n", pedidosAprovadosPeriodo);
	    double ticketMedio = pedidosAprovadosPeriodo > 0 ? receitaPeriodo / pedidosAprovadosPeriodo : 0;
	    System.out.printf("🎯 Ticket Médio: R$ %.2f%n", ticketMedio);
	    System.out.println("=" .repeat(60));
	    }

    public void visualizarRelatorioClientesFrequencia(EntityRepository<Cliente> repositorioClientes) {
	    List<String> clientesRanking = this.franquia.gerarRelatorioClientesFrequencia(repositorioClientes);
	    
	    System.out.println("👥 RELATÓRIO DE CLIENTES POR FREQUÊNCIA - " + this.franquia.getNome().toUpperCase());
	    System.out.println("=" .repeat(60));
	    System.out.println("Total de clientes ativos: " + clientesRanking.size());
	    System.out.println("=" .repeat(60));
	    
	    if (clientesRanking.isEmpty()) {
	        System.out.println("❌ Nenhum cliente ativo encontrado.");
	        return;
	    }
	    
	    for (int i = 0; i < clientesRanking.size(); i++) {
	        String posicao = String.format("%2d°", i + 1);
	        String medalha = "";
	        
	        if (i == 0) medalha = "🥇";
	        else if (i == 1) medalha = "🥈";
	        else if (i == 2) medalha = "🥉";
	        else medalha = "👤";
	        
	        System.out.printf("%s %s %s%n", medalha, posicao, clientesRanking.get(i));
	    }
	    
	    System.out.println("=" .repeat(60));
	}

    public void visualizarRelatorioEstoqueBaixo(int limiteMinimo, EntityRepository<Franquia> todasFranquias) {
	    List<String> produtosEstoqueBaixo = listarProdutosEstoqueBaixo(this.franquia.getId(), limiteMinimo, todasFranquias);
	    
	    System.out.println("⚠️ RELATÓRIO DE ESTOQUE BAIXO - " + this.franquia.getNome().toUpperCase());
	    System.out.println("=" .repeat(60));
	    System.out.println("Limite mínimo: " + limiteMinimo + " unidades");
	    System.out.println("Produtos com estoque baixo: " + produtosEstoqueBaixo.size());
	    System.out.println("=" .repeat(60));
	    
	    if (produtosEstoqueBaixo.isEmpty()) {
	        System.out.println("✅ Todos os produtos estão com estoque adequado!");
	        return;
	    }
	    
	    for (String produto : produtosEstoqueBaixo) {
	        System.out.println(produto);
	    }
	    
	    System.out.println("=" .repeat(60));
	}

    public void visualizarEstoqueCompleto() {
        Map<Produto, Integer> estoque = this.franquia.getEstoque();
        
        System.out.println("📦 ESTOQUE COMPLETO - " + this.franquia.getNome().toUpperCase());
        System.out.println("=" .repeat(70));
        System.out.println("Total de produtos: " + estoque.size());
        System.out.println("=" .repeat(70));
        
        if (estoque.isEmpty()) {
            System.out.println("❌ Nenhum produto em estoque.");
            return;
        }
        
        List<Map.Entry<Produto, Integer>> produtosOrdenados = estoque.entrySet().stream()
                .sorted(Map.Entry.comparingByKey((p1, p2) -> p1.getNome().compareToIgnoreCase(p2.getNome())))
                .collect(Collectors.toList());
        
        double valorTotalEstoque = 0.0;
        int totalUnidades = 0;
        
        System.out.printf("%-30s | %-10s | %-12s | %-15s%n", "PRODUTO", "QUANTIDADE", "PREÇO UNIT.", "VALOR TOTAL");
        System.out.println("-" .repeat(70));

        for (Map.Entry<Produto, Integer> entry : estoque.entrySet()) {
            Produto produto = entry.getKey();
            int quantidade = entry.getValue();
            double precoUnitario = produto.getPreco();
            double valorTotal = precoUnitario * quantidade;
            
            // Indicador visual de estoque
            String indicador = "";
            if (quantidade <= 5) {
                indicador = "⚠️ "; // Estoque muito baixo
            } else if (quantidade <= 10) {
                indicador = "🟡 "; // Estoque baixo
            } else {
                indicador = "✅ "; // Estoque adequado
            }
            
            System.out.printf("%s%-28s | %10d | R$ %9.2f | R$ %12.2f%n", 
                            indicador, 
                            produto.getCodigo(),
                            produto.getNome(), 
                            quantidade, 
                            precoUnitario, 
                            valorTotal);
            
            valorTotalEstoque += valorTotal;
            totalUnidades += quantidade;
        }
        
        System.out.println("-" .repeat(70));
        System.out.printf("TOTAIS: %d produtos | %d unidades | R$ %.2f%n", 
                         estoque.size(), totalUnidades, valorTotalEstoque);
        System.out.println("=" .repeat(70));
        
        // Legenda
        System.out.println("📊 LEGENDA:");
        System.out.println("✅ Estoque adequado (> 10 unidades)");
        System.out.println("🟡 Estoque baixo (6-10 unidades)");
        System.out.println("⚠️ Estoque crítico (≤ 5 unidades)");
        System.out.println("=" .repeat(70));
    }


    //getters e setters
    public Franquia getFranquia() {
        return this.franquia;
    }

    public void setFranquia(Franquia franquia) {
        this.franquia = franquia;
    }

    @Override
    public String toString() {
        return "Gerente: " + getNome() + " | Franquia Gerenciada: " + franquia.getNome();
    }

    @Override
    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.GERENTE;
    }
}
