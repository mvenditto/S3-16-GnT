package com.unibo.s3.main_system.map;

import org.junit.*;
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
}
