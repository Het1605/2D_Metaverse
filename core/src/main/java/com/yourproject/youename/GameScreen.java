package com.yourproject.youename;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen {

    private final Game game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private SpriteBatch batch;
    private Player player;

    private float mapWidth, mapHeight;
    private static final int VIRTUAL_WIDTH = 1100;
    private static final int VIRTUAL_HEIGHT = 500;
    private String nickname;


    public GameScreen(Game game, String nickname) {
        this.nickname = nickname;
        this.game = game;
    }

    @Override
    public void show() {
        Assets.load();

        Gdx.app.log("INFO", "GameScreen Loaded");

        // Load Tiled map
        map = new TmxMapLoader().load("MetaMap3.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        // Get map properties
        MapProperties properties = map.getProperties();
        int tileWidth = properties.get("tilewidth", Integer.class);
        int tileHeight = properties.get("tileheight", Integer.class);
        int mapTileWidth = properties.get("width", Integer.class);
        int mapTileHeight = properties.get("height", Integer.class);

        mapWidth = tileWidth * mapTileWidth;
        mapHeight = tileHeight * mapTileHeight;

        // Set up camera and viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        camera.update();

        // Set up batch
        batch = new SpriteBatch();

        // Create player
        player = new Player(300, -80, camera, map,nickname);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update player logic
        player.update(delta);

        // Clamp camera within map bounds
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        camera.position.x = Math.min(mapWidth - halfWidth, Math.max(halfWidth, player.getX()));
        camera.position.y = Math.min(mapHeight - halfHeight, Math.max(halfHeight, player.getY()));
        camera.update();

        // Render map
        mapRenderer.setView(camera);
        mapRenderer.render();

        // Render player
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        player.render(batch);


        for (RemotePlayer rp : MyGame.remotePlayers.values()) {
            rp.render(batch);
        }
        batch.end();

    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height);
        }
    }

    @Override
    public void dispose() {
        if (map != null) map.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        if (batch != null) batch.dispose();
        if (player != null) player.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        // Don’t dispose here if switching screens
    }
}
