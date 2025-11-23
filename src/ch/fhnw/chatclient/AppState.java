package ch.fhnw.chatclient;

public class AppState {

    private static AppState instance;

    private String username;
    private String token;

    private ApiClient apiClient;

    private AppState() {
        apiClient = new ApiClient();
    }

    public static AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    // -----------------------------
    // Username
    // -----------------------------
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    // -----------------------------
    // Token
    // -----------------------------
    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    // -----------------------------
    // ApiClient
    // -----------------------------
    public ApiClient getApi() {
        return apiClient;
    }
}
