package com.yourproject.youename.android;


import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {

    private static SocketManager instance;
    private  static Socket socket;

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

            // ✅ Add signaling listeners here

            socket.on("webrtcOffer", args -> {
                try {
                    JSONObject data = (JSONObject) args[0];
                    JSONObject offer = data.getJSONObject("offer");
                    String sender = data.getString("sender");

                    SessionDescription sdpOffer = new SessionDescription(
                        SessionDescription.Type.OFFER, offer.getString("sdp"));

                    // Pass to your PeerConnection logic
                    // Example (make sure these methods exist):
                    // WebRTCManager.getInstance().onReceivedOffer(sdpOffer, sender);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

            socket.on("webrtcAnswer", args -> {
                try {
                    JSONObject data = (JSONObject) args[0];
                    JSONObject answer = data.getJSONObject("answer");

                    SessionDescription sdpAnswer = new SessionDescription(
                        SessionDescription.Type.ANSWER, answer.getString("sdp"));

                    // Example usage:
                    // WebRTCManager.getInstance().onReceivedAnswer(sdpAnswer);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

            socket.on("iceCandidate", args -> {
                try {
                    JSONObject data = (JSONObject) args[0];
                    JSONObject candidateJson = data.getJSONObject("candidate");

                    IceCandidate candidate = new IceCandidate(
                        candidateJson.getString("sdpMid"),
                        candidateJson.getInt("sdpMLineIndex"),
                        candidateJson.getString("candidate"));

                    // Example usage:
                    // WebRTCManager.getInstance().onRemoteIceCandidate(candidate);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static Socket getSocket(){
        return socket;
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


    public void sendPlayerStopped() {
        socket.emit("playerStopped");
    }
    public void connectVoiceChannel(String channelCode,String nickname) {
        try {
            JSONObject data = new JSONObject();
            data.put("voiceChannelCode", channelCode);
            data.put("playerName",nickname);
            socket.emit("joinVoiceChannel", data);
            Log.d("SocketManager", "Voice channel " + channelCode + " connected.");
        } catch (JSONException e) {
            Log.e("SocketManager", "Error connecting to voice channel", e);
        }
    }

    public void createVoiceChannel(String channelCode,String nickname,String mapId) {
        try {
            JSONObject data = new JSONObject();
            data.put("voiceChannelCode", channelCode);
            data.put("playerName",nickname);
            data.put("mapId",mapId);
            socket.emit("createVoiceChannel", data);
            Log.d("SocketManager", "Voice channel " + channelCode + " connected.");
        } catch (JSONException e) {
            Log.e("SocketManager", "Error connecting to voice channel", e);
        }
    }

    public void onVoiceChannelCreation(Emitter.Listener listener){
        socket.on("voiceChannelCreated",listener);
    }

    public void disconnectVoiceChannel(String mapId,String voiceChannelCode,String playerName) {
        try {
            JSONObject data = new JSONObject();
            data.put("mapId",mapId);
            data.put("voiceChannelCode",voiceChannelCode);
            data.put("playerName",playerName);
            socket.emit("leaveVoiceChannel",data);
        } catch (Exception e) {
            Log.e("SocketManager", "Error disconnecting from voice channel", e);
        }
    }

    public void onPlayerLeftChannel(Emitter.Listener listener) {
        socket.on("playerLeftChannel", listener);
    }


    public void onCurrentPlayersInVoiceChannel(Emitter.Listener listener){
        socket.on("currentPlayersInVoiceChannel",listener);
    }
    public void onPlayerJoinedVoiceChannel(Emitter.Listener listener){
        socket.on("playerJoinedVoiceChannel",listener);
    }
    public void joinVoiceChannel(String channelCode,String nickname,String mapId) {
        try {
            JSONObject data = new JSONObject();
            data.put("voiceChannelCode", channelCode);
            data.put("playerName",nickname);
            data.put("mapId",mapId);
            socket.emit("joinVoiceChannel", data);
            Log.d("SocketManager", "Player join Voice channel " + channelCode + " connected.");
        } catch (JSONException e) {
            Log.e("SocketManager", "Error connecting to voice channel", e);
        }
    }





    public void onError(Emitter.Listener listener) {
        socket.on("error", listener);
    }
}
