package ufjf.dcc025.franquia.service;

import ufjf.dcc025.franquia.model.franquia.Franquia;
import ufjf.dcc025.franquia.model.usuarios.Dono;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.persistence.EntityRepository;
import ufjf.dcc025.franquia.exception.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DonoService {
    private final Dono dono;
    private final EntityRepository<Franquia> franquiasRepository;
    private final EntityRepository<Gerente> gerentesRepository;
    private final EntityRepository<Vendedor> vendedoresRepository;

    public DonoService(Dono dono, EntityRepository<Franquia> franquiasRepository, 
                       EntityRepository<Gerente> gerentesRepository, EntityRepository<Vendedor> vendedoresRepository) {
        this.dono = dono;
        this.franquiasRepository = franquiasRepository;
        this.gerentesRepository = gerentesRepository;
        this.vendedoresRepository = vendedoresRepository;
    }

    // Gerenciamento de Franquias
    public List<Franquia> checarFranquias() {
        List<Franquia> franquiasSemGerente = new ArrayList<>();
        for (Franquia franquia : franquiasRepository.findAll()) {
            if (franquia.getGerente() == null) {
                franquiasSemGerente.add(franquia);
            }
        }
        return franquiasSemGerente;
    }

    public Franquia cadastrarFranquia(String nome, String endereco, String gerenteId) {
        Gerente gerente = gerentesRepository.findById(gerenteId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(gerenteId));
        if (gerente.getFranquia() != null) {
            throw new DadosInvalidosException("Gerente já está associado a uma franquia.");
        }
        Franquia novaFranquia = new Franquia(nome, endereco, gerenteId, gerentesRepository);
        gerente.setFranquia(novaFranquia);
        franquiasRepository.upsert(novaFranquia);
        franquiasRepository.saveAllAsync();
        gerentesRepository.upsert(gerente);
        gerentesRepository.saveAllAsync();
        return novaFranquia;
    }

    public void removerFranquia(String idFranquia) {
        franquiasRepository.findById(idFranquia)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(idFranquia));
        franquiasRepository.delete(idFranquia);
        franquiasRepository.saveAllAsync();
    }

    public List<Franquia> listarFranquias() {
        return franquiasRepository.findAll();
    }

    public void atualizarFranquia(String id, String nome, String endereco) {
        if (nome == null || nome.trim().isEmpty() || endereco == null || endereco.trim().isEmpty()) {
            throw new DadosInvalidosException("Nome e endereço não podem ser vazios.");
        }
        Franquia franquia = franquiasRepository.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(id));
        franquia.setNome(nome);
        franquia.setEndereco(endereco);
        franquiasRepository.upsert(franquia);
        franquiasRepository.saveAllAsync();
    }

    // Gerenciamento de Gerentes
    public Gerente cadastrarGerente(String nome, String cpf, String email, String senha) {
        Gerente novoGerente = new Gerente(nome, cpf, email, senha);
        gerentesRepository.upsert(novoGerente);
        gerentesRepository.saveAllAsync();
        return novoGerente;
    }

    public void removerGerente(String idGerente) {
        Gerente gerente = gerentesRepository.findById(idGerente)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(idGerente));
        Franquia franquia = gerente.getFranquia();
        if (franquia != null) {
            franquia.setGerente(null);
            franquiasRepository.upsert(franquia);
            franquiasRepository.saveAllAsync();
        }
        gerentesRepository.delete(idGerente);
        gerentesRepository.saveAllAsync();
    }

    public List<Gerente> listarGerentes() {
        return gerentesRepository.findAll();
    }

    public void atualizarGerente(String id, String nome, String cpf, String email, String senha, String franquiaId) {
        Gerente gerente = gerentesRepository.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(id));
        Franquia novaFranquia = franquiasRepository.findById(franquiaId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(franquiaId));
        if (!gerente.getFranquia().getId().equals(novaFranquia.getId())) {
            if (novaFranquia.getGerente() != null) {
                throw new DadosInvalidosException("Franquia já tem gerente.");
            }
            gerente.getFranquia().setGerente(null);
            franquiasRepository.upsert(gerente.getFranquia());
            novaFranquia.setGerente(gerente);
            franquiasRepository.upsert(novaFranquia);
        }
        gerente.setNome(nome);
        gerente.setCpf(cpf);
        gerente.setEmail(email);
        gerente.setSenha(senha);
        gerente.setFranquia(novaFranquia);
        gerentesRepository.upsert(gerente);
        gerentesRepository.saveAllAsync();
        franquiasRepository.saveAllAsync();
    }

    public void setGerenteFranquia(String franquiaId, String gerenteId) {
        Franquia franquia = franquiasRepository.findById(franquiaId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(franquiaId));
        Gerente gerente = gerentesRepository.findById(gerenteId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(gerenteId));
        if (franquia.getGerente() != null && !franquia.getGerente().getId().equals(gerenteId)) {
            throw new DadosInvalidosException("Franquia já tem gerente.");
        }
        franquia.setGerente(gerente);
        gerente.setFranquia(franquia);
        franquiasRepository.upsert(franquia);
        gerentesRepository.upsert(gerente);
        franquiasRepository.saveAllAsync();
        gerentesRepository.saveAllAsync();
    }

    // Gerenciamento de Desempenho
    public double calcularFaturamentoBruto() {
        return franquiasRepository.findAll().stream()
            .mapToDouble(Franquia::getReceita)
            .sum();
    }

    public int calcularTotalPedidos() {
        return franquiasRepository.findAll().stream()
            .mapToInt(Franquia::quantidadePedidos)
            .sum();
    }

    public double calcularTicketMedio() {
        int totalPedidos = calcularTotalPedidos();
        double faturamentoBruto = calcularFaturamentoBruto();
        return totalPedidos > 0 ? faturamentoBruto / totalPedidos : 0.0;
    }

    public List<String> rankingVendedores() {
        Map<String, Double> ranking = new HashMap<>();
        for (Franquia franquia : franquiasRepository.findAll()) {
            for (Vendedor vendedor : franquia.getVendedores()) {
                ranking.put(vendedor.getId(), vendedor.getTotalVendas());
            }
        }
        return ranking.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public List<String> rankingVendedoresPorFranquia(String franquiaId) {
        Franquia franquia = franquiasRepository.findById(franquiaId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(franquiaId));
        Map<String, Double> ranking = new HashMap<>();
        for (Vendedor vendedor : franquia.getVendedores()) {
            ranking.put(vendedor.getId(), vendedor.getTotalVendas());
        }
        return ranking.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public Map<String, Double> listarFranquiasPorDesempenho() {
        Map<String, Double> desempenho = new HashMap<>();
        for (Franquia franquia : franquiasRepository.findAll()) {
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

    //Getters
    
    public Dono getDono() {
        return dono;
    }
    public EntityRepository<Franquia> getFranquiaRepo() {
    	return franquiasRepository;
    }
    public EntityRepository<Gerente> getGerenteRepo() {
    	return gerentesRepository;
    }
    public EntityRepository<Vendedor> getVendedorRepo() {
    	return vendedoresRepository;
    }

}
