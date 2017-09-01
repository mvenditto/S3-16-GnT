package com.unibo.s3.main_system.communication

import scala.collection.JavaConversions.asScalaBuffer
import akka.actor.{Props, UntypedAbstractActor}
import com.unibo.s3.main_system.communication.Messages.{GenerateMapMsg, MapElementMsg, MapSettingsMsg}
import com.unibo.s3.main_system.map.{MapGenerator, MazeMapGenerator}



class MapActor extends UntypedAbstractActor {

  // val FILEPATH = "maps/mapFile.txt" uncomment if file is required
  private[this] val mapGenerator: MapGenerator = new MazeMapGenerator
  private[this] var mapWidth: Int = _
  private[this] var mapHeight: Int = _

  override def onReceive(message: Any): Unit = message match {
    case MapSettingsMsg(w, h) =>
      this.mapWidth = w
      this.mapHeight = h

    case _: GenerateMapMsg =>
      this.mapGenerator.generate(8, this.mapWidth, this.mapHeight, 0, 0) //valori da decidere una volta decise le dimensioni possibili per la mappa
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
