package com.unibo.s3.main_system.communication

import akka.actor.ActorRef
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.BaseCharacter
import org.jgrapht.UndirectedGraph
import org.jgrapht.graph.DefaultEdge

object Messages {
  //message for synchronize
  case class ActMsg(dt: Float)

  //message for MapActor
  case class GenerateMapMsg() //ci andranno le info per generare la mappa(width: int, height: int, campo per decidere il tipo di grafo)

  //message for GraphActor
  case class MapElementMsg(line: String)
  case class GenerateGraphMsg()
  case class SendGraphMsg(graph: UndirectedGraph[Vector2, DefaultEdge])

  //message for CharacterActor
  case class AskNeighboursMsg(character: BaseCharacter)
  case class SendNeighboursMsg(neighbours: List[ActorRef])
  case class SendCopInfoMsg() //ci andranno le info che si devono scambiare i poliziotti

  //message for MasterActor
  case class RebuildQuadTreeMsg(characterList: Iterable[BaseCharacter])
  case class CreateCharacterMsg(position: Vector2)
  case class InitialSavingCharacterMsg(newCharacter: BaseCharacter, characterRef: ActorRef)
}
