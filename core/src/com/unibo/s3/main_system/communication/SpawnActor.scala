package com.unibo.s3.main_system.communication

import akka.actor.{Props, UntypedAbstractActor}
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.map.{RandomSpawnPointGenerator, SpawnPointGenerator}
import com.unibo.s3.main_system.util.GntUtils

class SpawnActor extends UntypedAbstractActor {

  private[this] var wall_thickness: Int = _
  private[this] val spawnGenerator = new SpawnPointGenerator
  private[this] var map: Array[Array[Int]] = _

  this.spawnGenerator.setSpawnStrategy(new RandomSpawnPointGenerator())

  override def onReceive(message: Any): Unit = message match {
    case msg: MapSettingsMsg =>
      this.map = Array.ofDim[Int](msg.width, msg.height)

    case msg: MapElementMsg =>
      val lineElements = GntUtils.parseMapEntry(msg.line)
      if (lineElements._1.forall(value => value != 0.0
        && value != (this.map.length*this.wall_thickness+2*this.wall_thickness)
        && lineElements._2.isEmpty)) {
        this.wall_thickness = lineElements._1(2).toInt
        val x = lineElements._1(0).toInt
        val y = lineElements._1(1).toInt
        def translation(start: Int): Int = {
          (start - (this.wall_thickness / 2) - this.wall_thickness) / this.wall_thickness
        }
        this.map(translation(x))(translation(y)) = 1
      }
      else {
        this.map.foreach(line => {
          line.foreach(elem => print(elem))
          println()
        })
      }

    case _: GenerateNewCharacterPositionMsg =>
      //generare la posizione del nuovo agente
      val spawnPoint = this.spawnGenerator.generateSpawnPoints(this.map, 1).get(0)
      SystemManager.getLocalGeneralActor(GeneralActors.MASTER_ACTOR)
        .tell(CreateCharacterMsg(new Vector2(spawnPoint.x + 2, spawnPoint.y + 2)), getSelf())
    case _ => println("(spawnActor) message unknown:" + message)
  }
}

object SpawnActor {
  def props(): Props = Props(new SpawnActor())
}
