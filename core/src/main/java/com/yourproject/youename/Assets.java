package com.yourproject.youename;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Assets {

    public static Animation<TextureRegion>[] remoteWalkAnimations;
    private static Texture remoteAvatarTexture;
    public static BitmapFont nameFont;
    public static NinePatch nameBackground;
    public static final float PLAYER_SCALE = 0.3f;

    private static final int FRAME_COLS = 4;
    private static final int FRAME_ROWS = 4;
    private static final float FRAME_DURATION = 0.1f;

    public static void load() {
        // Load spritesheet
        remoteAvatarTexture = new Texture(Gdx.files.internal("character/c.png"));

        TextureRegion[][] tempFrames = TextureRegion.split(
            remoteAvatarTexture,
            remoteAvatarTexture.getWidth() / FRAME_COLS,
            remoteAvatarTexture.getHeight() / FRAME_ROWS
        );

        remoteWalkAnimations = new Animation[FRAME_ROWS];
        for (int i = 0; i < FRAME_ROWS; i++) {
            Array<TextureRegion> frames = new Array<>();
            for (int j = 0; j < FRAME_COLS; j++) {
                frames.add(tempFrames[i][j]);
            }
            remoteWalkAnimations[i] = new Animation<>(FRAME_DURATION, frames, Animation.PlayMode.LOOP);
        }

        // Load font
        nameFont = new BitmapFont();
        nameFont.getData().setScale(0.9f);

        // Load background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.6f);
        pixmap.fill();
        Texture bgTexture = new Texture(pixmap);
        nameBackground = new NinePatch(bgTexture, 0, 0, 0, 0);
    }

    public static void dispose() {
        if (remoteAvatarTexture != null) remoteAvatarTexture.dispose();
        if (nameFont != null) nameFont.dispose();
        if (nameBackground != null) nameBackground.getTexture().dispose();
    }
}
