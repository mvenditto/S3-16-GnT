package com.unibo.s3.main_system.spawn

import com.badlogic.gdx.math.Vector2

trait SpawnStrategy {

    def  generateSpawnQuadrant(map: Array[Array[Int]]): Vector2

    /** true, spawn point allowed / false, spawn point denied **/
    protected def checkAllowedPosition(map: Array[Array[Int]], x: Int, y: Int) = map(x)(y) == 0

}
