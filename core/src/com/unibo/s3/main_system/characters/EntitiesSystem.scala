package com.unibo.s3.main_system.characters

import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.communication.CharacterActors

trait EntitiesSystem {
  type CharacterActors = CharacterActors.Value

  def spawnEntityAt(character: CharacterActors, position: Vector2, Id: Int): BaseCharacter

  def getEntities: Iterable[BaseCharacter]
}
