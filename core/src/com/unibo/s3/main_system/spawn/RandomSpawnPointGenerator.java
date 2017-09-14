package com.unibo.s3.main_system.spawn;

import com.badlogic.gdx.math.Vector2;
import scala.collection.Iterable;

import java.util.Random;

public class RandomSpawnPointGenerator implements SpawnStrategy{

    public Vector2 generateSpawnQuadrant(int maxX, int maxY) {
        return new Vector2(new Random().nextInt(maxX), new Random().nextInt(maxY));
    }

    @Override
    public Vector2 generateSpawnQuadrant(int[][] map) {
        return null;
    }
}
