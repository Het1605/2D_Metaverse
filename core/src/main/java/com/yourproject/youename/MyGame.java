package com.yourproject.youename;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;


public class MyGame extends Game {
    public String mapCode;
    private String nickname;

    public static SocketBridge socketBridge;

    public static HashMap<String, RemotePlayer> remotePlayers = new HashMap<>();

    public MyGame(String code, String nickname,SocketBridge bridge) {
        this.nickname = nickname;
        this.mapCode = code;
        MyGame.socketBridge = bridge;

    }

    @Override
    public void create() {
        setScreen(new GameScreen(this,this.nickname)); // Pass along mapCode if needed
    }
}
