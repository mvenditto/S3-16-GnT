package com.unibo.s3.main_system.map;

/**
 * @author Nicola Santolini
 */
public class MazeMapGenerator extends AbstractMapGenerator {

    private static final int MAZE_HEIGHT_THRESHOLD = 10;

    @Override
    public void generate(int width, int height, int startX, int startY){
        /*relative position, not absolute*/
        int wallV = width/2;
        int wallH = height/2;
        width--;
        height--;
        /*senso antiorario partendo da sopra*/
        int door1Coordinate = generateInRange(startY + wallH + 1, startY + height);
        int door2Coordinate = generateInRange(startX + 1, startX + wallV - 1);
        int door3Coordinate = generateInRange(startY + 1, startY + wallH - 1);
        int door4Coordinate = generateInRange(startX + wallV + 1, startX + width);

        if(isHorizontalWallDenied(startY + wallH, startX, startX+width+1)){
            buildWallWithRange(false, startY + wallH, startX + 1, startX+width);
        }else{
            buildWallWithRange(false, startY + wallH, startX, startX+width+1);
        }
        if(isVerticalWallDenied(startX+wallV, startY,startY+height+1)){
            buildWallWithRange(true, startX + wallV, startY + 1, startY+height);
        }else{
            buildWallWithRange(true, startX + wallV, startY, startY+height+1);
        }

        buildDoor((startX + wallV), door1Coordinate);
        buildDoor(door2Coordinate, (startY + wallH));
        buildDoor((startX + wallV), door3Coordinate);
        buildDoor(door4Coordinate, (startY + wallH));

        if (height > MAZE_HEIGHT_THRESHOLD){
            generate(wallV, wallH, startX, startY);
            generate(width - wallV , wallH, startX+wallV+1, startY);
            generate(wallV, height - wallH, startX, startY+wallH+1);
            generate(width - wallV, height - wallH,startX+wallV+1, startY+wallH+1);
        }
    }
}
