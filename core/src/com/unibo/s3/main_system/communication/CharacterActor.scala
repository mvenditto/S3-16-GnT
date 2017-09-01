package com.unibo.s3.main_system.communication

import akka.actor.{Props, UntypedAbstractActor}
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages._
import org.jgrapht.UndirectedGraph
import org.jgrapht.graph.DefaultEdge

class CharacterActor(private[this] val character: BaseCharacter) extends UntypedAbstractActor {

  private[this] var graph: UndirectedGraph[Vector2, DefaultEdge] = _

  override def onReceive(message: Any): Unit = message match {
    case ActMsg(dt) =>
      character.act(dt)
      SystemManager.getInstance().getLocalActor("quadTreeActor").tell(AskNeighboursMsg(this.character), getSelf())

    case msg: SendNeighboursMsg =>
      //msg.neighbours.foreach(neighbour => neighbour.tell(SendCopInfoMsg(), getSelf()))
      //dico sotto i vicini
    case msg: SendCopInfoMsg =>
      println("cop: " + getSelf() + "| info from: " + getSender())
      //qui ho le info dell'altro poliziotto quindi poi posso fare quello che devo

    //case ricevo grafo iniziale => salvo grafo iniziale
    //case
    case msg: SendGraphMsg=>
      this.graph = msg.graph
    case _ => println("(characterActor) message unknown:" + message)
  }
}

object CharacterActor {
  def props(baseCharacter: BaseCharacter): Props = Props(new CharacterActor(baseCharacter))
}
