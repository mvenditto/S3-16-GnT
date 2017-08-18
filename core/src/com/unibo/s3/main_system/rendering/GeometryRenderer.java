package com.unibo.s3.main_system.rendering;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.steer.MovableEntity;
import com.unibo.s3.main_system.graph.Graph;
import com.unibo.s3.main_system.graph.GraphAdapter;

/**
 *
 * @author mvenditto
 * */
public interface GeometryRenderer<T extends Vector<T>> {

    void renderCharacter(ShapeRenderer shapeRenderer, MovableEntity<T> character);

    void renderCharacterDebugInfo(ShapeRenderer shapeRenderer, MovableEntity<T> character);

    void renderGraph(ShapeRenderer shapeRenderer, GraphAdapter<T> graph, GraphRenderingConfig config);

    void renderMap(ShapeRenderer shapeRenderer, Object map);

}
