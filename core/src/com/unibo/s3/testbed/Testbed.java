package com.unibo.s3.testbed;

import com.badlogic.gdx.math.Vector2;

public interface Testbed {

    Vector2 screenToWorld(Vector2 screenPosition);

    Vector2 worldToScreen(Vector2 worldPosition);

}
