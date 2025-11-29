package ch.fhnw.chatclient;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

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
    private Timeline autoRefresh;

    @FXML
    public void initialize() {

        usernameLabel.setText(state.getUsername());

        sendButton.setOnAction(e -> handleSend());
        refreshButton.setOnAction(e -> handleRefresh());
        logoutButton.setOnAction(e -> handleLogout());
        addContactButton.setOnAction(e -> handleAddContact());

        // ----------------------------
        // ENTER sends message
        // ----------------------------
        messageField.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER -> handleSend();
            }
        });

        contactList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> showConversation(newVal)
        );

        startAutoRefresh();
    }

    // -------------------------------------------------------
    // AUTO REFRESH
    // -------------------------------------------------------
    private void startAutoRefresh() {
        autoRefresh = new Timeline(new KeyFrame(
                Duration.seconds(3),
                e -> handleRefresh()
        ));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    private void stopAutoRefresh() {
        if (autoRefresh != null) autoRefresh.stop();
    }

    // -------------------------------------------------------
    // Add Contact
    // -------------------------------------------------------
    private void handleAddContact() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Contact");
        dialog.setHeaderText("Enter username");
        dialog.setContentText("Username:");

        dialog.showAndWait().ifPresent(name -> {
            name = name.trim();
            if (name.isEmpty()) return;

            if (!contactList.getItems().contains(name)) {
                contactList.getItems().add(name);
                conversations.putIfAbsent(name, new ArrayList<>());
            }

            contactList.getSelectionModel().select(name);
        });
    }

    // -------------------------------------------------------
    // Send message
    // -------------------------------------------------------
    private void handleSend() {
        String contact = contactList.getSelectionModel().getSelectedItem();
        String msg = messageField.getText();
        String token = state.getToken();

        if (contact == null) {
            showSystem("⚠ Select/add a contact first.");
            return;
        }

        if (msg.isEmpty()) {
            showSystem("⚠ Message cannot be empty.");
            return;
        }

        JSONObject online = api.isOnline(token, contact);
        if (!online.optBoolean("online", false)) {
            showSystem("⚠ User is offline.");
            return;
        }

        JSONObject response = api.sendMessage(token, contact, msg);

        if (response.optBoolean("send", false)) {
            addMessage(contact, "You → " + contact + ": " + msg);
            showConversation(contact);
            messageField.clear();
        } else {
            showSystem("⚠ Error: " + response);
        }
    }

    // -------------------------------------------------------
    // Refresh / Poll
    // -------------------------------------------------------
    private void handleRefresh() {
        JSONObject response = api.pollMessages(state.getToken());

        if (!response.has("messages")) {
            showSystem("⚠ Error polling: " + response);
            return;
        }

        JSONArray msgs = response.getJSONArray("messages");

        for (int i = 0; i < msgs.length(); i++) {
            JSONObject m = msgs.getJSONObject(i);

            String sender = m.getString("username");
            String text = m.getString("message");

            addMessage(sender, sender + " → you: " + text);

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

    // -------------------------------------------------------
    // Logout
    // -------------------------------------------------------
    private void handleLogout() {
        stopAutoRefresh();

        api.logout(state.getToken());

        state.setToken(null);
        state.setUsername(null);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Scene scene = new Scene(loader.load(), 400, 600);

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            showSystem("⚠ Error logging out: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------
    private void addMessage(String contact, String msg) {
        conversations.putIfAbsent(contact, new ArrayList<>());
        conversations.get(contact).add(msg);
    }

    private void showConversation(String contact) {
        messageList.getItems().clear();

        if (contact != null && conversations.containsKey(contact)) {
            messageList.getItems().addAll(conversations.get(contact));
        }
    }

    private void showSystem(String msg) {
        messageList.getItems().add(msg);
    }
}
