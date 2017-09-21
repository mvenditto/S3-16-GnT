package com.unibo.s3.main_system.communication

import java.util

import akka.actor.{Props, Stash, UntypedAbstractActor}
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.game.{AkkaSettings, GameSettings, Wall}
import com.unibo.s3.main_system.spawn.{GuardStrategy, SpawnPointGenerator, ThiefStrategy}
import com.unibo.s3.main_system.util.GntUtils

class SpawnActor extends UntypedAbstractActor with Stash {

  private val WALL_THICKNESS = Wall.WALL_THICKNESS
  private val WALL_NUMBER = 2
  private[this] val spawnGenerator = new SpawnPointGenerator
  private[this] var map: Array[Array[Int]] = _

  private[this] val guardStrategy = GuardStrategy()

  /*
  override def onReceive(message: Any): Unit = message match {
    case msg: MapSettingsMsg =>
      this.map = Array.ofDim[Int](msg.width, msg.height)

    case msg: MapElementMsg =>
      val lineElements = GntUtils.parseMapEntry(msg.line)
      if (lineElements._1.forall(value => value != 0.0
        && value != (this.map.length * WALL_THICKNESS + WALL_NUMBER * WALL_THICKNESS)
        && lineElements._2.isEmpty)) {
        val x = lineElements._1(0).toInt
        val y = lineElements._1(1).toInt

        def translation(start: Int): Int = {
          (start - (WALL_THICKNESS / 2) - WALL_THICKNESS) / WALL_THICKNESS
        }

        this.map(translation(x))(translation(y)) = 1
      }

    case msg: GenerateNewCharacterPositionMsg =>
      if (msg.characterType.equals(CharacterActors.GUARD))
        this.spawnGenerator.setSpawnStrategy(guardStrategy)
      else
        this.spawnGenerator.setSpawnStrategy(ThiefSpawningStrategy())
        sender ! CreateCharacterMsg(this.spawnGenerator.generateSpawnPoints(this.map, 1).get(0), msg.characterType)

    case _ => println("(spawnActor) message unknown:" + message)
  }*/

  context.become(mapSettings())

  override def onReceive(message: Any): Unit = {}

  private def mapSettings(): Receive = {
    case GameSettingsMsg(g) =>
      val wt = g.mapSize.cpy().scl(1.0f / Wall.WALL_THICKNESS)
      this.map = Array.ofDim[Int](wt.x.toInt, wt.y.toInt)
      self ! GenerateNewCharacterPositionMsg(g.guardsNumber, CharacterActors.GUARD)
      self ! GenerateNewCharacterPositionMsg(g.thievesNumber, CharacterActors.THIEF)
      context.become(setMatrix())
      unstashAll()
    case _ => stash()
  }

  private def setMatrix(): Receive = {
    case msg: MapElementMsg =>
      val lineElements = GntUtils.parseMapEntry(msg.line)
      if (lineElements._1.forall(value => value != 0.0
        && value != (this.map.length * WALL_THICKNESS + WALL_NUMBER * WALL_THICKNESS)
        && lineElements._2.isEmpty)) {
        val x = lineElements._1(0).toInt
        val y = lineElements._1(1).toInt

        def translation(start: Int): Int = {
          (start - (WALL_THICKNESS / 2) - WALL_THICKNESS) / WALL_THICKNESS
        }

        this.map(translation(x))(translation(y)) = 1
      }
      if(lineElements._1.forall(value => value == 0.0)) {
        context.become(spawn())
        unstashAll()
      }

    case _ => stash()
  }

  private def spawn():Receive = {
    case msg: GenerateNewCharacterPositionMsg =>
      if (msg.characterType.equals(CharacterActors.GUARD))
        this.spawnGenerator.setSpawnStrategy(guardStrategy)
      else
        this.spawnGenerator.setSpawnStrategy(ThiefStrategy())

      val ref = SystemManager.getRemoteActor(AkkaSettings.GUISystem, "/user/",
        GeneralActors.MASTER_ACTOR.name)
      //ref ! CiaoMsg
      val list: util.List[Vector2] = this.spawnGenerator.generateSpawnPoints(this.map, msg.num)
      //println("Generati " + list.size() + " punti di spawn")
      val it = list.iterator()
      while(it.hasNext)
        ref ! CreateCharacterMsg(it.next(), msg.characterType)
        //ref ! CreateCharacterMsg(pos, msg.characterType))

  }
}

object SpawnActor {
  def props(): Props = Props(new SpawnActor())
}
