package com.unibo.s3.main_system.game

import com.badlogic.gdx.math.Vector2

trait GameMode
case object Simulated extends GameMode
case object Interactive extends GameMode

trait MapType
case object Maze extends MapType
case object Rooms extends MapType

case class GameSettings(
  guardsNumber: Int,
  thievesNumber: Int,
  mapSize: Vector2,
  gameMode: GameMode,
  mapType: MapType
)
