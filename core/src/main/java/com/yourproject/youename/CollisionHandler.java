package com.yourproject.youename;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class CollisionHandler {
    private TiledMap map;

    public CollisionHandler(TiledMap map) {
        this.map = map;
    }

    public boolean isColliding(Vector2 position, float width, float height) {
        MapLayer collisionLayer = map.getLayers().get("Collision1"); // Get collision layer
        if (collisionLayer == null) return false; // No collision layer found

        for (RectangleMapObject object : collisionLayer.getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = object.getRectangle();

            // Check if player collides with any tile
            if (rect.overlaps(new Rectangle(position.x, position.y, width, height))) {
                return true; // Collision detected
            }
        }
        return false; // No collision
    }
}
