// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.enums;

public enum TipoUsuario {
    DONO("D"), GERENTE("G"), VENDEDOR("V");
    private final String prefixo;
    TipoUsuario(String prefixo) {
        this.prefixo = prefixo;
    }
    public String getPrefixo() {
        return prefixo;
    }
}