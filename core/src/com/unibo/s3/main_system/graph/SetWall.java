package com.unibo.s3.main_system.graph;

import com.badlogic.gdx.math.Vector2;
import scala.Int;

public class SetWall implements Runnable {
    Vector2 bl, tr;
    Integer[][] grid;

    public SetWall(Vector2 bottomLeft, Vector2 topRight, Integer[][] grid) {
        this.bl = bottomLeft;
        this.tr = topRight;
        this.grid = grid;

    }

    @Override
    public void run() {
        for(int i = (int) bl.x; i <= (int) tr.x && i < grid.length; i++) {
            for(int j = (int) bl.y; j <= tr.y && j < grid[0].length; j++ ) {
                grid[i][j] = 1;
            }
        }
    }
}
