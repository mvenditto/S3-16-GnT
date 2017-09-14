package com.unibo.s3.main_system.spawn

import com.badlogic.gdx.math.Vector2

import scala.util.Random

class GuardStrategy extends SpawnStrategy {

  private val UP = 0
  private val RIGHT = 1
  private val DOWN = 2
  private val LEFT = 3

  private val WIDTH_PARTS_NUMBER = 4
  private val HEIGHT_PARTS_PARTS = 4

  private val HORIZONTAL_GUARD_PARTS = 9
  private val VERTICAL_GUARD_PARTS = 9

  private[this] var width_shift: Int = _
  private[this] var height_shift: Int = _
  private[this] var horizontalGuardShift: Int = _
  private[this] var verticalGuardShift: Int = _

  private[this] var guards = List[Vector2]()

  override def generateSpawnQuadrant(map: Array[Array[Int]]): Vector2 = {
    width_shift = map.length / WIDTH_PARTS_NUMBER
    height_shift = map(0).length / HEIGHT_PARTS_PARTS
    this.horizontalGuardShift = map.length / HORIZONTAL_GUARD_PARTS
    this.verticalGuardShift = map(0).length / VERTICAL_GUARD_PARTS
    spawnBySide(map, guards.size % 4)
  }

  private def spawnBySide(map: Array[Array[Int]], side: Int): Vector2 = side match {
    case UP =>
      spawnTop(map)
    case RIGHT =>
      spawnRight(map)
    case DOWN =>
      spawnBottom(map)
    case LEFT =>
      spawnLeft(map)
  }

  private def spawnTop(map: Array[Array[Int]]): Vector2 = {
    val x = Random.nextInt(this.width_shift * (WIDTH_PARTS_NUMBER - 1)) + this.width_shift
    val y = map(0).length - Random.nextInt(this.height_shift)
    val newGuards = new Vector2(x, y)
    this.guards :+= newGuards
    newGuards
  }

  private def spawnRight(map: Array[Array[Int]]): Vector2 = {
    val x = Random.nextInt(this.width_shift) + (WIDTH_PARTS_NUMBER - 1)
    val y = Random.nextInt(this.height_shift) * (HEIGHT_PARTS_PARTS - 1)
    val newGuards = new Vector2(x, y)
    this.guards :+= newGuards
    newGuards
  }

  private def spawnBottom(map: Array[Array[Int]]): Vector2 = {
    val x = Random.nextInt(this.width_shift * (WIDTH_PARTS_NUMBER - 1))
    val y = Random.nextInt(this.height_shift)
    val newGuards = new Vector2(x, y)
    this.guards :+= newGuards
    newGuards
  }

  private def spawnLeft(map: Array[Array[Int]]): Vector2 = {
    val x = Random.nextInt(this.width_shift)
    val y = Random.nextInt(this.height_shift * (HEIGHT_PARTS_PARTS - 1)) + this.height_shift
    val newGuards = new Vector2(x, y)
    this.guards :+= newGuards
    newGuards
  }



}

object GuardStrategy {
  def apply(): GuardStrategy = new GuardStrategy()
}