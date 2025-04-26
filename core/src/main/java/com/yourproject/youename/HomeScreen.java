//package com.yourproject.youename;
//
//
//import com.badlogic.gdx.Game;
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.InputAdapter;
//import com.badlogic.gdx.Screen;
//import com.badlogic.gdx.graphics.GL20;
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.g2d.BitmapFont;
//import com.badlogic.gdx.graphics.g2d.GlyphLayout;
//import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//
//public class HomeScreen implements Screen {
//    private Game game;
//
//    private SpriteBatch batch;
//
//    private Texture[] maps; // Array to hold multiple maps
//    private int currentMapIndex = 0; // Track the current map
//    private BitmapFont font;
//    private GlyphLayout layout;
//    private Texture leftArrow, rightArrow; // Arrow button textures
//
//    public HomeScreen(Game game) {
//        this.game = game;
//        this.batch = new SpriteBatch();
//
//
//        // Load multiple maps (make sure these images exist in the assets folder)
//        maps = new Texture[]{
//            new Texture(Gdx.files.internal("2DMeta.png")),
//            new Texture(Gdx.files.internal("map1.png")),
//        };
//
//
//        font = new BitmapFont();
//        layout = new GlyphLayout();
//
//        // Load arrow button textures
//        leftArrow = new Texture(Gdx.files.internal("left_arrow.png"));
//        rightArrow = new Texture(Gdx.files.internal("right_arrow.jpg"));
//
//        // Set up input processing for button clicks
//        Gdx.input.setInputProcessor(new InputAdapter() {
//            @Override
//            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
//                // Convert screenY to match LibGDX's coordinate system
//                screenY = Gdx.graphics.getHeight() - screenY;
//
//                // Check if left arrow is clicked
//                if (screenX >= 50 && screenX <= 150 && screenY >= Gdx.graphics.getHeight() / 2 - 50 && screenY <= Gdx.graphics.getHeight() / 2 + 50) {
//                    currentMapIndex = (currentMapIndex - 1 + maps.length) % maps.length; // Loop backwards
//                    return true;
//                }
//
//                // Check if right arrow is clicked
//                if (screenX >= Gdx.graphics.getWidth() - 150 && screenX <= Gdx.graphics.getWidth() - 50 && screenY >= Gdx.graphics.getHeight() / 2 - 50 && screenY <= Gdx.graphics.getHeight() / 2 + 50) {
//                    currentMapIndex = (currentMapIndex + 1) % maps.length; // Loop forward
//                    return true;
//                }
//
//                // Click anywhere else to start the game
//                game.setScreen(new GameScreen(game));
//                return true;
//            }
//        });
//    }
//
//    @Override
//    public void render(float delta) {
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        batch.begin();
//
//        // Draw "Choose Map" text
//        font.getData().setScale(4f);
//        layout.setText(font, "Choose Map");
//        float textX = (Gdx.graphics.getWidth() - layout.width) / 2;
//        float textY = Gdx.graphics.getHeight() - 50;
//        font.draw(batch, layout, textX, textY);
//
//        // Draw current map image centered
//        float mapX = (Gdx.graphics.getWidth() - maps[currentMapIndex].getWidth()) / 2;
//        float mapY = (Gdx.graphics.getHeight() - maps[currentMapIndex].getHeight()) / 2;
//        batch.draw(maps[currentMapIndex], mapX, mapY);
//
//        // Draw arrow buttons
//        batch.draw(leftArrow, 50, Gdx.graphics.getHeight() / 2 - 50, 100, 100);
//        batch.draw(rightArrow, Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() / 2 - 50, 100, 100);
//
//        batch.end();
//    }
//
//    @Override public void resize(int width, int height) {}
//    @Override public void pause() {}
//    @Override public void resume() {}
//    @Override public void hide() {}
//
//    @Override
//    public void dispose() {
//        batch.dispose();
//        for (Texture map : maps) {
//            map.dispose();
//        }
//        leftArrow.dispose();
//        rightArrow.dispose();
//        font.dispose();
//    }
//
//    @Override
//    public void show() {}
//}
//
