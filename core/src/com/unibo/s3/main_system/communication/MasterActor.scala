package com.unibo.s3.main_system.communication

import akka.actor.{ActorRef, Props, Stash, UntypedAbstractActor}
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.Guard.Guard
import com.unibo.s3.main_system.characters.Thief.Thief
import com.unibo.s3.main_system.characters.EntitiesSystemImpl
import com.unibo.s3.main_system.characters.steer.collisions.Box2dProxyDetectorsFactory
import com.unibo.s3.main_system.communication.Messages._

class MasterActor extends UntypedAbstractActor with Stash {

  private[this] var charactersList = List[ActorRef]()
  private[this] val entitiesSystem = new EntitiesSystemImpl()
  private[this] var collisionDetector: RaycastCollisionDetector[Vector2] = _
  private[this] var guardID = 0
  private[this] var thiefID = 0

  override def onReceive(message: Any): Unit = message match {

    case msg: ActMsg =>
      SystemManager.getLocalGeneralActor(GeneralActors.WORLD_ACTOR).tell(msg, getSelf())
      SystemManager.getLocalGeneralActor(GeneralActors.QUAD_TREE_ACTOR).tell(RebuildQuadTreeMsg(), getSelf())
      charactersList.foreach(cop => cop.tell(msg, getSelf()))
      //manca il ladro o i ladri

    case msg: CreateCharacterMsg =>
      if(msg.characterType.equals(CharacterActors.GUARD)) guardID = guardID + 1 else thiefID = thiefID + 1

      val newCharacter = entitiesSystem.spawnEntityAt(msg.characterType, msg.position, guardID)
      newCharacter.setColor(Color.ORANGE)

      if (collisionDetector == null) {
        val worldActorRef = SystemManager.getLocalGeneralActor(GeneralActors.WORLD_ACTOR)
        collisionDetector = new Box2dProxyDetectorsFactory(worldActorRef).newRaycastCollisionDetector()
      }

      newCharacter.setCollisionDetector(collisionDetector)
      newCharacter
        .setComplexSteeringBehavior()
        .avoidCollisionsWithWorld()
        .wander()
        .buildPriority(true)

      var characterRef: ActorRef = null
      if(msg.characterType.equals(CharacterActors.GUARD))
        characterRef = SystemManager.createCharacterActor(
          GuardActor.props(newCharacter.asInstanceOf[Guard]), CharacterActors.GUARD, guardID)
      else
        characterRef = SystemManager.createCharacterActor(
          ThiefActor.props(newCharacter.asInstanceOf[Thief]), CharacterActors.THIEF, thiefID)
      charactersList :+= characterRef
      SystemManager.getLocalGeneralActor(GeneralActors.QUAD_TREE_ACTOR)
        .tell(InitialSavingCharacterMsg(newCharacter, characterRef), getSelf())
      SystemManager.getLocalGeneralActor(GeneralActors.GRAPH_ACTOR)
        .tell(InitialSavingCharacterMsg(newCharacter, characterRef), getSelf())

    case _ => println("(masterActor) message unknown: " + message)
  }
}

object MasterActor {
  def props(): Props = Props(new MasterActor())
}