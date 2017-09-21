package com.unibo.s3.main_system.map;

import com.unibo.s3.main_system.game.GameSettings;
import com.unibo.s3.main_system.game.Wall;

import java.util.List;

public class MapGenerator {

    private static final int BASE_UNIT = Wall.WALL_THICKNESS();
    private GenerationStrategy strategy;

    public void generateMap(int width, int height) {
        //System.out.println("generator: " + width + " " + height);
        //System.out.println("generator: " + BASE_UNIT);
        strategy.initialSetup(width/BASE_UNIT, height/BASE_UNIT);
        strategy.generate(8, width/BASE_UNIT, height/BASE_UNIT, 0, 0); }/**todo eliminare primo parametro**/

    public List<String> getMap() { return strategy.getMap(); }

    public void setStrategy(GenerationStrategy strategy){ this.strategy = strategy; }

}
