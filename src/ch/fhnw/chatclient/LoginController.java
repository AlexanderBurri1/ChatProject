package ch.fhnw.chatclient;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class LoginController {

    @FXML private TextField serverField;
    @FXML private TextField portField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;

    private final AppState state = AppState.getInstance();
    private final ApiClient api = state.getApi();

    @FXML
    public void initialize() {
        // Pre-fill defaults
        serverField.setText("http://localhost");
        portField.setText("50001");

        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> handleRegister());
    }

    private void handleLogin() {
        configureApi();

        String username = usernameField.getText();
        String password = passwordField.getText();

        String response = api.login(username, password);

        if (response.contains("token")) {
            String token = extractValue(response, "token");

            state.setUsername(username);
            state.setToken(token);

            statusLabel.setText("Login successful!");
            openChatWindow();

        } else {
            statusLabel.setText(response);
        }
    }

    private void handleRegister() {
        configureApi();

        String username = usernameField.getText();
        String password = passwordField.getText();

        String response = api.register(username, password);

        if (response.contains("username")) {
            statusLabel.setText("User registered! You can now login.");
        } else {
            statusLabel.setText(response);
        }
    }

    private void configureApi() {
        String server = serverField.getText();
        int port = Integer.parseInt(portField.getText());

        api.setServer(server, port);
    }

    // Simple JSON extraction for key:value pairs
    private String extractValue(String json, String key) {
        int start = json.indexOf(key) + key.length() + 3;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    // Load the chat screen (Step 11)
    private void openChatWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("chat.fxml"));
            Scene scene = new Scene(loader.load(), 400, 600);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            statusLabel.setText("Error opening chat: " + e.getMessage());
        }
    }
}
