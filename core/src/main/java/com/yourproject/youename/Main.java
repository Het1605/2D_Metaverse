package com.yourproject.youename;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Main extends Game {

    public SpriteBatch batch; // Manage SpriteBatch globally
    public static String nickname;



    @Override
    public void create() {
        Gdx.app.log("INFO", "Game Started");
        batch = new SpriteBatch(); // Initialize batch
        setScreen(new GameScreen(this,nickname));  // ✅ Start with HomeScreen
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
