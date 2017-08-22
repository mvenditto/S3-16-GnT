package com.unibo.s3.main_system.map;

import org.junit.*;
import static org.junit.Assert.assertTrue;

public class TestMapGeneration {


    @Test
    public void testRoomsGeneration(){
        RoomMapGenerator generator = new RoomMapGenerator();
        for(int i = 0; i < 100; i++){
            try{
                generator.generate(4, AbstractMapGenerator.WIDTH_SPLITS, AbstractMapGenerator.HEIGHT_SPLITS, 0, 0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testMazeGeneration(){
        MazeMapGenerator generator = new MazeMapGenerator();
        for(int i = 0; i < 100; i++){
            try{
                generator.generate(4, AbstractMapGenerator.WIDTH_SPLITS, AbstractMapGenerator.HEIGHT_SPLITS, 0, 0);
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
