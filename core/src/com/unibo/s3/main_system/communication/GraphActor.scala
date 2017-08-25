package com.unibo.s3.main_system.communication

import akka.actor.{Props, UntypedAbstractActor}
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.unibo.s3.main_system.communication.Messages.{GraphGenerationMsg, MapMsg}
import com.unibo.s3.main_system.graph.GraphGenerator


class GraphActor extends  UntypedAbstractActor {

  val FILEPATH = "/maps/outputGraphActor.txt" //ci va il percorso del file dove salvare la mappa(Sara)
  val testPath = "test.txt" //ci va il percorso del file dove salvare la mappa(Sara)

  val file: FileHandle = Gdx.files.local(FILEPATH)
  file.writeString("", false)

  override def onReceive(message: Any): Unit = message match {
    case msg: MapMsg =>
      val verifyClose = msg.line.split(":").map(_.toFloat)
      def writeFunction(verifyClose: Array[Float]): Unit = verifyClose match {
        case _ if verifyClose.forall(value => value == 0.0) =>
          getSelf().tell(GraphGenerationMsg(), getSender())
        case _ => file.writeString(msg.line + "\n", true)
      }
      writeFunction(verifyClose)
    case _: GraphGenerationMsg =>
      //GraphGenerator.createGraph("C:\\Users\\Sara\\Maps\\test.txt");
      GraphGenerator.createGraph(testPath);
      //qui ho il file con la mappa, bisogna generare il grafo(Sara)
      //println("graph created!")
    case _ => println("(graph actor) message unknown: " + message)
  }
}

object GraphActor {
  def props() : Props = Props(new GraphActor())
}
