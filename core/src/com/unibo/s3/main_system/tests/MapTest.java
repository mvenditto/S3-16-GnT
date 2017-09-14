package com.unibo.s3.main_system.tests;

import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.spawn.RandomSpawnPointGenerator;
import com.unibo.s3.main_system.spawn.SpawnPointGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MapTest implements TestClass {
    @Override
    public Map<String, Boolean> doTests() {
        Map<String, Boolean> res = new HashMap<>();
        res.put("Test spawn point", testSpawnPoint());
        return res;
    }

    private Boolean testSpawnPoint() {
        int[][] map = new int[30][20];
        Random rand = new Random();
        for(int k = 0; k < 100; k++){ //100 iterations
            for(int i = 0; i < 20; i ++){ //random matrix
                for(int j = 0; j < 20; j++){
                    map[i][j] = rand.nextInt(2);
                }
            }
            SpawnPointGenerator spawner = new SpawnPointGenerator();
            spawner.setSpawnStrategy(new RandomSpawnPointGenerator());
            List<Vector2> list = spawner.generateSpawnPoints(map, 4);

            for(Vector2 v : list){
                if (map[(int) v.x][(int) v.y] != 0)
                    return false;
            }
        }
        return true;
    }


}
