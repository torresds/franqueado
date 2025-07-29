package ufjf.dcc025.franquia.util;

public class CssManager {
    public static String getStyles() {
        return """
        .login-view { -fx-background-color: linear-gradient(to bottom right, #e0f7fa, #b2ebf2); }
        .form-container { -fx-background-color: white; -fx-padding: 40px; -fx-border-radius: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 5); }
        .login-field { -fx-font-size: 16px; -fx-pref-height: 50px; -fx-border-color: #cfd8dc; -fx-border-radius: 8px; -fx-background-radius: 8px; }
        .login-button { -fx-pref-width: 400px; -fx-pref-height: 50px; -fx-background-color: #00796b; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand; }
        .login-button:hover { -fx-background-color: #004d40; }
        """;
    }
}
