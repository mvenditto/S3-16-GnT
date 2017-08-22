package com.unibo.s3.main_system.communication


import java.util

import akka.actor.{Props, UntypedAbstractActor}
import com.badlogic.gdx.Gdx
import com.unibo.s3.main_system.communication.Messages.{MapMsg, StartMsg}
import com.unibo.s3.main_system.map.{MapGenerator, MazeMapGenerator}



class MapActor extends UntypedAbstractActor {

  val FILEPATH = "maps/mapFile.txt"
  val mapGenerator: MapGenerator = new MazeMapGenerator

  override def onReceive(message: Any): Unit = message match {
    case _: StartMsg =>
      mapGenerator.generate(8, 20, 20, 0, 0) //valori da decidere una volta decise le dimensioni possibili per la mappa
      val map : util.List[String] = mapGenerator.getMap

      println(map.toString);
      for(a <- 0 until map.size()){
        SystemManager.getInstance().getLocalActor("graphActor").tell(MapMsg(map.get(a)), getSelf())
      }

     // val file = Gdx.files.local(FILEPATH)
     // file.readString().split("\\n").foreach(line => SystemManager.getInstance().getLocalActor("graphActor").tell(MapMsg(line), getSelf()))
    case _ => println("(mapActor) message unknown:" + message)
  }
}

object MapActor {
  def props(): Props = Props(new MapActor())
}
