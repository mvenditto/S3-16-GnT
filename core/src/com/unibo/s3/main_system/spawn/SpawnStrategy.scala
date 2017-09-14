package com.unibo.s3.main_system.spawn

import com.badlogic.gdx.math.Vector2

trait SpawnStrategy {

    def  generateSpawnQuadrant(map: Array[Array[Int]]): Vector2

}
