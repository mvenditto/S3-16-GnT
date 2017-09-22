package com.unibo.s3.main_system.spawn

import com.badlogic.gdx.math.Vector2

/**
  * @author Nicola Santolini
  */
trait SpawnStrategy {

    /**
      * Method that generates a spawn point
      * @param map where the new point is checked
      * @return A new spawn point
      */
    def generateSpawnQuadrant(map: Array[Array[Int]]): Vector2

    /**
      * Checks if a point is correct
      * @param map where the new point is checked
      * @param x coordinate of the point
      * @param y coordinate of the point
      * @return true if the point is correct
      */
    protected def checkAllowedPosition(map: Array[Array[Int]], x: Int, y: Int) = map(x)(y) == 0

}
