package com.unibo.s3.main_system.map;

import java.util.List;

public interface MapGenerator {

    void generateMap(int width, int height);

    List<String> getMap();
}
