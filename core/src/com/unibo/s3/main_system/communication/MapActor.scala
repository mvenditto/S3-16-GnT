package com.unibo.s3.main_system.communication

import scala.collection.JavaConversions.asScalaBuffer
import akka.actor.{Props, Stash, UntypedAbstractActor}
import com.unibo.s3.main_system.communication.Messages.{GameSettingsMsg, GenerateMapMsg, MapElementMsg, RestartMsg}
import com.unibo.s3.main_system.game.{AkkaSystemNames, Maze, Rooms}
import com.unibo.s3.main_system.map.{MapGenerator, MazeMapGenerator, RoomMapGenerator}


/**
  * Class used to wrap map creation
  * @author Daniele Rosetti
  */
class MapActor extends UntypedAbstractActor with Stash {

  private[this] val mapGenerator: MapGenerator = new MapGenerator
  private[this] var mapWidth: Int = _
  private[this] var mapHeight: Int = _
  private[this] var mapType: Boolean = true

  context.become(this.settings)

  override def onReceive(message: Any): Unit = {}

  private def settings: Receive = {
    case GameSettingsMsg(gs) =>
      val w = gs.mapSize.x.toInt
      val h = gs.mapSize.y.toInt
      this.mapWidth = w
      this.mapHeight = h
      gs.mapType match {
        case Maze => this.mapType = true
        case Rooms => this.mapType = false
      }
      context.become(generateMap)
      unstashAll()

    case _ => stash()
  }

  private def generateMap: Receive = {
    case _: GenerateMapMsg =>
      mapType match {
        case true =>
          this.mapGenerator.setStrategy(new MazeMapGenerator)
        case false =>
          this.mapGenerator.setStrategy(new RoomMapGenerator)
      }
      this.mapGenerator.generateMap(this.mapWidth, this.mapHeight)
      this.mapGenerator.getMap.foreach(line => {
        SystemManager.getLocalActor(GeneralActors.GRAPH_ACTOR).tell(MapElementMsg(line), getSelf())
        val refWorld = SystemManager.getRemoteActor(AkkaSystemNames.GUISystem, "/user/",
          GeneralActors.WORLD_ACTOR.name)
          refWorld.tell(MapElementMsg(line), getSelf())
        SystemManager.getLocalActor(GeneralActors.SPAWN_ACTOR).tell(MapElementMsg(line), getSelf())
      })

    case _: RestartMsg =>
      context.become(this.settings)

    case _ =>
  }
}

/**
  * Companion object of MapActor
  * @author Daniele Rosetti
  */
object MapActor {
  def props(): Props = Props(new MapActor())
}
