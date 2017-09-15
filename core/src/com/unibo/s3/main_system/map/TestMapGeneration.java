package com.unibo.s3.main_system.map;

import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.spawn.GuardSpawningStrategy;
import com.unibo.s3.main_system.spawn.RandomSpawnPointGenerator;
import com.unibo.s3.main_system.spawn.SpawnPointGenerator;
import com.unibo.s3.main_system.spawn.ThiefSpawningStrategy;
import org.junit.*;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestMapGeneration {

    MapGenerator generator;

    @Before
    public void init(){
        generator = new MapGenerator();
    }

    @Test
    public void testRoomsGeneration(){
        generator.setStrategy(new RoomMapGenerator());
        for(int i = 0; i < 100; i++){
            try{
                generator.generateMap(60, 60);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testMazeGeneration(){
        generator.setStrategy(new MazeMapGenerator());
        for(int i = 0; i < 100; i++){
            try{
                generator.generateMap(60, 60);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testRangeGen(){
        MazeMapGenerator generator = new MazeMapGenerator();
        for(int i = 0; i < 1000; i++){
            int gen = generator.generateInRange(0, 100);
            assertTrue(gen >= 0 && gen <= 100);
        }
    }

    @Test
    public void testSpawnPoint(){
        int[][] map = new int[30][20];
        Random rand = new Random();
        for(int k = 0; k < 100; k++){ //100 iterations
            for(int i = 0; i < 20; i ++){ //random matrix
                for(int j = 0; j < 20; j++){
                    map[i][j] = rand.nextInt(2);
                }
            }
            SpawnPointGenerator spawnPointGenerator = new SpawnPointGenerator();
            spawnPointGenerator.setSpawnStrategy(new GuardSpawningStrategy());
            List<Vector2> guardsList = spawnPointGenerator.generateSpawnPoints(map, 10); //generate 10 spawn points
            for(Vector2 v : guardsList){
                int xInMatrix = (int) ((v.x - 2) / 2);
                int yInMatrix = (int) ((v.y - 2) / 2);
                assertEquals(0, map[xInMatrix][yInMatrix]);
            }
            spawnPointGenerator.setSpawnStrategy(new ThiefSpawningStrategy());
            List<Vector2> thievesList = spawnPointGenerator.generateSpawnPoints(map, 10); //generate 10 spawn points
            for(Vector2 v : thievesList){
                int xInMatrix = (int) ((v.x - 2) / 2);
                int yInMatrix = (int) ((v.y - 2) / 2);
                assertEquals(0, map[xInMatrix][yInMatrix]);
            }
        }
    }
}
