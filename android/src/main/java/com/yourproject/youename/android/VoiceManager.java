package com.yourproject.youename.android;


import android.util.Log;
import org.webrtc.*;
import io.socket.client.Socket;
import org.json.JSONObject;


import java.util.HashMap;

public class VoiceManager {

    private static final String TAG = "VoiceManager";
    private final Socket socket;

    private final String senderId;
    private final String voiceChannelCode;

    private PeerConnectionFactory factory;
    private HashMap<String, PeerConnection> peerConnections = new HashMap<>();

    private PeerConnection.Observer peerObserver = new PeerConnection.Observer() {
        @Override
        public void onIceCandidate(IceCandidate candidate) {
            sendIceCandidate(candidate);
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

        }

        @Override public void onAddStream(MediaStream mediaStream) {
            if (mediaStream.audioTracks.size() > 0) {
                mediaStream.audioTracks.get(0).setEnabled(true); // Enable the incoming audio track
                Log.d(TAG, "Incoming audio stream added.");
            }
        }
        @Override public void onDataChannel(DataChannel dataChannel) {}
        @Override public void onIceConnectionReceivingChange(boolean b) {}
        @Override public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {}
        @Override public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}
        @Override public void onSignalingChange(PeerConnection.SignalingState signalingState) {}
        @Override public void onRemoveStream(MediaStream mediaStream) {}
        @Override public void onRenegotiationNeeded() {}
        @Override public void onTrack(RtpTransceiver transceiver) {}
    };

    public VoiceManager(Socket socket, String senderId, String voiceChannelCode, android.content.Context context) {
        this.socket = socket;
        this.senderId = senderId;
        this.voiceChannelCode = voiceChannelCode;
        initPeerFactory(context);
        registerSocketEvents();
    }

    private void initPeerFactory(android.content.Context context) {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
        );
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory();
    }
    public PeerConnection createPeerConnection(String targetId) {
        PeerConnection.RTCConfiguration config = new PeerConnection.RTCConfiguration(new java.util.ArrayList<>());
        config.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN; // Ensure Unified Plan is explicitly set

        PeerConnection peer = factory.createPeerConnection(config, peerObserver);

        // Setup audio
        MediaConstraints audioConstraints = new MediaConstraints();
        AudioSource audioSource = factory.createAudioSource(audioConstraints);
        AudioTrack localAudioTrack = factory.createAudioTrack("ARDAMSa0", audioSource);

        // Add the audio track to the peer connection
        peer.addTrack(localAudioTrack);

        peerConnections.put(targetId, peer);
        return peer;
    }


    public void createOffer(String targetId) {
        PeerConnection peer = peerConnections.get(targetId);
        if (peer == null) peer = createPeerConnection(targetId);

        final PeerConnection finalPeer = peer;
        peer.createOffer(new SdpObserverAdapter() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                finalPeer.setLocalDescription(new SdpObserverAdapter(), sdp);
                sendOffer(sdp, targetId);
            }
        }, new MediaConstraints());
    }

    public void createAnswer(String targetId) {
        PeerConnection peer = peerConnections.get(targetId);
        peer.createAnswer(new SdpObserverAdapter() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                peer.setLocalDescription(new SdpObserverAdapter(), sdp);
                sendAnswer(sdp, targetId);
            }
        }, new MediaConstraints());
    }

    public void handleOffer(String from, JSONObject offerJson) {
        PeerConnection peer = createPeerConnection(from);
        SessionDescription offer = new SessionDescription(
            SessionDescription.Type.OFFER,
            offerJson.optString("sdp")
        );
        peer.setRemoteDescription(new SdpObserverAdapter(), offer);
        createAnswer(from);
    }

    public void handleAnswer(String from, JSONObject answerJson) {
        PeerConnection peer = peerConnections.get(from);
        SessionDescription answer = new SessionDescription(
            SessionDescription.Type.ANSWER,
            answerJson.optString("sdp")
        );
        peer.setRemoteDescription(new SdpObserverAdapter(), answer);
    }

    public void handleCandidate(String from, JSONObject candidateJson) {
        PeerConnection peer = peerConnections.get(from);
        IceCandidate candidate = new IceCandidate(
            candidateJson.optString("sdpMid"),
            candidateJson.optInt("sdpMLineIndex"),
            candidateJson.optString("candidate")
        );
        peer.addIceCandidate(candidate);
    }

    private void sendOffer(SessionDescription sdp, String to) {
        try {
            JSONObject offer = new JSONObject();
            offer.put("type", sdp.type.canonicalForm());
            offer.put("sdp", sdp.description);

            JSONObject payload = new JSONObject();
            payload.put("voiceChannelCode", voiceChannelCode);
            payload.put("sender", senderId);
            payload.put("offer", offer);

            socket.emit("webrtcOffer", payload);
        } catch (Exception e) {
            Log.e(TAG, "Error sending offer", e);
        }
    }

    private void sendAnswer(SessionDescription sdp, String to) {
        try {
            JSONObject answer = new JSONObject();
            answer.put("type", sdp.type.canonicalForm());
            answer.put("sdp", sdp.description);

            JSONObject payload = new JSONObject();
            payload.put("voiceChannelCode", voiceChannelCode);
            payload.put("sender", senderId);
            payload.put("answer", answer);

            socket.emit("webrtcAnswer", payload);
        } catch (Exception e) {
            Log.e(TAG, "Error sending answer", e);
        }
    }

    private void sendIceCandidate(IceCandidate candidate) {
        try {
            JSONObject ice = new JSONObject();
            ice.put("sdpMid", candidate.sdpMid);
            ice.put("sdpMLineIndex", candidate.sdpMLineIndex);
            ice.put("candidate", candidate.sdp);

            JSONObject payload = new JSONObject();
            payload.put("voiceChannelCode", voiceChannelCode);
            payload.put("sender", senderId);
            payload.put("candidate", ice);

            socket.emit("iceCandidate", payload);
        } catch (Exception e) {
            Log.e(TAG, "Error sending ICE candidate", e);
        }
    }

    private void registerSocketEvents() {
        socket.on("webrtcOffer", args -> {
            JSONObject data = (JSONObject) args[0];
            String from = data.optString("sender");
            handleOffer(from, data.optJSONObject("offer"));
        });

        socket.on("webrtcAnswer", args -> {
            JSONObject data = (JSONObject) args[0];
            String from = data.optString("sender");
            handleAnswer(from, data.optJSONObject("answer"));
        });

        socket.on("iceCandidate", args -> {
            JSONObject data = (JSONObject) args[0];
            String from = data.optString("sender");
            handleCandidate(from, data.optJSONObject("candidate"));
        });
    }

    public PeerConnection getPeerConnection(String targetId) {
        return peerConnections.get(targetId);
    }

    public void removePeerConnection(String targetId) {
        PeerConnection peer = peerConnections.get(targetId);
        if (peer != null) {
            peer.close(); // Close the peer connection
            peerConnections.remove(targetId); // Remove it from the map
            Log.d(TAG, "PeerConnection removed for targetId: " + targetId);
        }
    }


}
