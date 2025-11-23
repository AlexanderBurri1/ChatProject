package ch.fhnw.chatclient;

public class TestRegister {
    public static void main(String[] args) {
        ApiClient api = new ApiClient();

        String username = "alex";     // change as needed
        String password = "1234";

        String response = api.register(username, password);
        System.out.println("Register response: " + response);
    }
}
