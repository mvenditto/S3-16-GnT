package com.unibo.s3.main_system.communication

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.unibo.s3.main_system.characters.EntitiesSystemImpl
import com.unibo.s3.main_system.communication.Messages.{ActMsg, CreateCharacterMsg, InitialSavingCharacter, RebuildQuadTreeMsg}

class MasterActor extends UntypedAbstractActor {

  var charactersList = List[ActorRef]()
  val entitiesSystem = new EntitiesSystemImpl()
  var copID = 0

  override def onReceive(message: Any): Unit = message match {
    case msg: ActMsg =>
      SystemManager.getInstance().getLocalActor("worldActor").tell(msg, getSelf())
      //SystemManager.getInstance().getLocalActor("quadTreeActor").tell(RebuildQuadTreeMsg(), getSelf())
      charactersList.foreach(cop => cop.tell(msg, getSelf()))
      //manca il ladro o i ladri
    case msg: CreateCharacterMsg =>
      copID = copID + 1
      val newCharacter = entitiesSystem.spawnEntityAt(msg.position, copID)
      val characterRef = SystemManager.getInstance().createActor(CharacterActor.props(newCharacter), "cop"+copID)
      charactersList :+= characterRef
      SystemManager.getInstance().getLocalActor("quadTreeActor")
        .tell(InitialSavingCharacter(newCharacter, characterRef), getSelf())
    case _ => println("(worldActor) message unknown: " + message)
  }
}

object MasterActor {
  def props(): Props = Props(new MasterActor())
}