package com.unibo.s3.main_system.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractMapGenerator implements GenerationStrategy{


    public static final int BASE_UNIT = 2;
    private static final float HALF_BASE_UNIT = BASE_UNIT/2;
   // private static final int MAP_WIDTH = 60;
   // private static final int MAP_HEIGHT = 60;

    private int widthSplits;// = (int) (MAP_WIDTH / BASE_UNIT);
    private int heightSplits;// = (int) (MAP_HEIGHT / BASE_UNIT);
    private static final String END_OF_FILE = "0.0:0.0:0.0:0.0";
    private static final String SEPARATOR = ":";

    private int[][] generatedMap;// = new int[widthSplits][heightSplits];


    public void initialSetup(int width, int height){
        this.widthSplits = width;
        this.heightSplits = height;
        this.generatedMap = new int[widthSplits][heightSplits];
        System.out.println("splits: " + widthSplits + " " + heightSplits);
        System.out.println("Generated matrix " + generatedMap.length + "x" + generatedMap[0].length);
    }

    public List<String> getMap(){
        ArrayList<String> map = new ArrayList<>();
        for(int i = 0; i < widthSplits; i++){
            for (int j = 0; j < heightSplits; j++){
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
        perimeter.add(HALF_BASE_UNIT + SEPARATOR + ((heightSplits * BASE_UNIT)/2 + BASE_UNIT) + SEPARATOR + BASE_UNIT + SEPARATOR +  (BASE_UNIT * heightSplits + BASE_UNIT));
        perimeter.add(((widthSplits * BASE_UNIT) + BASE_UNIT + HALF_BASE_UNIT) + SEPARATOR + ((heightSplits * BASE_UNIT)/2 + BASE_UNIT) + SEPARATOR + BASE_UNIT + SEPARATOR + (BASE_UNIT * heightSplits + BASE_UNIT));
        perimeter.add(((widthSplits * BASE_UNIT)/2 + BASE_UNIT) + SEPARATOR + HALF_BASE_UNIT + SEPARATOR + (BASE_UNIT * widthSplits + BASE_UNIT) + SEPARATOR + BASE_UNIT);
        perimeter.add(((widthSplits * BASE_UNIT)/2 + BASE_UNIT) + SEPARATOR + (heightSplits * BASE_UNIT + BASE_UNIT + HALF_BASE_UNIT) + SEPARATOR + (BASE_UNIT * widthSplits + BASE_UNIT + SEPARATOR + BASE_UNIT));

        return perimeter;
    }

    protected void buildWall(boolean orientation, int coord){
        if(orientation){ //vertical wall
            for(int i = 0; i < widthSplits; i++){
                this.generatedMap[coord - 1][i] = 1;
            }
        } else{
            for(int i = 0; i < heightSplits; i++){
                this.generatedMap[i][coord - 1] = 1;
            }
        }
    }

    protected void buildWallWithRange(boolean orientation, int coordinate, int start, int stop){

        if(orientation){ //vertical wall
            if (stop > heightSplits){
                stop = heightSplits;
            }
            for(int i = start; i < stop; i++){
                this.generatedMap[coordinate][i] = 1;
            }
        } else{
            if (stop > widthSplits){
                stop = widthSplits;
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
        if(x > 1 && y > 1 && x < widthSplits -1 && y < heightSplits -1){
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
        for(int i = 1; i < widthSplits -1; i++){
            for(int j = 1; j < heightSplits -1; j++){
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
        if (startY == 0 && endY == heightSplits) {
            //System.out.println("Initial wall OK");
        }
        else if(endY == heightSplits){
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
        if (startX == 0 && endX == widthSplits) {
            //System.out.println("Initial wall OK");
        }
        else if(endX == widthSplits){
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
