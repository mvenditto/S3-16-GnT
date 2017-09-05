package com.unibo.s3.main_system.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractMapGenerator implements GenerationStrategy{

    public static final int BASE_UNIT = 2;
    private static final float HALF_BASE_UNIT = BASE_UNIT/2;
    private static final int MAP_WIDTH = 60;
    private static final int MAP_HEIGHT = 60;
    private static final int WIDTH_SPLITS = (int) (MAP_WIDTH / BASE_UNIT);
    private static final int HEIGHT_SPLITS = (int) (MAP_HEIGHT / BASE_UNIT);
    private static final String END_OF_FILE = "0.0:0.0:0.0:0.0";
    private static final String SEPARATOR = ":";

    private int[][] generatedMap = new int[WIDTH_SPLITS][HEIGHT_SPLITS];

    public List<String> getMap(){
        ArrayList<String> map = new ArrayList<>();
        for(int i = 0; i < WIDTH_SPLITS; i++){
            for (int j = 0; j < HEIGHT_SPLITS; j++){
                if(generatedMap[i][j] == 1){
                    map.add(((i * BASE_UNIT + HALF_BASE_UNIT) + BASE_UNIT) + SEPARATOR + ((j * BASE_UNIT + HALF_BASE_UNIT) + BASE_UNIT) + SEPARATOR + BASE_UNIT + SEPARATOR + BASE_UNIT);
                }
            }
        }
        map.addAll(generatePerimeterWalls());
        map.add(END_OF_FILE);
        return map;
    }

    private ArrayList<String> generatePerimeterWalls(){
        ArrayList<String> perimeter = new ArrayList<>();
        perimeter.add(HALF_BASE_UNIT + SEPARATOR + ((HEIGHT_SPLITS * BASE_UNIT)/2 + BASE_UNIT) + SEPARATOR + BASE_UNIT + SEPARATOR +  (BASE_UNIT * HEIGHT_SPLITS + BASE_UNIT));
        perimeter.add(((WIDTH_SPLITS * BASE_UNIT) + BASE_UNIT + HALF_BASE_UNIT) + SEPARATOR + ((HEIGHT_SPLITS * BASE_UNIT)/2 + BASE_UNIT) + SEPARATOR + BASE_UNIT + SEPARATOR + (BASE_UNIT * HEIGHT_SPLITS + BASE_UNIT));
        perimeter.add(((WIDTH_SPLITS * BASE_UNIT)/2 + BASE_UNIT) + SEPARATOR + HALF_BASE_UNIT + SEPARATOR + (BASE_UNIT * WIDTH_SPLITS + BASE_UNIT) + SEPARATOR + BASE_UNIT);
        perimeter.add(((WIDTH_SPLITS * BASE_UNIT)/2 + BASE_UNIT) + SEPARATOR + (HEIGHT_SPLITS * BASE_UNIT + BASE_UNIT + HALF_BASE_UNIT) + SEPARATOR + (BASE_UNIT * WIDTH_SPLITS + BASE_UNIT + SEPARATOR + BASE_UNIT));

        return perimeter;
    }

    protected void buildWall(boolean orientation, int coord){
        if(orientation){ //vertical wall
            for(int i = 0; i < WIDTH_SPLITS; i++){
                this.generatedMap[coord - 1][i] = 1;
            }
        } else{
            for(int i = 0; i < HEIGHT_SPLITS; i++){
                this.generatedMap[i][coord - 1] = 1;
            }
        }
    }

    protected void buildWallWithRange(boolean orientation, int coordinate, int start, int stop){

        if(orientation){ //vertical wall
            if (stop > HEIGHT_SPLITS){
                stop = HEIGHT_SPLITS;
            }
            for(int i = start; i < stop; i++){
                this.generatedMap[coordinate][i] = 1;
            }
        } else{
            if (stop > WIDTH_SPLITS){
                stop = WIDTH_SPLITS;
            }
            for(int i = start; i < stop; i++){
                this.generatedMap[i][coordinate] = 1;
            }
        }
    }

    protected void buildDoor(int x, int y){ generatedMap[x][y] = 0; }

    /**true vertical, false horizontal**/
    protected boolean getVerticalOrHorizontal(){
        return new Random().nextBoolean();
    }

    protected int generateInRange(int lowerBound, int upperbound){
        int out = -1;
        while(out < lowerBound){
            //System.out.println("bound: " + lowerBound);
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
            System.out.println(generatedMap[x][y+1] + " " + generatedMap[x+1][y] + " " + generatedMap[x][y-1] + " " + generatedMap[x-1][y]);
            if(generatedMap[x][y+1] == 1 && generatedMap[x+1][y] == 1 && generatedMap[x][y-1] == 1 && generatedMap[x-1][y] == 1){
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
                if(generatedMap[i][j] == 0 && generatedMap[i][j+1] == 1 && generatedMap[i+1][j] == 1 && generatedMap[i][j-1] == 1 && generatedMap[i-1][j] == 1){
                    System.out.println();
                    System.out.println(i + " " + j + " is a bad INTERSECTION!");
                    System.out.println();
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isVerticalWallDenied(int coordinate, int startY, int endY){
        if (startY == 0 && endY == HEIGHT_SPLITS) {
            //System.out.println("Initial wall OK");
        }
        else if(endY == HEIGHT_SPLITS){
            if (generatedMap[coordinate][startY - 1] == 0) {
                return  true;
            }
        }else if(startY == 0){
            if (generatedMap[coordinate][endY] == 0) {
                System.out.println();
                return true;
            }
        } else {
            if (generatedMap[coordinate][startY - 1] == 0 || generatedMap[coordinate][endY] == 0) {
                System.out.println();
                return true;
            }
        }
        return false;
    }

    protected boolean isHorizontalWallDenied(int coordinate, int startX, int endX) {
        if (startX == 0 && endX == WIDTH_SPLITS) {
            //System.out.println("Initial wall OK");
        }
        else if(endX == WIDTH_SPLITS){
            if(generatedMap[startX - 1][coordinate] == 0){
                System.out.println();
                return true;
            }
        } else if(startX == 0){
            if (generatedMap[endX][coordinate] == 0) {
                System.out.println();
                return true;
            }
        } else {
            if (generatedMap[startX - 1][coordinate] == 0 || generatedMap[endX][coordinate] == 0) {
                System.out.println();
                return true;
            }
        }
        return false;
    }
}
