package com.unibo.s3.main_system.map;

public class MazeMapGenerator extends AbstractMapGenerator {


    @Override
    public void generate(int width, int height, int startX, int startY){
        /**relative position, not absolute*/
        int wallV = width/2;
        int wallH = height/2;
        width--;
        height--;
        /**senso antiorario partendo da sopra*/
        int door1Coord = generateInRange(startY + wallH + 1, startY + height);
        int door2Coord = generateInRange(startX + 1, startX + wallV - 1);
        int door3Coord = generateInRange(startY + 1, startY + wallH - 1);
        int door4Coord = generateInRange(startX + wallV + 1, startX + width);

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

        buildDoor((startX + wallV), door1Coord);
        buildDoor(door2Coord, (startY + wallH));
        buildDoor((startX + wallV), door3Coord);
        buildDoor(door4Coord, (startY + wallH));

        if (height > 10){
            generate(wallV, wallH, startX, startY);
            generate(width - wallV , wallH, startX+wallV+1, startY);
            generate(wallV, height - wallH, startX, startY+wallH+1);
            generate(width - wallV, height - wallH,startX+wallV+1, startY+wallH+1);
        }
    }
}
