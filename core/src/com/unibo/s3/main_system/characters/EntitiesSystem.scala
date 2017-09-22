package com.unibo.s3.main_system.characters

import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.communication.CharacterActors

/**
  * This trait is used to generate a new character
  * @author Daniele Rosetti
  */
trait EntitiesSystem {
  type CharacterActors = CharacterActors.Value

  /**
    * Generate a new BaseCharacter
    * @param character Type of character to generate
    * @param position Place where new character is spawn
    * @param Id A number to identify the BaseCharacter
    * @return New BaseCharacter
    */
  def spawnEntityAt(character: CharacterActors, position: Vector2, Id: Int): BaseCharacter

  /**
    * this method return all character
    * @return All BaseCharacter
    */
  def getEntities: Iterable[BaseCharacter]
}
