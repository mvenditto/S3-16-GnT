package com.unibo.s3.main_system.communication

import akka.actor.{Props, Stash, UntypedAbstractActor}
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.graph.GraphManagerImpl
import org.jgrapht.UndirectedGraph
import org.jgrapht.graph.DefaultEdge


class GraphActor extends  UntypedAbstractActor with Stash {

  private[this] val FILEPATH = "maps/map.txt" //ci va il percorso del file dove salvare la mappa(Sara)
  private[this] var graph: Option[UndirectedGraph[Vector2, DefaultEdge]] = None
  private[this] val graphManager = new GraphManagerImpl()
  private[this] var width, height: Int = _

  private[this] val file: FileHandle = Gdx.files.local(FILEPATH)
  file.writeString("", false)

  override def onReceive(message: Any): Unit = message match {

    case MapSettingsMsg(w, h) =>
      this.width = w
      this.height = h

    case msg: MapElementMsg =>
      val verifyClose = msg.line.split(":").map(_.toFloat)
      def writeFunction(verifyClose: Array[Float]): Unit = verifyClose match {
        case _ if verifyClose.forall(value => value == 0.0) =>
          getSelf().tell(GenerateGraphMsg(), getSender())
        case _ =>
          file.writeString(msg.line + "\n", true)
      }
      writeFunction(verifyClose)

    case AskForGraphMsg =>
      if (graph.isDefined) sender ! SendGraphMsg(graph.get)
      else stash()

    case _: GenerateGraphMsg =>
      this.graph = Option(graphManager.createGraph(this.width, this.height, FILEPATH))
      unstashAll()

    case msg: InitialSavingCharacterMsg =>
      msg.characterRef.tell(SendGraphMsg(graph.get), getSelf())

    case _ => println("(graph actor) message unknown: " + message)
  }
}

object GraphActor {
  def props() : Props = Props(new GraphActor())
}
