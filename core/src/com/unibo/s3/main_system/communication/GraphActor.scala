package com.unibo.s3.main_system.communication

import akka.actor.Props
import org.jgrapht.UndirectedGraph
import org.jgrapht.graph.DefaultEdge


class GraphActor(override val name: String) extends NamedActor {

  override def onReceive(message: Any): Unit = message match {
    case msg: UndirectedGraph[String, DefaultEdge] =>
      println("name: " + name + "| graph: " + msg.toString + " from: " + sender())
    case _ => println("unknown message")
  }
}

object GraphActor {
  def props(name: String) : Props = Props(new GraphActor(name))
}
