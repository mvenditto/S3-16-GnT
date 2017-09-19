package com.unibo.s3.main_system.communication

import akka.actor.{Props, Stash, UntypedAbstractActor}
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.graph.GraphGenerator
import org.jgrapht.UndirectedGraph
import org.jgrapht.graph.DefaultEdge


class GraphActor extends  UntypedAbstractActor with Stash {

  private[this] val FILEPATH = "maps/map.txt" //ci va il percorso del file dove salvare la mappa(Sara)
  private[this] var graph: Option[UndirectedGraph[Vector2, DefaultEdge]] = None
  private[this] var width, height: Int = _

  private[this] val file: FileHandle = Gdx.files.local(FILEPATH)
  file.writeString("", false)
  /*
  override def onReceive(message: Any): Unit = message match {

    case MapSettingsMsg(w, h) =>
      this.width = w
      this.height = h

    case msg: MapElementMsg =>
      val verifyClose = msg.line.split(":")
      def writeFunction(verifyClose: Array[String]): Unit = verifyClose match {
        case _ if verifyClose.forall(value => value == "0.0") =>
          getSelf().tell(GenerateGraphMsg(), getSender())
        case _ =>
          file.writeString(msg.line + "\n", true)
      }
      writeFunction(verifyClose)

    case AskForGraphMsg =>
      if (graph.isDefined) sender ! SendGraphMsg(graph.get)
      else stash()

    case _: GenerateGraphMsg =>
      this.graph = Option(GraphGenerator.createGraph(this.width, this.height, FILEPATH))
      unstashAll()

    case _ => println("(graphActor) message unknown: " + message)
  }*/

  context.become(mapSettings())

  override def onReceive(message: Any): Unit = {}

  private def mapSettings(): Receive = {
    case MapSettingsMsg(w, h) =>
      this.width = w
      this.height = h
      context.become(generateGraph())
      unstashAll()
    case _ => stash()
  }

  private def generateGraph(): Receive = {
    case msg: MapElementMsg =>
      println("[GRAPH ACTOR] Riga ricevuta")
      val verifyClose = msg.line.split(":")
      def writeFunction(verifyClose: Array[String]): Unit = verifyClose match {
        case _ if verifyClose.forall(value => value == "0.0") =>
          getSelf().tell(GenerateGraphMsg(), getSender())
        case _ =>
          file.writeString(msg.line + "\n", true)
      }
      writeFunction(verifyClose)

    case _: GenerateGraphMsg =>
      println("Ricevuto messaggio di generare grafo")
      this.graph = Option(GraphGenerator.createGraph(this.width, this.height, FILEPATH))
      println("Grafo genera")
      context.become(askGraph())
      unstashAll()

    case _ => stash()
  }

  private def askGraph(): Receive = {
    case AskForGraphMsg =>
      sender ! SendGraphMsg(graph.get)
  }
}

object GraphActor {
  def props() : Props = Props(new GraphActor())
}
