package com.unibo.s3.main_system.spawn;

import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.map.AbstractMapGenerator;

import java.util.ArrayList;
import java.util.List;

public class SpawnPointGenerator {

    private SpawnStrategy spawnStrategy;

    public List<Vector2> generateSpawnPoints(int[][] map, int nSpawnPoints){
        List<Vector2> spawnPoints = new ArrayList<>();

        Vector2 spawnQuadrant;
        while(nSpawnPoints > 0){

            spawnQuadrant = spawnStrategy.generateSpawnQuadrant(map);/*
            while(!checkAllowedPosition(map, (int) spawnQuadrant.x, (int) spawnQuadrant.y)){
                spawnQuadrant = spawnStrategy.generateSpawnQuadrant(map);
            }*/
           // spawnPoints.add(spawnQuadrant);
            spawnPoints.add(new Vector2(spawnQuadrant.x * 2 + AbstractMapGenerator.BASE_UNIT,
                    spawnQuadrant.y * 2 + AbstractMapGenerator.BASE_UNIT));
            nSpawnPoints--;
        }
        //System.out.println("SpawnPoints " + spawnPoints);
        return spawnPoints;
    }

    public void setSpawnStrategy(SpawnStrategy strategy){ this.spawnStrategy = strategy; }

    /**true, spawn point allowed / false, spawn point denied**/
    private boolean checkAllowedPosition(int[][] map, int x, int y){
        return map[x][y] == 0;
    }

}
