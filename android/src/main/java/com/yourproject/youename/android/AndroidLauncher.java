package com.yourproject.youename.android;

import static com.yourproject.youename.MyGame.player;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.example.multiplayerui.MainActivity;
import com.yourproject.youename.DirectionController;
import com.yourproject.youename.MyGame;
import com.yourproject.youename.Player;
import com.yourproject.youename.R;
import com.yourproject.youename.RemotePlayer;
import com.yourproject.youename.SocketBridge;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;

public class AndroidLauncher extends AndroidApplication {
    private final HashMap<String, String> voiceParticipants = new HashMap<>();
    private AlertDialog activeVoiceDialog; // to manage the open popup
    private LinearLayout usersContainer;  // UI container inside dialog
    private boolean isVoicePopupOpen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String nickname = MainActivity.hashMap.get("nickname");
        Toast.makeText(this, "nickname: " + nickname, Toast.LENGTH_SHORT).show();

        String mapCode = getIntent().getStringExtra("MAP_CODE");
        SocketManager socketManager = SocketManager.getInstance();
        socketManager.connectSocket();

        // Only call one of these based on "creating"
        if (MainActivity.hashMap.containsKey("creating") &&
            MainActivity.hashMap.get("creating").equals("true")) {
            MainActivity.hashMap.put("mapId",mapCode);
            socketManager.createMap(nickname, mapCode);
        } else {
            MainActivity.hashMap.put("mapId",mapCode);
            socketManager.joinMap(nickname, mapCode);
        }

        // === SOCKET EVENTS ===

        socketManager.onCurrentPlayers(args -> {
            try {

                JSONArray players = (JSONArray) args[0];
                for (int i = 0; i < players.length(); i++) {

                    JSONObject p = players.getJSONObject(i);
                    String id = p.getString("playerId");
                    String name = p.getString("playerName");
                    float x,y;

                    if(name == nickname){
                         x = 300f;
                         y = -80f;
                    } else{
                         x = (float) p.getDouble("x");
                         y = (float) p.getDouble("y");
                    }


                    RemotePlayer rp = new RemotePlayer(x, y, name);
                    rp.isMoving = false; // ❌ Not moving initially

                    Log.d("SOCKET", "currentPlayer: " + name);
                    MyGame.remotePlayers.put(id, rp);
                }
            } catch (Exception e) {
                Log.d("SOCKET", "Error in onCurrentPlayers", e);
            }
        });

        socketManager.onPlayerJoined(args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String id = data.getString("playerId");
                String name = data.getString("playerName");
                float x = 300f;
                float y = -80f;

                Log.d("SOCKET", "playerJoined: " + name);
                MyGame.remotePlayers.put(id, new RemotePlayer(x, y, name));
            } catch (Exception e) {
                Log.e("SOCKET", "Error in onPlayerJoined", e);
            }
        });
        socketManager.onError(args -> {
            try {
                JSONObject errorData = (JSONObject) args[0];
                String message = errorData.optString("message", "Unknown error");
                Log.e("SOCKET", "Error received: " + message);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
                    finish(); // Exit or redirect based on your logic
                });
            } catch (Exception e) {
                Log.e("SOCKET", "Error parsing error event", e);
            }
        });

        socketManager.onVoiceChannelCreation(args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String voiceChannelCode = data.getString("voiceChannelCode");
                String name = data.getString("playerName");

                // Update the remote player label
                if (MyGame.remotePlayers != null) {
                    for (RemotePlayer rp : MyGame.remotePlayers.values()) {
                        if (rp.nickname.equals(name)) {
                            rp.setVoiceChannelCode(voiceChannelCode);
                            break;
                        }
                    }
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Voice Channel Code: " + voiceChannelCode, Toast.LENGTH_SHORT).show();
                });


            } catch (Exception e) {
                Log.e("SOCKET", "Error parsing error event", e);
            }
        });

//        socketManager.onPlayerLeftChannel(args -> {
//            try {
//                JSONObject data = (JSONObject) args[0];
//                String name = data.getString("playerName");
//
//                // Update the remote player label
//                if (MyGame.remotePlayers != null) {
//                    for (RemotePlayer rp : MyGame.remotePlayers.values()) {
//                        if (rp.nickname.equals(name)) {
//                            rp.setVoiceChannelCode(null);
//                            break;
//                        }
//                    }
//                }
//
//                runOnUiThread(() -> {
//                    Toast.makeText(this, "Voice Channel Code Remove: " , Toast.LENGTH_SHORT).show();
//                });
//            } catch (Exception e){
//                Log.e("SOCKET","Error in Player left voice channel");
//            }
//        });

        socketManager.onPlayerLeftChannel(args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String name = data.getString("playerName");

                // Remove from voiceParticipants map
                String playerKeyToRemove = null;
                for (String key : voiceParticipants.keySet()) {
                    if (voiceParticipants.get(key).equals(name)) {
                        playerKeyToRemove = key;
                        break;
                    }
                }
                if (playerKeyToRemove != null) {
                    voiceParticipants.remove(playerKeyToRemove);
                }

                // Update UI
                runOnUiThread(() -> {
                    if (activeVoiceDialog != null && activeVoiceDialog.isShowing()) {
                        removeUserDivByName(usersContainer, name);
                    }
                });

            } catch (Exception e) {
                Log.e("SOCKET", "Error in onPlayerLeftChannel", e);
            }
        });

        socketManager.onPlayerLeft(args -> {
            try {
                String id = (String) args[0];
                MyGame.remotePlayers.remove(id);
                Log.d("SOCKET", "playerLeft: " + id);
            } catch (Exception e) {
                Log.e("SOCKET", "Error in onPlayerLeft", e);
            }
        });

        socketManager.onPlayerMoved(args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String id = data.getString("playerId");
                float x = (float) data.getDouble("x");
                float y = (float) data.getDouble("y");
                int direction = data.getInt("direction");
                boolean isMoving = data.getBoolean("isMoving");

                if (MyGame.remotePlayers.containsKey(id)) {
                    RemotePlayer rp = MyGame.remotePlayers.get(id);
                    rp.x = x;
                    rp.y = y;
                    rp.currentDirection = direction;
                    rp.isMoving = isMoving;
                }
            } catch (Exception e) {
                Log.e("SOCKET", "Error in onPlayerMoved", e);
            }
        });

        socketManager.onCurrentPlayersInVoiceChannel(args -> {
            try {
                JSONArray players = (JSONArray) args[0];
                voiceParticipants.clear();

                String myNickname = MainActivity.hashMap.get("nickname");


                for (int i = 0; i < players.length(); i++) {
                    JSONObject p = players.getJSONObject(i);
                    String playerId = p.getString("playerId");
                    String playerName = p.getString("playerName");
                    voiceParticipants.put(playerId, playerName);


                }


                    runOnUiThread(() -> showVoiceChatPopup(MainActivity.hashMap.get("voiceChannelCode")));

            } catch (Exception e) {
                Log.e("SOCKET", "Error in onCurrentPlayersInVoiceChannel", e);
            }
        });

        socketManager.onPlayerJoinedVoiceChannel(args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String playerId = data.getString("playerId");
                String name = data.getString("playerName");


                if (!voiceParticipants.containsKey(playerId)) {
                    voiceParticipants.put(playerId, name);

                    runOnUiThread(() -> {
                        if (activeVoiceDialog != null && activeVoiceDialog.isShowing()) {
                            addUserDiv(usersContainer, name, false);
                        }
                    });
                }

            } catch (Exception e) {
                Log.e("SOCKET", "Error in onPlayerJoinedVoiceChannel", e);
            }
        });



        // === GAME INITIALIZATION ===

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        View gameView = initializeForView(new MyGame(mapCode, nickname, new SocketBridge() {
            @Override
            public void emitPlayerMoved(float x, float y,int direction, boolean isMoving) {
                SocketManager.getInstance().sendPlayerMoved(x, y, direction, isMoving);
            }
        }), config);

        // === UI ===

        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.addView(gameView);

        FrameLayout buttonContainer = new FrameLayout(this);
        FrameLayout dpad = new FrameLayout(this);
        FrameLayout.LayoutParams dpadParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        dpadParams.gravity = Gravity.BOTTOM | Gravity.START;
        dpadParams.setMargins(30, 0, 0, 30);
        buttonContainer.addView(dpad, dpadParams);

        Button up = new Button(this);   up.setText("↑");
        Button down = new Button(this); down.setText("↓");
        Button left = new Button(this); left.setText("←");
        Button right = new Button(this);right.setText("→");

        int btnSize = 110;

        FrameLayout.LayoutParams upParams = new FrameLayout.LayoutParams(btnSize, btnSize);
        upParams.leftMargin = btnSize;
        dpad.addView(up, upParams);

        FrameLayout.LayoutParams downParams = new FrameLayout.LayoutParams(btnSize, btnSize);
        downParams.topMargin = btnSize * 2;
        downParams.leftMargin = btnSize;
        dpad.addView(down, downParams);

        FrameLayout.LayoutParams leftParams = new FrameLayout.LayoutParams(btnSize, btnSize);
        leftParams.topMargin = btnSize;
        dpad.addView(left, leftParams);

        FrameLayout.LayoutParams rightParams = new FrameLayout.LayoutParams(btnSize, btnSize);
        rightParams.topMargin = btnSize;
        rightParams.leftMargin = btnSize * 2;
        dpad.addView(right, rightParams);

        View.OnTouchListener listener = (v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    if (v == up) DirectionController.setDirection("up");
                    else if (v == down) DirectionController.setDirection("down");
                    else if (v == left) DirectionController.setDirection("left");
                    else if (v == right) DirectionController.setDirection("right");
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    DirectionController.stop();
                    SocketManager.getInstance().sendPlayerStopped(); // 🚀 Emit stopping event!
                    break;
            }
            return true;
        };

        up.setOnTouchListener(listener);
        down.setOnTouchListener(listener);
        left.setOnTouchListener(listener);
        right.setOnTouchListener(listener);

        // Back Button
        Button backButton = new Button(this);
        backButton.setText("Back");
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        backParams.gravity = Gravity.TOP | Gravity.START;
        backParams.setMargins(20, 20, 0, 0);
        buttonContainer.addView(backButton, backParams);

        backButton.setOnClickListener(v -> finish());

        // Chat Button (placeholder)
        // Inflate the voice button from XML
        View voiceButtonView = getLayoutInflater().inflate(R.layout.voice_button, null);
        FrameLayout.LayoutParams voiceParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        voiceParams.gravity = Gravity.BOTTOM | Gravity.END;
        voiceParams.setMargins(0, 0, 30, 60);
        rootLayout.addView(voiceButtonView, voiceParams);

        View joinButtonView = getLayoutInflater().inflate(R.layout.join_button, null);
        FrameLayout.LayoutParams joinParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        joinParams.gravity = Gravity.BOTTOM | Gravity.END;
        joinParams.setMargins(0, 0, 230, 60);
        rootLayout.addView(joinButtonView, joinParams);

        ImageButton joinButton = joinButtonView.findViewById(R.id.joinVoiceChat);
        joinButton.setOnClickListener(v -> {
            openJoinVoicePopup(); // ✅ Open the popup when Join Button clicked
        });

// Set click listener
        ImageButton voiceButton = voiceButtonView.findViewById(R.id.buttonVoiceChat);
        voiceButton.setOnClickListener(v -> {
            String voiceChannelCode = generateVoiceChannelCode();  // Generate the voice channel code
            String mapId = MainActivity.hashMap.get("mapId");
            MainActivity.hashMap.put("voiceChannelCode",voiceChannelCode);
            SocketManager.getInstance().createVoiceChannel(voiceChannelCode,nickname,mapId); // Connect to voice channel


            MyGame.pendingVoiceChatCode = voiceChannelCode;
            Toast.makeText(this, "Connected to voice channel: " + voiceChannelCode, Toast.LENGTH_SHORT).show();
            Player.setVoiceChatActive(voiceChannelCode);
            voiceParticipants.put("creator",nickname);
            showVoiceChatPopup(voiceChannelCode);
        });



        rootLayout.addView(buttonContainer);
        setContentView(rootLayout);
    }



    private void showVoiceChatPopup(String voiceChannelCode) {
        if (isVoicePopupOpen) return; // Prevent multiple popups

        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        View popupView = getLayoutInflater().inflate(R.layout.dialog_voice_chat, null);
        builder.setView(popupView);

        activeVoiceDialog = builder.create();
        activeVoiceDialog.setCanceledOnTouchOutside(false);
        activeVoiceDialog.setCancelable(false);
        activeVoiceDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        activeVoiceDialog.show();

        isVoicePopupOpen = true;

        ImageButton endCallButton = popupView.findViewById(R.id.end_call_button);
        usersContainer = popupView.findViewById(R.id.users_container);
        HashSet <String> participantsSet = new HashSet<>();
        // Add all current participants
        participantsSet.addAll(voiceParticipants.values());
        if (voiceParticipants != null && !voiceParticipants.isEmpty()) {
            participantsSet.addAll(voiceParticipants.values());
            for (String name : participantsSet) {
                if (name != null && !name.isEmpty()) {
                    addUserDiv(usersContainer, name, false);  // Avoid null crash
                }
            }
        }

        endCallButton.setOnClickListener(v -> {
            SocketManager.getInstance().disconnectVoiceChannel(
                MainActivity.hashMap.get("mapId"),
                voiceChannelCode,
                MainActivity.hashMap.get("nickname")
            );
            Player.setVoiceChatActive(null);
            activeVoiceDialog.dismiss();
            isVoicePopupOpen = false;
        });
    }
    // Method to add a user circle dynamically
    private void addUserDiv(LinearLayout container, String userName, boolean isSpeaking) {
        // Create a new LinearLayout for each user
        LinearLayout userLayout = new LinearLayout(this);
        userLayout.setOrientation(LinearLayout.HORIZONTAL);
        userLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Create a circle for the user (ImageView)
        ImageView userCircle = new ImageView(this);
        userCircle.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        userCircle.setPadding(0,0,0,20);
        userCircle.setImageResource(R.drawable.call); // Replace with actual user image


        // Add the circle and user name
        TextView userNameText = new TextView(this);
        userNameText.setText(userName);
        userNameText.setPadding(16, 0, 0, 0);

        userLayout.addView(userCircle);
        userLayout.addView(userNameText);

        // Add the user layout to the container
        container.addView(userLayout);
    }

    private void openJoinVoicePopup() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);
        View popupView = getLayoutInflater().inflate(R.layout.dialog_join_voice, null);
        builder.setView(popupView);

        final android.app.AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // ❌ Don't close on outside click
        dialog.setCancelable(true); // ✅ Allow back press to cancel if you want

        dialog.show();

        // Find views
        TextView title = popupView.findViewById(R.id.voice_join_title);
        EditText codeInput = popupView.findViewById(R.id.voice_code_input);
        Button joinNowButton = popupView.findViewById(R.id.join_now_button);
        Button cancelButton = popupView.findViewById(R.id.cancel_button);

        // Handle join button
        joinNowButton.setOnClickListener(v -> {
            String code = codeInput.getText().toString().trim();
            MainActivity.hashMap.put("voiceChannelCode",code);
            if (!code.isEmpty()) {
                SocketManager.getInstance().joinVoiceChannel(code, MainActivity.hashMap.get("nickname"),MainActivity.hashMap.get("mapId"));
                Toast.makeText(this, "Joining voice channel: " + code, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please enter a valid code", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle cancel button
        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    private void removeUserDivByName(LinearLayout container, String userName) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) child;
                if (layout.getChildCount() > 1 && layout.getChildAt(1) instanceof TextView) {
                    TextView textView = (TextView) layout.getChildAt(1);
                    if (textView.getText().toString().equals(userName)) {
                        container.removeViewAt(i);
                        break;
                    }
                }
            }
        }
    }

    private String generateVoiceChannelCode() {
        final String characters = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int randomIndex = (int) (Math.random() * characters.length());
            code.append(characters.charAt(randomIndex));
        }
        return code.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketManager.getInstance().disconnectSocket();
    }
}
