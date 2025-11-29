package ch.fhnw.chatclient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

    private String serverAddress = "http://javaprojects.ch";
    private int serverPort = 50001;

    public ApiClient() {}

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
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            sb.append(line);
        }

        in.close();
        return sb.toString();
    }

    private String sendPost(String path, JSONObject json) throws Exception {
        URL url = new URL(buildUrl(path));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            os.write(json.toString().getBytes());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            sb.append(line);
        }

        in.close();
        return sb.toString();
    }

    public JSONObject ping() {
        try { return new JSONObject(sendGet("/ping")); }
        catch (Exception e) { return new JSONObject().put("Error", e.getMessage()); }
    }

    public JSONObject login(String username, String password) {
        JSONObject req = new JSONObject()
                .put("username", username)
                .put("password", password);

        try { return new JSONObject(sendPost("/user/login", req)); }
        catch (Exception e) { return new JSONObject().put("Error", e.getMessage()); }
    }

    public JSONObject register(String username, String password) {
        JSONObject req = new JSONObject()
                .put("username", username)
                .put("password", password);

        try { return new JSONObject(sendPost("/user/register", req)); }
        catch (Exception e) { return new JSONObject().put("Error", e.getMessage()); }
    }

    public JSONObject logout(String token) {
        JSONObject req = new JSONObject().put("token", token);

        try { return new JSONObject(sendPost("/user/logout", req)); }
        catch (Exception e) { return new JSONObject().put("Error", e.getMessage()); }
    }

    public JSONObject isOnline(String token, String username) {
        JSONObject req = new JSONObject()
                .put("token", token)
                .put("username", username);

        try { return new JSONObject(sendPost("/user/online", req)); }
        catch (Exception e) { return new JSONObject().put("Error", e.getMessage()); }
    }

    public JSONObject sendMessage(String token, String recipient, String message) {
        JSONObject req = new JSONObject()
                .put("token", token)
                .put("username", recipient)
                .put("message", message);

        try { return new JSONObject(sendPost("/chat/send", req)); }
        catch (Exception e) { return new JSONObject().put("Error", e.getMessage()); }
    }

    public JSONObject pollMessages(String token) {
        JSONObject req = new JSONObject().put("token", token);

        try { return new JSONObject(sendPost("/chat/poll", req)); }
        catch (Exception e) { return new JSONObject().put("Error", e.getMessage()); }
    }
}
