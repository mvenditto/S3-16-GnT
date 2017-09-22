package com.unibo.s3.main_system.spawn;

import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.game.Wall;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicola Santolini
 * @author Daniele Rosetti
 */
public class SpawnPointGenerator {

    private static final int WALL_THICKNESS = Wall.WALL_THICKNESS();
    private SpawnStrategy spawnStrategy;

    public List<Vector2> generateSpawnPoints(int[][] map, int nSpawnPoints){
        List<Vector2> spawnPoints = new ArrayList<>();

        Vector2 spawnQuadrant;
        while(nSpawnPoints > 0){

            spawnQuadrant = spawnStrategy.generateSpawnQuadrant(map);
            spawnPoints.add(new Vector2(spawnQuadrant.x * 2 + WALL_THICKNESS,
                    spawnQuadrant.y * 2 + WALL_THICKNESS));
            nSpawnPoints--;
        }
        return spawnPoints;
    }

    public void setSpawnStrategy(SpawnStrategy strategy){ this.spawnStrategy = strategy; }
}
