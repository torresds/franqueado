package ufjf.dcc025.franquia.service;

import ufjf.dcc025.franquia.exception.UsuarioInvalidoException;
import ufjf.dcc025.franquia.model.usuarios.Dono;
import ufjf.dcc025.franquia.model.usuarios.Gerente;
import ufjf.dcc025.franquia.model.usuarios.Usuario;
import ufjf.dcc025.franquia.model.usuarios.Vendedor;
import ufjf.dcc025.franquia.persistence.EntityRepository;

import java.util.Optional;

/**
 * Serviço responsável pela lógica de autenticação de usuários.
 */
public class AuthenticationService {

    private final EntityRepository<Dono> donoRepo;
    private final EntityRepository<Gerente> gerenteRepo;
    private final EntityRepository<Vendedor> vendedorRepo;

    public AuthenticationService(EntityRepository<Dono> donoRepo, EntityRepository<Gerente> gerenteRepo, EntityRepository<Vendedor> vendedorRepo) {
        this.donoRepo = donoRepo;
        this.gerenteRepo = gerenteRepo;
        this.vendedorRepo = vendedorRepo;
    }

    /**
     * Autentica um usuário com base no login (email ou CPF) e senha.
     * Tenta autenticar como Dono, depois Gerente e por último Vendedor.
     * @param login O email ou CPF do usuário.
     * @param senha A senha do usuário.
     * @return O objeto Usuario se a autenticação for bem-sucedida.
     * @throws UsuarioInvalidoException se as credenciais forem inválidas.
     */
    public Usuario authenticate(String login, String senha) throws UsuarioInvalidoException {
        Optional<Usuario> usuario = donoRepo.findAll().stream()
                .filter(u -> (u.getEmail().equalsIgnoreCase(login) || u.getCpf().equals(login)) && u.getSenha().equals(senha))
                .map(u -> (Usuario) u)
                .findFirst();

        if (usuario.isPresent()) {
            return usuario.get();
        }

        usuario = gerenteRepo.findAll().stream()
                .filter(u -> (u.getEmail().equalsIgnoreCase(login) || u.getCpf().equals(login)) && u.getSenha().equals(senha))
                .map(u -> (Usuario) u)
                .findFirst();

        if (usuario.isPresent()) {
            return usuario.get();
        }

        usuario = vendedorRepo.findAll().stream()
                .filter(u -> (u.getEmail().equalsIgnoreCase(login) || u.getCpf().equals(login)) && u.getSenha().equals(senha))
                .map(u -> (Usuario) u)
                .findFirst();

        return usuario.orElseThrow(UsuarioInvalidoException::new);
    }
}