package com.yourproject.youename;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RemotePlayer {
    public float x, y;
    public String nickname;
    public int currentDirection = 0;
    public boolean isMoving = false;
    private BitmapFont font;
    private float stateTime = 0f;

    public RemotePlayer(float x, float y, String nickname) {
        this.x = x;
        this.y = y;
        this.nickname = nickname;
        this.font = new BitmapFont();
        font.getData().setScale(0.7f);
    }

    public void render(Batch batch) {
        if (isMoving) {
            stateTime += Gdx.graphics.getDeltaTime();
        } else {
            stateTime = 0;
        }

        TextureRegion frame = Assets.remoteAvatar; // Default static

        if (Assets.remoteWalkAnimations != null) {
            frame = Assets.remoteWalkAnimations[currentDirection].getKeyFrame(stateTime, true);
        }

        batch.draw(frame, x, y, 32, 32);
        font.draw(batch, nickname, x, y + 40);
    }
}
