package com.unibo.s3.main_system.characters

import com.badlogic.gdx.math.Vector2

trait EntitiesSystem {
  def spawnEntityAt(position: Vector2, ID: Int): BaseCharacter

  def getEntities(): Iterable[BaseCharacter]
}
