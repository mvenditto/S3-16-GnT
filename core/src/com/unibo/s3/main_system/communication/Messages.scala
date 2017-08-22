package com.unibo.s3.main_system.communication

object Messages {
  case class MapMsg(line: String)
  case class StartMsg()
  case class GraphGenerationMsg()
}
