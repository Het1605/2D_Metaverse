package com.yourproject.youename;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Assets {

    public static TextureRegion remoteAvatar; // simple static if needed
    private static Texture remoteAvatarTexture;

    public static Animation<TextureRegion>[] remoteWalkAnimations; // ✅ Animation array for remote players

    private static final int FRAME_COLS = 4;
    private static final int FRAME_ROWS = 4;
    private static final float FRAME_DURATION = 0.1f; // Adjust speed of walking animation

    public static void load() {
        // Load the full sprite sheet
        remoteAvatarTexture = new Texture(Gdx.files.internal("girls1/c.png"));

        // Create a default avatar (just first frame)
        remoteAvatar = new TextureRegion(remoteAvatarTexture, 0, 0,
            remoteAvatarTexture.getWidth() / FRAME_COLS,
            remoteAvatarTexture.getHeight() / FRAME_ROWS
        );

        // Split into frames
        TextureRegion[][] tempFrames = TextureRegion.split(
            remoteAvatarTexture,
            remoteAvatarTexture.getWidth() / FRAME_COLS,
            remoteAvatarTexture.getHeight() / FRAME_ROWS
        );

        // Create animations
        remoteWalkAnimations = new Animation[FRAME_ROWS];
        for (int i = 0; i < FRAME_ROWS; i++) {
            Array<TextureRegion> frames = new Array<>();
            for (int j = 0; j < FRAME_COLS; j++) {
                frames.add(tempFrames[i][j]);
            }
            remoteWalkAnimations[i] = new Animation<>(FRAME_DURATION, frames, Animation.PlayMode.LOOP);
        }
    }

    public static void dispose() {
        if (remoteAvatarTexture != null) {
            remoteAvatarTexture.dispose();
        }
    }
}


//
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.g2d.Animation;
//import com.badlogic.gdx.graphics.g2d.TextureRegion;
//import com.badlogic.gdx.utils.Array;
//
//public class Assets {
//
//    private static Texture spriteSheet; // Full sprite sheet
//    public static Animation<TextureRegion>[] remoteWalkAnimations; // Animations array (same as Player)
//
//    private static final int FRAME_COLS = 4; // Columns in sprite sheet
//    private static final int FRAME_ROWS = 4; // Rows (directions)
//    private static final float FRAME_DURATION = 0.1f; // Speed of animation
//
//    public static void load() {
//        // Load the full sprite sheet once
//        spriteSheet = new Texture(Gdx.files.internal("girls1/c.png"));
//
//        // Split sprite into frames
//        TextureRegion[][] splitFrames = TextureRegion.split(
//            spriteSheet,
//            spriteSheet.getWidth() / FRAME_COLS,
//            spriteSheet.getHeight() / FRAME_ROWS
//        );
//
//        // Initialize animations
//        remoteWalkAnimations = new Animation[FRAME_ROWS];
//
//        for (int i = 0; i < FRAME_ROWS; i++) {
//            Array<TextureRegion> frames = new Array<>();
//            for (int j = 0; j < FRAME_COLS; j++) {
//                frames.add(splitFrames[i][j]);
//            }
//            remoteWalkAnimations[i] = new Animation<>(FRAME_DURATION, frames, Animation.PlayMode.LOOP);
//        }
//    }
//
//    public static void dispose() {
//        if (spriteSheet != null) {
//            spriteSheet.dispose();
//        }
//    }
//}
