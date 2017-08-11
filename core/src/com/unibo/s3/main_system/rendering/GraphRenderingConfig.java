package com.unibo.s3.main_system.rendering;

import com.badlogic.gdx.graphics.Color;

public interface GraphRenderingConfig {

    Color getEdgeColor();

    Color getVertexColor();

    float getVertexRadiusMeters();
}
