package ch.fhnw.chatclient;

public class TestPing {
    public static void main(String[] args) {
        ApiClient api = new ApiClient();
        String response = api.ping();
        System.out.println("Ping response: " + response);
    }
}
