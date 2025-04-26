package com.yourproject.youename.android;


import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {

    private static SocketManager instance;
    private Socket socket;

    private final String SERVER_URL = "https://twodmetaverse-qfs5.onrender.com"; // Replace this

    private SocketManager() {
        try {
            socket = IO.socket(SERVER_URL);
        } catch (URISyntaxException e) {
            Log.e("SocketManager", "Socket URI error: " + e.getMessage());
        }
    }

    public static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void createMap(String playerName, String mapId) {
        try {
            JSONObject data = new JSONObject();
            data.put("playerName", playerName);
            data.put("mapId", mapId);
            socket.emit("createMap", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void connectSocket() {
        if (socket != null && !socket.connected()) {
            socket.connect();
            Log.d("SocketManager", "Connecting...");
        }
    }

    public void disconnectSocket() {
        if (socket != null) {
            socket.disconnect();
            Log.d("SocketManager", "Disconnected");
        }
    }

    public void joinMap(String playerName, String mapId) {
        try {
            JSONObject data = new JSONObject();
            data.put("playerName", playerName);
            data.put("mapId", mapId);
            socket.emit("joinMap", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendPlayerMoved(float x, float y, int direction, boolean isMoving) {
        try {
            JSONObject data = new JSONObject();
            data.put("x", x);
            data.put("y", y);
            data.put("direction", direction);
            data.put("isMoving", isMoving);
            socket.emit("playerMoved", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onCurrentPlayers(Emitter.Listener listener) {
        socket.on("currentPlayers", listener);
    }

    public void onPlayerJoined(Emitter.Listener listener) {
        socket.on("playerJoined", listener);
    }


    public void onPlayerMoved(Emitter.Listener listener) {
        socket.on("playerMoved", listener);
    }

    public void onPlayerLeft(Emitter.Listener listener) {
        socket.on("playerLeft", listener);
    }
}
