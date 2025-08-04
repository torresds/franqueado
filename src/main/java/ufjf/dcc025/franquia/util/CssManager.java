package ufjf.dcc025.franquia.util;

public class CssManager {
    public static String getStyles() {
        return """
            .root {
                -fx-font-family: 'Arial';
                -fx-background-color: #f4f4f4;
            }
            .label {
                -fx-font-size: 14px;
                -fx-text-fill: #333;
            }
            .button {
                -fx-background-color: #0078d7;
                -fx-text-fill: white;   
                -fx-font-size: 14px;
                -fx-padding: 8 16;
                -fx-background-radius: 5;
            }
            .button:hover {
                -fx-background-color: #005ba1;
            }
            .text-field, .password-field {
                -fx-font-size: 14px;
                -fx-padding: 8;
                -fx-background-color: white;
                -fx-border-color: #ccc;
                -fx-border-radius: 5;
            }
            .text-area {
                -fx-font-size: 14px;
                -fx-background-color: white;
                -fx-border-color: #ccc;
                -fx-border-radius: 5;
            }
        """;
    }
}