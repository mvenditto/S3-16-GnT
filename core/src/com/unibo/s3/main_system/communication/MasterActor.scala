package com.unibo.s3.main_system.communication

import akka.actor.{ActorRef, Props, Stash, UntypedAbstractActor}
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.Guard.Guard
import com.unibo.s3.main_system.characters.Thief.Thief
import com.unibo.s3.main_system.characters.{BaseCharacter, EntitiesSystemImpl}
import com.unibo.s3.main_system.characters.steer.collisions.Box2dProxyDetectorsFactory
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.game.AkkaSettings

class MasterActor extends UntypedAbstractActor with Stash {

  private[this] var charactersList = List[ActorRef]()
  private[this] val entitiesSystem = new EntitiesSystemImpl()
  private[this] var collisionDetector: RaycastCollisionDetector[Vector2] = _
  private[this] var characterID = 0
  /*
  override def onReceive(message: Any): Unit = message match {

    case msg: ActMsg =>
      SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR).tell(msg, getSelf())
      SystemManager.getLocalActor(GeneralActors.QUAD_TREE_ACTOR).tell(RebuildQuadTreeMsg(), getSelf())
      charactersList.foreach(cop => cop.tell(msg, getSelf()))
      //manca il ladro o i ladri

    case msg: CreateCharacterMsg =>

      def createCharacter(msg: CreateCharacterMsg): Unit = msg.characterType match {
        case CharacterActors.GUARD =>
          this.characterID = this.characterID + 1
          val newCharacter = entitiesSystem.spawnEntityAt(msg.characterType, msg.position, this.characterID).asInstanceOf[Guard]
          newCharacter.setColor(Color.BLUE)
          val characterRef = SystemManager.createActor(
            GuardActor.props(newCharacter), CharacterActors.GUARD, this.characterID)
          characterSettings(newCharacter, characterRef)
        case CharacterActors.THIEF =>
          this.characterID = this.characterID + 1
          val newCharacter = entitiesSystem.spawnEntityAt(msg.characterType, msg.position, this.characterID).asInstanceOf[Thief]
          newCharacter.setColor(Color.RED)
          newCharacter.setMaxLinearAcceleration(8f)
          newCharacter.setMaxLinearSpeed(2.5f)
          val characterRef = SystemManager.createActor(
            ThiefActor.props(newCharacter), CharacterActors.THIEF, this.characterID)
          characterSettings(newCharacter, characterRef)
      }

      def characterSettings(newCharacter: BaseCharacter, characterRef: ActorRef): Unit = {
        if (collisionDetector == null) {
          val worldActorRef = SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR)
          collisionDetector = new Box2dProxyDetectorsFactory(worldActorRef).newRaycastCollisionDetector()
        }

        newCharacter.setCollisionDetector(collisionDetector)

        charactersList :+= characterRef

        SystemManager.getLocalActor(GeneralActors.QUAD_TREE_ACTOR)
          .tell(InitialSavingCharacterMsg(newCharacter, characterRef), getSelf())
        SystemManager.getLocalActor(GeneralActors.GRAPH_ACTOR)
          .tell(AskForGraphMsg, characterRef)
      }

      createCharacter(msg)
    case _ => println("(masterActor) message unknown: " + message)
  }*/

  context.become(firstCreation)

  override def onReceive(message: Any): Unit = {}

  private def firstCreation: Receive  = {
    case msg: CreateCharacterMsg =>
      this.createCharacter(msg)
      context.become(this.actAndCreate)
  }

  private def actAndCreate: Receive = {
    case msg: ActMsg =>
      /*SystemManager.getRemoteActor(AkkaSettings.RemoteSystem, "/user/",
        GeneralActors.WORLD_ACTOR.name).tell(msg, getSelf())*/
      SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR).tell(msg, getSelf())
      SystemManager.getRemoteActor(AkkaSettings.RemoteSystem, "/user/",
        GeneralActors.QUAD_TREE_ACTOR.name).tell(RebuildQuadTreeMsg(), getSelf())
      charactersList.foreach(cop => cop.tell(msg, getSelf()))
    //manca il ladro o i ladri

    case msg: CreateCharacterMsg =>
      this.createCharacter(msg)
  }

  private def createCharacter(msg: CreateCharacterMsg): Unit = {
    def createCharacter(msg: CreateCharacterMsg): Unit = msg.characterType match {
      case CharacterActors.GUARD =>
        this.characterID = this.characterID + 1
        val newCharacter = entitiesSystem.spawnEntityAt(msg.characterType, msg.position, this.characterID).asInstanceOf[Guard]
        newCharacter.setColor(Color.BLUE)
        val characterRef = SystemManager.createActor(
          GuardActor.props(newCharacter), CharacterActors.GUARD, this.characterID)
        characterSettings(newCharacter, characterRef)
      case CharacterActors.THIEF =>
        this.characterID = this.characterID + 1
        val newCharacter = entitiesSystem.spawnEntityAt(msg.characterType, msg.position, this.characterID).asInstanceOf[Thief]
        newCharacter.setColor(Color.RED)
        newCharacter.setMaxLinearAcceleration(8f)
        newCharacter.setMaxLinearSpeed(2.5f)
        val characterRef = SystemManager.createActor(
          ThiefActor.props(newCharacter), CharacterActors.THIEF, this.characterID)
        characterSettings(newCharacter, characterRef)
    }

    def characterSettings(newCharacter: BaseCharacter, characterRef: ActorRef): Unit = {
      if (collisionDetector == null) {
        val worldActorRef = SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR)
        collisionDetector = Box2dProxyDetectorsFactory.of(worldActorRef).newRaycastCollisionDetector()
      }

      newCharacter.setCollisionDetector(collisionDetector)

      charactersList :+= characterRef

      SystemManager.getRemoteActor(AkkaSettings.RemoteSystem, "/user/",
        GeneralActors.QUAD_TREE_ACTOR.name)
        .tell(InitialSavingCharacterMsg(newCharacter, characterRef), getSelf())
      /*SystemManager.getRemoteActor(AkkaSettings.RemoteSystem, "/user/",
        GeneralActors.GRAPH_ACTOR.name).tell(AskForGraphMsg, characterRef)*/
      SystemManager.getLocalActor(GeneralActors.GRAPH_ACTOR).tell(AskForGraphMsg, characterRef)
    }

    createCharacter(msg)
  }
}

object MasterActor {
  def props(): Props = Props(new MasterActor())
}