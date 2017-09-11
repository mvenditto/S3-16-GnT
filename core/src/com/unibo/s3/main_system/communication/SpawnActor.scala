package com.unibo.s3.main_system.communication

import akka.actor.{Props, UntypedAbstractActor}
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.map.{RandomSpawnPointGenerator, SpawnPointGenerator}

class SpawnActor extends UntypedAbstractActor {

  private[this] val WALL_THICKNESS = 3
  private[this] val spawnGenerator = new SpawnPointGenerator
  private[this] var map: Array[Array[Int]] = _

  this.spawnGenerator.setSpawnStrategy(new RandomSpawnPointGenerator())

  override def onReceive(message: Any): Unit = message match {
    case msg: MapSettingsMsg =>
      this.map = Array.ofDim[Int](msg.width, msg.height)
    case msg: MapElementMsg =>
      val lineElements = msg.line.split(":").map(_.toFloat)
      def addWall(lineElements: Array[Float]): Unit = lineElements match {
        case _ =>
          if (lineElements.forall(value => value != 0.0 && value != this.map.length+WALL_THICKNESS)) {
            val x = lineElements(0).toInt-WALL_THICKNESS
            val y = lineElements(1).toInt-WALL_THICKNESS
            val width = lineElements(2).toInt
            val height = lineElements(3).toInt
            def bottomBorder(start: Int, space: Int): Int = {
              start-(space-1)/2
            }
            def topBorder(start: Int, space: Int): Int = {
              start+(space-1)/2
            }
            for(i <- bottomBorder(x, width) to topBorder(x, width)) {
              for(j <- this.map.length-topBorder(y, height)-1 until this.map.length-bottomBorder(y, height)-1) {
                this.map(j)(i) = 1
              }
            }
          }
          else {
            this.map.foreach(line => {
              line.foreach(elem => print(elem))
              println()
            })
          }
      }
      addWall(lineElements)

    case _: GenerateNewCharacterPositionMsg =>
      //generare la posizione del nuovo agente
      SystemManager.getLocalGeneralActor(GeneralActors.MASTER_ACTOR)
        .tell(CreateCharacterMsg(this.spawnGenerator.generateSpawnPoints(this.map, 1).get(0)), getSelf())
        //.tell(CreateCharacterMsg(new Vector2(3, 3)), getSelf())

    case _ => println("(spawnActor) message unknown:" + message)
  }
}

object SpawnActor {
  def props(): Props = Props(new SpawnActor())
}
