package com.unibo.s3.main_system.map;

import com.badlogic.gdx.math.Vector2;

import java.util.Random;

public class RandomSpawnPointGenerator implements SpawnStrategy{

    @Override
    public Vector2 generateSpawnQuadrant(int maxX, int maxY) {
        return new Vector2(new Random().nextInt(maxX), new Random().nextInt(maxY));
    }
}
