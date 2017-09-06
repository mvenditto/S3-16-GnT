package com.unibo.s3.main_system.communication


object GeneralActors extends Enumeration {

  val MASTER_ACTOR = EnumerationType("masterActor")
  val WORLD_ACTOR = EnumerationType("worldActor")
  val QUAD_TREE_ACTOR = EnumerationType("quadTreeActor")
  val MAP_ACTOR = EnumerationType("mapActor")
  val GRAPH_ACTOR = EnumerationType("graphActor")

  protected case class EnumerationType(name: String) extends Val(name)
}

object CharacterActors extends Enumeration {

  val COP = EnumerationType("cop")
  val THIEF = EnumerationType("thief")

  protected case class EnumerationType(name: String) extends Val(name)

}