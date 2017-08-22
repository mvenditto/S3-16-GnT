package com.unibo.s3.main_system.communication

import akka.actor.ActorRef

object Messages {
  case class StartMsg()
  case class MapMsg(line: String)
  case class GraphGenerationMsg()

  case class askNeighbourMsg()
  case class responseNeighbourMsg(neighbours: List[ActorRef])
  case class sendCopInfoMsg()
}
