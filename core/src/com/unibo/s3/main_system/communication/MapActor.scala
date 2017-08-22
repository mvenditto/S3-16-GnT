package com.unibo.s3.main_system.communication


import akka.actor.{Props, UntypedAbstractActor}
import com.badlogic.gdx.Gdx
import com.unibo.s3.main_system.communication.Messages.{MapMsg, StartMsg}



class MapActor extends UntypedAbstractActor {

  val FILEPATH = "prova1.txt" //ci va il percorso del file con la mappa(Santo)
  override def onReceive(message: Any): Unit = message match {
    case _: StartMsg =>
      println("map start")
      //qui generi la mappa e la scrivi su un file(Santo)
      val file = Gdx.files.local(FILEPATH)
      file.readString().split("\\n").foreach(line => SystemManager.getInstance().getLocalActor("graphActor").tell(MapMsg(line), getSelf()))
    case _ => println("(mapActor) message unknown:" + message)
  }
}

object MapActor {
  def props(): Props = Props(new MapActor())
}
