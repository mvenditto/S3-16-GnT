package com.unibo.s3.main_system.rendering;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unibo.s3.main_system.characters.MovableCharacter;
import com.unibo.s3.main_system.graph.Graph;

public interface GeometryRenderer {

    void renderCharacter(ShapeRenderer shapeRenderer, MovableCharacter character);

    void renderGraph(ShapeRenderer shapeRenderer, Graph graph);

    void renderMap(ShapeRenderer shapeRenderer, Object map);

}
