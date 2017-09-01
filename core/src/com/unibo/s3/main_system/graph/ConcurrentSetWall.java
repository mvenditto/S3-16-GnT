package com.unibo.s3.main_system.graph;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ConcurrentSetWall implements Callable<List<Vector2>> {
    private List<String> lines;
    private int maxDimensionX, maxDimensionY;

    public ConcurrentSetWall(List<String> l, int maxX, int maxY) {
        this.lines = l;
        this.maxDimensionX = maxX;
        this.maxDimensionY = maxY;
    }

    /*@Override
    public void run() {
        for(int i = (int) bl.x; i <= (int) tr.x && i < grid.length; i++) {
            for(int j = (int) bl.y; j <= tr.y && j < grid[0].length; j++ ) {
                grid[i][j] = 1;
            }
        }
    }*/

    @Override
    public List<Vector2> call() throws Exception {
        List<Vector2> res = new ArrayList<>();

        for(String l : this.lines) {
            String[] toks = l.split(":");
            float x = Float.parseFloat(toks[0]);
            float y = Float.parseFloat(toks[1]);
            float w = Float.parseFloat(toks[2]);
            float h = Float.parseFloat(toks[3]);

            float halfw = w / 2;
            float halfh = h / 2;
            Vector2 bl = new Vector2((x - halfw), (y - halfh));
            Vector2 tr = new Vector2((x + halfw), (y + halfh));

            for(int i = (int) bl.x; i <= (int) tr.x && i < maxDimensionX; i++) {
                for(int j = (int) bl.y; j <= tr.y && j < maxDimensionY; j++ ) {
                    res.add(new Vector2(i,j));
                }
            }
        }

        return res;
    }
}
