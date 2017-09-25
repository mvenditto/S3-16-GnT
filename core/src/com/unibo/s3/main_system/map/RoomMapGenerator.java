package com.unibo.s3.main_system.map;

/**
 * Generator for room maps
 * @author Nicola Santolini
 */
public class RoomMapGenerator extends AbstractMapGenerator {

    private static final int ROOM_HEIGHT_THRESHOLD = 15;

    @Override
    public void generate(int width, int height, int startX, int startY){
        int lowerX = (width/2);
        int upperX = (width/2);
        int lowerY = (height/2);
        int upperY = (height/2);
        width--;
        height--;
        /*relative position, not absolute*/
        int wallV = generateInRange(lowerX, upperX);
        int wallH = generateInRange(lowerY, upperY);
        /*senso antiorario partendo da sopra*/
        int door1Coord = generateInRange(startY + wallH + 1, startY + height);
        int door2Coord;
        if(getVerticalOrHorizontal()){
            door2Coord = generateInRange(startX + 1, startX + wallV - 1);
        }
        int door3Coord = generateInRange(startY + 1, startY + wallH - 1);
        door2Coord = generateInRange(startX + wallV + 1, startX + width);
         if(isHorizontalWallDenied(startY + wallH, startX, startX+width+1)){
            buildWallWithRange(false, startY + wallH, startX + 1, startX+width);
        }else{
            buildWallWithRange(false, startY + wallH, startX, startX+width+1);
        }

        if(isVerticalWallDenied(startX + wallV, startY, startY+height+1 )){
            buildWallWithRange(true, startX + wallV, startY + 1, startY+height);
        }else{
            buildWallWithRange(true, startX + wallV, startY, startY+height+1);
        }

        buildDoor((startX + wallV), door1Coord);
        buildDoor(door2Coord, (startY + wallH));
        buildDoor((startX + wallV), door3Coord);
        if (height > ROOM_HEIGHT_THRESHOLD){
            generate(wallV, wallH, startX, startY);
            generate(width - wallV , wallH, startX+wallV+1, startY);
            generate(wallV, height - wallH, startX, startY+wallH+1);
            generate(width - wallV, height - wallH,startX+wallV+1, startY+wallH+1);
        }
    }
}
