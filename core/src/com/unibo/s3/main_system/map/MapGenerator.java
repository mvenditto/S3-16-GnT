package com.unibo.s3.main_system.map;

public interface MapGenerator {

    void generate(int n, int width, int height, int startX, int startY);

    int[][] getMap();

}
