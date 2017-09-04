package com.unibo.s3.main_system.map;

import com.badlogic.gdx.math.Vector2;

public interface SpawnStrategy {

    public Vector2 generateSpawnQuadrant(int maxX, int maxY);

}
