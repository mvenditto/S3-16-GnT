package com.unibo.s3.main_system.game

import com.badlogic.gdx.math.Vector2

trait GameMode
case object Simulated extends GameMode
case object Interactive extends GameMode

trait MapType
case object Maze extends MapType
case object Rooms extends MapType

case class GameSettings(
   guardsNumber: Int = 5,
   thievesNumber: Int = 5,
   mapSize: Vector2 = new Vector2(60, 60),
   gameMode: GameMode = Simulated,
   mapType: MapType = Maze
)

object Wall {
  val WALL_THICKNESS: Int = 2
}
