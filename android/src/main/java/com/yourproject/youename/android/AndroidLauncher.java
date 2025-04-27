package com.yourproject.youename.android;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.example.multiplayerui.MainActivity;
import com.yourproject.youename.DirectionController;
import com.yourproject.youename.MyGame;
import com.yourproject.youename.RemotePlayer;
import com.yourproject.youename.SocketBridge;

import org.json.JSONArray;
import org.json.JSONObject;

public class AndroidLauncher extends AndroidApplication {
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
            socketManager.createMap(nickname, mapCode);
        } else {
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
        Button chatButton = new Button(this);
        chatButton.setText("Chat");
        FrameLayout.LayoutParams chatParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        chatParams.gravity = Gravity.BOTTOM | Gravity.END;
        chatParams.setMargins(0, 0, 20, 30);
        buttonContainer.addView(chatButton, chatParams);

        chatButton.setOnClickListener(v -> {
            Toast.makeText(this, "Chat feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        rootLayout.addView(buttonContainer);
        setContentView(rootLayout);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketManager.getInstance().disconnectSocket();
    }
}
