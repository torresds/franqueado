package ufjf.dcc025.franquia.modelo;
import java.util.regex.Pattern;

import ufjf.dcc025.franquia.enums.TipoUsuario;


public abstract class Usuario implements Identifiable {
    private final String id;
    private String nome;
    private String cpf;
    private String email;
    private String senha;

   

    public Usuario(String nome, String cpf, String email, String senha) {
        setNome(nome);
        setCpf(cpf);
        setEmail(email);
        setSenha(senha);
        setId();
    }

    // Métodos de validação (exigidos pelo PDF - Seção 2: "validação dos dados de entrada")
    protected void setNome(String nome) {
        if (nome == null ) {
            throw new IllegalArgumentException("Nome não pode ser vazio.");
        }
        this.nome = nome.trim();
    }

    protected void setCpf(String cpf) {
        if (!validarCPF(cpf)) {
            throw new IllegalArgumentException("CPF inválido.");
        }
        this.cpf = cpf.replaceAll("[^0-9]", "");
    }

    protected void setEmail(String email) {
        if (email == null || !Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z]{2,})+$").matcher(email).matches()) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        this.email = email;
    }

    protected void setSenha(String senha) {
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres.");
        }
        this.senha = senha;
    }

    protected abstract void setId();
    
    protected void setId(String Id) {
    	this.id = Id;
    }

    private boolean validarCPF(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", "");
        return cpf.length() == 11;
    }

    //Getters
    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getId(){
        return id;
    }

    public abstract TipoUsuario getTipoUsuario();
    
    public boolean autenticar(String email, String senha) {
       return this.email.equalsIgnoreCase(email) && this.senha.equals(senha);
    }
}