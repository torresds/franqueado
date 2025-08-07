// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.model.usuarios;

import java.util.UUID;
import java.util.regex.Pattern;
import ufjf.dcc025.franquia.enums.TipoUsuario;
import ufjf.dcc025.franquia.persistence.Identifiable;
import ufjf.dcc025.franquia.exception.*;

public abstract class Usuario implements Identifiable {
    private final String id;
    private String nome;
    private String cpf;
    private String email;
    private String senha;

    public Usuario(String nome, String cpf, String email, String senha) {
        this.id = UUID.randomUUID().toString();
        setNome(nome);
        setCpf(cpf);
        setEmail(email);
        setSenha(senha);
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DadosInvalidosException("Nome não pode ser vazio.");
        }
        this.nome = nome.trim();
    }

    public void setCpf(String cpf) {
        if (!validarCPF(cpf)) {
            throw new DadosInvalidosException("CPF inválido.");
        }
        this.cpf = cpf.replaceAll("[^0-9]", "");
    }

    public void setEmail(String email) {
        if (email == null || !Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z]{2,})+$").matcher(email).matches()) {
            throw new DadosInvalidosException("E-mail inválido.");
        }
        this.email = email;
    }

    public void setSenha(String senha) {
        if (senha == null || senha.length() < 6) {
            throw new DadosInvalidosException("Senha deve ter pelo menos 6 caracteres.");
        }
        this.senha = senha;
    }

    private boolean validarCPF(String cpf) {
        return true;
        /**
         *     cpf = cpf.replaceAll("[^0-9]", "");
         *         if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
         *             return false;
         *         }
         *         try {
         *             int soma = 0;
         *             for (int i = 0; i < 9; i++) {
         *                 soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
         *             }
         *             int primeiroDigito = 11 - (soma % 11);
         *             if (primeiroDigito >= 10) primeiroDigito = 0;
         *
         *             soma = 0;
         *             for (int i = 0; i < 10; i++) {
         *                 soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
         *             }
         *             int segundoDigito = 11 - (soma % 11);
         *             if (segundoDigito >= 10) segundoDigito = 0;
         *
         *             return Character.getNumericValue(cpf.charAt(9)) == primeiroDigito &&
         *                     Character.getNumericValue(cpf.charAt(10)) == segundoDigito;
         *         } catch (Exception e) {
         *             return false;
         *         }
         */
    }

    // Getters
    @Override
    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public abstract TipoUsuario getTipoUsuario();
}