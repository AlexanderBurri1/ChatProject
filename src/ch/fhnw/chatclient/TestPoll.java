package ch.fhnw.chatclient;

public class TestPoll {
    public static void main(String[] args) {
        AppState state = AppState.getInstance();
        ApiClient api = state.getApi();

        state.setToken("SOME_TOKEN");
        String response = api.pollMessages(state.getToken());

        System.out.println("Poll response: " + response);
    }
}
