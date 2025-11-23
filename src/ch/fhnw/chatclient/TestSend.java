package ch.fhnw.chatclient;

public class TestSend {
    public static void main(String[] args) {
        AppState state = AppState.getInstance();
        ApiClient api = state.getApi();

        state.setToken("SOME_TOKEN"); // later real token
        String response = api.sendMessage(
                state.getToken(),
                "sunnu",          // recipient
                "Hello from ChatProject!"
        );

        System.out.println("Send response: " + response);
    }
}
