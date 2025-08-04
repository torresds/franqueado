package ufjf.dcc025.franquia.controller;

import ufjf.dcc025.franquia.service.VendedorService;
import ufjf.dcc025.franquia.model.produtos.Produto;
import ufjf.dcc025.franquia.model.pedidos.Pedido;
import ufjf.dcc025.franquia.exception.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Scanner;

public class VendedorController {
    public final VendedorService vendedorService;

    public VendedorController(VendedorService vendedorService) {
        this.vendedorService = vendedorService;
    }

    public Map<Produto, Integer> criarPedido() {
        Map<Produto, Integer> produtos = new HashMap<>();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("Digite o código do produto (ou 'sair' para finalizar):");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("sair")) {
                    break;
                }
                Produto produto = vendedorService.getVendedor().getFranquia().buscarProduto(input);
                if (produto == null) {
                    throw new EntidadeNaoEncontradaException(input);
                }
                System.out.println("Digite a quantidade:");
                int quantidade = Integer.parseInt(scanner.nextLine());
                if (quantidade <= 0) {
                    throw new DadosInvalidosException("Quantidade deve ser maior que zero!");
                }
                produtos.put(produto, quantidade);
                System.out.println("Produto adicionado: " + produto.getNome());
            } catch (IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
        return produtos;
    }

    public void visualizarPedidos() {
        List<Pedido> pedidos = vendedorService.listaPedidos();
        System.out.println("🛒 LISTA DE PEDIDOS - VENDEDOR: " + vendedorService.getVendedor().getNome());
        System.out.println("=" .repeat(60));
        if (pedidos.isEmpty()) {
            System.out.println("❌ Nenhum pedido registrado.");
            return;
        }
        for (Pedido pedido : pedidos) {
            System.out.printf("🛒 %s | %s | R$ %.2f | %s%n", 
                pedido.getId(), pedido.getCliente().getNome(), pedido.getValorTotal(), pedido.getStatus());
        }
        System.out.println("=" .repeat(60));
    }
}