package ufjf.dcc025.franquia.model.clientes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ufjf.dcc025.franquia.persistence.Identifiable;

/**
 * Classe que representa um cliente do sistema
 */
public class Cliente implements Identifiable {
    private String id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String endereco;
    private Map<String, List<String>> pedidosPorFranquia; // Mapeia ID da franquia para lista de IDs dos pedidos

    public Cliente(String nome, String cpf, String email, String telefone, String endereco) {
        setId();
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.pedidosPorFranquia = new HashMap<>();
    }

    public void adicionarPedido(String pedidoId, String franquiaId) {
        pedidosPorFranquia.computeIfAbsent(franquiaId, k -> new ArrayList<>());
        List<String> pedidosDaFranquia = pedidosPorFranquia.get(franquiaId);
        if (!pedidosDaFranquia.contains(pedidoId)) {
            pedidosDaFranquia.add(pedidoId);
        }
    }
  
    public List<String> getPedidosDaFranquia(String franquiaId) {
        List<String> pedidos = pedidosPorFranquia.get(franquiaId);
        return pedidos != null ? new ArrayList<>(pedidos) : new ArrayList<>();
    }

    public List<String> getTodosPedidosId() {
        List<String> todosPedidos = new ArrayList<>();
        for (List<String> pedidos : pedidosPorFranquia.values()) {
            todosPedidos.addAll(pedidos);
        }
        return todosPedidos;
    }

    public List<String> getFranquiasId() {
        return new ArrayList<>(pedidosPorFranquia.keySet());
    }

    public Map<String, List<String>> getListaCompleta() {
        Map<String, List<String>> copia = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : pedidosPorFranquia.entrySet()) {
            copia.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copia;
    }

    public int getTotalPedidosNaFranquia(String franquiaId) {
        List<String> pedidos = pedidosPorFranquia.get(franquiaId);
        return pedidos != null ? pedidos.size() : 0;
    }


    public int getTotalPedidos() {
        return pedidosPorFranquia.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    // Getters
    @Override
    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    // Setters
    private void setId() {
    	String id = "C" + cpf;
    	this.id = id;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public void setCpf(String cpf) {
    	this.cpf = cpf;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    @Override
    public String toString() {
        return String.format("Cliente: %s (ID: %s) | Pedidos: %d | Franquias visitadas: %d", 
                           nome, id, getTotalPedidos(), pedidosPorFranquia.keySet().size());
    }
}