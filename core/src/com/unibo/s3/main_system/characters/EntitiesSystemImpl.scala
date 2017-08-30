package com.unibo.s3.main_system.characters

import com.badlogic.gdx.math.Vector2

class EntitiesSystemImpl extends EntitiesSystem {

  var charactersList = List[BaseCharacter]()

  override def spawnEntityAt(position: Vector2, ID: Int) = {
    val newEntity = new BaseCharacter(position, ID)
    charactersList :+= newEntity
    newEntity
  }

  override def getEntities = {
    charactersList
  }
}
