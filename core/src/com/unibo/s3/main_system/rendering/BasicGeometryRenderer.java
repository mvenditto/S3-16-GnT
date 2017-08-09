package com.unibo.s3.main_system.rendering;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.steer.MovableEntity;
import com.unibo.s3.main_system.graph.Graph;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class BasicGeometryRenderer implements GeometryRenderer {


    @Override
    public void renderCharacter(ShapeRenderer shapeRenderer, MovableEntity<Vector2> character) {



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
