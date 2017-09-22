package com.unibo.s3.main_system.map;

import java.util.List;

public interface GenerationStrategy {

    void initialSetup(int width, int height);

    void generate(int mapWidth, int mapHeight, int startX, int startY);

    List<String> getMap();
}
