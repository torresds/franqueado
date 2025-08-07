// Discentes: Ana (202465512B), Miguel (202465506B)

package ufjf.dcc025.franquia.util;

import java.net.URL;

public class CssManager {
    /**
     * Retorna a URL para o arquivo de folha de estilos principal da aplicação.
     * @return A URL do arquivo styles.css.
     */
    public static String getStylesheetURL() {
        URL resource = CssManager.class.getResource("/styles.css");
        if (resource == null) {
            System.err.println("Stylesheet não encontrado!");
            return "data:text/css,.root{-fx-font-size:14px;}";
        }
        return resource.toExternalForm();
    }
}