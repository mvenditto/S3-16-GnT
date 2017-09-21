package com.unibo.s3.main_system.communication

import scala.collection.JavaConversions.asScalaBuffer
import akka.actor.{Props, Stash, UntypedAbstractActor}
import com.unibo.s3.main_system.communication.Messages.{GameSettingsMsg, GenerateMapMsg, MapElementMsg}
import com.unibo.s3.main_system.game.{AkkaSettings, Maze, Rooms}
import com.unibo.s3.main_system.map.{AbstractMapGenerator, MapGenerator, MazeMapGenerator, RoomMapGenerator}



class MapActor extends UntypedAbstractActor with Stash {

  // val FILEPATH = "maps/mapFile.txt" uncomment if file is required
  private[this] val mapGenerator: MapGenerator = new MapGenerator
  private[this] var mapWidth: Int = _
  private[this] var mapHeight: Int = _
  private[this] var mapType: Boolean = true
//per ora di default true
  /*
  override def onReceive(message: Any): Unit = message match {

    case msg: MapSettingsMsg =>
      println("ricevute: " + msg.width + " " + msg.height)
      this.mapWidth = msg.width
      this.mapHeight = msg.height

    case _: GenerateMapMsg =>
      //this.mapGenerator.generate(8, this.mapWidth/3, this.mapHeight/3, 0, 0) //valori da decidere una volta decise le dimensioni possibili per la mappa
      mapType match {
        case true =>
          this.mapGenerator.setStrategy(new MazeMapGenerator)
        case false =>
          this.mapGenerator.setStrategy(new RoomMapGenerator)
      }
      //valori da decidere una volta decise le dimensioni possibili per la mappa
      this.mapGenerator.generateMap(this.mapWidth, this.mapHeight)
      this.mapGenerator.getMap.foreach(line => {
        SystemManager.getLocalActor(GeneralActors.GRAPH_ACTOR).tell(MapElementMsg(line), getSelf())
        SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR).tell(MapElementMsg(line), getSelf())
        SystemManager.getLocalActor(GeneralActors.SPAWN_ACTOR).tell(MapElementMsg(line), getSelf())
      })
    // val file = Gdx.files.local(FILEPATH)
     // file.readString().split("\\n").foreach(line => SystemManager.getInstance().getLocalActor("graphActor").tell(MapMsg(line), getSelf()))
    case _ => println("(mapActor) message unknown:" + message)
  }*/
  context.become(this.settings)

  override def onReceive(message: Any): Unit = {}

  private def settings: Receive = {
    case GameSettingsMsg(gs) =>
      val w = gs.mapSize.x.toInt
      val h = gs.mapSize.y.toInt
      //println("ricevute: " + w + " " + h)
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
      //this.mapGenerator.generate(8, this.mapWidth/3, this.mapHeight/3, 0, 0) //valori da decidere una volta decise le dimensioni possibili per la mappa
      mapType match {
        case true =>
          this.mapGenerator.setStrategy(new MazeMapGenerator)
        case false =>
          this.mapGenerator.setStrategy(new RoomMapGenerator)
      }
      //valori da decidere una volta decise le dimensioni possibili per la mappa
      this.mapGenerator.generateMap(this.mapWidth, this.mapHeight)
      this.mapGenerator.getMap.foreach(line => {
        SystemManager.getLocalActor(GeneralActors.GRAPH_ACTOR).tell(MapElementMsg(line), getSelf())
        //val refWorld = SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR)
        val refWorld = SystemManager.getRemoteActor(AkkaSettings.GUISystem, "/user/",
          GeneralActors.WORLD_ACTOR.name)
          refWorld.tell(MapElementMsg(line), getSelf())
        SystemManager.getLocalActor(GeneralActors.SPAWN_ACTOR).tell(MapElementMsg(line), getSelf())
      })
    // val file = Gdx.files.local(FILEPATH)
    // file.readString().split("\\n").foreach(line => SystemManager.getInstance().getLocalActor("graphActor").tell(MapMsg(line), getSelf()))
  }
}

object MapActor {
  def props(): Props = Props(new MapActor())
}
