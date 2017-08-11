package daniele.comunication

import akka.actor.Props
import org.jgrapht.UndirectedGraph
import org.jgrapht.graph.DefaultEdge


class GraphActor(override val name: String) extends NamedActor {

  override def onReceive(message: Any): Unit = message match {
    case msg: UndirectedGraph[String, DefaultEdge] =>
      println("received graph: " + msg.toString)
    case _ => println("unknown message")
  }
}

object GraphActor {
  def props(name: String) : Props = Props(new GraphActor(name))
}
