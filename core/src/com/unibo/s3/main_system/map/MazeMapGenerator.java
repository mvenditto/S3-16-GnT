package com.unibo.s3.main_system.map;

public class MazeMapGenerator extends AbstractMapGenerator {

    @Override
    public void generate(int n, int width, int height, int startX, int startY){
        //System.out.println(n + " Matrice " + width + "x" + height + " starting from " + startX + "," + startY);
        //System.out.println("V  range is " + width + " good value between " + lowerX + " and " + upperX);
        //System.out.println("H  range is " + height + " good value between " + lowerY + " and " + upperY);
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
        //System.out.println("First door " + (startX + wallV) + "," + door1Coord);
        //System.out.println("Second door " + door2Coord + "," + (startY + wallH));
        //System.out.println("Third door " + (startX + wallV) + "," + door3Coord);
        //System.out.println("Fourth door " + door4Coord + "," + (startY + wallH));

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

        if (n <= 2) {
            return;
        }else{
            generate(n/2, wallV, wallH, startX, startY);
            generate(n/2, width - wallV , wallH, startX+wallV+1, startY);
            generate(n/2, wallV, height - wallH, startX, startY+wallH+1);
            generate(n/2, width - wallV, height - wallH,startX+wallV+1, startY+wallH+1);
        }
    }
}
