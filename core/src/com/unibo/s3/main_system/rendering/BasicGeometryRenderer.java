package com.unibo.s3.main_system.rendering;

import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.steer.MovableEntity;
import com.unibo.s3.main_system.graph.Graph;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static com.unibo.s3.main_system.rendering.ScaleUtils.getPixelsPerMeter;
import static com.unibo.s3.main_system.rendering.ScaleUtils.metersToPixels;

public class BasicGeometryRenderer implements GeometryRenderer {

    @Override
    public void renderCharacter(ShapeRenderer shapeRenderer, MovableEntity<Vector2> character) {
        final float scale = getPixelsPerMeter();
        final Vector2 position = character.getPosition();
        final float size = 0.45f;
        final Color backupColor = shapeRenderer.getColor();

        final Polygon triangle = new Polygon(new float[]{
                scale * (position.x - size), position.y * scale,
                (position.x + size) * scale, position.y * scale,
                position.x * scale, (position.y + size) * scale});

        triangle.setOrigin(position.x * scale, position.y * scale);
        triangle.rotate((character.getOrientation() * MathUtils.radiansToDegrees));
        shapeRenderer.setColor(character.getColor());

        float[] v = triangle.getTransformedVertices();
        Vector2 v1 = new Vector2(v[0], v[1]);
        Vector2 v2 = new Vector2(v[2], v[3]);
        Vector2 v3 = new Vector2(v[4], v[5]);

        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rectLine(v1, v2, 4);
        shapeRenderer.rectLine(v2, v3, 4);
        shapeRenderer.rectLine(v3, v1, 4);
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(backupColor);
    }

    @Override
    public void renderCharacterDebugInfo(ShapeRenderer shapeRenderer, MovableEntity<Vector2> character) {
        Color backupColor = shapeRenderer.getColor();
        shapeRenderer.setColor(Color.RED);
        Ray<Vector2>[] rays = character.getRays();
        Vector2 tmp = new Vector2();
        Vector2 tmp2 = new Vector2();
        for (Ray<Vector2> ray : rays) {
            tmp.set(ray.start);
            tmp.x = metersToPixels(tmp.x);
            tmp.y = metersToPixels(tmp.y);
            tmp2.set(ray.end);
            tmp2.x = metersToPixels(tmp2.x);
            tmp2.y = metersToPixels(tmp2.y);
            shapeRenderer.line(tmp, tmp2);
        }
        shapeRenderer.setColor(backupColor);
    }

    @Override
    public void renderGraph(ShapeRenderer shapeRenderer, Graph graph) {
        throw new NotImplementedException();
    }

    @Override
    public void renderMap(ShapeRenderer shapeRenderer, Object map) {
        throw new NotImplementedException();
    }
}
