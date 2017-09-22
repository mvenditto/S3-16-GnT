package com.unibo.s3.main_system.communication

import akka.actor.ActorRef
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.game.GameSettings
import org.jgrapht.UndirectedGraph
import org.jgrapht.graph.DefaultEdge

object Messages {
  type CharacterActors = CharacterActors.Value
  //message for synchronize
  case class ActMsg(dt: Float)

  //message for MapActor
  case class GameSettingsMsg(g: GameSettings)
  case class GenerateMapMsg()

  //message for GraphActor
  case class MapElementMsg(line: String)
  case class GenerateGraphMsg()
  case class AskForGraphMsg()
  case class SendGraphMsg(graph: UndirectedGraph[Vector2, DefaultEdge])

  //message for CharacterActor
  case class AskNeighboursMsg(character: BaseCharacter, radius: Option[Float] = None)
  case class SendNeighboursMsg(neighbours: Iterable[ActorRef])
  case class AskAllCharactersMsg()
  case class SendAllCharactersMsg(characters: Iterable[BaseCharacter])
  case class SendGuardInfoMsg(visitedVertices: Iterable[Vector2])

  //messages for GameActor
  case class ThiefCaughtMsg(thief: BaseCharacter, guard: BaseCharacter)
  case class ThiefReachedExitMsg(thief: BaseCharacter)
  case class ToggleViewDebug(viewDebug: Boolean)

  //message for MasterActor
  case class RebuildQuadTreeMsg()
  case class CreateCharacterMsg(position: Vector2, characterType: CharacterActors)
  case class InitialSavingCharacterMsg(newCharacter: BaseCharacter, characterRef: ActorRef)

  //message for SpawnActor
  case class GenerateNewCharacterPositionMsg(num: Int, characterType: CharacterActors)
  case class ResetSpawnMsg()

  //message for CommunicatorActor
  case class SendIPMsg(IP: String)
  case class AskIPMsg()
  case class ACKComputationNode()
  case class RestartMsg()
}
