package ufjf.dcc025.franquia.modelo;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import ufjf.dcc025.franquia.enums.TipoUsuario;

public class Dono extends Usuario {
	private static int donoId = 1;
    
    public Dono(String nome, String cpf, String email, String senha) {
    	super(nome, cpf, email, senha);
    	donoId++;
    }

    //notificação caso uma unidade esteja sem gerente (feito)
    //botar função de atualizar informações de franquia e gerente, se necessário (feito)
    //botar função de ver desempenho de franquias o faturamento bruto, o número total de pedidos e o ticket médio (feito)
    //função de ver ranking de vendedores por franquia (feito, feito para todas as franquias também)
    //Adicionar logica de colocar a franquia no gerente (???) (nem eu lembro oq eu quis dizer com isso)
    

    //------------ GERENCIAMENTO DE FRANQUIAS ------------

    public List<Franquia> checarFranquias(EntityRepository<Franquia> todasFranquias){
    	List<Franquia> franquiasSemGerente = new ArrayList<>();
    	
    	for (Franquia franquia : todasFranquias) {
    		if (franquia.getGerente().equals(null)) {
    			franquiasSemGerente.add(franquia);
    		}
    	}
    	
    	if (franquiasSemGerente.isEmpty()) {
    		return null;
    	}
    	return franquiasSemGerente;
    }
    public Franquia cadastrarFranquia(String nome, String endereco, String gerenteId, EntityRepository<Gerente> gerentesValidos, EntityRepository<Franquia> franquiasValidas) {
        Franquia novaFranquia = new Franquia(nome, endereco, gerenteId, gerentesValidos);
        franquiasValidas.upsert(novaFranquia);
        return novaFranquia;
    }
    public void removerFranquia(EntityRepository<Franquia> franquias, String idFranquia) {
        franquias.delete(idFranquia);
    }
    public List<Franquia> listarFranquias(EntityRepository<Franquia> franquias) {
        return franquias.findAll();
    }

    public void atualizarFranquia(String id, String nome, String endereco, EntityRepository<Franquia> franquias) {
        Franquia franquia = franquias.findbyId(id);
        if (franquia != null) {
            franquia.setNome(nome);
            franquia.setEndereco(endereco);
            franquias.upsert(franquia);
        } else {
            throw new IllegalArgumentException("Franquia não encontrada.");
        }
    }
 
    //------------ GERENCIAMENTO DE USUÁRIOS ------------

    public Gerente cadastrarGerente(String nome, String cpf, String email, String senha, EntityRepository<Gerente> gerentesValidos, String franquiaId, EntityRepository<Franquia> franquiasValidas) {
        Gerente novoGerente = new Gerente(nome, cpf, email, senha, franquiaId, franquiasValidas);
        gerentesValidos.upsert(novoGerente);
        return novoGerente;
    }

    public void removerGerente(EntityRepository<Gerente> gerentes, String idGerente) {
    	Gerente gerente = gerentes.findById(idGerente);
    	gerente.getFranquia().setGerente(null);
        gerentes.delete(idGerente);
    }

    public List<Gerente> listarGerentes(EntityRepository<Gerente> gerentes) {
        return gerentes.findAll();
    }

    public void atualizarGerente(String id, String nome, String cpf, String email, String senha, String franquiaId, EntityRepository<Gerente> gerentes, EntityRepository<Franquia> franquias) {
    	Gerente gerente = gerentes.findbyId(id);
        if (gerente != null) {
        	Franquia novaFranquia = franquias.findById(franquiaId);
        	if (!gerente.getFranquia().equals(novaFranquia)) {
        		if (!novaFranquia.getGerente().equals(null)) {
        			throw new IllegalArgumentException("Franquia já tem Gerente.");
        		}
        		gerente.getFranquia().setGerente(null);
        		novaFranquia.setGerente(gerente);
        	}
        	
            gerente.setNome(nome);
            gerente.setCpf(cpf);
            gerente.setEmail(email);
            gerente.setSenha(senha);
            gerente.setFranquia(novaFranquia);
        	
        } else {
            throw new IllegalArgumentException("Gerente não encontrado.");
        }
    }

    public void SetGerenteFranquia(String franquiaId, String gerenteId, EntityRepository<Franquia> franquias, EntityRepository<Gerente> gerentes) {
        Franquia franquia = franquias.findbyId(franquiaId);
        Gerente gerente = gerentes.findbyId(gerenteId);
        if (franquia != null && gerente != null) {
            franquia.setGerente(gerente);
            franquias.upsert(franquia);
        } else {
            throw new IllegalArgumentException("Franquia ou Gerente não encontrado.");
        }
    }

    //------------ GERENCIAMENTO DE DESEMPENHO ------------
   
    public double calcularFaturamentoBruto(EntityRepository<Franquia> franquias) {
        double faturamentoTotal = 0.0;
        for (Franquia franquia : franquias.findAll()) {
            faturamentoTotal += franquia.getReceita();
        }
        return faturamentoTotal;
    }
   
    public int calcularTotalPedidos(EntityRepository<Franquia> franquias) {
        int totalPedidos = 0;
        for (Franquia franquia : franquias.findAll()) {
            totalPedidos += franquia.quantidadePedidos();
        }
        return totalPedidos;
    }
    
    public double calcularTicketMedio(EntityRepository<Franquia> franquias) {
        int totalPedidos = calcularTotalPedidos(franquias);
        double faturamentoBruto = calcularFaturamentoBruto(franquias);
        return totalPedidos > 0 ? faturamentoBruto / totalPedidos : 0.0;
    }

    public List<String> rankingVendedores(EntityRepository<Franquia> franquias, EntityRepository<Vendedor> vendedores) {
        Map<String, Double> ranking = new HashMap<>();
        for (Franquia franquia : franquias.findAll()) {
            for (Vendedor vendedor : franquia.getVendedores()) {
                ranking.put(vendedor.getId(), vendedor.getTotalVendas());
            }
        }
        return ranking.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<String> rankingVendedoresPorFranquia(EntityRepository<Franquia> franquias, EntityRepository<Vendedor> vendedores, String franquiaId) {
        Franquia franquia = franquias.findbyId(franquiaId);
        if (franquia == null) {
            throw new IllegalArgumentException("Franquia não encontrada.");
        }
        
        Map<String, Double> ranking = new HashMap<>();
        for (Vendedor vendedor : franquia.getVendedores()) {
            ranking.put(vendedor.getId(), vendedor.getTotalVendas());
        }
        
        return ranking.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());  

    }

    public Map<String,Double> listarFranquiasPorDesempenho(EntityRepository<Franquia> franquias) {
        Map<String, Double> desempenho = new HashMap<>();
        
        for (Franquia franquia : franquias.findAll()) {
            desempenho.put(franquia.getNome(), franquia.getReceita());
        }
        
        return desempenho.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue,
                    LinkedHashMap::new
                ));
    }
    
    @Override
    protected void setId() {
    	String id = "D" + donoId;
    	super.setId(id);
    }

    @Override
    public String toString() {
        return "Dono: " + getNome();
    }

    @Override
    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.DONO;
    }
}