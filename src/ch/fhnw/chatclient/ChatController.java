package ch.fhnw.chatclient;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ChatController {

    @FXML private Label usernameLabel;
    @FXML private ListView<String> messageList;
    @FXML private TextField recipientField;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Button refreshButton;
    @FXML private Button logoutButton;

    private final AppState state = AppState.getInstance();
    private final ApiClient api = state.getApi();

    @FXML
    public void initialize() {
        // Show the username in the top bar
        usernameLabel.setText(state.getUsername());

        sendButton.setOnAction(e -> handleSend());
        refreshButton.setOnAction(e -> handleRefresh());
        logoutButton.setOnAction(e -> handleLogout());
    }

    private void handleSend() {
        String recipient = recipientField.getText();
        String message = messageField.getText();
        String token = state.getToken();

        if (recipient.isEmpty() || message.isEmpty()) {
            messageList.getItems().add("⚠ Please fill in all fields.");
            return;
        }

        String response = api.sendMessage(token, recipient, message);

        if (response.contains("\"send\":true")) {
            messageList.getItems().add("You → " + recipient + ": " + message);
            messageField.clear();
        } else {
            messageList.getItems().add("⚠ Error sending message: " + response);
        }
    }

    private void handleRefresh() {
        String token = state.getToken();
        String response = api.pollMessages(token);

        if (response.contains("messages")) {
            // Very simple parsing because the JSON format is known
            String[] parts = response.split("\\{");

            for (String p : parts) {
                if (p.contains("username") && p.contains("message")) {
                    String sender = extractValue(p, "username");
                    String msg = extractValue(p, "message");

                    messageList.getItems().add(sender + " → you: " + msg);
                }
            }
        } else {
            messageList.getItems().add("⚠ Error polling messages: " + response);
        }
    }

    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Scene scene = new Scene(loader.load(), 400, 600);

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);

            state.setToken(null);
            state.setUsername(null);

        } catch (Exception e) {
            messageList.getItems().add("⚠ Error logging out: " + e.getMessage());
        }
    }

    // Helper to extract "value" from JSON fragments
    private String extractValue(String text, String key) {
        int start = text.indexOf(key) + key.length() + 3;
        int end = text.indexOf("\"", start);
        return text.substring(start, end);
    }
}
