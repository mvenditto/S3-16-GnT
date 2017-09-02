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
  case class MapSettingsMsg(width: Int, height: Int)
  case class GenerateMapMsg() //ci va un flag con la tipologia di grafo

  //message for GraphActor
  case class MapElementMsg(line: String)
  case class GenerateGraphMsg()
  case class AskForGraphMsg()
  case class SendGraphMsg(graph: UndirectedGraph[Vector2, DefaultEdge])

  //message for CharacterActor
  case class AskNeighboursMsg(character: BaseCharacter)
  case class SendNeighboursMsg(neighbours: List[ActorRef])
  case class AskAllCharactersMsg()
  case class SendAllCharactersMsg(characters: Iterable[BaseCharacter])
  case class SendCopInfoMsg(visitedVertices: List[Vector2])

  //message for MasterActor
  case class RebuildQuadTreeMsg()
  case class CreateCharacterMsg(position: Vector2)
  case class InitialSavingCharacterMsg(newCharacter: BaseCharacter, characterRef: ActorRef)
}
