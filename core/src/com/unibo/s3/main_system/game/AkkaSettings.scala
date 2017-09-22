package com.unibo.s3.main_system.game

/**
  * Set of System name
  * @author Daniele Rosetti
  */
object AkkaSystemNames {
  val GUISystem: String = "GUISystem"
  val ComputeSystem: String = "ComputeSystem"
}

/**
  * Representation for ports of Akka System
  * @author Daniele Rosetti
  */
sealed trait Ports {
  def portNumber: Int
}

/**
  * Port for GUI node
  * @author Daniele Rosetti
  */
object  GUISystemPort extends Ports {
  override val portNumber = 5050
}

/**
  * Port for Compute node
  * @author Daniele Rosetti
  */
object ComputeSystemPort extends Ports {
  override val portNumber = 2727
}

