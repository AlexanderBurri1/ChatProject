package ch.fhnw.chatclient;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import org.json.JSONObject;

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
        serverField.setText("http://javaprojects.ch");
        portField.setText("50001");

        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> handleRegister());
    }

    private void configureApi() {
        String server = serverField.getText();
        int port = Integer.parseInt(portField.getText());
        api.setServer(server, port);
    }

    private void handleLogin() {
        configureApi();

        String username = usernameField.getText();
        String password = passwordField.getText();

        JSONObject response = api.login(username, password);

        if (response.has("token")) {
            state.setUsername(username);
            state.setToken(response.getString("token"));

            statusLabel.setText("Login successful!");
            openChatWindow();
        } else {
            statusLabel.setText(response.toString());
        }
    }

    private void handleRegister() {
        configureApi();

        String username = usernameField.getText();
        String password = passwordField.getText();

        JSONObject response = api.register(username, password);

        if (response.has("username")) {
            statusLabel.setText("User registered. You can now login.");
        } else {
            statusLabel.setText(response.toString());
        }
    }

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
