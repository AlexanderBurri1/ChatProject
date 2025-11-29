package ch.fhnw.chatclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

    private String serverAddress = "http://javaprojects.ch";
    private int serverPort = 50001;

    public ApiClient() { }

    public void setServer(String address, int port) {
        this.serverAddress = address;
        this.serverPort = port;
    }

    private String buildUrl(String path) {
        return serverAddress + ":" + serverPort + path;
    }

    private String sendGet(String path) throws Exception {
        URL url = new URL(buildUrl(path));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }

        in.close();
        return response.toString();
    }

    private String sendPost(String path, String jsonBody) throws Exception {
        URL url = new URL(buildUrl(path));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }

        in.close();
        return response.toString();
    }

    // ----------------------------
    // API METHODS
    // ----------------------------

    public String ping() {
        try { return sendGet("/ping"); }
        catch (Exception e) { return "{\"Error\":\"" + e.getMessage() + "\"}"; }
    }

    public String pingAuth(String token) {
        String jsonBody = String.format("{ \"token\": \"%s\" }", token);
        try { return sendPost("/ping", jsonBody); }
        catch (Exception e) { return "{\"Error\":\"" + e.getMessage() + "\"}"; }
    }

    public String register(String username, String password) {
        String jsonBody = String.format(
                "{ \"username\": \"%s\", \"password\": \"%s\" }",
                username, password
        );

        try { return sendPost("/user/register", jsonBody); }
        catch (Exception e) { return "{\"Error\":\"" + e.getMessage() + "\"}"; }
    }

    public String login(String username, String password) {
        String jsonBody = String.format(
                "{ \"username\": \"%s\", \"password\": \"%s\" }",
                username, password
        );

        try { return sendPost("/user/login", jsonBody); }
        catch (Exception e) { return "{\"Error\":\"" + e.getMessage() + "\"}"; }
    }

    public String logout(String token) {
        String jsonBody = String.format("{ \"token\": \"%s\" }", token);

        try { return sendPost("/user/logout", jsonBody); }
        catch (Exception e) { return "{\"Error\":\"" + e.getMessage() + "\"}"; }
    }

    public String isOnline(String token, String username) {
        String jsonBody = String.format(
                "{ \"token\": \"%s\", \"username\": \"%s\" }",
                token, username
        );

        try { return sendPost("/user/online", jsonBody); }
        catch (Exception e) { return "{\"Error\":\"" + e.getMessage() + "\"}"; }
    }

    public String sendMessage(String token, String username, String message) {
        String jsonBody = String.format(
                "{ \"token\": \"%s\", \"username\": \"%s\", \"message\": \"%s\" }",
                token, username, message
        );

        try { return sendPost("/chat/send", jsonBody); }
        catch (Exception e) { return "{\"Error\":\"" + e.getMessage() + "\"}"; }
    }

    public String pollMessages(String token) {
        String jsonBody = String.format("{ \"token\": \"%s\" }", token);

        try { return sendPost("/chat/poll", jsonBody); }
        catch (Exception e) { return "{\"Error\":\"" + e.getMessage() + "\"}"; }
    }
}
