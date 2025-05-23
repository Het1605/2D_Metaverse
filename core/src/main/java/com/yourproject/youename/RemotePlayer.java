package com.yourproject.youename;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RemotePlayer {
    public float x, y;
    public String nickname;
    public int currentDirection = 0;
    public boolean isMoving = false;
    private float stateTime = 0f;
    private GlyphLayout layout = new GlyphLayout();

    private String voiceChannelCode = null;  // New field
    private GlyphLayout voiceLayout = new GlyphLayout();

    public RemotePlayer(float x, float y, String nickname) {
        this.x = x;
        this.y = y;
        this.nickname = nickname != null ? nickname : "Player";
    }

    public void render(Batch batch) {
        if (Assets.remoteWalkAnimations == null) return;

        if (isMoving) {
            stateTime += Gdx.graphics.getDeltaTime();
        }



        TextureRegion frame = Assets.remoteWalkAnimations[currentDirection].getKeyFrame(stateTime, true);

        float width = frame.getRegionWidth() * Assets.PLAYER_SCALE;
        float height = frame.getRegionHeight() * Assets.PLAYER_SCALE;

        batch.draw(frame, x, y, width, height);

        layout.setText(Assets.nameFont, nickname);
        float textWidth = layout.width;
        float textHeight = layout.height;

        float textX = x + width / 2 - textWidth / 2;
        float textY = y + height + 10 + textHeight;

        float padding = 6;
        Assets.nameBackground.draw(batch, textX - padding, textY - textHeight - padding,
            textWidth + 2 * padding, textHeight + 2 * padding);

        Assets.nameFont.draw(batch, layout, textX, textY);

        // === VOICE LABEL below the player ===
        if (voiceChannelCode != null) {
            String label = "Voice: " + voiceChannelCode;
            voiceLayout.setText(Assets.nameFont, label);
            float voiceWidth = voiceLayout.width;
            float voiceHeight = voiceLayout.height;

            float voiceX = x + width / 2 - voiceWidth / 2;
            float voiceY = y - 15;  // below the feet of avatar

            Assets.nameBackground.draw(batch, voiceX - padding, voiceY - voiceHeight - padding,
                voiceWidth + 2 * padding, voiceHeight + 2 * padding);

            Assets.nameFont.draw(batch, voiceLayout, voiceX, voiceY);
        }
    }

    public void setVoiceChannelCode(String code) {
        this.voiceChannelCode = code;
    }
}
