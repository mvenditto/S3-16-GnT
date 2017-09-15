package com.unibo.s3.main_system.communication

import akka.actor.{Props, UntypedAbstractActor}
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.game.{GameSettings, Wall}
import com.unibo.s3.main_system.spawn.{GuardSpawningStrategy, SpawnPointGenerator, ThiefSpawningStrategy}
import com.unibo.s3.main_system.util.GntUtils

class SpawnActor extends UntypedAbstractActor {

  private val WALL_THICKNESS = Wall.WALL_THICKNESS
  private val WALL_NUMBER = 2
  private[this] val spawnGenerator = new SpawnPointGenerator
  private[this] var map: Array[Array[Int]] = _

  private[this] val guardStrategy = GuardSpawningStrategy()

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
  }
}

object SpawnActor {
  def props(): Props = Props(new SpawnActor())
}
