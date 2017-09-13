package com.unibo.s3.main_system.characters

import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.communication.CharacterActors

class EntitiesSystemImpl extends EntitiesSystem {

  private[this] var charactersList = List[BaseCharacter]()


  override def spawnEntityAt(character: CharacterActors, position: Vector2, Id: Int): BaseCharacter = character match {
    //val newEntity = new BaseCharacter(position, ID)
    case CharacterActors.GUARD =>
      val newEntity = Guard(position, Id)
      charactersList :+= newEntity
      newEntity
    case CharacterActors.THIEF =>
      val newEntity = Thief(position, Id)
      charactersList :+= newEntity
      newEntity
  }

  override def getEntities: Iterable[BaseCharacter] = {
    charactersList
  }
}
