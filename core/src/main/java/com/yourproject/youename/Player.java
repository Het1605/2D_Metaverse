package com.yourproject.youename;



import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
public class Player {

    private static final int FRAME_COLS = 4;
    private static final int FRAME_ROWS = 4;
    private static final float FRAME_DURATION = 0.1f;

    private float mapWidth = 1700;
    private float mapHeight = 900;
    private float viewportWidth = 800;
    private float viewportHeight = 480;

    private Vector2 position;

    private float speed = 1.5f; // Speed of player movement

    private Texture spriteSheet;
    private Animation<TextureRegion>[] walkAnimations;
    private float stateTime;
    private int currentDirection = 0; // 0: down, 1: up, 2: left, 3: right
    private boolean isMoving = false;

    private OrthographicCamera camera;
    private CollisionHandler collisionHandler;
    private String nickname;
    private BitmapFont font;

    private GlyphLayout layout;
    private NinePatch nameTagBg;
    private static String voiceChatCode = null; // ✅ NEW: to store current voice channel code

    private GlyphLayout voiceLayout; // ✅ NEW: separate layout for voice chat
    private NinePatch voiceTagBg;    // ✅ NEW: background for voice chat



    public Player(float x, float y, OrthographicCamera camera, TiledMap map,String nickname) {
        position = new Vector2(x, y);
        this.nickname = nickname;
        this.font=new BitmapFont();
        this.layout = new GlyphLayout();
        this.voiceLayout = new GlyphLayout(); // ✅ initialize


        // Optional styling
        font.getData().setScale(0.9f); // smaller font

        // Create background using a NinePatch (you can use your own patch or color rect)
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.6f); // semi-transparent black
        pixmap.fill();
        Texture bgTexture = new Texture(pixmap);
        nameTagBg = new NinePatch(bgTexture, 0, 0, 0, 0); // No stretch margins

        voiceTagBg = new NinePatch(bgTexture, 0, 0, 0, 0);

        this.camera = camera;
        this.collisionHandler = new CollisionHandler(map);

        spriteSheet = new Texture(Gdx.files.internal("character/c.png"));

        // ✅ FIXED: Removed ClassCastException — no casting needed
        TextureRegion[][] tempFrames = TextureRegion.split(
            spriteSheet,
            spriteSheet.getWidth() / FRAME_COLS,
            spriteSheet.getHeight() / FRAME_ROWS
        );

        walkAnimations = new Animation[FRAME_ROWS];
        for (int i = 0; i < FRAME_ROWS; i++) {
            Array<TextureRegion> frames = new Array<>();
            for (int j = 0; j < FRAME_COLS; j++) {
                frames.add(tempFrames[i][j]);
            }
            walkAnimations[i] = new Animation<>(FRAME_DURATION, frames, Animation.PlayMode.LOOP);
        }

        stateTime = 0f;
    }

    public void update(float delta) {


        String direction = DirectionController.getDirection();
        isMoving = false;

        if (!direction.isEmpty()) {
            Vector2 newPos = new Vector2(position.x, position.y);

            switch (direction) {
                case "left":
                    newPos.x -= speed;
                    currentDirection = 2;
                    break;
                case "right":
                    newPos.x += speed;
                    currentDirection = 3;
                    break;
                case "up":
                    newPos.y += speed;
                    currentDirection = 1;
                    break;
                case "down":
                    newPos.y -= speed;
                    currentDirection = 0;
                    break;
            }

            // Collision check
            if (!collisionHandler.isColliding(newPos, 32, 32)) {
                position.set(newPos);
                isMoving = true;

                // ✅ Emit movement via the bridge
                if (MyGame.socketBridge != null) {
                    MyGame.socketBridge.emitPlayerMoved(position.x, position.y, currentDirection, isMoving);
                }
            }
        }

        if (isMoving) stateTime += delta;
        else stateTime = 0;

        // 📷 Clamp camera to stay within map bounds
        float cameraHalfWidth = viewportWidth / 2f;
        float cameraHalfHeight = viewportHeight / 2f;

        float minX = cameraHalfWidth;
        float maxX = mapWidth - cameraHalfWidth;
        float minY = cameraHalfHeight;
        float maxY = mapHeight - cameraHalfHeight;

        camera.position.x = Math.min(maxX, Math.max(minX, position.x));
        camera.position.y = Math.min(maxY, Math.max(minY, position.y));
        camera.update();


    }

    // ✅ ADD THIS method to update voice chat code dynamically
    public static void setVoiceChatActive(String code) {
        voiceChatCode = code;
    }

    public void render(Batch batch) {
        TextureRegion currentFrame = walkAnimations[currentDirection].getKeyFrame(stateTime, true);
        float scale = 0.3f;

        float width = currentFrame.getRegionWidth() * scale;
        float height = currentFrame.getRegionHeight() * scale;

        batch.draw(currentFrame, position.x, position.y, width, height);

        // === Nickname Drawing ===
        layout.setText(font, nickname);
        float textWidth = layout.width;
        float textHeight = layout.height;

        float textX = position.x + width / 2 - textWidth / 2;
        float textY = position.y + height + 10 + textHeight;

        float padding = 6;
        nameTagBg.draw(batch, textX - padding, textY - textHeight - padding, textWidth + 2 * padding, textHeight + 2 * padding);

        font.draw(batch, layout, textX, textY);

        // === Voice Chat Drawing (✅ NEW) ===
        if (voiceChatCode != null && !voiceChatCode.isEmpty()) {
            String voiceLabel = "Voice: " + voiceChatCode;
            voiceLayout.setText(font, voiceLabel);

            float voiceTextWidth = voiceLayout.width;
            float voiceTextHeight = voiceLayout.height;

            float voiceTextX = position.x + width / 2 - voiceTextWidth / 2;
            float voiceTextY = position.y - 10; // 📌 BELOW player

            voiceTagBg.draw(batch, voiceTextX - padding, voiceTextY - voiceTextHeight - padding, voiceTextWidth + 2 * padding, voiceTextHeight + 2 * padding);

            font.draw(batch, voiceLayout, voiceTextX, voiceTextY);
        }
    }
    public int getCurrentDirection() {
        return currentDirection;
    }

    public boolean isMoving() {
        return isMoving;
    }
    public void dispose() {
        spriteSheet.dispose();
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }
}


