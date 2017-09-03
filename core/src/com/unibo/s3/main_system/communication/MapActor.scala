package com.unibo.s3.main_system.communication

import scala.collection.JavaConversions.asScalaBuffer
import akka.actor.{Props, UntypedAbstractActor}
import com.unibo.s3.main_system.communication.Messages.{GenerateMapMsg, MapElementMsg, MapSettingsMsg}
import com.unibo.s3.main_system.map.{AbstractMapGenerator, MapGenerator, MazeMapGenerator, RoomMapGenerator}



class MapActor extends UntypedAbstractActor {

  // val FILEPATH = "maps/mapFile.txt" uncomment if file is required
  private[this] val mapGenerator: MapGenerator = new MapGenerator
  private[this] var mapWidth: Int = _
  private[this] var mapHeight: Int = _
  private[this] var mapType: Boolean = true
//per ora di default true
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
      this.mapGenerator.generateMap(this.mapWidth/AbstractMapGenerator.BASE_UNIT, this.mapHeight/AbstractMapGenerator.BASE_UNIT)
      this.mapGenerator.getMap.foreach(line => SystemManager.getInstance().getLocalActor("graphActor").tell(MapElementMsg(line), getSelf()))
      this.mapGenerator.getMap.foreach(line => SystemManager.getInstance().getLocalActor("worldActor").tell(MapElementMsg(line), getSelf()))
    // val file = Gdx.files.local(FILEPATH)
     // file.readString().split("\\n").foreach(line => SystemManager.getInstance().getLocalActor("graphActor").tell(MapMsg(line), getSelf()))
    case _ => println("(mapActor) message unknown:" + message)
  }
}

object MapActor {
  def props(): Props = Props(new MapActor())
}
