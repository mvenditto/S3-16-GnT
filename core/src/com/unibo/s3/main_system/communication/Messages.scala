package com.unibo.s3.main_system.communication

import java.util

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

  //message for CharacterActor
  case class AskNeighboursMsg()
  case class SendNeighboursMsg(neighbours: util.List[ActorRef])//rimettere liste scala non java
  case class SendCopInfoMsg(visitedVertices: util.List[Vector2]) //ci andranno le info che si devono scambiare i poliziotti
  case class SetupGraphMsg(graph: UndirectedGraph[Vector2, DefaultEdge])

  //message for MasterActor
  case class RebuildQuadTreeMsg(characterList: Iterable[BaseCharacter])
  case class CreateCharacterMsg(position: Vector2)
  case class InitialSavingCharacter(newCharacter: BaseCharacter, characterRef: ActorRef)
}
