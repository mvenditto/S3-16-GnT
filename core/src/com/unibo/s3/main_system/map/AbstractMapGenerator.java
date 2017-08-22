package com.unibo.s3.main_system.map;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractMapGenerator implements MapGenerator{

    private static final float BASE_UNIT = 3;
    private static final float HALF_BASE_UNIT = BASE_UNIT/2;
    public static final int MAP_WIDTH = 60;
    public static final int MAP_HEIGHT = 60;
    public static final int WIDTH_SPLITS = (int) (MAP_WIDTH / BASE_UNIT);
    public static final int HEIGHT_SPLITS = (int) (MAP_HEIGHT / BASE_UNIT);
    private static final String END_OF_FILE = "0.0:0.0:0.0:0.0";

    private int[][] maze = new int[WIDTH_SPLITS][HEIGHT_SPLITS];

    public List<String> getMap(){
        ArrayList<String> map = new ArrayList<>();
        for(int i = 0; i < WIDTH_SPLITS; i++){
            for (int j = 0; j < HEIGHT_SPLITS; j++){
                if(maze[i][j] == 1){
                    System.out.println((i * BASE_UNIT + HALF_BASE_UNIT) + ":" + (j * BASE_UNIT + HALF_BASE_UNIT) + ":" + BASE_UNIT + ":" + BASE_UNIT);
                    map.add((i * BASE_UNIT + HALF_BASE_UNIT) + ":" + (j * BASE_UNIT + HALF_BASE_UNIT) + ":" + BASE_UNIT + ":" + BASE_UNIT);
                }
            }
        }
        map.add(END_OF_FILE);
        return map;
    }


    protected void buildWall(boolean orientation, int coord){
        if(orientation){ //vertical wall
            System.out.println("Build vertical wall in " + coord);
            for(int i = 0; i < WIDTH_SPLITS; i++){
                this.maze[coord - 1][i] = 1;
            }
        } else{
            System.out.println("Build horizontal wall in " + coord);
            for(int i = 0; i < HEIGHT_SPLITS; i++){
                this.maze[i][coord - 1] = 1;
            }
        }
    }

    protected void buildWallWithRange(boolean orientation, int coord, int start, int stop){

        if(orientation){ //vertical wall
            if (stop > HEIGHT_SPLITS){
                stop = HEIGHT_SPLITS;
            }
            System.out.println("Build vertical wall in " + coord + " from " + start + " to " + stop);
            for(int i = start; i < stop; i++){
                this.maze[coord][i] = 1;
            }
        } else{
            if (stop > WIDTH_SPLITS){
                stop = WIDTH_SPLITS;
            }
            System.out.println("Build horizontal wall in " + coord + " from " + start + " to " + stop);
            for(int i = start; i < stop; i++){
                this.maze[i][coord] = 1;
            }
        }
    }

    protected void buildDoor(int x, int y){
        System.out.println("Building hole in " + x + "," + y);
        maze[x][y] = 0;
    }

    /**true vertical, false horizontal**/
    protected boolean getVerticalOrHorizontal(){
        return new Random().nextBoolean();
    }

    protected int generateInRange(int lowerBound, int upperbound){
        int out = -1;
        while(out < lowerBound){
            out = new Random().nextInt(upperbound+1);
        }
        return out;
    }

    protected int generateWithExclusions(List<Integer> l, int lowerBound, int upperbound){
        int out = -1;
        while(out < lowerBound){
            out = generateInRange(lowerBound, upperbound + 1);
            if(l.contains(out)){
                out = -1;
            }
        }
        return out;
    }

    protected boolean isIntersection(int x, int y){
        if(x > 1 && y > 1 && x < WIDTH_SPLITS-1 && y < HEIGHT_SPLITS-1){
            System.out.println("up dx down sx");
            System.out.println(maze[x][y+1] + " " + maze[x+1][y] + " " + maze[x][y-1] + " " + maze[x-1][y]);
            if(maze[x][y+1] == 1 && maze[x+1][y] == 1 && maze[x][y-1] == 1 && maze[x-1][y] == 1){
                System.out.println();
                System.out.println(x + " " + y + " IS AN INTERSECTION!");

                System.out.println();
                return  true;
            } else {
                return false;
            }
        }
        return false;
    }

    public boolean containsIntersections(){
        for(int i = 1; i < WIDTH_SPLITS-1; i++){
            for(int j = 1; j < HEIGHT_SPLITS-1; j++){
                if(maze[i][j] == 0 && maze[i][j+1] == 1 && maze[i+1][j] == 1 && maze[i][j-1] == 1 && maze[i-1][j] == 1){
                    System.out.println();
                    System.out.println(i + " " + j + " is a bad INTERSECTION!");
                    System.out.println();
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isVerticalWallDenied(int coord, int startY, int endY){
        if (startY == 0 && endY == HEIGHT_SPLITS) {
            System.out.println("Initial wall OK");
        }
        else if(endY == HEIGHT_SPLITS){
            if (maze[coord][startY - 1] == 0) {
                System.out.println();
                System.out.println("Wall from " + coord + ", " + startY + " to " + coord + "," + endY + " ends in 0");
                System.out.println();
                return  true;
            }
        }else if(startY == 0){
            if (maze[coord][endY] == 0) {
                System.out.println();
                System.out.println("Wall from " + coord + ", " + startY + " to " + coord + "," + endY + " starts in 0");
                System.out.println();
                return true;
            }
        } else {
            if (maze[coord][startY - 1] == 0 || maze[coord][endY] == 0) {
                System.out.println();
                System.out.println("Wall from " + coord + ", " + startY + " to " + coord + "," + endY + " not good");
                System.out.println();
                return true;
            }
        }
        return false;
    }

    protected boolean isHorizontalWallDenied(int coord, int startX, int endX) {
        if (startX == 0 && endX == WIDTH_SPLITS) {
            System.out.println("Initial wall OK");
        }
        else if(endX == WIDTH_SPLITS){
            if(maze[startX - 1][coord] == 0){
                System.out.println();
                System.out.println("Wall from " + startX + ", " + coord + " to " + endX + "," + coord + " starts in 0");
                System.out.println();
                return true;
            }
        } else if(startX == 0){
            if (maze[endX][coord] == 0) {
                System.out.println();
                System.out.println("Wall from " + startX + ", " + coord + " to " + endX + "," + coord + " ends in 0");
                System.out.println();
                return true;
            }
        } else {
            if (maze[startX - 1][coord] == 0 || maze[endX][coord] == 0) {
                System.out.println();
                System.out.println("Wall from " + startX + ", " + coord + " to " + endX + "," + coord + " not good");
                System.out.println();
                return true;
            }
        }
        return false;
    }
}
