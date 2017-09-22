package com.unibo.s3.main_system.map;

import java.util.List;

public interface GenerationStrategy {

    /**
     * Initial setup of map's width and height
     * @param width of the map
     * @param height of the map
     */
    void initialSetup(int width, int height);

    /**
     * Recursive method for map generation with the selected strategy
     * @param mapWidth width of the task to generate
     * @param mapHeight height of the task to generate
     * @param startX starting x position of the task to generate
     * @param startY starting y position of the task to generate
     */
    void generate(int mapWidth, int mapHeight, int startX, int startY);

    /**
     * Getter for the generated map
     * @return List of every map element in string form
     */
    List<String> getMap();
}
