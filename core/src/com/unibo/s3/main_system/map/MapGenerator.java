package com.unibo.s3.main_system.map;

import java.util.List;

public interface MapGenerator {

    void generate(int n, int width, int height, int startX, int startY);

    List<String> getMap();

}
