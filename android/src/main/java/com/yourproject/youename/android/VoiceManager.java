package com.yourproject.youename.android;


import android.content.Context;
import android.media.AudioManager;
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
            Log.d(TAG, "ICE Candidate created: " + candidate.sdp);
            sendIceCandidate(candidate);
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

        }

        @Override public void onAddStream(MediaStream mediaStream) {
            Log.d(TAG, "onAddStream triggered");

            if (mediaStream.audioTracks.size() > 0) {

                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0); // Enable the incoming audio track
                remoteAudioTrack.setEnabled(true);
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
        @Override public void onTrack(RtpTransceiver transceiver) {
            Log.d(TAG, "onTrack triggered");

            MediaStreamTrack track = transceiver.getReceiver().track();
            if (track instanceof AudioTrack) {
                AudioTrack remoteAudioTrack = (AudioTrack) track;
                remoteAudioTrack.setEnabled(true);
                // Optionally: log or attach a sink if required
                Log.d("Voice", "Remote audio track received and enabled.");
            }
        }
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
            PeerConnectionFactory.InitializationOptions.builder(context).setEnableInternalTracer(true)
                .createInitializationOptions()
        );
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory();

        Log.d(TAG, "PeerConnectionFactory initialized: " + (factory != null));
    }
    public PeerConnection createPeerConnection(String targetId) {
        Log.d(TAG, "Creating PeerConnection for targetId: " + targetId);

        PeerConnection.RTCConfiguration config = new PeerConnection.RTCConfiguration(new java.util.ArrayList<>());
        config.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN; // Ensure Unified Plan is explicitly set

        config.iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
//        config.iceServers.add(PeerConnection.IceServer.builder("turn:2409:40c1:3a:1486:f5b7:c573:cb80:bf4f:3478")
//            .setUsername("user1")
//            .setPassword("password1")
//            .createIceServer());

        PeerConnection peer = factory.createPeerConnection(config, peerObserver);

        if(peer == null){
            Log.e(TAG,"failed to create peer creation");
            return null;
        }

        // Setup audio
        MediaConstraints audioConstraints = new MediaConstraints();
        AudioSource audioSource = factory.createAudioSource(audioConstraints);
        AudioTrack localAudioTrack = factory.createAudioTrack("ARDAMSa0", audioSource);


        if (audioSource == null) {
            Log.e(TAG, "Failed to create AudioSource");
            return null;
        }

        if (localAudioTrack == null) {
            Log.e(TAG, "Failed to create AudioTrack");
            return null;
        }

        // Add the audio track to the peer connection
        peer.addTrack(localAudioTrack);

        peerConnections.put(targetId, peer);

        Log.d(TAG, "PeerConnection created and local audio track added.");
        return peer;
    }


    public void createOffer(String targetId) {
        Log.d(TAG, "Creating offer for target: " + targetId);

        PeerConnection peer = peerConnections.get(targetId);
        if (peer == null) peer = createPeerConnection(targetId);

        final PeerConnection finalPeer = peer;
        peer.createOffer(new SdpObserverAdapter() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                Log.d(TAG, "Offer created successfully.");

                finalPeer.setLocalDescription(new SdpObserverAdapter(), sdp);
                sendOffer(sdp, targetId);
            }
        }, new MediaConstraints());
    }

    public void createAnswer(String targetId) {
        Log.d(TAG, "Creating answer for target: " + targetId);

        PeerConnection peer = peerConnections.get(targetId);
        peer.createAnswer(new SdpObserverAdapter() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                Log.d(TAG, "Answer created successfully.");

                peer.setLocalDescription(new SdpObserverAdapter(), sdp);
                sendAnswer(sdp, targetId);
            }
        }, new MediaConstraints());
    }

    public void handleOffer(String from, JSONObject offerJson) {
        Log.d(TAG, "Received offer from: " + from);

        PeerConnection peer = createPeerConnection(from);
        SessionDescription offer = new SessionDescription(
            SessionDescription.Type.OFFER,
            offerJson.optString("sdp")
        );
        peer.setRemoteDescription(new SdpObserverAdapter(), offer);
        createAnswer(from);
    }

    public void handleAnswer(String from, JSONObject answerJson) {
        Log.d(TAG, "Received answer from: " + from);

        PeerConnection peer = peerConnections.get(from);
        SessionDescription answer = new SessionDescription(
            SessionDescription.Type.ANSWER,
            answerJson.optString("sdp")
        );
        peer.setRemoteDescription(new SdpObserverAdapter(), answer);
    }

    public void handleCandidate(String from, JSONObject candidateJson) {
        Log.d(TAG, "Received ICE candidate from: " + from);

        PeerConnection peer = peerConnections.get(from);
        if(peer ==null){
            Log.e(TAG,"Peer Connection is not found for:" + from);
            return;
        }
        IceCandidate candidate = new IceCandidate(
            candidateJson.optString("sdpMid"),
            candidateJson.optInt("sdpMLineIndex"),
            candidateJson.optString("candidate")
        );
        peer.addIceCandidate(candidate);
    }

    private void sendOffer(SessionDescription sdp, String to) {
        Log.d(TAG, "Sending offer to: " + to);

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
        Log.d(TAG, "Sending answer to: " + to);

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
        Log.d(TAG, "Sending ICE candidate: " + candidate.sdp);

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
            Log.d(TAG, "Received 'webrtcOffer' event.");

            String from = data.optString("sender");
            handleOffer(from, data.optJSONObject("offer"));
        });

        socket.on("webrtcAnswer", args -> {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "Received 'webrtcAnswer' event.");

            String from = data.optString("sender");
            handleAnswer(from, data.optJSONObject("answer"));
        });

        socket.on("iceCandidate", args -> {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "Received 'iceCandidate' event.");

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
