package com.unibo.s3.main_system.map;

import java.util.List;
import java.util.Random;

public abstract class AbstractMapGenerator implements MapGenerator{

    private static final float BASE_UNIT = 3;
    private static final float HALF_BASE_UNIT = BASE_UNIT/2;
    private static final int MAP_SIZE = 60;
    private static final int SPLITS = (int) (MAP_SIZE / BASE_UNIT);
    private static final int CORE_SPLITS = (int) (SPLITS / 3);

    private int[][] maze = new int[SPLITS][SPLITS];

    public int[][] getMap(){
        return this.maze;
    }


    protected void buildWall(boolean orientation, int coord){
        if(orientation){ //vertical wall
            System.out.println("Build vertical wall in " + coord);
            for(int i = 0; i < SPLITS; i++){
                this.maze[coord - 1][i] = 1;
            }
        } else{
            System.out.println("Build horizontal wall in " + coord);
            for(int i = 0; i < SPLITS; i++){
                this.maze[i][coord - 1] = 1;
            }
        }
    }

    protected void buildWallWithRange(boolean orientation, int coord, int start, int stop){
        if (stop > SPLITS){
            stop = SPLITS;
        }
        if(orientation){ //vertical wall
            System.out.println("Build vertical wall in " + coord + " from " + start + " to " + stop);
            for(int i = start; i < stop; i++){
                this.maze[coord][i] = 1;
            }
        } else{
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

    protected int generateInCore(int splits){
        return generateInRange(2, splits - 2);
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
        if(x > 1 && y > 1 && x < SPLITS-1 && y < SPLITS-1){
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
        for(int i = 1; i < SPLITS-1; i++){
            for(int j = 1; j < SPLITS-1; j++){
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

    protected boolean isVerticalWallAllowed(int coord, int startY, int endY){
        if (startY == 0 && endY == SPLITS - 1) {
            //starting walls
        }
        else if(endY == SPLITS - 1){
            if (maze[coord][startY - 1] == 0) {
                System.out.println();
                System.out.println("Wall from " + coord + ", " + startY + " to " + coord + "," + endY + " not good");
                System.out.println();
                return  true;
            }
        }else if(startY == 0){
            if (maze[coord][startY + 1] == 0) {
                System.out.println();
                System.out.println("Wall from " + coord + ", " + startY + " to " + coord + "," + endY + " not good");
                System.out.println();
                return true;
            }
        } else if(startY > 0) {
            if (maze[coord][startY - 1] == 0 || maze[coord][endY + 1] == 0) {
                System.out.println();
                System.out.println("Wall from " + coord + ", " + startY + " to " + coord + "," + endY + " not good");
                System.out.println();
                return true;
            }
        }
        return false;
    }

    protected boolean isHorizontalWallAllowed(int coord, int startX, int endX) {
        if (startX == 0 && endX == SPLITS - 1) {

        }
        else if(endX == SPLITS -1){
            if(maze[startX - 1][coord] == 0){
                System.out.println();
                System.out.println("Wall from " + startX + ", " + coord + " to " + endX + "," + coord + " not good");
                System.out.println();
                return true;
            }
        } else if(startX == 0){
            if (maze[endX + 1][coord] == 0) {
                System.out.println();
                System.out.println("Wall from " + startX + ", " + coord + " to " + endX + "," + coord + " not good");
                System.out.println();
                return true;
            }
        } else if(startX > 0){
            if (maze[startX - 1][coord] == 0 || maze[endX + 1][coord] == 0) {
                System.out.println();
                System.out.println("Wall from " + startX + ", " + coord + " to " + endX + "," + coord + " not good");
                System.out.println();
                return true;
            }
        }
        return false;
    }
}
