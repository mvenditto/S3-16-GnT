package com.unibo.s3.main_system.communication

import akka.actor.{Props, UntypedAbstractActor}
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.spawn.{GuardStrategy, SpawnPointGenerator, ThiefStrategy}
import com.unibo.s3.main_system.util.GntUtils

class SpawnActor extends UntypedAbstractActor {

  private[this] var wall_thickness: Int = _
  private[this] val spawnGenerator = new SpawnPointGenerator
  private[this] var map: Array[Array[Int]] = _

  private[this] val guardStrategy = GuardStrategy()

  override def onReceive(message: Any): Unit = message match {
    case msg: MapSettingsMsg =>
      this.map = Array.ofDim[Int](msg.width, msg.height)

    case msg: MapElementMsg =>
      val lineElements = GntUtils.parseMapEntry(msg.line)
      if (lineElements._1.forall(value => value != 0.0
        && value != (this.map.length * this.wall_thickness + 2 * this.wall_thickness)
        && lineElements._2.isEmpty)) {
        this.wall_thickness = lineElements._1(2).toInt
        val x = lineElements._1(0).toInt
        val y = lineElements._1(1).toInt

        def translation(start: Int): Int = {
          (start - (this.wall_thickness / 2) - this.wall_thickness) / this.wall_thickness
        }

        this.map(translation(x))(translation(y)) = 1
      }

    case msg: GenerateNewCharacterPositionMsg =>
      if (msg.characterType.equals(CharacterActors.GUARD))
        this.spawnGenerator.setSpawnStrategy(guardStrategy)
      else
        this.spawnGenerator.setSpawnStrategy(ThiefStrategy())
      SystemManager.getLocalGeneralActor(GeneralActors.MASTER_ACTOR)
        .tell(CreateCharacterMsg(this.spawnGenerator.generateSpawnPoints(this.map, 1).get(0), msg.characterType), getSelf())

    case _ => println("(spawnActor) message unknown:" + message)
  }
}

object SpawnActor {
  def props(): Props = Props(new SpawnActor())
}
