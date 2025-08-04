package ufjf.dcc025.franquia.model.usuarios;

import ufjf.dcc025.franquia.enums.TipoUsuario;

public class Dono extends Usuario {

    public Dono(String nome, String cpf, String email, String senha) {
        super(nome, cpf, email, senha);
    }

    @Override
    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.DONO;
    }

    @Override
    public String toString() {
        return "Dono: " + getNome();
    }
}