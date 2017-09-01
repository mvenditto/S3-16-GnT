package com.unibo.s3.main_system.communication

import akka.actor.{Props, UntypedAbstractActor}
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.communication.Messages.{GenerateGraphMsg, InitialSavingCharacterMsg, MapElementMsg, SendGraphMsg}
import com.unibo.s3.main_system.graph.GraphManagerImpl
import org.jgrapht.UndirectedGraph
import org.jgrapht.graph.DefaultEdge


class GraphActor extends  UntypedAbstractActor {

  private[this] val FILEPATH = "maps/map.txt" //ci va il percorso del file dove salvare la mappa(Sara)
  private[this] var graph: UndirectedGraph[Vector2, DefaultEdge] = _
  private[this] val graphManager = new GraphManagerImpl()

  private[this] val file: FileHandle = Gdx.files.local(FILEPATH)
  file.writeString("", false)

  override def onReceive(message: Any): Unit = message match {
    case msg: MapElementMsg =>
      val verifyClose = msg.line.split(":").map(_.toFloat)
      def writeFunction(verifyClose: Array[Float]): Unit = verifyClose match {
        case _ if verifyClose.forall(value => value == 0.0) =>
          getSelf().tell(GenerateGraphMsg(), getSender())
        case _ => file.writeString(msg.line + "\n", true)
      }
      writeFunction(verifyClose)
    case _: GenerateGraphMsg =>
      this.graph = graphManager.createGraph(FILEPATH)
      //GraphGenerator.createGraph("C:\\Users\\Sara\\Maps\\test.txt");
      sender ! graph
      //qui ho il file con la mappa, bisogna generare il grafo(Sara)
      //println("graph created!")
    case msg: InitialSavingCharacterMsg =>
      msg.characterRef.tell(SendGraphMsg(graph), getSelf())
    case _ => println("(graph actor) message unknown: " + message)
  }
}

object GraphActor {
  def props() : Props = Props(new GraphActor())
}
