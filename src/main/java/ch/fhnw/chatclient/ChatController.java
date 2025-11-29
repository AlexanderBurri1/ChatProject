package ch.fhnw.chatclient;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.*;

public class ChatController {

    @FXML private Label usernameLabel;
    @FXML private ListView<String> messageList;
    @FXML private ListView<String> contactList;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Button refreshButton;
    @FXML private Button logoutButton;
    @FXML private Button addContactButton;

    private final AppState state = AppState.getInstance();
    private final ApiClient api = state.getApi();

    private final Map<String, List<String>> conversations = new HashMap<>();

    @FXML
    public void initialize() {

        usernameLabel.setText(state.getUsername());

        sendButton.setOnAction(e -> handleSend());
        refreshButton.setOnAction(e -> handleRefresh());
        logoutButton.setOnAction(e -> handleLogout());
        addContactButton.setOnAction(e -> handleAddContact());

        // When selecting a contact, show their messages
        contactList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> showConversation(newVal)
        );
    }

    // -----------------------------------
    // Add a new contact (dialog box)
    // -----------------------------------
    private void handleAddContact() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Contact");
        dialog.setHeaderText("Add someone you want to chat with");
        dialog.setContentText("Username:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String username = result.get().trim();

            if (username.isEmpty()) {
                showSystemMessage("⚠ Username cannot be empty.");
                return;
            }

            // Add contact
            if (!contactList.getItems().contains(username)) {
                contactList.getItems().add(username);
                conversations.putIfAbsent(username, new ArrayList<>());
            }

            contactList.getSelectionModel().select(username);
            showConversation(username);
        }
    }

    // -----------------------------------
    // Send message
    // -----------------------------------
    private void handleSend() {
        String contact = contactList.getSelectionModel().getSelectedItem();
        String message = messageField.getText();
        String token = state.getToken();

        if (contact == null) {
            showSystemMessage("⚠ Select or add a contact first.");
            return;
        }

        if (message.isEmpty()) {
            showSystemMessage("⚠ Message cannot be empty.");
            return;
        }

        // Check online
        String onlineResponse = api.isOnline(token, contact);
        if (!onlineResponse.contains("true")) {
            showSystemMessage("⚠ User is offline.");
            return;
        }

        // Send
        String response = api.sendMessage(token, contact, message);
        if (response.contains("\"send\":true")) {
            addMessage(contact, "You → " + contact + ": " + message);
            showConversation(contact);
            messageField.clear();
        } else {
            showSystemMessage("⚠ Error: " + response);
        }
    }

    // -----------------------------------
    // Refresh messages
    // -----------------------------------
    private void handleRefresh() {
        String token = state.getToken();
        String response = api.pollMessages(token);

        if (!response.contains("messages")) {
            showSystemMessage("⚠ Error polling messages: " + response);
            return;
        }

        String[] parts = response.split("\\{");

        for (String p : parts) {
            if (p.contains("username") && p.contains("message")) {

                String sender = extractValue(p, "username");
                String msg = extractValue(p, "message");

                addMessage(sender, sender + " → you: " + msg);

                if (!contactList.getItems().contains(sender)) {
                    contactList.getItems().add(sender);
                }

                if (contactList.getSelectionModel().isEmpty()) {
                    contactList.getSelectionModel().select(sender);
                }

                if (contactList.getSelectionModel().getSelectedItem().equals(sender)) {
                    showConversation(sender);
                }
            }
        }
    }

    // -----------------------------------
    // Logout
    // -----------------------------------
    private void handleLogout() {
        String token = state.getToken();
        api.logout(token);

        state.setToken(null);
        state.setUsername(null);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Scene scene = new Scene(loader.load(), 400, 600);

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            showSystemMessage("⚠ Error logging out: " + e.getMessage());
        }
    }

    // -----------------------------------
    // Helpers
    // -----------------------------------
    private void addMessage(String contact, String msg) {
        conversations.putIfAbsent(contact, new ArrayList<>());
        conversations.get(contact).add(msg);
    }

    private void showConversation(String contact) {
        messageList.getItems().clear();

        if (contact == null || !conversations.containsKey(contact)) return;

        messageList.getItems().addAll(conversations.get(contact));
    }

    private void showSystemMessage(String msg) {
        messageList.getItems().add(msg);
    }

    private String extractValue(String text, String key) {
        int start = text.indexOf(key) + key.length() + 3;
        int end = text.indexOf("\"", start);
        if (start < 0 || end < start) return "";
        return text.substring(start, end);
    }
}
