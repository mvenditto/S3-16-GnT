package com.unibo.s3.main_system.rendering;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.steer.MovableEntity;
import com.unibo.s3.main_system.graph.Graph;

public interface GeometryRenderer {

    void renderCharacter(ShapeRenderer shapeRenderer, MovableEntity<Vector2> character);

    void renderGraph(ShapeRenderer shapeRenderer, Graph graph);

    void renderMap(ShapeRenderer shapeRenderer, Object map);

}
